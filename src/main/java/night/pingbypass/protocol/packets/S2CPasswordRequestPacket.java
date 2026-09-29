/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package night.pingbypass.protocol.packets;

import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PbPacket;

public class S2CPasswordRequestPacket
extends PbPacket {
    public static final int ID = 1;

    public S2CPasswordRequestPacket() {
    }

    public S2CPasswordRequestPacket(FriendlyByteBuf buf) {
    }

    @Override
    public int getPacketId() {
        return 1;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
    }
}

