/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package night.pingbypass.protocol.packets;

import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PbPacket;

public class C2SPasswordPacket
extends PbPacket {
    public static final int ID = 2;
    private final String password;

    public C2SPasswordPacket(String password) {
        this.password = password;
    }

    public C2SPasswordPacket(FriendlyByteBuf buf) {
        this.password = buf.readUtf();
    }

    @Override
    public int getPacketId() {
        return 2;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.password);
    }

    public String getPassword() {
        return this.password;
    }
}

