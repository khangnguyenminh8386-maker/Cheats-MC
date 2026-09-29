/*
 * Decompiled with CFR 0.152.
 */
package night.modules.impl.movement;

import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.api.IFastClimbModule;
import night.settings.Setting;
import night.settings.impl.NumberSetting;

@RegisterModule(name="FastClimb", description="Modifies your climbing speed on ladders, scaffolding, vines and other climbables.", category=Module.Category.MOVEMENT)
public class FastClimbModule
extends Module
implements IFastClimbModule {
    public NumberSetting speed = new NumberSetting("Speed", "Speed", "How much of vanilla's climb speed to use. Ignore walks past climbables like normal blocks, 1 is vanilla's normal speed.", new Setting.Visibility(), Float.valueOf(1.0f), Float.valueOf(0.0f), Float.valueOf(1.0f), Float.valueOf(0.1f), true);

    @Override
    public float getSpeedValue() {
        return this.speed.getValue().floatValue();
    }
}

