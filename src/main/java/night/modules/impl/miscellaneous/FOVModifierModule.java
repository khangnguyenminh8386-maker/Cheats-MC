/*
 * Decompiled with CFR 0.152.
 */
package night.modules.impl.miscellaneous;

import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.api.IFOVModifierModule;
import night.settings.impl.NumberSetting;

@RegisterModule(name="FOVModifier", description="Gives you more customizability for the games FOV.", category=Module.Category.MISCELLANEOUS)
public class FOVModifierModule
extends Module
implements IFOVModifierModule {
    public NumberSetting fov = new NumberSetting("FOV", "The FOV you want to use.", 120, 50, 150);

    @Override
    public String getMetaData() {
        return "" + this.fov.getValue().intValue();
    }

    @Override
    public float getFovValue() {
        return this.fov.getValue().floatValue();
    }
}

