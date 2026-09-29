/*
 * Decompiled with CFR 0.152.
 */
package night.settings.impl;

import night.Night;
import night.events.impl.SettingChangeEvent;
import night.settings.Setting;
import night.utils.animations.Animation;
import night.utils.animations.Easing;

public class BooleanSetting
extends Setting {
    private boolean value;
    private final boolean defaultValue;
    private final Animation openAnim;

    public float getOpenAmount() {
        return this.openAnim.get(this.value ? 1.0f : 0.0f);
    }

    public BooleanSetting(String name, String description, boolean value) {
        super(name, name, description, new Setting.Visibility());
        this.value = value;
        this.defaultValue = value;
        this.openAnim = BooleanSetting.newOpenAnim(value);
    }

    public BooleanSetting(String name, String tag, String description, boolean value) {
        super(name, tag, description, new Setting.Visibility());
        this.value = value;
        this.defaultValue = value;
        this.openAnim = BooleanSetting.newOpenAnim(value);
    }

    public BooleanSetting(String name, String description, Setting.Visibility visibility, boolean value) {
        super(name, name, description, visibility);
        this.value = value;
        this.defaultValue = value;
        this.openAnim = BooleanSetting.newOpenAnim(value);
    }

    public BooleanSetting(String name, String tag, String description, Setting.Visibility visibility, boolean value) {
        super(name, tag, description, visibility);
        this.value = value;
        this.defaultValue = value;
        this.openAnim = BooleanSetting.newOpenAnim(value);
    }

    private static Animation newOpenAnim(boolean startingValue) {
        float start = startingValue ? 1.0f : 0.0f;
        return new Animation(start, start, 180, Easing.Method.EASE_OUT_QUAD);
    }

    public boolean getValue() {
        return this.value;
    }

    public void setValue(boolean value) {
        this.value = value;
        Night.EVENT_HANDLER.post(new SettingChangeEvent(this));
    }

    public boolean getDefaultValue() {
        return this.defaultValue;
    }

    public void resetValue() {
        this.value = this.defaultValue;
    }

    public static class Visibility
    extends Setting.Visibility {
        private final BooleanSetting value;
        private final boolean targetValue;

        public Visibility(BooleanSetting value, boolean targetValue) {
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
            this.setVisible(this.getOpenAmount() > 0.001f);
        }

        public float getOpenAmount() {
            float amount = this.value.getOpenAmount();
            return this.targetValue ? amount : 1.0f - amount;
        }
    }
}

