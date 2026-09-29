/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.multiplayer.ServerData
 *  net.minecraft.client.multiplayer.resolver.ServerAddress
 */
package night.events.impl;

import lombok.Generated;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import night.events.Event;

public class ServerConnectEvent
extends Event {
    private final ServerAddress address;
    private final ServerData info;

    @Generated
    public ServerConnectEvent(ServerAddress address, ServerData info) {
        this.address = address;
        this.info = info;
    }

    @Generated
    public ServerAddress getAddress() {
        return this.address;
    }

    @Generated
    public ServerData getInfo() {
        return this.info;
    }
}

