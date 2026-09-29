/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.Connection
 *  net.minecraft.network.DisconnectionDetails
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.ping.ClientboundPongResponsePacket
 *  net.minecraft.network.protocol.ping.ServerboundPingRequestPacket
 *  net.minecraft.network.protocol.status.ClientboundStatusResponsePacket
 *  net.minecraft.network.protocol.status.ServerStatus
 *  net.minecraft.network.protocol.status.ServerStatusPacketListener
 *  net.minecraft.network.protocol.status.ServerboundStatusRequestPacket
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package night.pingbypass.handler;

import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.protocol.status.ServerStatusPacketListener;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;
import night.pingbypass.server.ProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbStatusHandler
implements ServerStatusPacketListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(PbStatusHandler.class);
    private final ProxyServer proxyServer;
    private final Connection connection;
    private boolean responseSent;

    public PbStatusHandler(ProxyServer proxyServer, Connection connection) {
        this.proxyServer = proxyServer;
        this.connection = connection;
    }

    public void handleStatusRequest(ServerboundStatusRequestPacket packet) {
        if (this.responseSent) {
            this.connection.disconnect((Component)Component.literal((String)"Status already sent"));
            return;
        }
        this.responseSent = true;
        Component description = this.buildDescription();
        ServerStatus metadata = new ServerStatus(description, Optional.empty(), Optional.empty(), Optional.empty(), false);
        this.connection.send((Packet)new ClientboundStatusResponsePacket(metadata));
    }

    public void handlePingRequest(ServerboundPingRequestPacket packet) {
        this.connection.send((Packet)new ClientboundPongResponsePacket(packet.getTime()));
        this.connection.disconnect((Component)Component.literal((String)"Ping done"));
    }

    public void onDisconnect(DisconnectionDetails info) {
    }

    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    private Component buildDescription() {
        Minecraft mc = Minecraft.getInstance();
        StringBuilder motd = new StringBuilder();
        motd.append("\u00a7dCheats MC PingBypass\u00a7r\n");
        if (mc.getConnection() != null && mc.player != null && mc.level != null) {
            String serverBrand = mc.getConnection().serverBrand();
            motd.append("\u00a7aConnected\u00a7r");
            if (this.proxyServer.getServerConnection() != null) {
                motd.append(" \u2014 ").append(mc.getCurrentServer() != null ? mc.getCurrentServer().ip : "unknown");
            }
        } else {
            motd.append("\u00a77Idle\u00a7r \u2014 not connected to any server");
        }
        return Component.literal((String)motd.toString());
    }
}

