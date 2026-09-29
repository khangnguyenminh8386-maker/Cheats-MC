/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package night.settings.impl;

import lombok.Generated;
import night.Night;
import night.events.impl.SettingChangeEvent;
import night.settings.Setting;

public class ImageSetting
extends Setting {
    private String value;
    private final String defaultValue;

    public ImageSetting(String name, String description, String value) {
        super(name, name, description, new Setting.Visibility());
        this.value = value;
        this.defaultValue = value;
    }

    public ImageSetting(String name, String tag, String description, String value) {
        super(name, tag, description, new Setting.Visibility());
        this.value = value;
        this.defaultValue = value;
    }

    public ImageSetting(String name, String description, Setting.Visibility visibility, String value) {
        super(name, name, description, visibility);
        this.value = value;
        this.defaultValue = value;
    }

    public ImageSetting(String name, String tag, String description, Setting.Visibility visibility, String value) {
        super(name, tag, description, visibility);
        this.value = value;
        this.defaultValue = value;
    }

    public void resetValue() {
        this.setValue(this.defaultValue);
    }

    public void setValue(String value) {
        this.value = value;
        if (Night.IMAGE_MANAGER != null) {
            Night.IMAGE_MANAGER.setCurrentActiveImage(value);
        }
        Night.EVENT_HANDLER.post(new SettingChangeEvent(this));
    }

    @Generated
    public String getValue() {
        return this.value;
    }

    @Generated
    public String getDefaultValue() {
        return this.defaultValue;
    }
}

