/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package night.settings.impl;

import lombok.Generated;
import night.settings.Setting;
import night.utils.animations.Animation;
import night.utils.animations.Easing;

public class CategorySetting
extends Setting {
    private boolean open = false;
    private final Animation openAnim = new Animation(200, Easing.Method.EASE_OUT_CUBIC);

    public CategorySetting(String name, String description) {
        super(name, name, description, new Setting.Visibility());
    }

    public CategorySetting(String name, String tag, String description) {
        super(name, tag, description, new Setting.Visibility());
    }

    public CategorySetting(String name, String description, Setting.Visibility visibility) {
        super(name, name, description, visibility);
    }

    public CategorySetting(String name, String tag, String description, Setting.Visibility visibility) {
        super(name, tag, description, visibility);
    }

    public float getOpenAmount() {
        return this.openAnim.get(this.open ? 1.0f : 0.0f);
    }

    @Generated
    public boolean isOpen() {
        return this.open;
    }

    @Generated
    public Animation getOpenAnim() {
        return this.openAnim;
    }

    @Generated
    public void setOpen(boolean open) {
        this.open = open;
    }

    public static class Visibility
    extends Setting.Visibility {
        private final CategorySetting value;

        public Visibility(CategorySetting value) {
            super(value);
            this.value = value;
        }

        public CategorySetting getValue() {
            return this.value;
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
            this.setVisible(this.value.getOpenAmount() > 0.001f);
        }
    }
}

