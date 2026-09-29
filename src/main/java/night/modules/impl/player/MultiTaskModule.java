/*
 * Decompiled with CFR 0.152.
 */
package night.modules.impl.player;

import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;

@RegisterModule(name="MultiTask", description="Allows you to interact with blocks while eating or using an item.", category=Module.Category.PLAYER)
public class MultiTaskModule
extends Module {
    public BooleanSetting pearl = new BooleanSetting("Pearl", "Don't stop eating when you throw a pearl with Phase / KeyAction.", true);
    public BooleanSetting autoTotem = new BooleanSetting("AutoTotem", "Don't stop/reset your eating when AutoTotem swaps a totem into your offhand.", true);
}

