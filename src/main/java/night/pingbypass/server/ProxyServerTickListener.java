/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ServerboundClientInformationPacket
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket
 *  net.minecraft.network.protocol.game.ServerboundClientTickEndPacket
 *  net.minecraft.network.protocol.game.ServerboundContainerClosePacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
 *  net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket
 *  net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package night.pingbypass.server;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PacketSendEvent;
import night.events.impl.TickEvent;
import night.pingbypass.PingBypassFlags;
import night.pingbypass.server.ProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyServerTickListener {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"Night/Rubberband");
    private final ProxyServer proxyServer;
    private static final ThreadLocal<Boolean> ALLOW_SEND = ThreadLocal.withInitial(() -> false);

    public ProxyServerTickListener(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        Night.PB_MODULE_MANAGER.tick();
        if (this.proxyServer.isAlive()) {
            this.proxyServer.tick();
        }
    }

    public static void allowSend(Runnable action) {
        ALLOW_SEND.set(true);
        try {
            action.run();
        }
        finally {
            ALLOW_SEND.set(false);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!PingBypassFlags.proxyForwardingActive) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof ClientboundPlayerPositionPacket) {
            ClientboundPlayerPositionPacket p = (ClientboundPlayerPositionPacket)packet;
            LOGGER.info("[PB] Real server sent teleport/correction id={} pos={} rel={} onGround(from client, stale on ghost until next move)={}", new Object[]{p.id(), p.change().position(), p.relatives(), PingBypassFlags.clientOnGround});
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof ServerboundAcceptTeleportationPacket) {
            ServerboundAcceptTeleportationPacket p = (ServerboundAcceptTeleportationPacket)packet;
            LOGGER.info("[PB] AcceptTeleportation id={} isRealClientConfirm(allowSend)={}", (Object)p.getId(), (Object)ALLOW_SEND.get());
        }
        if (!PingBypassFlags.proxyForwardingActive) {
            return;
        }
        if (ALLOW_SEND.get().booleanValue()) {
            return;
        }
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof ServerboundClientTickEndPacket || packet2 instanceof ServerboundPlayerInputPacket || packet2 instanceof ServerboundMoveVehiclePacket || packet2 instanceof ServerboundPaddleBoatPacket || packet2 instanceof ServerboundClientInformationPacket || packet2 instanceof ServerboundPlayerAbilitiesPacket || packet2 instanceof ServerboundContainerClosePacket || packet2 instanceof ServerboundPlayerLoadedPacket || packet2 instanceof ServerboundMovePlayerPacket || packet2 instanceof ServerboundAcceptTeleportationPacket || packet2 instanceof ServerboundPlayerCommandPacket) {
            event.setCancelled(true);
        }
    }
}

