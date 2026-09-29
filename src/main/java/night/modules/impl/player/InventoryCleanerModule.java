/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.contents.TranslatableContents
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.inventory.ShulkerBoxMenu
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 */
package night.modules.impl.player;

import java.util.ArrayList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import night.events.SubscribeEvent;
import night.events.impl.TickEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.player.InventorySorterModule;
import night.modules.impl.player.RekitModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.settings.impl.WhitelistSetting;

@RegisterModule(name="InventoryCleaner", description="Auto-drops unwanted items from your inventory each tick.", category=Module.Category.PLAYER)
public class InventoryCleanerModule
extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "WhiteList = drop items IN list. BlackList = drop items NOT in list. All = drop everything.", "WhiteList", new String[]{"WhiteList", "BlackList", "All"});
    public WhitelistSetting whitelist = new WhitelistSetting("Whitelist", "Items this mode's WhiteList/BlackList compares against.", WhitelistSetting.Type.ITEMS);
    public BooleanSetting ignoreHotbar = new BooleanSetting("IgnoreHotbar", "Skip hotbar slots (0-8) when cleaning.", true);
    public BooleanSetting throwWorse = new BooleanSetting("ThrowWorse", "Drop lower-tier duplicate tools/armor of the same type (e.g. diamond+iron pickaxe -> drop iron).", true);
    public BooleanSetting others = new BooleanSetting("Others", "Also drop from other open GUIs like Chest, Shulker, EnderChest.", false);
    public BooleanSetting ignoreCustomName = new BooleanSetting("IgnoreCustomName", "(With Others on) skip acting on custom-titled containers such as a shop GUI, except shulker boxes. Does not affect individual items.", false);
    public NumberSetting delay = new NumberSetting("Delay", "Tick delay between drop passes.", 1, 0, 20);
    public NumberSetting actionsPerTick = new NumberSetting("ActionsPerTick", "Max items to drop per pass.", 5, 1, 20);
    private int ticks = 0;

    @SubscribeEvent
    public void onTick(TickEvent event) {
        boolean customShopGui;
        boolean externalGui;
        if (InventoryCleanerModule.mc.player == null || InventoryCleanerModule.mc.level == null || InventoryCleanerModule.mc.gameMode == null) {
            return;
        }
        if (InventoryCleanerModule.mc.player.isCreative()) {
            return;
        }
        if (!InventoryCleanerModule.mc.player.containerMenu.getCarried().isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - RekitModule.lastContainerActionMs < 200L || now - InventorySorterModule.lastContainerActionMs < 200L) {
            return;
        }
        boolean bl = externalGui = InventoryCleanerModule.mc.gui.screen() instanceof AbstractContainerScreen && !(InventoryCleanerModule.mc.gui.screen() instanceof InventoryScreen);
        if (this.ticks < this.delay.getValue().intValue()) {
            ++this.ticks;
            return;
        }
        this.ticks = 0;
        int containerId = InventoryCleanerModule.mc.player.containerMenu.containerId;
        int actions = 0;
        int maxActions = this.actionsPerTick.getValue().intValue();
        for (int invSlot = 0; invSlot < 36 && actions < maxActions; ++invSlot) {
            int handlerSlot;
            ItemStack stack;
            if (this.ignoreHotbar.getValue() && invSlot <= 8 || (stack = InventoryCleanerModule.mc.player.getInventory().getItem(invSlot)).isEmpty() || !this.shouldDrop(stack) || (handlerSlot = this.invToHandlerSlot(invSlot)) < 0) continue;
            this.throwSlot(containerId, handlerSlot);
            ++actions;
        }
        if (this.throwWorse.getValue() && actions < maxActions) {
            actions += this.runThrowWorsePass(containerId, maxActions - actions);
        }
        boolean bl2 = customShopGui = this.ignoreCustomName.getValue() && this.hasCustomContainerTitle() && !(InventoryCleanerModule.mc.player.containerMenu instanceof ShulkerBoxMenu);
        if (externalGui && this.others.getValue() && !customShopGui && actions < maxActions) {
            int totalSlots = InventoryCleanerModule.mc.player.containerMenu.slots.size();
            int containerSlotCount = totalSlots - 36;
            for (int slot = 0; slot < containerSlotCount && actions < maxActions; ++slot) {
                ItemStack stack = ((Slot)InventoryCleanerModule.mc.player.containerMenu.slots.get(slot)).getItem();
                if (stack.isEmpty() || !this.shouldDrop(stack)) continue;
                this.throwSlot(containerId, slot);
                ++actions;
            }
        }
    }

    private void throwSlot(int containerId, int slot) {
        InventoryCleanerModule.mc.gameMode.handleContainerInput(containerId, slot, 1, ContainerInput.THROW, (Player)InventoryCleanerModule.mc.player);
    }

    private boolean hasCustomContainerTitle() {
        Screen screen = InventoryCleanerModule.mc.gui.screen();
        if (!(screen instanceof AbstractContainerScreen)) {
            return false;
        }
        AbstractContainerScreen screen2 = (AbstractContainerScreen)screen;
        Component title = screen2.getTitle();
        if (title == null) {
            return false;
        }
        return !(title.getContents() instanceof TranslatableContents);
    }

    private boolean shouldDrop(ItemStack stack) {
        boolean listed = this.whitelist.isWhitelistContains(stack.getItem());
        return switch (this.mode.getValue()) {
            case "All" -> true;
            case "BlackList" -> {
                if (!listed) {
                    yield true;
                }
                yield false;
            }
            default -> listed;
        };
    }

    private int runThrowWorsePass(int containerId, int budget) {
        String[] suffixes = new String[]{"_pickaxe", "_axe", "_shovel", "_hoe", "_sword", "_helmet", "_chestplate", "_leggings", "_boots"};
        int remaining = budget;
        block0: for (String suffix : suffixes) {
            if (remaining <= 0) break;
            ArrayList<int[]> group = new ArrayList<int[]>();
            for (int invSlot = 0; invSlot < 36; ++invSlot) {
                String key;
                ItemStack stack;
                if (this.ignoreHotbar.getValue() && invSlot <= 8 || (stack = InventoryCleanerModule.mc.player.getInventory().getItem(invSlot)).isEmpty() || !(key = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()).endsWith(suffix)) continue;
                group.add(new int[]{invSlot, stack.getMaxDamage()});
            }
            if (group.size() < 2) continue;
            int maxDur = group.stream().mapToInt(e -> e[1]).max().orElse(0);
            for (int[] entry : group) {
                int handlerSlot;
                if (remaining <= 0) continue block0;
                if (entry[1] >= maxDur || (handlerSlot = this.invToHandlerSlot(entry[0])) < 0) continue;
                this.throwSlot(containerId, handlerSlot);
                --remaining;
            }
        }
        return budget - remaining;
    }

    private int invToHandlerSlot(int invSlot) {
        Inventory playerInv = InventoryCleanerModule.mc.player.getInventory();
        for (Slot slot : InventoryCleanerModule.mc.player.containerMenu.slots) {
            if (slot.container != playerInv || slot.getContainerSlot() != invSlot) continue;
            return slot.index;
        }
        return -1;
    }
}

