/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.Connection
 */
package night.pingbypass.protocol;

import net.minecraft.network.Connection;
import night.pingbypass.protocol.PbPacket;

@FunctionalInterface
public interface PbPacketHandler<T extends PbPacket> {
    public void handle(T var1, Connection var2);
}

