/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.core.UUIDUtil
 *  net.minecraft.network.Connection
 *  net.minecraft.network.DisconnectionDetails
 *  net.minecraft.network.PacketListener
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.configuration.ConfigurationProtocols
 *  net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket
 *  net.minecraft.network.protocol.login.ClientboundHelloPacket
 *  net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket
 *  net.minecraft.network.protocol.login.ClientboundLoginFinishedPacket
 *  net.minecraft.network.protocol.login.ServerLoginPacketListener
 *  net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket
 *  net.minecraft.network.protocol.login.ServerboundHelloPacket
 *  net.minecraft.network.protocol.login.ServerboundKeyPacket
 *  net.minecraft.network.protocol.login.ServerboundLoginAcknowledgedPacket
 *  net.minecraft.util.Crypt
 *  net.minecraft.util.CryptException
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package night.pingbypass.handler;

import com.mojang.authlib.GameProfile;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Random;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ClientboundLoginFinishedPacket;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.network.protocol.login.ServerboundLoginAcknowledgedPacket;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import night.pingbypass.handler.PbConfigurationHandler;
import night.pingbypass.server.ProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbLoginHandler
implements ServerLoginPacketListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(PbLoginHandler.class);
    private static final Random RANDOM = new Random();
    private final ProxyServer proxyServer;
    private final Connection connection;
    private final KeyPair keyPair;
    private final byte[] nonce = new byte[4];
    private LoginState state = LoginState.HELLO;
    private String playerName;
    private GameProfile acceptedProfile;

    public PbLoginHandler(ProxyServer proxyServer, Connection connection) {
        this.proxyServer = proxyServer;
        this.connection = connection;
        try {
            this.keyPair = Crypt.generateKeyPair();
        }
        catch (CryptException e) {
            throw new IllegalStateException("Failed to generate RSA keypair", e);
        }
        RANDOM.nextBytes(this.nonce);
    }

    public void handleHello(ServerboundHelloPacket packet) {
        if (this.state != LoginState.HELLO) {
            throw new IllegalStateException("Unexpected hello packet");
        }
        this.playerName = packet.name();
        LOGGER.info("Login attempt from {}", (Object)this.playerName);
        this.state = LoginState.KEY;
        this.connection.send((Packet)new ClientboundHelloPacket("", this.keyPair.getPublic().getEncoded(), this.nonce, false));
    }

    public void handleKey(ServerboundKeyPacket packet) {
        SecretKey secretKey;
        if (this.state != LoginState.KEY) {
            throw new IllegalStateException("Unexpected key packet");
        }
        PrivateKey privateKey = this.keyPair.getPrivate();
        if (!packet.isChallengeValid(this.nonce, privateKey)) {
            throw new IllegalStateException("Protocol error: invalid nonce!");
        }
        try {
            secretKey = packet.getSecretKey(privateKey);
        }
        catch (CryptException e) {
            throw new IllegalStateException("Protocol error", e);
        }
        this.state = LoginState.READY_TO_ACCEPT;
        try {
            Cipher decryptCipher = Crypt.getCipher((int)2, (Key)secretKey);
            Cipher encryptCipher = Crypt.getCipher((int)1, (Key)secretKey);
            this.connection.setEncryptionKey(decryptCipher, encryptCipher);
        }
        catch (CryptException e) {
            throw new IllegalStateException("Protocol error", e);
        }
        this.acceptPlayer();
    }

    private void acceptPlayer() {
        GameProfile profile;
        this.acceptedProfile = profile = new GameProfile(UUIDUtil.createOfflinePlayerUUID((String)this.playerName), this.playerName);
        this.state = LoginState.PROTOCOL_SWITCHING;
        this.connection.send((Packet)new ClientboundLoginFinishedPacket(profile, UUID.randomUUID()));
        LOGGER.info("Login success for {} ({}), waiting for LOGIN_ACKNOWLEDGED", (Object)profile.name(), (Object)profile.id());
    }

   public void handleLoginAcknowledgement(ServerboundLoginAcknowledgedPacket packet) {
      if (this.state != PbLoginHandler.LoginState.PROTOCOL_SWITCHING) {
         throw new IllegalStateException("Unexpected login acknowledgement packet");
      }

      this.connection.setupOutboundProtocol(ConfigurationProtocols.CLIENTBOUND);
      this.connection
         .setupInboundProtocol(ConfigurationProtocols.SERVERBOUND, new PbConfigurationHandler(this.proxyServer, this.connection, this.acceptedProfile));
      this.state = PbLoginHandler.LoginState.ACCEPTED;
      LOGGER.info("Transitioning {} to configuration phase", this.playerName);
   }

    public void handleCustomQueryPacket(ServerboundCustomQueryAnswerPacket packet) {
        this.disconnect((Component)Component.literal((String)"Unexpected query response"));
    }

    public void handleCookieResponse(ServerboundCookieResponsePacket packet) {
        this.disconnect((Component)Component.literal((String)"Unexpected cookie response"));
    }

    public void onDisconnect(DisconnectionDetails info) {
        LOGGER.info("Client {} disconnected during login: {}", (Object)(this.playerName != null ? this.playerName : "unknown"), (Object)info.reason());
    }

    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    private void disconnect(Component reason) {
        try {
            this.connection.send((Packet)new ClientboundLoginDisconnectPacket(reason));
            this.connection.disconnect(reason);
        }
        catch (Exception e) {
            LOGGER.error("Error disconnecting client", (Throwable)e);
        }
    }

    public String getPlayerName() {
        return this.playerName;
    }

    private static enum LoginState {
        HELLO,
        KEY,
        READY_TO_ACCEPT,
        PROTOCOL_SWITCHING,
        ACCEPTED;

    }
}

