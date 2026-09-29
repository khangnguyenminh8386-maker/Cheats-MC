/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.Packet
 */
package night.events.impl;

import lombok.Generated;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import night.events.Event;

public class PacketReceiveEvent
extends Event {
    private final Packet<?> packet;
    private final Connection connection;

    public PacketReceiveEvent(Packet<?> packet) {
        this(packet, null);
    }

    @Generated
    public Packet<?> getPacket() {
        return this.packet;
    }

    @Generated
    public Connection getConnection() {
        return this.connection;
    }

    @Generated
    public PacketReceiveEvent(Packet<?> packet, Connection connection) {
        this.packet = packet;
        this.connection = connection;
    }
}

