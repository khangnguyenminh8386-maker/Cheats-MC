/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package night.pingbypass.protocol.packets;

import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PbPacket;

public class C2SJoinPacket
extends PbPacket {
    public static final int ID = 0;
    private final String serverIp;
    private final int serverPort;

    public C2SJoinPacket(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    public C2SJoinPacket(FriendlyByteBuf buf) {
        this.serverIp = buf.readUtf();
        this.serverPort = buf.readVarInt();
    }

    @Override
    public int getPacketId() {
        return 0;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.serverIp);
        buf.writeVarInt(this.serverPort);
    }

    public String getServerIp() {
        return this.serverIp;
    }

    public int getServerPort() {
        return this.serverPort;
    }
}

