/*
 * Decompiled with CFR 0.152.
 */
package night.modules.impl.core;

import java.awt.Color;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.CategorySetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.NumberSetting;

@RegisterModule(name="Color", description="Manages the client's global color system.", category=Module.Category.CORE, persistent=true, drawn=false)
public class ColorModule
extends Module {
    public ColorSetting color = new ColorSetting("Color", "The global color that is used in sync and in most of the elements.", new ColorSetting.Color(new Color(160, 120, 255), false, false));
    public CategorySetting rainbowCategory = new CategorySetting("Rainbow", "The category containing all settings related to rainbow coloring.");
    public NumberSetting rainbowSpeed = new NumberSetting("RainbowSpeed", "Speed", "The speed that rainbow colors will be cycling at.", new CategorySetting.Visibility(this.rainbowCategory), 6L, 1L, 20L);
    public NumberSetting rainbowSaturation = new NumberSetting("RainbowSaturation", "Saturation", "The saturation value of the rainbow color.", new CategorySetting.Visibility(this.rainbowCategory), Float.valueOf(100.0f), Float.valueOf(0.0f), Float.valueOf(100.0f));
    public NumberSetting rainbowBrightness = new NumberSetting("RainbowBrightness", "Brightness", "The brightness value of the rainbow color.", new CategorySetting.Visibility(this.rainbowCategory), Float.valueOf(100.0f), Float.valueOf(0.0f), Float.valueOf(100.0f));
    public NumberSetting rainbowScale = new NumberSetting("RainbowScale", "Scale", "The scale / density of the rainbow bands.", new CategorySetting.Visibility(this.rainbowCategory), Float.valueOf(100.0f), Float.valueOf(10.0f), Float.valueOf(500.0f));
}

