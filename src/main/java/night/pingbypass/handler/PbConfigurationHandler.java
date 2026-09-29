/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.core.RegistryAccess$Frozen
 *  net.minecraft.network.Connection
 *  net.minecraft.network.DisconnectionDetails
 *  net.minecraft.network.PacketListener
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.TickablePacketListener
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ServerboundClientInformationPacket
 *  net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket
 *  net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.ServerboundKeepAlivePacket
 *  net.minecraft.network.protocol.common.ServerboundPongPacket
 *  net.minecraft.network.protocol.common.ServerboundResourcePackPacket
 *  net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket
 *  net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks
 *  net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener
 *  net.minecraft.network.protocol.configuration.ServerboundAcceptCodeOfConductPacket
 *  net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket
 *  net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks
 *  net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket
 *  net.minecraft.network.protocol.game.GameProtocols
 *  net.minecraft.network.protocol.game.GameProtocols$Context
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package night.pingbypass.handler;
import net.minecraft.network.protocol.game.GameProtocols.Context;


import com.mojang.authlib.GameProfile;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ServerboundAcceptCodeOfConductPacket;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.network.protocol.game.GameProtocols;
import night.Night;
import night.pingbypass.handler.PbPasswordHandler;
import night.pingbypass.handler.PbWaitingHandler;
import night.pingbypass.server.ProxyServer;
import night.pingbypass.server.RegistryCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbConfigurationHandler
implements ServerConfigurationPacketListener,
TickablePacketListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(PbConfigurationHandler.class);
    private static final GameProtocols.Context NO_INFINITE_MATERIALS = () -> false;
    private final ProxyServer proxyServer;
    private final Connection connection;
    private final GameProfile profile;
    private boolean knownPacksSent;

    public PbConfigurationHandler(ProxyServer proxyServer, Connection connection, GameProfile profile) {
        this.proxyServer = proxyServer;
        this.connection = connection;
        this.profile = profile;
    }

    public void tick() {
        if (!this.knownPacksSent) {
            this.knownPacksSent = true;
            RegistryCache cache = this.proxyServer.getRegistryCache();
            if (cache.isLoaded()) {
                this.connection.send((Packet)new ClientboundSelectKnownPacks(cache.getKnownPacks()));
            } else {
                this.connection.send((Packet)new ClientboundSelectKnownPacks(List.of()));
            }
            LOGGER.info("Sent SelectKnownPacks to {}", (Object)this.profile.name());
        }
    }

    public void handleSelectKnownPacks(ServerboundSelectKnownPacks packet) {
        LOGGER.info("Client {} responded with known packs, sending registries", (Object)this.profile.name());
        RegistryCache cache = this.proxyServer.getRegistryCache();
        if (cache.isLoaded()) {
            for (Packet<?> p : cache.getRegistryPackets()) {
                this.connection.send(p);
            }
        }
        this.connection.send((Packet)ClientboundFinishConfigurationPacket.INSTANCE);
        LOGGER.info("Sent {} registry packets and FINISH_CONFIGURATION to {}", (Object)(cache.isLoaded() ? cache.getRegistryPackets().size() : 0), (Object)this.profile.name());
    }

   public void handleConfigurationFinished(ServerboundFinishConfigurationPacket packet) {
      LOGGER.info("Client {} acknowledged FINISH_CONFIGURATION, transitioning to PLAY", this.profile.name());
      RegistryCache cache = this.proxyServer.getRegistryCache();
      RegistryAccess registryManager = cache.isLoaded() ? cache.getRegistryManager() : RegistryAccess.EMPTY;
      this.connection.setupOutboundProtocol(GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(registryManager)));
      if (Night.PINGBYPASS_CONFIG.hasPassword()) {
         this.connection
            .setupInboundProtocol(
               GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(RegistryAccess.EMPTY), NO_INFINITE_MATERIALS),
               new PbPasswordHandler(this.proxyServer, this.connection, this.profile, registryManager)
            );
         LOGGER.info("Transitioning {} to password verification", this.profile.name());
      } else {
         this.connection
            .setupInboundProtocol(
               GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(RegistryAccess.EMPTY), NO_INFINITE_MATERIALS),
               new PbWaitingHandler(this.proxyServer, this.connection, this.profile, registryManager)
            );
         LOGGER.info("Transitioning {} to waiting state", this.profile.name());
      }
   }

    public void handleAcceptCodeOfConduct(ServerboundAcceptCodeOfConductPacket packet) {
    }

    public void onDisconnect(DisconnectionDetails info) {
        LOGGER.info("Client {} disconnected during configuration: {}", (Object)this.profile.name(), (Object)info.reason());
    }

    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    public void handleClientInformation(ServerboundClientInformationPacket p) {
    }

    public void handleKeepAlive(ServerboundKeepAlivePacket p) {
    }

    public void handlePong(ServerboundPongPacket p) {
    }

    public void handleResourcePackResponse(ServerboundResourcePackPacket p) {
    }

    public void handleCookieResponse(ServerboundCookieResponsePacket p) {
    }

    public void handleCustomPayload(ServerboundCustomPayloadPacket p) {
    }

    public void handleCustomClickAction(ServerboundCustomClickActionPacket p) {
    }
}

