/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.Connection
 *  net.minecraft.network.FriendlyByteBuf
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package night.pingbypass.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PbPacket;
import night.pingbypass.protocol.PbPacketHandler;
import night.pingbypass.protocol.packets.C2SJoinPacket;
import night.pingbypass.protocol.packets.C2SModuleTogglePacket;
import night.pingbypass.protocol.packets.C2SPasswordPacket;
import night.pingbypass.protocol.packets.C2SSettingChangePacket;
import night.pingbypass.protocol.packets.C2SStayPacket;
import night.pingbypass.protocol.packets.S2CErrorPacket;
import night.pingbypass.protocol.packets.S2CModuleStatePacket;
import night.pingbypass.protocol.packets.S2CPasswordRequestPacket;
import night.pingbypass.protocol.packets.S2CServerNamePacket;
import night.pingbypass.protocol.packets.S2CSettingStatePacket;
import night.pingbypass.server.ProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbProtocolHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PbProtocolHandler.class);
    private final Map<Integer, Function<FriendlyByteBuf, PbPacket>> factories = new HashMap<Integer, Function<FriendlyByteBuf, PbPacket>>();
    private final Map<Integer, PbPacketHandler<?>> handlers = new HashMap();

    public PbProtocolHandler() {
        this.registerFactories();
    }

   public <T extends PbPacket> void register(int packetId, Function<FriendlyByteBuf, T> factory, PbPacketHandler<T> handler) {
      this.factories.put(packetId, (Function)factory);
      this.handlers.put(packetId, handler);
   }

   public <T extends PbPacket> void registerFactory(int packetId, Function<FriendlyByteBuf, T> factory) {
      this.factories.put(packetId, (Function)factory);
   }

    public <T extends PbPacket> void registerHandler(int packetId, PbPacketHandler<T> handler) {
        this.handlers.put(packetId, handler);
    }

   public void handle(FriendlyByteBuf buf, Connection connection) {
      int packetId;
      try {
         packetId = buf.readVarInt();
      } catch (Exception e) {
         LOGGER.warn("[PbProtocol] Failed to read packet ID from buffer", e);
         return;
      }

      Function<FriendlyByteBuf, PbPacket> factory = this.factories.get(packetId);
      if (factory == null) {
         LOGGER.warn("[PbProtocol] Unknown packet ID: {}", packetId);
      } else {
         PbPacket packet;
         try {
            packet = factory.apply(buf);
         } catch (Exception e) {
            LOGGER.warn("[PbProtocol] Failed to deserialize packet ID {}", packetId, e);
            return;
         }

         PbPacketHandler<PbPacket> handler = (PbPacketHandler<PbPacket>)this.handlers.get(packetId);
         if (handler == null) {
            LOGGER.warn("[PbProtocol] No handler registered for packet ID: {}", packetId);
         } else {
            try {
               handler.handle(packet, connection);
            } catch (Exception e) {
               LOGGER.warn("[PbProtocol] Error handling packet ID {}", packetId, e);
            }
         }
      }
   }

    private void registerFactories() {
        this.registerFactory(0, C2SJoinPacket::new);
        this.registerFactory(1, S2CPasswordRequestPacket::new);
        this.registerFactory(2, C2SPasswordPacket::new);
        this.registerFactory(3, C2SModuleTogglePacket::new);
        this.registerFactory(4, C2SSettingChangePacket::new);
        this.registerFactory(5, S2CModuleStatePacket::new);
        this.registerFactory(6, S2CSettingStatePacket::new);
        this.registerFactory(7, S2CErrorPacket::new);
        this.registerFactory(8, S2CServerNamePacket::new);
        this.registerFactory(9, C2SStayPacket::new);
    }

    public void registerStayHandler(ProxyServer proxyServer) {
        this.register(9, C2SStayPacket::new, (packet, connection) -> {
            proxyServer.setStayConnected(packet.isStay());
            LOGGER.info("[PbProtocol] Stay Connected set to {} via protocol handler", (Object)packet.isStay());
        });
    }
}

