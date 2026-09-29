/*
 * Decompiled with CFR 0.152.
 */
package night.modules.impl.core;

import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;

@RegisterModule(name="Menu", description="Replaces the default title screen with the client's custom main menu screen.", category=Module.Category.CORE, persistent=true, drawn=false)
public class MenuModule
extends Module {
    public CategorySetting mainMenuCategory = new CategorySetting("MainMenu", "The category for settings related to the main menu.");
    public BooleanSetting mainMenu = new BooleanSetting("MainMenu", "Enabled", "Replaces Minecraft's default main menu with a customizable one.", new CategorySetting.Visibility(this.mainMenuCategory), true);
}

