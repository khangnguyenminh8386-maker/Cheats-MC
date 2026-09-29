/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.FriendlyByteBuf
 */
package night.pingbypass.protocol.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PbPacket;

public class S2CBlockRenderPacket
extends PbPacket {
    public static final int ID = 11;
    private final BlockPos position;

    public S2CBlockRenderPacket(BlockPos position) {
        this.position = position;
    }

    public S2CBlockRenderPacket(FriendlyByteBuf buf) {
        this.position = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public int getPacketId() {
        return 11;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.position.getX());
        buf.writeInt(this.position.getY());
        buf.writeInt(this.position.getZ());
    }

    public BlockPos getPosition() {
        return this.position;
    }
}

