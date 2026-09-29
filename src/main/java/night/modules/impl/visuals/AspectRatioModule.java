/*
 * Decompiled with CFR 0.152.
 */
package night.modules.impl.visuals;

import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.NumberSetting;

@RegisterModule(name="AspectRatio", description="Modifies the game's aspect ratio.", category=Module.Category.VISUALS)
public class AspectRatioModule
extends Module {
    public NumberSetting ratio = new NumberSetting("Ratio", "The aspect ratio that will be applied to the game's rendering.", Float.valueOf(1.78f), Float.valueOf(0.0f), Float.valueOf(5.0f));

    @Override
    public String getMetaData() {
        return String.valueOf(this.ratio.getValue().floatValue());
    }
}

