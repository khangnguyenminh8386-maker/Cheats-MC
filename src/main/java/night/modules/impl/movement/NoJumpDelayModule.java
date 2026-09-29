/*
 * Decompiled with CFR 0.152.
 */
package night.modules.impl.movement;

import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.api.INoJumpDelayModule;
import night.settings.impl.NumberSetting;

@RegisterModule(name="NoJumpDelay", description="Removes the delay that slows down your jumping.", category=Module.Category.MOVEMENT)
public class NoJumpDelayModule
extends Module
implements INoJumpDelayModule {
    public NumberSetting ticks = new NumberSetting("Ticks", "The amount of ticks that have to be waited for before jumping again.", 1, 0, 20);

    @Override
    public String getMetaData() {
        return String.valueOf(this.ticks.getValue());
    }

    @Override
    public int getTicksValue() {
        return this.ticks.getValue().intValue();
    }
}

