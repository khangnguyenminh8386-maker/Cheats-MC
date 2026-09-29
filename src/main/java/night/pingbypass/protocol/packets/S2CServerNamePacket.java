/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package night.pingbypass.protocol.packets;

import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PbPacket;

public class S2CServerNamePacket
extends PbPacket {
    public static final int ID = 8;
    private final String serverIp;

    public S2CServerNamePacket(String serverIp) {
        this.serverIp = serverIp;
    }

    public S2CServerNamePacket(FriendlyByteBuf buf) {
        this.serverIp = buf.readUtf();
    }

    @Override
    public int getPacketId() {
        return 8;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.serverIp);
    }

    public String getServerIp() {
        return this.serverIp;
    }
}

