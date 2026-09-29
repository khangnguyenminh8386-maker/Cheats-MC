/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.tags.ItemTags
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Items
 */
package night.modules.impl.player;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.NumberSetting;

@RegisterModule(name="FastPlace", description="Allows you to customize the tick delay between using items.", category=Module.Category.PLAYER)
public class FastPlaceModule
extends Module {
    public NumberSetting ticks = new NumberSetting("Ticks", "The amount of ticks that have to be waited for before using items again.", 1, 0, 20);
    public BooleanSetting ignoreBlocks = new BooleanSetting("IgnoreBlocks", "Uses the default Minecraft delay when holding a block item.", true);
    public BooleanSetting ignoreFireworks = new BooleanSetting("IgnoreFireworks", "Uses the default Minecraft delay when holding fireworks.", true);
    public BooleanSetting ignorePearls = new BooleanSetting("IgnorePearls", "Uses the default Minecraft delay when holding pearls.", true);
    public BooleanSetting ignoreEquipment = new BooleanSetting("IgnoreEquipment", "Uses the default Minecraft delay when holding an equipment item.", true);

    @Override
    public String getMetaData() {
        return String.valueOf(this.ticks.getValue().intValue());
    }

    public boolean isValidItem(Item item) {
        if (this.ignoreBlocks.getValue() && item instanceof BlockItem) {
            return false;
        }
        if (this.ignoreFireworks.getValue() && item == Items.FIREWORK_ROCKET) {
            return false;
        }
        if (this.ignorePearls.getValue() && item == Items.ENDER_PEARL) {
            return false;
        }
        boolean isArmor = item.builtInRegistryHolder().is(ItemTags.FOOT_ARMOR) || item.builtInRegistryHolder().is(ItemTags.LEG_ARMOR) || item.builtInRegistryHolder().is(ItemTags.CHEST_ARMOR) || item.builtInRegistryHolder().is(ItemTags.HEAD_ARMOR);
        return !this.ignoreEquipment.getValue() || !isArmor && item != Items.ELYTRA;
    }
}

