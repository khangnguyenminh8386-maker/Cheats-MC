/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package night.settings;

import lombok.Generated;

public class Setting {
    private final String name;
    private final String tag;
    private final String description;
    private final Visibility visibility;

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public String getTag() {
        return this.tag;
    }

    @Generated
    public String getDescription() {
        return this.description;
    }

    @Generated
    public Visibility getVisibility() {
        return this.visibility;
    }

    @Generated
    public Setting(String name, String tag, String description, Visibility visibility) {
        this.name = name;
        this.tag = tag;
        this.description = description;
        this.visibility = visibility;
    }

    public static class Visibility {
        private final Setting setting;
        private boolean visible = true;

        public Visibility() {
            this.setting = null;
        }

        public void update() {
        }

        @Generated
        public Setting getSetting() {
            return this.setting;
        }

        @Generated
        public boolean isVisible() {
            return this.visible;
        }

        @Generated
        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        @Generated
        public Visibility(Setting setting) {
            this.setting = setting;
        }
    }
}

