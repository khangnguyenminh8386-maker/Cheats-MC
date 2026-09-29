/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package night.pingbypass.protocol.packets;

import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PbPacket;

public class C2SOpenInventoryPacket
extends PbPacket {
    public static final int ID = 15;
    private final boolean open;

    public C2SOpenInventoryPacket(boolean open) {
        this.open = open;
    }

    public C2SOpenInventoryPacket(FriendlyByteBuf buf) {
        this.open = buf.readBoolean();
    }

    @Override
    public int getPacketId() {
        return 15;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(this.open);
    }

    public boolean isOpen() {
        return this.open;
    }
}

