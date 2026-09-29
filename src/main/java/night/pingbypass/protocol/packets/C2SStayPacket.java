/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package night.pingbypass.protocol.packets;

import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PbPacket;

public class C2SStayPacket
extends PbPacket {
    public static final int ID = 9;
    private final boolean stay;

    public C2SStayPacket(boolean stay) {
        this.stay = stay;
    }

    public C2SStayPacket(FriendlyByteBuf buf) {
        this.stay = buf.readBoolean();
    }

    @Override
    public int getPacketId() {
        return 9;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(this.stay);
    }

    public boolean isStay() {
        return this.stay;
    }
}

