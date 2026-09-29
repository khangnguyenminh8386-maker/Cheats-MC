/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package night.settings.impl;

import java.util.Arrays;
import java.util.List;
import lombok.Generated;
import night.Night;
import night.events.impl.SettingChangeEvent;
import night.settings.Setting;

public class ModeSetting
extends Setting {
    private String value;
    private final String defaultValue;
    private final List<String> modes;

    public ModeSetting(String name, String description, String value, String[] modes) {
        super(name, name, description, new Setting.Visibility());
        this.value = value;
        this.defaultValue = value;
        this.modes = Arrays.asList(modes);
    }

    public ModeSetting(String name, String tag, String description, String value, String[] modes) {
        super(name, tag, description, new Setting.Visibility());
        this.value = value;
        this.defaultValue = value;
        this.modes = Arrays.asList(modes);
    }

    public ModeSetting(String name, String description, Setting.Visibility visibility, String value, String[] modes) {
        super(name, name, description, visibility);
        this.value = value;
        this.defaultValue = value;
        this.modes = Arrays.asList(modes);
    }

    public ModeSetting(String name, String tag, String description, Setting.Visibility visibility, String value, String[] modes) {
        super(name, tag, description, visibility);
        this.value = value;
        this.defaultValue = value;
        this.modes = Arrays.asList(modes);
    }

    public void setValue(String value) {
        if (!this.modes.contains(value)) {
            return;
        }
        this.value = value;
        Night.EVENT_HANDLER.post(new SettingChangeEvent(this));
    }

    public void resetValue() {
        this.value = this.defaultValue;
    }

    @Generated
    public String getValue() {
        return this.value;
    }

    @Generated
    public String getDefaultValue() {
        return this.defaultValue;
    }

    @Generated
    public List<String> getModes() {
        return this.modes;
    }

    public static class Visibility
    extends Setting.Visibility {
        private final ModeSetting value;
        private final List<String> targetValues;

        public Visibility(ModeSetting value, String ... targetValues) {
            super(value);
            this.value = value;
            this.targetValues = Arrays.asList(targetValues);
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
            boolean visible = false;
            for (String value : this.targetValues) {
                if (!this.value.getValue().equals(value)) continue;
                visible = true;
                break;
            }
            this.setVisible(visible);
        }
    }
}

