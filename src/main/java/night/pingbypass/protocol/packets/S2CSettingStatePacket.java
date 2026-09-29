/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package night.pingbypass.protocol.packets;

import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PbPacket;

public class S2CSettingStatePacket
extends PbPacket {
    public static final int ID = 6;
    private final String moduleName;
    private final String settingName;
    private final String value;

    public S2CSettingStatePacket(String moduleName, String settingName, String value) {
        this.moduleName = moduleName;
        this.settingName = settingName;
        this.value = value;
    }

    public S2CSettingStatePacket(FriendlyByteBuf buf) {
        this.moduleName = buf.readUtf();
        this.settingName = buf.readUtf();
        this.value = buf.readUtf();
    }

    @Override
    public int getPacketId() {
        return 6;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.moduleName);
        buf.writeUtf(this.settingName);
        buf.writeUtf(this.value);
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public String getSettingName() {
        return this.settingName;
    }

    public String getValue() {
        return this.value;
    }
}

