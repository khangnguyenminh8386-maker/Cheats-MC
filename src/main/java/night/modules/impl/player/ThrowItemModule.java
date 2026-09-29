/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 */
package night.modules.impl.player;

import java.util.Map;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.TickEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.player.RekitModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.NumberSetting;

@RegisterModule(name="ThrowItem", description="Throw out items that arent belong to your loaded kit. Useful when you need to loot gears during combat", category=Module.Category.PLAYER)
public class ThrowItemModule
extends Module {
    public NumberSetting delay = new NumberSetting("Delay", "Tick delay between throw actions.", 1, 0, 20);
    public NumberSetting actionsPerTick = new NumberSetting("ActionsPerTick", "Maximum items to drop per pass.", 3, 1, 20);
    public BooleanSetting autoDisable = new BooleanSetting("AutoDisable", "Automatically disables the module when no items are left to throw.", false);
    public BooleanSetting keepKitItems = new BooleanSetting("KeepKitItems", "Keep items that belong to the loaded kit even if in a different slot.", true);
    public BooleanSetting minimumCount = new BooleanSetting("MinimumCount", "Keep matching items only if their stack count reaches minimum count.", false);
    public NumberSetting minCount = new NumberSetting("MinCount", "The minimum stack count to keep an item.", new BooleanSetting.Visibility(this.minimumCount, true), (Number)16, (Number)1, (Number)64);
    public BooleanSetting ignoreHotbar = new BooleanSetting("IgnoreHotbar", "Do not throw items from the hotbar (slots 0-8).", false);
    public BooleanSetting onlyInventory = new BooleanSetting("OnlyInventory", "Only throw items when in regular gameplay or InventoryScreen (not in Chests/Shulkers).", true);
    private int ticks = 0;

    @Override
    public void onEnable() {
        this.ticks = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        boolean foreignGui;
        if (ThrowItemModule.mc.player == null || ThrowItemModule.mc.level == null || ThrowItemModule.mc.gameMode == null) {
            return;
        }
        if (ThrowItemModule.mc.player.isCreative() || ThrowItemModule.mc.player.isSpectator()) {
            return;
        }
        if (!ThrowItemModule.mc.player.containerMenu.getCarried().isEmpty()) {
            return;
        }
        RekitModule rekit = Night.MODULE_MANAGER.getModule(RekitModule.class);
        if (rekit == null) {
            return;
        }
        Map<Integer, RekitModule.KitItem> activeKit = rekit.getActiveKit();
        if (activeKit == null || activeKit.isEmpty()) {
            return;
        }
        if (rekit.isAutoActive() || System.currentTimeMillis() - RekitModule.lastContainerActionMs < 200L) {
            return;
        }
        if (ThrowItemModule.mc.gui.screen() != null && !(ThrowItemModule.mc.gui.screen() instanceof InventoryScreen)) {
            return;
        }
        boolean bl = foreignGui = ThrowItemModule.mc.gui.screen() instanceof AbstractContainerScreen && !(ThrowItemModule.mc.gui.screen() instanceof InventoryScreen);
        if (this.onlyInventory.getValue() && foreignGui) {
            return;
        }
        if (this.ticks < this.delay.getValue().intValue()) {
            ++this.ticks;
            return;
        }
        this.ticks = 0;
        int containerId = ThrowItemModule.mc.player.containerMenu.containerId;
        int actions = 0;
        int maxActions = this.actionsPerTick.getValue().intValue();
        int totalItemsToThrow = 0;
        for (int invSlot = 0; invSlot < 36; ++invSlot) {
            int handlerSlot;
            ItemStack stack;
            if (this.ignoreHotbar.getValue() && invSlot <= 8 || (stack = ThrowItemModule.mc.player.getInventory().getItem(invSlot)).isEmpty()) continue;
            RekitModule.KitItem kitItem = activeKit.get(invSlot);
            String stackId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            boolean isKitItem = false;
            if (kitItem != null && kitItem.id != null && stackId.equalsIgnoreCase(kitItem.id)) {
                isKitItem = true;
            } else if (this.keepKitItems.getValue()) {
                for (RekitModule.KitItem item : activeKit.values()) {
                    if (item == null || item.id == null || !stackId.equalsIgnoreCase(item.id)) continue;
                    isKitItem = true;
                    break;
                }
            }
            boolean shouldThrow = false;
            if (!isKitItem) {
                shouldThrow = true;
            } else if (this.minimumCount.getValue() && stack.getMaxStackSize() > 1 && stack.getCount() < this.minCount.getValue().intValue()) {
                shouldThrow = true;
            }
            if (!shouldThrow) continue;
            ++totalItemsToThrow;
            if (actions >= maxActions || (handlerSlot = this.invToHandlerSlot(invSlot)) < 0) continue;
            this.throwSlot(containerId, handlerSlot);
            ++actions;
        }
        if (totalItemsToThrow == 0 && this.autoDisable.getValue()) {
            this.setToggled(false);
        }
    }

    private void throwSlot(int containerId, int slot) {
        ThrowItemModule.mc.gameMode.handleContainerInput(containerId, slot, 1, ContainerInput.THROW, (Player)ThrowItemModule.mc.player);
    }

    private int invToHandlerSlot(int invSlot) {
        Inventory playerInv = ThrowItemModule.mc.player.getInventory();
        for (Slot slot : ThrowItemModule.mc.player.containerMenu.slots) {
            if (slot.container != playerInv || slot.getContainerSlot() != invSlot) continue;
            return slot.index;
        }
        return -1;
    }
}

