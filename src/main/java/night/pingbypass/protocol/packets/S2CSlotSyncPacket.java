/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package night.pingbypass.protocol.packets;

import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PbPacket;

public class S2CSlotSyncPacket
extends PbPacket {
    public static final int ID = 13;
    private final int slot;

    public S2CSlotSyncPacket(int slot) {
        this.slot = slot;
    }

    public S2CSlotSyncPacket(FriendlyByteBuf buf) {
        this.slot = buf.readVarInt();
    }

    @Override
    public int getPacketId() {
        return 13;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.slot);
    }

    public int getSlot() {
        return this.slot;
    }
}

