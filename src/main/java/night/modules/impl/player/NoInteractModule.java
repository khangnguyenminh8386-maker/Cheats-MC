/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Items
 */
package night.modules.impl.player;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;

@RegisterModule(name="NoInteract", description="Prevents you from interacting with right-clickable blocks.", category=Module.Category.PLAYER)
public class NoInteractModule
extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "The way that right-clickable blocks will be ignored.", "Sneak", new String[]{"Sneak", "Disable"});
    public BooleanSetting gapple = new BooleanSetting("Gapple", "Only disables interactions when holding a golden apple in your main hand.", false);

    @Override
    public String getMetaData() {
        return this.mode.getValue();
    }

    public boolean shouldNoInteract() {
        if (!this.gapple.getValue()) {
            return true;
        }
        Item held = NoInteractModule.mc.player.getMainHandItem().getItem();
        return held.equals(Items.ENCHANTED_GOLDEN_APPLE) || held.equals(Items.GOLDEN_APPLE);
    }
}

