/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package night.events.impl;

import lombok.Generated;
import night.events.Event;
import night.settings.Setting;

public class SettingChangeEvent
extends Event {
    private final Setting setting;

    @Generated
    public Setting getSetting() {
        return this.setting;
    }

    @Generated
    public SettingChangeEvent(Setting setting) {
        this.setting = setting;
    }
}

