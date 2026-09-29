/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package night.pingbypass.protocol.packets;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PbPacket;

public class C2SFriendSyncPacket
extends PbPacket {
    public static final int ID = 14;
    private final List<String> friends;

    public C2SFriendSyncPacket(List<String> friends) {
        this.friends = friends;
    }

    public C2SFriendSyncPacket(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        this.friends = new ArrayList<String>(count);
        for (int i = 0; i < count; ++i) {
            this.friends.add(buf.readUtf());
        }
    }

    @Override
    public int getPacketId() {
        return 14;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.friends.size());
        for (String friend : this.friends) {
            buf.writeUtf(friend);
        }
    }

    public List<String> getFriends() {
        return this.friends;
    }
}

