/*
 * Decompiled with CFR 0.152.
 */
package night.modules.impl.miscellaneous;

import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.NumberSetting;

@RegisterModule(name="ExtraTab", description="Extends the size of the player list.", category=Module.Category.MISCELLANEOUS)
public class ExtraTabModule
extends Module {
    public NumberSetting limit = new NumberSetting("Limit", "The maximum amount of players that will be listed.", 1000, 1, 1000);
    public BooleanSetting friends = new BooleanSetting("Friends", "Highlights your friends on the player list.", true);

    @Override
    public String getMetaData() {
        return String.valueOf(this.limit.getValue().intValue());
    }
}

