/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.Connection
 *  net.minecraft.network.ConnectionProtocol
 *  net.minecraft.network.DisconnectionDetails
 *  net.minecraft.network.PacketListener
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.handshake.ClientIntentionPacket
 *  net.minecraft.network.protocol.handshake.ServerHandshakePacketListener
 *  net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket
 *  net.minecraft.network.protocol.login.LoginProtocols
 *  net.minecraft.network.protocol.status.StatusProtocols
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package night.pingbypass.handler;

import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.status.StatusProtocols;
import night.pingbypass.handler.PbLoginHandler;
import night.pingbypass.handler.PbStatusHandler;
import night.pingbypass.server.ProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbHandshakeHandler
implements ServerHandshakePacketListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(PbHandshakeHandler.class);
    private static final int PROTOCOL_VERSION = 775;
    private final ProxyServer proxyServer;
    private final Connection connection;

    public PbHandshakeHandler(ProxyServer proxyServer, Connection connection) {
        this.proxyServer = proxyServer;
        this.connection = connection;
    }

    public void handleIntention(ClientIntentionPacket packet) {
        switch (packet.intention()) {
            case LOGIN: {
                this.handleLogin(packet);
                break;
            }
            case STATUS: {
                this.handleStatus();
                break;
            }
            default: {
                throw new UnsupportedOperationException("Invalid intention " + String.valueOf(packet.intention()));
            }
        }
    }

   private void handleLogin(ClientIntentionPacket packet) {
      this.connection.setupOutboundProtocol(LoginProtocols.CLIENTBOUND);
      if (packet.protocolVersion() != 775) {
         Component message;
         if (packet.protocolVersion() < 775) {
            message = Component.translatable("multiplayer.disconnect.outdated_client", "26.1.2");
         } else {
            message = Component.translatable("multiplayer.disconnect.incompatible", "26.1.2");
         }

         this.connection.send(new ClientboundLoginDisconnectPacket(message));
         this.connection.disconnect(message);
         LOGGER.info("Rejected connection from {}: wrong protocol version {}", this.connection.getLoggableAddress(false), packet.protocolVersion());
      } else if (this.hasActivePlayConnection()) {
         Component message = Component.literal("This PingBypass server is already in use!");
         this.connection.send(new ClientboundLoginDisconnectPacket(message));
         this.connection.disconnect(message);
         LOGGER.info("Rejected connection from {}: another client is already connected", this.connection.getLoggableAddress(false));
      } else {
         this.connection.setupInboundProtocol(LoginProtocols.SERVERBOUND, new PbLoginHandler(this.proxyServer, this.connection));
         LOGGER.info("Client {} starting login", this.connection.getLoggableAddress(false));
      }
   }

   private void handleStatus() {
      this.connection.setupOutboundProtocol(StatusProtocols.CLIENTBOUND);
      this.connection.setupInboundProtocol(StatusProtocols.SERVERBOUND, new PbStatusHandler(this.proxyServer, this.connection));
      LOGGER.debug("Received STATUS handshake, serving proxy status");
   }

    private boolean hasActivePlayConnection() {
        for (Connection conn : this.proxyServer.getConnections()) {
            if (conn == this.connection || !conn.isConnected() || conn.getPacketListener() == null || conn.getPacketListener().protocol() != ConnectionProtocol.PLAY) continue;
            return true;
        }
        return false;
    }

    public void onDisconnect(DisconnectionDetails info) {
    }

    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }
}

