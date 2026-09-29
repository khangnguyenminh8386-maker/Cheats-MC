/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.Connection
 *  net.minecraft.network.ConnectionProtocol
 *  net.minecraft.network.PacketListener
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ClientboundKeepAlivePacket
 *  net.minecraft.network.protocol.common.ClientboundPingPacket
 *  net.minecraft.network.protocol.common.ClientboundTransferPacket
 *  net.minecraft.network.protocol.game.ClientboundBundlePacket
 *  net.minecraft.network.protocol.game.ClientboundStartConfigurationPacket
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package night.pingbypass.server;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ClientboundTransferPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundStartConfigurationPacket;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PacketReceiveEvent;
import night.pingbypass.PingBypassFlags;
import night.pingbypass.ProtocolTransitionTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S2CForwarder {
    private static final Logger LOGGER = LoggerFactory.getLogger(S2CForwarder.class);
    private final Connection clientConnection;
    private final List<Packet<?>> queuedPackets = new CopyOnWriteArrayList();
    private final AtomicBoolean locked = new AtomicBoolean(false);
    private volatile boolean active;

    public S2CForwarder(Connection clientConnection) {
        this.clientConnection = clientConnection;
        this.active = false;
    }

    public void start() {
        this.active = true;
        PingBypassFlags.proxyForwardingActive = true;
        Night.EVENT_HANDLER.subscribe(this);
        LOGGER.info("S2C forwarder started");
    }

    public void stop() {
        this.active = false;
        PingBypassFlags.proxyForwardingActive = false;
        Night.EVENT_HANDLER.unsubscribe(this);
        LOGGER.info("S2C forwarder stopped");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void lock() {
        AtomicBoolean atomicBoolean = this.locked;
        synchronized (atomicBoolean) {
            this.locked.set(true);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void unlockAndFlush() {
        AtomicBoolean atomicBoolean = this.locked;
        synchronized (atomicBoolean) {
            for (Packet<?> packet : this.queuedPackets) {
                this.sendToClient(packet);
            }
            this.queuedPackets.clear();
            this.locked.set(false);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketListener proxyListener;
        Connection serverConnection;
        if (!this.active || !this.clientConnection.isConnected()) {
            return;
        }
        Connection connection = serverConnection = Night.PROXY_SERVER != null ? Night.PROXY_SERVER.getServerConnection() : null;
        if (serverConnection == null || event.getConnection() != serverConnection) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof ClientboundKeepAlivePacket || packet instanceof ClientboundPingPacket) {
            return;
        }
        if (packet instanceof ClientboundTransferPacket || packet instanceof ClientboundStartConfigurationPacket) {
            LOGGER.info("Suppressed {} from being forwarded to client (proxy handles it internally)", (Object)packet.getClass().getSimpleName());
            ProtocolTransitionTracker.mark();
            return;
        }
        PacketListener packetListener = proxyListener = Night.PROXY_SERVER != null && Night.PROXY_SERVER.getServerConnection() != null ? Night.PROXY_SERVER.getServerConnection().getPacketListener() : null;
        if (proxyListener != null && proxyListener.protocol() != ConnectionProtocol.PLAY) {
            LOGGER.info("Suppressed {} while proxy's server connection is mid-reconfigure (protocol={})", (Object)packet.getClass().getSimpleName(), (Object)proxyListener.protocol());
            ProtocolTransitionTracker.mark();
            return;
        }
        if (packet instanceof ClientboundBundlePacket) {
            return;
        }
        AtomicBoolean atomicBoolean = this.locked;
        synchronized (atomicBoolean) {
            if (this.locked.get()) {
                this.queuedPackets.add(packet);
            } else {
                this.sendToClient(packet);
            }
        }
    }

    private void sendToClient(Packet<?> packet) {
        if (this.clientConnection.isConnected()) {
            this.clientConnection.send(packet);
        }
    }
}

