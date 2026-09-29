/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.FriendlyByteBuf
 *  org.jetbrains.annotations.Nullable
 */
package night.pingbypass.protocol.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PbPacket;
import org.jetbrains.annotations.Nullable;

public class S2CRenderPositionPacket
extends PbPacket {
    public static final int ID = 10;
    @Nullable
    private final BlockPos position;

    public S2CRenderPositionPacket(@Nullable BlockPos position) {
        this.position = position;
    }

    public S2CRenderPositionPacket(FriendlyByteBuf buf) {
        this.position = buf.readBoolean() ? new BlockPos(buf.readInt(), buf.readInt(), buf.readInt()) : null;
    }

    @Override
    public int getPacketId() {
        return 10;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(this.position != null);
        if (this.position != null) {
            buf.writeInt(this.position.getX());
            buf.writeInt(this.position.getY());
            buf.writeInt(this.position.getZ());
        }
    }

    @Nullable
    public BlockPos getPosition() {
        return this.position;
    }
}

