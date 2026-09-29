/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package night.pingbypass.protocol;

import net.minecraft.network.FriendlyByteBuf;

public abstract class PbPacket {
    public abstract int getPacketId();

    public abstract void write(FriendlyByteBuf var1);
}

