/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.ShulkerBoxBlock
 */
package night.modules.impl.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.TickEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.player.RekitModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.NumberSetting;

@RegisterModule(name="InventorySorter", description="Auto-sorts your inventory into the active Rekit kit's layout.", category=Module.Category.PLAYER)
public class InventorySorterModule
extends Module {
    public NumberSetting delay = new NumberSetting("Delay", "Tick delay.", 1, 0, 10);
    public NumberSetting actionsPerTick = new NumberSetting("ActionsPerTick", "Max actions per tick.", 1, 1, 5);
    public BooleanSetting ignoreHotbar = new BooleanSetting("IgnoreHotbar", "Skip hotbar slots (0-8) when sorting.", false);
    private int ticks = 0;
    public static volatile long lastContainerActionMs = 0L;
    private int cursorWaitTicks = 0;
    private static final int CURSOR_DUMP_AFTER_TICKS = 20;

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (InventorySorterModule.mc.player == null) {
            return;
        }
        RekitModule rekit = Night.MODULE_MANAGER.getModule(RekitModule.class);
        if (rekit.getActiveKit().isEmpty()) {
            return;
        }
        if (InventorySorterModule.mc.player.isCreative()) {
            return;
        }
        if (InventorySorterModule.mc.gui.screen() instanceof AbstractContainerScreen && !(InventorySorterModule.mc.gui.screen() instanceof InventoryScreen)) {
            return;
        }
        if (this.ticks < this.delay.getValue().intValue()) {
            ++this.ticks;
            return;
        }
        this.ticks = 0;
        for (int executed = 0; executed < this.actionsPerTick.getValue().intValue() && this.sortTick(rekit); ++executed) {
        }
    }

    private boolean sortTick(RekitModule rekit) {
        AbstractContainerMenu handler = InventorySorterModule.mc.player.containerMenu;
        if (!handler.getCarried().isEmpty()) {
            ++this.cursorWaitTicks;
            if (this.cursorWaitTicks >= 20) {
                this.cursorWaitTicks = 0;
                int emptySlot = this.findEmptySlot(handler);
                if (emptySlot != -1) {
                    this.click(emptySlot, 0, ContainerInput.PICKUP);
                }
            }
            return false;
        }
        this.cursorWaitTicks = 0;
        HashMap<String, List> itemGroups = new HashMap<String, List>();
        for (int i = 0; i < 36; ++i) {
            RekitModule.KitItem kitI;
            int slotI;
            ItemStack stackI;
            if (this.ignoreHotbar.getValue() && i <= 8 || (stackI = handler.getSlot(slotI = this.getHandlerSlot(i)).getItem()).isEmpty() || this.isShulkerBox(stackI) || stackI.getCount() >= stackI.getItem().getDefaultMaxStackSize() || this.isCorrectItem(stackI, kitI = rekit.getActiveKit().get(i))) continue;
            String key = BuiltInRegistries.ITEM.getKey(stackI.getItem()).toString();
            itemGroups.computeIfAbsent(key, k -> new ArrayList()).add(slotI);
        }
        for (List slots : itemGroups.values()) {
            if (slots.size() <= 1) continue;
            for (int i = 0; i < slots.size(); ++i) {
                int slot1 = (Integer)slots.get(i);
                ItemStack s1 = handler.getSlot(slot1).getItem();
                for (int j = i + 1; j < slots.size(); ++j) {
                    int slot2 = (Integer)slots.get(j);
                    ItemStack s2 = handler.getSlot(slot2).getItem();
                    if (!ItemStack.isSameItemSameComponents((ItemStack)s1, (ItemStack)s2)) continue;
                    this.atomicSwap(slot2, slot1);
                    return true;
                }
            }
        }
        for (int i = 0; i < 36; ++i) {
            int sourceInvSlot;
            RekitModule.KitItem kit;
            int targetSlot;
            ItemStack currentStack;
            if (this.ignoreHotbar.getValue() && i <= 8 || this.isShulkerBox(currentStack = handler.getSlot(targetSlot = this.getHandlerSlot(i)).getItem()) || this.isCorrectItem(currentStack, kit = rekit.getActiveKit().get(i)) || (sourceInvSlot = this.findItemForKit(handler, rekit, kit)) == -1) continue;
            int sourceHandlerSlot = this.getHandlerSlot(sourceInvSlot);
            if (i <= 8 && sourceInvSlot >= 9) {
                this.click(sourceHandlerSlot, i, ContainerInput.SWAP);
                return true;
            }
            if (sourceInvSlot <= 8 && i >= 9) {
                this.click(targetSlot, sourceInvSlot, ContainerInput.SWAP);
                return true;
            }
            this.atomicSwap(sourceHandlerSlot, targetSlot);
            return true;
        }
        return false;
    }

    private void atomicSwap(int slot1, int slot2) {
        this.click(slot1, 0, ContainerInput.PICKUP);
        this.click(slot2, 0, ContainerInput.PICKUP);
        this.click(slot1, 0, ContainerInput.PICKUP);
    }

    private void click(int slotId, int button, ContainerInput type) {
        lastContainerActionMs = System.currentTimeMillis();
        InventorySorterModule.mc.gameMode.handleContainerInput(InventorySorterModule.mc.player.containerMenu.containerId, slotId, button, type, (Player)InventorySorterModule.mc.player);
    }

    private boolean isCorrectItem(ItemStack stack, RekitModule.KitItem kit) {
        if (kit == null) {
            return true;
        }
        if (stack.isEmpty()) {
            return false;
        }
        if (kit.id == null || kit.id.isEmpty()) {
            return false;
        }
        Identifier identifier = Identifier.tryParse((String)kit.id);
        if (identifier == null) {
            return false;
        }
        Item expected = (Item)BuiltInRegistries.ITEM.getValue(identifier);
        return expected != null && stack.getItem() == expected;
    }

    private int findItemForKit(AbstractContainerMenu handler, RekitModule rekit, RekitModule.KitItem targetKit) {
        if (targetKit == null || targetKit.id == null || targetKit.id.isEmpty()) {
            return -1;
        }
        Identifier identifier = Identifier.tryParse((String)targetKit.id);
        if (identifier == null) {
            return -1;
        }
        Item expected = (Item)BuiltInRegistries.ITEM.getValue(identifier);
        if (expected == null) {
            return -1;
        }
        for (int i = 0; i < 36; ++i) {
            RekitModule.KitItem itsOwnKit;
            int slotI;
            ItemStack stack;
            if (this.ignoreHotbar.getValue() && i <= 8 || (stack = handler.getSlot(slotI = this.getHandlerSlot(i)).getItem()).isEmpty() || this.isShulkerBox(stack) || stack.getItem() != expected || this.isCorrectItem(stack, itsOwnKit = rekit.getActiveKit().get(i))) continue;
            return i;
        }
        return -1;
    }

    private int findEmptySlot(AbstractContainerMenu handler) {
        int slotId;
        int i;
        RekitModule rekit = Night.MODULE_MANAGER.getModule(RekitModule.class);
        for (i = 0; i < 36; ++i) {
            if (this.ignoreHotbar.getValue() && i <= 8 || !handler.getSlot(slotId = this.getHandlerSlot(i)).getItem().isEmpty() || rekit.getActiveKit().get(i) != null) continue;
            return slotId;
        }
        for (i = 0; i < 36; ++i) {
            if (this.ignoreHotbar.getValue() && i <= 8 || !handler.getSlot(slotId = this.getHandlerSlot(i)).getItem().isEmpty()) continue;
            return slotId;
        }
        return -1;
    }

    private int getHandlerSlot(int invSlot) {
        if (invSlot >= 0 && invSlot <= 8) {
            return 36 + invSlot;
        }
        if (invSlot >= 9 && invSlot <= 35) {
            return invSlot;
        }
        return -1;
    }

    private boolean isShulkerBox(ItemStack stack) {
        BlockItem blockItem;
        Item item;
        return !stack.isEmpty() && (item = stack.getItem()) instanceof BlockItem && (blockItem = (BlockItem)item).getBlock() instanceof ShulkerBoxBlock;
    }
}

