/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package night.modules.impl.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PlayerUpdateEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.combat.MainhandModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.minecraft.InventoryUtils;

@RegisterModule(name="Replenish", description="Automatically replenishes stacks in your hotbar with new ones when they meet a specified threshold.", category=Module.Category.PLAYER)
public class ReplenishModule
extends Module {
    public ModeSetting switchMode = new ModeSetting("Switch", "The mode that will be used for switching items.", "Swap", new String[]{"Pickup", "Swap", "Quick"});
    public NumberSetting threshold = new NumberSetting("Threshold", "The minimum amount of items in a stack before that stack is replaced.", 12, 1, 64);
    public NumberSetting minimumCount = new NumberSetting("MinimumCount", "The minimum amount of items that should be in the new stack.", 48, 1, 64);
    public BooleanSetting unstackable = new BooleanSetting("Unstackable", "Replenishes items that cannot be stacked (like Totems).", false);
    public BooleanSetting inventory = new BooleanSetting("Inventory", "Allows replenishing items while an inventory, container, or custom GUI is open.", false);
    private int ticks;
    private final Item[] hotbarItems = new Item[9];
    private final int[] hotbarCounts = new int[9];

    @Override
    public void onEnable() {
        if (ReplenishModule.mc.player == null) {
            return;
        }
        for (int i = 0; i <= 8; ++i) {
            ItemStack stack = ReplenishModule.mc.player.getInventory().getItem(i);
            this.hotbarItems[i] = stack.getItem();
            this.hotbarCounts[i] = stack.getCount();
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (ReplenishModule.mc.player == null || ReplenishModule.mc.level == null) {
            return;
        }
        if (!this.inventory.getValue()) {
            boolean inGui;
            boolean bl = inGui = ReplenishModule.mc.gui != null && ReplenishModule.mc.gui.screen() != null || ReplenishModule.mc.player.containerMenu != null && ReplenishModule.mc.player.containerMenu != ReplenishModule.mc.player.inventoryMenu;
            if (inGui) {
                for (int i = 0; i <= 8; ++i) {
                    ItemStack stack = ReplenishModule.mc.player.getInventory().getItem(i);
                    this.hotbarItems[i] = stack.getItem();
                    this.hotbarCounts[i] = stack.getCount();
                }
                return;
            }
        }
        if (ReplenishModule.mc.player.containerMenu != null && ReplenishModule.mc.player.containerMenu != ReplenishModule.mc.player.inventoryMenu && ReplenishModule.mc.player.containerMenu.slots.size() < 36) {
            return;
        }
        if (this.ticks <= 0) {
            for (int i = 0; i <= 8; ++i) {
                int slot;
                ItemStack targetStack;
                ItemStack stack = ReplenishModule.mc.player.getInventory().getItem(i);
                Item currentItem = stack.getItem();
                int currentCount = stack.getCount();
                Item previousItem = this.hotbarItems[i];
                int previousCount = this.hotbarCounts[i];
                this.hotbarItems[i] = currentItem;
                this.hotbarCounts[i] = currentCount;
                if (previousItem == null || previousItem == Items.AIR || currentItem == previousItem && currentCount >= previousCount) continue;
                Item targetItem = currentItem == Items.AIR ? previousItem : currentItem;
                ItemStack itemStack = targetStack = currentItem == Items.AIR ? new ItemStack((ItemLike)previousItem) : stack;
                if (!targetStack.isStackable() && !this.unstackable.getValue() || targetItem == Items.TOTEM_OF_UNDYING && Night.MODULE_MANAGER.getModule(MainhandModule.class).legitOwnsTotems() || (!targetStack.isStackable() ? currentCount > 0 : currentCount > (int)(this.threshold.getValue().floatValue() / 64.0f * (float)targetStack.getMaxStackSize())) || (slot = InventoryUtils.findInventory(targetItem, (int)((float)this.minimumCount.getValue().intValue() / 64.0f * (float)targetStack.getMaxStackSize()))) == -1) continue;
                if (this.switchMode.getValue().equalsIgnoreCase("Quick")) {
                    ReplenishModule.mc.gameMode.handleContainerInput(ReplenishModule.mc.player.containerMenu.containerId, InventoryUtils.indexToSlot(slot), 0, ContainerInput.QUICK_MOVE, (Player)ReplenishModule.mc.player);
                } else {
                    InventoryUtils.swap(this.switchMode.getValue(), slot, i);
                }
                this.ticks = 2 + Night.SERVER_MANAGER.getPingDelay();
            }
        }
        --this.ticks;
    }

    @Override
    public String getMetaData() {
        return String.valueOf(this.threshold.getValue().intValue());
    }
}

