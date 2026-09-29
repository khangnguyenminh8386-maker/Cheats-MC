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

public class StringSetting
extends Setting {
    private String value;
    private final String defaultValue;

    public StringSetting(String name, String description, String value) {
        super(name, name, description, new Setting.Visibility());
        this.value = value;
        this.defaultValue = value;
    }

    public StringSetting(String name, String tag, String description, String value) {
        super(name, tag, description, new Setting.Visibility());
        this.value = value;
        this.defaultValue = value;
    }

    public StringSetting(String name, String description, Setting.Visibility visibility, String value) {
        super(name, name, description, visibility);
        this.value = value;
        this.defaultValue = value;
    }

    public StringSetting(String name, String tag, String description, Setting.Visibility visibility, String value) {
        super(name, tag, description, visibility);
        this.value = value;
        this.defaultValue = value;
    }

    public void resetValue() {
        this.value = this.defaultValue;
    }

    public void setValue(String value) {
        this.value = value;
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

    public static class Visibility
    extends Setting.Visibility {
        private final StringSetting value;
        private final String targetValue;

        public Visibility(StringSetting value, String targetValue) {
            super(value);
            this.value = value;
            this.targetValue = targetValue;
        }

        @Override
        public void update() {
            if (this.value.getVisibility() != null) {
                this.value.getVisibility().update();
                if (!this.value.getVisibility().isVisible()) {
                    this.setVisible(false);
                    return;
                }
            }
            this.setVisible(this.value.getValue().equals(this.targetValue));
        }
    }
}

