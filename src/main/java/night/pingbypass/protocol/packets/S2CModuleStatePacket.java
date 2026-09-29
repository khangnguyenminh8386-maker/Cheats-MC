/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package night.pingbypass.protocol.packets;

import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PbPacket;

public class S2CModuleStatePacket
extends PbPacket {
    public static final int ID = 5;
    private final String moduleName;
    private final boolean enabled;

    public S2CModuleStatePacket(String moduleName, boolean enabled) {
        this.moduleName = moduleName;
        this.enabled = enabled;
    }

    public S2CModuleStatePacket(FriendlyByteBuf buf) {
        this.moduleName = buf.readUtf();
        this.enabled = buf.readBoolean();
    }

    @Override
    public int getPacketId() {
        return 5;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.moduleName);
        buf.writeBoolean(this.enabled);
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public boolean isEnabled() {
        return this.enabled;
    }
}

