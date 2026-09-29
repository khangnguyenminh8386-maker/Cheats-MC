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

public class S2CMiningStatePacket
extends PbPacket {
    public static final int ID = 12;
    private final boolean hasPrimary;
    @Nullable
    private final BlockPos primaryPos;
    private final float primaryProgress;
    private final boolean hasSecondary;
    @Nullable
    private final BlockPos secondaryPos;
    private final float secondaryProgress;

    public S2CMiningStatePacket(@Nullable BlockPos primaryPos, float primaryProgress, @Nullable BlockPos secondaryPos, float secondaryProgress) {
        this.hasPrimary = primaryPos != null;
        this.primaryPos = primaryPos;
        this.primaryProgress = primaryProgress;
        this.hasSecondary = secondaryPos != null;
        this.secondaryPos = secondaryPos;
        this.secondaryProgress = secondaryProgress;
    }

    public S2CMiningStatePacket(FriendlyByteBuf buf) {
        this.hasPrimary = buf.readBoolean();
        if (this.hasPrimary) {
            this.primaryPos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
            this.primaryProgress = buf.readFloat();
        } else {
            this.primaryPos = null;
            this.primaryProgress = 0.0f;
        }
        this.hasSecondary = buf.readBoolean();
        if (this.hasSecondary) {
            this.secondaryPos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
            this.secondaryProgress = buf.readFloat();
        } else {
            this.secondaryPos = null;
            this.secondaryProgress = 0.0f;
        }
    }

    @Override
    public int getPacketId() {
        return 12;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(this.hasPrimary);
        if (this.hasPrimary) {
            buf.writeInt(this.primaryPos.getX());
            buf.writeInt(this.primaryPos.getY());
            buf.writeInt(this.primaryPos.getZ());
            buf.writeFloat(this.primaryProgress);
        }
        buf.writeBoolean(this.hasSecondary);
        if (this.hasSecondary) {
            buf.writeInt(this.secondaryPos.getX());
            buf.writeInt(this.secondaryPos.getY());
            buf.writeInt(this.secondaryPos.getZ());
            buf.writeFloat(this.secondaryProgress);
        }
    }

    public boolean hasPrimary() {
        return this.hasPrimary;
    }

    @Nullable
    public BlockPos getPrimaryPos() {
        return this.primaryPos;
    }

    public float getPrimaryProgress() {
        return this.primaryProgress;
    }

    public boolean hasSecondary() {
        return this.hasSecondary;
    }

    @Nullable
    public BlockPos getSecondaryPos() {
        return this.secondaryPos;
    }

    public float getSecondaryProgress() {
        return this.secondaryProgress;
    }
}

