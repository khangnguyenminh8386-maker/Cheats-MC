/*
 * Decompiled with CFR 0.152.
 */
package night.modules.impl.player;

import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.NumberSetting;

@RegisterModule(name="NoBreakDelay", description="Allows you to customize the delay between breaking blocks.", category=Module.Category.PLAYER)
public class NoBreakDelayModule
extends Module {
    public NumberSetting delay = new NumberSetting("Delay", "The delay between block breaks in ticks (0 is no delay, 5 is vanilla).", 0, 0, 5);

    @Override
    public String getMetaData() {
        return String.valueOf(this.delay.getValue().intValue());
    }
}

