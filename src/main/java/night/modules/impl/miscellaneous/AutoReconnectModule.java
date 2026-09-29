/*
 * Decompiled with CFR 0.152.
 */
package night.modules.impl.miscellaneous;

import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.NumberSetting;

@RegisterModule(name="AutoReconnect", description="Automatically reconnects you to a server after a specified time period.", category=Module.Category.MISCELLANEOUS)
public class AutoReconnectModule
extends Module {
    public NumberSetting delay = new NumberSetting("Delay", "The amount of seconds that have to pass before reconnecting.", 5, 0, 20);

    @Override
    public String getMetaData() {
        return String.valueOf(this.delay.getValue().intValue());
    }
}

