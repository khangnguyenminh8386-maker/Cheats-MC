/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package night.pingbypass.protocol.packets;

import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PbPacket;

public class C2SSettingChangePacket
extends PbPacket {
    public static final int ID = 4;
    private final String moduleName;
    private final String settingName;
    private final String value;

    public C2SSettingChangePacket(String moduleName, String settingName, String value) {
        this.moduleName = moduleName;
        this.settingName = settingName;
        this.value = value;
    }

    public C2SSettingChangePacket(FriendlyByteBuf buf) {
        this.moduleName = buf.readUtf();
        this.settingName = buf.readUtf();
        this.value = buf.readUtf();
    }

    @Override
    public int getPacketId() {
        return 4;
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

