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
import night.modules.impl.core.ColorModule;
import night.settings.Setting;
import night.utils.color.ColorUtils;

public class ColorSetting
extends Setting {
    private Color value;
    private final Color defaultValue;

    public ColorSetting(String name, String description, Color value) {
        super(name, name, description, new Setting.Visibility());
        this.value = value;
        this.defaultValue = new Color(value.getColor(), value.isSync(), value.isRainbow());
    }

    public ColorSetting(String name, String tag, String description, Color value) {
        super(name, tag, description, new Setting.Visibility());
        this.value = value;
        this.defaultValue = new Color(value.getColor(), value.isSync(), value.isRainbow());
    }

    public ColorSetting(String name, String description, Setting.Visibility visibility, Color value) {
        super(name, name, description, visibility);
        this.value = value;
        this.defaultValue = new Color(value.getColor(), value.isSync(), value.isRainbow());
    }

    public ColorSetting(String name, String tag, String description, Setting.Visibility visibility, Color value) {
        super(name, tag, description, visibility);
        this.value = value;
        this.defaultValue = new Color(value.getColor(), value.isSync(), value.isRainbow());
    }

    public java.awt.Color getColor() {
        return this.getColor(0L);
    }

    public java.awt.Color getColor(long index) {
        if (this.isSync()) {
            return ColorUtils.getGlobalColor(this.getAlpha(), index);
        }
        if (this.isRainbow()) {
            ColorModule cm = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ColorModule.class) : null;
            long speed = cm != null ? cm.rainbowSpeed.getValue().longValue() : 6L;
            float sat = cm != null ? cm.rainbowSaturation.getValue().floatValue() / 100.0f : 1.0f;
            float bri = cm != null ? cm.rainbowBrightness.getValue().floatValue() / 100.0f : 1.0f;
            float scale = cm != null && cm.rainbowScale != null ? cm.rainbowScale.getValue().floatValue() / 100.0f : 1.0f;
            return ColorUtils.getRainbow(speed, sat, bri, this.getAlpha(), (long)((float)index * scale));
        }
        if (Night.MODULE_MANAGER != null && this == Night.MODULE_MANAGER.getModule(ColorModule.class).color) {
            return ColorUtils.getColor(this.value.getColor(), 255);
        }
        return this.value.getColor();
    }

    public void setValue(Color value) {
        this.value = value;
        if (Night.EVENT_HANDLER != null) {
            Night.EVENT_HANDLER.post(new SettingChangeEvent(this));
        }
    }

    public void setColor(java.awt.Color color) {
        this.value.setColor(color);
        if (Night.EVENT_HANDLER != null) {
            Night.EVENT_HANDLER.post(new SettingChangeEvent(this));
        }
    }

    public int getAlpha() {
        return this.getValue().getColor().getAlpha();
    }

    public boolean isSync() {
        return this.value.isSync();
    }

    public void setSync(boolean sync) {
        this.value.setSync(sync);
        if (Night.EVENT_HANDLER != null) {
            Night.EVENT_HANDLER.post(new SettingChangeEvent(this));
        }
    }

    public boolean isRainbow() {
        return this.value.isRainbow();
    }

    public void setRainbow(boolean rainbow) {
        this.value.setRainbow(rainbow);
        if (Night.EVENT_HANDLER != null) {
            Night.EVENT_HANDLER.post(new SettingChangeEvent(this));
        }
    }

    public void resetValue() {
        this.value = new Color(new java.awt.Color(this.defaultValue.getColor().getRGB(), true), this.defaultValue.isSync(), this.defaultValue.isRainbow());
        if (Night.EVENT_HANDLER != null) {
            Night.EVENT_HANDLER.post(new SettingChangeEvent(this));
        }
    }

    @Generated
    public Color getValue() {
        return this.value;
    }

    @Generated
    public Color getDefaultValue() {
        return this.defaultValue;
    }

    public static class Color {
        private java.awt.Color color;
        private boolean sync;
        private boolean rainbow;

        @Generated
        public java.awt.Color getColor() {
            return this.color;
        }

        @Generated
        public boolean isSync() {
            return this.sync;
        }

        @Generated
        public boolean isRainbow() {
            return this.rainbow;
        }

        @Generated
        public void setColor(java.awt.Color color) {
            this.color = color;
        }

        @Generated
        public void setSync(boolean sync) {
            this.sync = sync;
        }

        @Generated
        public void setRainbow(boolean rainbow) {
            this.rainbow = rainbow;
        }

        @Generated
        public Color(java.awt.Color color, boolean sync, boolean rainbow) {
            this.color = color;
            this.sync = sync;
            this.rainbow = rainbow;
        }
    }

    public static class Visibility
    extends Setting.Visibility {
        private final ColorSetting value;
        private final boolean targetValue;
        private final Target target;

        public Visibility(ColorSetting value, boolean targetValue, Target target) {
            super(value);
            this.value = value;
            this.targetValue = targetValue;
            this.target = target;
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
            this.setVisible(this.target == Target.RAINBOW ? this.value.isRainbow() == this.targetValue : this.value.isSync() == this.targetValue);
        }

        public static enum Target {
            RAINBOW,
            SYNC;

        }
    }
}

