/*
 * Decompiled with CFR 0.152.
 */
package night.pingbypass.modules;

import java.awt.Color;
import night.pingbypass.modules.PbModule;
import night.settings.Setting;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.settings.impl.StringSetting;

public class SyncModule {
    public static void applyToggle(PbModule module, boolean enabled) {
        module.setToggled(enabled);
    }

    public static void applySetting(PbModule module, String settingName, String value) {
        Setting setting = module.getSetting(settingName);
        if (setting == null) {
            return;
        }
        if (setting instanceof BooleanSetting) {
            BooleanSetting s = (BooleanSetting)setting;
            s.setValue(Boolean.parseBoolean(value));
        } else if (setting instanceof NumberSetting) {
            NumberSetting s = (NumberSetting)setting;
            switch (s.getType()) {
                case INTEGER: {
                    s.setValue(Integer.parseInt(value));
                    break;
                }
                case LONG: {
                    s.setValue(Long.parseLong(value));
                    break;
                }
                case FLOAT: {
                    s.setValue(Float.valueOf(Float.parseFloat(value)));
                    break;
                }
                case DOUBLE: {
                    s.setValue(Double.parseDouble(value));
                }
            }
        } else if (setting instanceof ModeSetting) {
            ModeSetting s = (ModeSetting)setting;
            s.setValue(value);
        } else if (setting instanceof StringSetting) {
            StringSetting s = (StringSetting)setting;
            s.setValue(value);
        } else if (setting instanceof ColorSetting) {
            ColorSetting s = (ColorSetting)setting;
            String[] parts = value.split(",");
            if (parts.length >= 4) {
                s.setColor(new Color(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3])));
                if (parts.length >= 5) {
                    s.setSync(Boolean.parseBoolean(parts[4]));
                }
                if (parts.length >= 6) {
                    s.setRainbow(Boolean.parseBoolean(parts[5]));
                }
            }
        }
    }
}

