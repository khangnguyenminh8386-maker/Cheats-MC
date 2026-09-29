/*
 * Decompiled with CFR 0.152.
 */
package night.pingbypass.modules;

import java.util.List;
import night.settings.Setting;

public abstract class PbModule {
    private final String name;
    private boolean toggled = false;

    protected PbModule(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public boolean isToggled() {
        return this.toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
        if (toggled) {
            this.onEnable();
        } else {
            this.onDisable();
        }
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public abstract void tick();

    public abstract List<Setting> getSettings();

    public Setting getSetting(String settingName) {
        return this.getSettings().stream().filter(s -> s.getName().equalsIgnoreCase(settingName)).findFirst().orElse(null);
    }
}

