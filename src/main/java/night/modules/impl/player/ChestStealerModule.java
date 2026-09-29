/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
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

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
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

@RegisterModule(name="ChestStealer", description="Automatically steals, dumps, or drops items to and from an open container.", category=Module.Category.PLAYER)
public class ChestStealerModule
extends Module {
    public BooleanSetting steal = new BooleanSetting("Steal", "Move matching items FROM the open container INTO your inventory.", true);
    public ModeSetting stealMode = new ModeSetting("Mode", "WhiteList = steal items IN the list. BlackList = steal items NOT in the list. All = steal everything.", new BooleanSetting.Visibility(this.steal, true), "All", new String[]{"WhiteList", "BlackList", "All"});
    public WhitelistSetting stealWhitelist = new WhitelistSetting("List", "Items to compare against.", new BooleanSetting.Visibility(this.steal, true), WhitelistSetting.Type.ITEMS);
    public BooleanSetting dump = new BooleanSetting("Dump", "Move matching items FROM your inventory INTO the open container.", false);
    public ModeSetting dumpMode = new ModeSetting("Mode", "WhiteList = dump items IN the list. BlackList = dump items NOT in the list. All = dump everything.", new BooleanSetting.Visibility(this.dump, true), "WhiteList", new String[]{"WhiteList", "BlackList", "All"});
    public WhitelistSetting dumpWhitelist = new WhitelistSetting("List", "Items to compare against.", new BooleanSetting.Visibility(this.dump, true), WhitelistSetting.Type.ITEMS);
    public BooleanSetting drop = new BooleanSetting("Drop", "Throw matching items OUT (Q / THROW).", false);
    public ModeSetting dropMode = new ModeSetting("DropMode", "Mode", "WhiteList = drop items IN the list. BlackList = drop items NOT in the list. All = drop everything.", new BooleanSetting.Visibility(this.drop, true), "All", new String[]{"WhiteList", "BlackList", "All"});
    public WhitelistSetting dropWhitelist = new WhitelistSetting("DropList", "List", "Items to compare against for drop.", new BooleanSetting.Visibility(this.drop, true), WhitelistSetting.Type.ITEMS);
    public ModeSetting interact = new ModeSetting("Interact", "Which of YOUR slots Dump/Drop act on: Hotbar (0-8), Inventory (9-35), or Both.", "Both", new String[]{"Hotbar", "Inventory", "Both"});
    public NumberSetting delay = new NumberSetting("Delay", "Tick delay between action passes.", 1, 0, 20);
    public NumberSetting actionsPerTick = new NumberSetting("ActionsPerTick", "Max slot actions per pass.", 5, 1, 36);
    public BooleanSetting buttonMode = new BooleanSetting("ButtonMode", "Adds vanilla Steal/Dump/Drop buttons at the top-right of the container/inventory GUI.", false);
    public BooleanSetting ignoreCustomName = new BooleanSetting("IgnoreCustomName", "Skip any container with a custom (non-vanilla) title such as a shop GUI, except shulker boxes.", false);
    private int ticks = 0;
    public static volatile long lastContainerActionMs = 0L;
    private Task activeTask = Task.NONE;
    private int taskStep = 0;
    private int lastContainerId = -1;
    private int autoStealStep = 0;
    private int autoDumpStep = 0;
    private int autoDropStep = 0;

    @Override
    public void onDisable() {
        this.activeTask = Task.NONE;
        this.taskStep = 0;
        this.autoStealStep = 0;
        this.autoDumpStep = 0;
        this.autoDropStep = 0;
        this.lastContainerId = -1;
        this.ticks = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        int containerSlotCount;
        boolean externalGui;
        long now;
        if (ChestStealerModule.mc.player == null || ChestStealerModule.mc.level == null || ChestStealerModule.mc.gameMode == null) {
            return;
        }
        if (ChestStealerModule.mc.player.isCreative()) {
            return;
        }
        if (ChestStealerModule.mc.player.containerMenu == null) {
            return;
        }
        if (!ChestStealerModule.mc.player.containerMenu.getCarried().isEmpty()) {
            return;
        }
        int currentContainerId = ChestStealerModule.mc.player.containerMenu.containerId;
        if (currentContainerId != this.lastContainerId) {
            this.lastContainerId = currentContainerId;
            this.taskStep = 0;
            this.autoStealStep = 0;
            this.autoDumpStep = 0;
            this.autoDropStep = 0;
            this.activeTask = Task.NONE;
        }
        if ((now = System.currentTimeMillis()) - RekitModule.lastContainerActionMs < 200L || now - InventorySorterModule.lastContainerActionMs < 200L) {
            return;
        }
        if (this.ticks < this.delay.getValue().intValue()) {
            ++this.ticks;
            return;
        }
        this.ticks = 0;
        int budget = this.actionsPerTick.getValue().intValue();
        if (this.activeTask != Task.NONE) {
            boolean isContainer = ChestStealerModule.mc.gui.screen() instanceof AbstractContainerScreen && !(ChestStealerModule.mc.gui.screen() instanceof InventoryScreen);
            boolean isInventory = ChestStealerModule.mc.gui.screen() instanceof InventoryScreen;
            if (!isContainer && !isInventory) {
                this.activeTask = Task.NONE;
                this.taskStep = 0;
                return;
            }
            switch (this.activeTask.ordinal()) {
                case 1: {
                    if (isContainer) {
                        int containerSlotCount2 = ChestStealerModule.mc.player.containerMenu.slots.size() - 36;
                        this.taskStep += this.processStealStep(this.stealMode.getValue(), this.stealWhitelist, budget, this.taskStep);
                        if (this.taskStep < containerSlotCount2) break;
                        this.activeTask = Task.NONE;
                        this.taskStep = 0;
                        break;
                    }
                    this.activeTask = Task.NONE;
                    this.taskStep = 0;
                    break;
                }
                case 2: {
                    if (isContainer) {
                        this.taskStep += this.processDumpStep(this.dumpMode.getValue(), this.dumpWhitelist, budget, this.taskStep);
                        if (this.taskStep < 36) break;
                        this.activeTask = Task.NONE;
                        this.taskStep = 0;
                        break;
                    }
                    this.activeTask = Task.NONE;
                    this.taskStep = 0;
                    break;
                }
                case 3: {
                    boolean external = isContainer;
                    int containerSlotCount3 = external ? ChestStealerModule.mc.player.containerMenu.slots.size() - 36 : 0;
                    int totalSlots = containerSlotCount3 + 36;
                    this.taskStep += this.processDropStep(this.dropMode.getValue(), this.dropWhitelist, budget, this.taskStep);
                    if (this.taskStep < totalSlots) break;
                    this.activeTask = Task.NONE;
                    this.taskStep = 0;
                }
            }
            return;
        }
        if (this.buttonMode.getValue()) {
            return;
        }
        boolean bl = externalGui = ChestStealerModule.mc.gui.screen() instanceof AbstractContainerScreen && !(ChestStealerModule.mc.gui.screen() instanceof InventoryScreen);
        if (externalGui && this.ignoreCustomName.getValue() && this.hasCustomContainerTitle() && !(ChestStealerModule.mc.player.containerMenu instanceof ShulkerBoxMenu)) {
            return;
        }
        if (this.steal.getValue() && externalGui && budget > 0) {
            containerSlotCount = ChestStealerModule.mc.player.containerMenu.slots.size() - 36;
            int scanned = this.processStealStep(this.stealMode.getValue(), this.stealWhitelist, budget, this.autoStealStep);
            this.autoStealStep += scanned;
            if (this.autoStealStep >= containerSlotCount) {
                this.autoStealStep = 0;
            }
        }
        if (this.dump.getValue() && externalGui && budget > 0) {
            int scanned = this.processDumpStep(this.dumpMode.getValue(), this.dumpWhitelist, budget, this.autoDumpStep);
            this.autoDumpStep += scanned;
            if (this.autoDumpStep >= 36) {
                this.autoDumpStep = 0;
            }
        }
        if (this.drop.getValue() && budget > 0) {
            containerSlotCount = externalGui ? ChestStealerModule.mc.player.containerMenu.slots.size() - 36 : 0;
            int totalSlots = containerSlotCount + 36;
            int scanned = this.processDropStep(this.dropMode.getValue(), this.dropWhitelist, budget, this.autoDropStep);
            this.autoDropStep += scanned;
            if (this.autoDropStep >= totalSlots) {
                this.autoDropStep = 0;
            }
        }
    }

    public void triggerSteal() {
        if (this.canAct(true)) {
            this.activeTask = Task.STEAL;
            this.taskStep = 0;
            this.ticks = this.delay.getValue().intValue();
        }
    }

    public void triggerDump() {
        if (this.canAct(true)) {
            this.activeTask = Task.DUMP;
            this.taskStep = 0;
            this.ticks = this.delay.getValue().intValue();
        }
    }

    public void triggerDrop() {
        if (this.canAct(false)) {
            this.activeTask = Task.DROP;
            this.taskStep = 0;
            this.ticks = this.delay.getValue().intValue();
        }
    }

    private boolean hasCustomContainerTitle() {
        Screen screen = ChestStealerModule.mc.gui.screen();
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

    private boolean canAct(boolean requireContainer) {
        if (ChestStealerModule.mc.player == null || ChestStealerModule.mc.level == null || ChestStealerModule.mc.gameMode == null) {
            return false;
        }
        if (ChestStealerModule.mc.player.isCreative()) {
            return false;
        }
        if (ChestStealerModule.mc.player.containerMenu == null) {
            return false;
        }
        if (!ChestStealerModule.mc.player.containerMenu.getCarried().isEmpty()) {
            return false;
        }
        if (requireContainer) {
            boolean isContainer = ChestStealerModule.mc.gui.screen() instanceof AbstractContainerScreen && !(ChestStealerModule.mc.gui.screen() instanceof InventoryScreen);
            boolean isInventory = ChestStealerModule.mc.gui.screen() instanceof InventoryScreen;
            if (!isContainer && !isInventory) {
                return false;
            }
        }
        return true;
    }

    private int processStealStep(String modeVal, WhitelistSetting wl, int budget, int startStep) {
        int step;
        if (ChestStealerModule.mc.gui.screen() instanceof InventoryScreen) {
            return 0;
        }
        int containerId = ChestStealerModule.mc.player.containerMenu.containerId;
        int containerSlotCount = ChestStealerModule.mc.player.containerMenu.slots.size() - 36;
        int actions = 0;
        for (step = startStep; step < containerSlotCount && actions < budget; ++step) {
            ItemStack stack = ((Slot)ChestStealerModule.mc.player.containerMenu.slots.get(step)).getItem();
            if (stack.isEmpty() || !this.matches(stack, modeVal, wl)) continue;
            this.quickMove(containerId, step);
            ++actions;
        }
        return Math.max(1, step - startStep);
    }

    private int processDumpStep(String modeVal, WhitelistSetting wl, int budget, int startStep) {
        int step;
        if (ChestStealerModule.mc.gui.screen() instanceof InventoryScreen) {
            return 0;
        }
        int containerId = ChestStealerModule.mc.player.containerMenu.containerId;
        int actions = 0;
        for (step = startStep; step < 36 && actions < budget; ++step) {
            int handlerSlot;
            ItemStack stack;
            if (!this.inInteractRange(step) || (stack = ChestStealerModule.mc.player.getInventory().getItem(step)).isEmpty() || !this.matches(stack, modeVal, wl) || (handlerSlot = this.invToHandlerSlot(step)) < 0) continue;
            this.quickMove(containerId, handlerSlot);
            ++actions;
        }
        return Math.max(1, step - startStep);
    }

    private int processDropStep(String modeVal, WhitelistSetting wl, int budget, int startStep) {
        int step;
        int containerId = ChestStealerModule.mc.player.containerMenu.containerId;
        boolean externalGui = ChestStealerModule.mc.gui.screen() instanceof AbstractContainerScreen && !(ChestStealerModule.mc.gui.screen() instanceof InventoryScreen);
        int containerSlotCount = externalGui ? ChestStealerModule.mc.player.containerMenu.slots.size() - 36 : 0;
        int totalSlots = containerSlotCount + 36;
        int actions = 0;
        for (step = startStep; step < totalSlots && actions < budget; ++step) {
            int handlerSlot;
            ItemStack stack;
            if (step < containerSlotCount) {
                ItemStack stack2 = ((Slot)ChestStealerModule.mc.player.containerMenu.slots.get(step)).getItem();
                if (stack2.isEmpty() || !this.matches(stack2, modeVal, wl)) continue;
                this.throwSlot(containerId, step);
                ++actions;
                continue;
            }
            int invSlot = step - containerSlotCount;
            if (!this.inInteractRange(invSlot) || (stack = ChestStealerModule.mc.player.getInventory().getItem(invSlot)).isEmpty() || !this.matches(stack, modeVal, wl) || (handlerSlot = this.invToHandlerSlot(invSlot)) < 0) continue;
            this.throwSlot(containerId, handlerSlot);
            ++actions;
        }
        return Math.max(1, step - startStep);
    }

    private void quickMove(int containerId, int slot) {
        ChestStealerModule.mc.gameMode.handleContainerInput(containerId, slot, 0, ContainerInput.QUICK_MOVE, (Player)ChestStealerModule.mc.player);
        lastContainerActionMs = System.currentTimeMillis();
    }

    private void throwSlot(int containerId, int slot) {
        ChestStealerModule.mc.gameMode.handleContainerInput(containerId, slot, 1, ContainerInput.THROW, (Player)ChestStealerModule.mc.player);
        lastContainerActionMs = System.currentTimeMillis();
    }

    private boolean matches(ItemStack stack, String modeVal, WhitelistSetting wl) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        boolean listed = wl != null && wl.isWhitelistContains(stack.getItem());
        return switch (modeVal) {
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

    private boolean inInteractRange(int invSlot) {
        return switch (this.interact.getValue()) {
            case "Hotbar" -> {
                if (invSlot <= 8) {
                    yield true;
                }
                yield false;
            }
            case "Inventory" -> {
                if (invSlot >= 9) {
                    yield true;
                }
                yield false;
            }
            default -> true;
        };
    }

    private int invToHandlerSlot(int invSlot) {
        Inventory playerInv = ChestStealerModule.mc.player.getInventory();
        for (Slot slot : ChestStealerModule.mc.player.containerMenu.slots) {
            if (slot.container != playerInv || slot.getContainerSlot() != invSlot) continue;
            return slot.index;
        }
        return -1;
    }

    @Override
    public String getMetaData() {
        StringBuilder sb = new StringBuilder();
        if (this.steal.getValue()) {
            sb.append("Steal");
        }
        if (this.dump.getValue()) {
            sb.append(sb.length() > 0 ? "/Dump" : "Dump");
        }
        if (this.drop.getValue()) {
            sb.append(sb.length() > 0 ? "/Drop" : "Drop");
        }
        return sb.toString();
    }

    private static enum Task {
        NONE,
        STEAL,
        DUMP,
        DROP;

    }
}

