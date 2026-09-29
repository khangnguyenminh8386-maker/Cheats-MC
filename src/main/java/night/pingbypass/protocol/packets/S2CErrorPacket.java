/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package night.pingbypass.protocol.packets;

import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PbPacket;

public class S2CErrorPacket
extends PbPacket {
    public static final int ID = 7;
    private final String message;

    public S2CErrorPacket(String message) {
        this.message = message;
    }

    public S2CErrorPacket(FriendlyByteBuf buf) {
        this.message = buf.readUtf();
    }

    @Override
    public int getPacketId() {
        return 7;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.message);
    }

    public String getMessage() {
        return this.message;
    }
}

