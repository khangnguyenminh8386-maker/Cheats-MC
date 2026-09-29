/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.world.inventory.ContainerInput
 */
package night.modules.impl.player;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.ClientDisconnectEvent;
import night.events.impl.PlayerUpdateEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BindSetting;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.NumberSetting;
import night.utils.minecraft.InventoryUtils;

@RegisterModule(name="AutoShop", description="Automates the server /shop GUI to buy totems, end crystals, or exp bottles.", category=Module.Category.PLAYER, persistent=true)
public class AutoShopModule
extends Module {
    public CategorySetting totemCategory = new CategorySetting("Totem", "Automatic Totem restock and manual purchase settings.");
    public BooleanSetting totemBelowThreshold = new BooleanSetting("TotemBelowThreshold", "Enabled", "Automatically buy Totems after the configured number are lost.", new CategorySetting.Visibility(this.totemCategory), false);
    public BindSetting totem = new BindSetting("Totem", "Bind", "Keybind to auto buy totems.", new CategorySetting.Visibility(this.totemCategory), 0).disableHoldModes();
    public NumberSetting totemAmount = new NumberSetting("TotemAmount", "Amount", "How many totems to buy.", new CategorySetting.Visibility(this.totemCategory), 1, 1, 10);
    public NumberSetting totemLostCount = new NumberSetting("TotemLostCount", "Lost Totems", "Tracked Totems lost before one automatic purchase.", new CategorySetting.Visibility(this.totemCategory), 2, 1, 64);
    public CategorySetting crystalCategory = new CategorySetting("Crystal", "Automatic Crystal restock and manual purchase settings.");
    public BooleanSetting crystalBelowThreshold = new BooleanSetting("CrystalBelowThreshold", "Enabled", "Buy one Crystal order when stock reaches the threshold.", new CategorySetting.Visibility(this.crystalCategory), false);
    public BindSetting crystal = new BindSetting("Crystal", "Bind", "Keybind to auto buy end crystals.", new CategorySetting.Visibility(this.crystalCategory), 0).disableHoldModes();
    public NumberSetting crystalStacks = new NumberSetting("CrystalStacks", "Stacks", "How many stacks (64) of end crystals to buy.", new CategorySetting.Visibility(this.crystalCategory), 1, 1, 10);
    public NumberSetting crystalThreshold = new NumberSetting("CrystalThreshold", "Threshold", "Crystal stack threshold.", new CategorySetting.Visibility(this.crystalCategory), 1, 0, 20);
    public CategorySetting expCategory = new CategorySetting("Exp", "Automatic Exp restock and manual purchase settings.");
    public BooleanSetting expBelowThreshold = new BooleanSetting("ExpBelowThreshold", "Enabled", "Buy one Exp order when stock reaches the threshold.", new CategorySetting.Visibility(this.expCategory), false);
    public BindSetting exp = new BindSetting("Exp", "Bind", "Keybind to auto buy exp bottles.", new CategorySetting.Visibility(this.expCategory), 0).disableHoldModes();
    public NumberSetting expStacks = new NumberSetting("ExpStacks", "Stacks", "How many stacks (64) of exp bottles to buy.", new CategorySetting.Visibility(this.expCategory), 1, 1, 10);
    public NumberSetting expThreshold = new NumberSetting("ExpThreshold", "Threshold", "Exp stack threshold.", new CategorySetting.Visibility(this.expCategory), 1, 0, 20);
    public CategorySetting obsidianCategory = new CategorySetting("Obsidian", "Automatic Obsidian restock and manual purchase settings.");
    public BooleanSetting obsidianBelowThreshold = new BooleanSetting("ObsidianBelowThreshold", "Enabled", "Buy one Obsidian order when stock reaches the threshold.", new CategorySetting.Visibility(this.obsidianCategory), false);
    public BindSetting obsidian = new BindSetting("Obsidian", "Bind", "Keybind to auto buy obsidian.", new CategorySetting.Visibility(this.obsidianCategory), 0).disableHoldModes();
    public NumberSetting obsidianStacks = new NumberSetting("ObsidianStacks", "Stacks", "How many stacks (64) of obsidian to buy.", new CategorySetting.Visibility(this.obsidianCategory), 1, 1, 10);
    public NumberSetting obsidianThreshold = new NumberSetting("ObsidianThreshold", "Threshold", "Obsidian stack threshold.", new CategorySetting.Visibility(this.obsidianCategory), 1, 0, 20);
    public CategorySetting enderPearlCategory = new CategorySetting("EnderPearl", "Automatic EnderPearl restock and manual purchase settings.");
    public BooleanSetting enderPearlBelowThreshold = new BooleanSetting("EnderPearlBelowThreshold", "Enabled", "Buy one EnderPearl order when stock reaches the threshold.", new CategorySetting.Visibility(this.enderPearlCategory), false);
    public BindSetting enderPearl = new BindSetting("EnderPearl", "Bind", "Keybind to auto buy ender pearls.", new CategorySetting.Visibility(this.enderPearlCategory), 0).disableHoldModes();
    public NumberSetting enderPearlStacks = new NumberSetting("EnderPearlStacks", "Stacks", "How many stacks (64) of ender pearls to buy.", new CategorySetting.Visibility(this.enderPearlCategory), 1, 1, 10);
    public NumberSetting enderPearlThreshold = new NumberSetting("EnderPearlThreshold", "Threshold", "EnderPearl stack threshold.", new CategorySetting.Visibility(this.enderPearlCategory), 1, 0, 20);
    public BooleanSetting silent = new BooleanSetting("Silent", "Suppress expected real shop screens before presentation.", false);
    public NumberSetting actionDelay = new NumberSetting("ActionDelay", "Ticks between each shop GUI click.", 4, 0, 20);
    private static final int SLOT_ITEM_TOTEM = 13;
    private static final int SLOT_ITEM_CRYSTAL = 10;
    private static final int SLOT_ITEM_EXP = 16;
    private static final int SLOT_ITEM_OBSIDIAN = 9;
    private static final int SLOT_ITEM_ENDERPEARL = 14;
    private static final int SLOT_SET_1 = 15;
    private static final int SLOT_SET_11 = 16;
    private static final int SLOT_CONFIRM = 23;
    private static final int STEP_TIMEOUT_TICKS = 100;
    private State state = State.IDLE;
    private final Queue<PurchaseOrder> orderQueue = new ConcurrentLinkedQueue<PurchaseOrder>();
    private PurchaseOrder currentOrder = null;
    private int ticks = 0;
    private int timeoutTicks = 0;
    private boolean quantitySet = false;
    private final AutoControl[] autoControls = {new AutoControl(), new AutoControl(), new AutoControl(), new AutoControl(), new AutoControl()};
    private boolean totemTrackingInitialized;
    private boolean totemContainerResyncPending;
    private boolean totemZeroFailsafePending;
    private boolean totemZeroFailsafeUsed;
    private boolean totemOrderRecovered;
    private String totemEpisodeTriggerReason = "LOSS";
    private int baselineTotems;
    private int previousObservedTotems;
    private Object lastWorld;
    private Object lastPlayer;
    private long generation;
    private long expectedGeneration;
    private ResponseReason expectedReason = ResponseReason.NONE;
    private long expectationExpiresAt;
    private boolean realShopScreenIntercepted;
    private boolean silentSession;

    public AutoShopModule() {
        this.bind.disableHoldModes();
    }

    public void handleKey(int key) {
        if (key == 0) {
            return;
        }
        if (this.totem.getValue() != 0 && key == this.totem.getValue()) {
            this.enqueueOrder(ShopMode.Totem, this.totemAmount.getValue().intValue());
        } else if (this.crystal.getValue() != 0 && key == this.crystal.getValue()) {
            this.enqueueOrder(ShopMode.Crystal, this.crystalStacks.getValue().intValue());
        } else if (this.exp.getValue() != 0 && key == this.exp.getValue()) {
            this.enqueueOrder(ShopMode.Exp, this.expStacks.getValue().intValue());
        } else if (this.obsidian.getValue() != 0 && key == this.obsidian.getValue()) {
            this.enqueueOrder(ShopMode.Obsidian, this.obsidianStacks.getValue().intValue());
        } else if (this.enderPearl.getValue() != 0 && key == this.enderPearl.getValue()) {
            this.enqueueOrder(ShopMode.EnderPearl, this.enderPearlStacks.getValue().intValue());
        }
    }

    public synchronized void enqueueOrder(ShopMode shopMode, int amount) {
        Night.LOGGER.info("[AutoShop] MANUAL_REQUEST mode={} amount={}", shopMode, amount);
        this.enqueuePurchaseOrder(new PurchaseOrder(shopMode, amount));
    }

    private synchronized void enqueuePurchaseOrder(PurchaseOrder order) {
        if (AutoShopModule.mc.player == null || mc.getConnection() == null) {
            return;
        }
        if (order.remaining <= 0) {
            return;
        }
        this.orderQueue.add(order);
        if (this.state == State.IDLE) {
            this.currentOrder = this.orderQueue.poll();
            this.beginOrder();
        }
    }

    private void beginOrder() {
            this.generation++;
            this.clearSilentState();
            this.lastWorld = AutoShopModule.mc.level;
            this.lastPlayer = AutoShopModule.mc.player;
            this.ticks = 0;
            this.timeoutTicks = 0;
            this.quantitySet = false;
            this.sendShopCommand();
            this.state = State.WAIT_SHOP;
    }

    private void sendShopCommand() {
        if (mc.getConnection() != null) {
            this.armExpectedResponse(ResponseReason.SHOP_COMMAND);
            Night.LOGGER.info("[AutoShop] SHOP_COMMAND source={} mode={}", this.currentOrder == null ? "unknown" : this.currentOrder.origin, this.currentOrder == null ? "unknown" : this.currentOrder.mode);
            mc.getConnection().sendCommand("shop open gear");
        }
    }

    public synchronized ScreenDecision classifyScreen(Screen screen) {
        if (this.state == State.IDLE || !this.silent.getValue() || this.currentOrder == null ||
                !(screen instanceof AbstractContainerScreen<?> containerScreen) || screen instanceof InventoryScreen) {
            return ScreenDecision.PASS;
        }
        if (AutoShopModule.mc.player == null || AutoShopModule.mc.player.containerMenu == null ||
                AutoShopModule.mc.player.containerMenu == AutoShopModule.mc.player.inventoryMenu ||
                containerScreen.getMenu() != AutoShopModule.mc.player.containerMenu ||
                AutoShopModule.mc.gui.screen() != null) {
            this.resetTransactionWithoutClosingContainer("Unexpected container structure");
            return ScreenDecision.FAIL_OPEN_UNEXPECTED;
        }
        this.expireExpectedResponse();
        if (this.expectedReason == ResponseReason.NONE || this.expectedGeneration != this.generation) {
            this.resetTransactionWithoutClosingContainer("No valid expected shop response");
            return ScreenDecision.FAIL_OPEN_UNEXPECTED;
        }
        ResponseReason reason = this.expectedReason;
        Night.LOGGER.info("[AutoShop] SHOP_SCREEN_INTERCEPT state={} reason={} generation={} containerId={} screenClass={}", this.state, reason, this.generation, AutoShopModule.mc.player.containerMenu.containerId, screen.getClass().getSimpleName());
        if (reason == ResponseReason.SHOP_COMMAND) this.realShopScreenIntercepted = true;
        this.silentSession = true;
        this.expectedReason = ResponseReason.NONE;
        Night.LOGGER.info("[AutoShop] SILENT_SUPPRESS generation={} containerId={}", this.generation, AutoShopModule.mc.player.containerMenu.containerId);
        return ScreenDecision.SUPPRESS_EXPECTED;
    }

    private void armExpectedResponse(ResponseReason reason) {
        if (!this.silent.getValue() || this.currentOrder == null) return;
        this.expectedReason = reason;
        this.expectedGeneration = this.generation;
        // Two seconds bounds attribution without delaying the D1 purchase flow.
        this.expectationExpiresAt = System.nanoTime() + 2_000_000_000L;
        Night.LOGGER.info("[AutoShop] SILENT_EXPECT reason={} generation={} mode={}", reason, this.generation, this.currentOrder.mode);
    }

    private void expireExpectedResponse() {
        if (this.expectedReason != ResponseReason.NONE && System.nanoTime() - this.expectationExpiresAt >= 0) {
            Night.LOGGER.info("[AutoShop] SILENT_EXPECT_EXPIRE reason={} generation={}", this.expectedReason, this.expectedGeneration);
            this.expectedReason = ResponseReason.NONE;
        }
    }

    private void clearSilentState() {
        this.expectedReason = ResponseReason.NONE;
        this.expectedGeneration = 0;
        this.realShopScreenIntercepted = false;
        this.silentSession = false;
    }

    private void resetTransactionWithoutClosingContainer(String reason) {
        Night.LOGGER.warn("[AutoShop] UNEXPECTED_SCREEN FAIL_OPEN_RESET reason={} state={} generation={}", reason, this.state, this.generation);
        this.markAutoFailure();
        this.totemOrderRecovered = false;
        this.state = State.IDLE;
        this.orderQueue.clear();
        this.currentOrder = null;
        this.quantitySet = false;
        this.ticks = 0;
        this.timeoutTicks = 0;
        this.generation++;
        this.clearSilentState();
    }

    public boolean isRunning() {
        return this.state != State.IDLE;
    }

    private void resetAutoControls() {
        for (AutoControl control : this.autoControls) control.reset();
    }

    private AutoControl totemAutoControl() {
        return this.autoControls[ShopMode.Totem.ordinal()];
    }

    private void clearTotemTracking() {
        this.totemTrackingInitialized = false;
        this.totemContainerResyncPending = false;
        this.totemZeroFailsafePending = false;
        this.totemZeroFailsafeUsed = false;
        this.totemOrderRecovered = false;
        this.totemEpisodeTriggerReason = "LOSS";
        this.baselineTotems = 0;
        this.previousObservedTotems = 0;
        this.totemAutoControl().reset();
    }

    private void resetTotemBaseline(int stock, String reason) {
        int old = this.baselineTotems;
        this.baselineTotems = stock;
        this.previousObservedTotems = stock;
        this.totemTrackingInitialized = true;
        this.totemContainerResyncPending = false;
        this.totemZeroFailsafePending = false;
        this.totemZeroFailsafeUsed = false;
        this.totemEpisodeTriggerReason = "LOSS";
        AutoControl control = this.totemAutoControl();
        control.reset();
        control.wasEnabled = true;
        Night.LOGGER.info("[AutoShop] TOTEM_BASELINE_RESET reason={} old={} new={}", reason, old, stock);
    }

    private boolean isTotemAutoOrder() {
        return this.currentOrder != null && this.currentOrder.mode == ShopMode.Totem && this.currentOrder.origin != OrderOrigin.MANUAL;
    }

    private boolean isUnrelatedInventorySession() {
        Screen screen = AutoShopModule.mc.gui.screen();
        boolean ownedShop = this.state != State.IDLE && this.currentOrder != null;
        if (screen instanceof InventoryScreen) return true;
        if (screen instanceof AbstractContainerScreen<?>) return !ownedShop;
        if (AutoShopModule.mc.player.hasContainerOpen()) {
            return !(ownedShop && this.silent.getValue() &&
                    (this.silentSession || this.expectedReason != ResponseReason.NONE));
        }
        return false;
    }

    private void observeTotemStock() {
        AutoControl control = this.totemAutoControl();
        if (!this.totemBelowThreshold.getValue()) {
            if (this.totemTrackingInitialized || control.wasEnabled) {
                Night.LOGGER.info("[AutoShop] TOTEM_AUTO_DISABLE");
                this.clearTotemTracking();
            }
            return;
        }
        if (AutoShopModule.mc.player == null || AutoShopModule.mc.level == null || mc.getConnection() == null ||
                !AutoShopModule.mc.player.isAlive() || AutoShopModule.mc.player.isDeadOrDying() ||
                AutoShopModule.mc.player.getHealth() <= 0.0f) {
            if (this.totemTrackingInitialized) this.clearTotemTracking();
            return;
        }
        int stock = this.stockCount(Items.TOTEM_OF_UNDYING);
        if (this.isUnrelatedInventorySession()) {
            this.totemContainerResyncPending = true;
            return;
        }
        if (this.totemContainerResyncPending) {
            this.resetTotemBaseline(stock, "CONTAINER_RESYNC");
            return;
        }
        if (!this.totemTrackingInitialized) {
            this.resetTotemBaseline(stock, "ENABLE/SESSION");
            Night.LOGGER.info("[AutoShop] TOTEM_AUTO_ENABLE baseline={}", stock);
            return;
        }
        if (stock > this.previousObservedTotems) {
            if (this.isTotemAutoOrder()) this.totemOrderRecovered = true;
            this.resetTotemBaseline(stock, "STOCK_INCREASE");
            Night.LOGGER.info("[AutoShop] TOTEM_AUTO_RECOVER stock={}", stock);
            return;
        }
        if (stock < this.previousObservedTotems) {
            if (this.previousObservedTotems > 0 && stock == 0 && this.baselineTotems > 0 && !this.totemZeroFailsafeUsed) {
                this.totemZeroFailsafePending = true;
                Night.LOGGER.info("[AutoShop] TOTEM_ZERO_FAILSAFE baseline={} current=0 configured={}", this.baselineTotems, this.totemLostCount.getValue().intValue());
            }
            int lost = Math.max(0, this.baselineTotems - stock);
            Night.LOGGER.info("[AutoShop] TOTEM_LOSS baseline={} current={} lost={} configured={}", this.baselineTotems, stock, lost, this.totemLostCount.getValue().intValue());
        }
        this.previousObservedTotems = stock;
    }

    @Override
    public void onEnable() {
        this.resetAutoControls();
        this.clearTotemTracking();
    }

    @Override
    public void onDisable() {
        this.resetAutoControls();
        this.clearTotemTracking();
    }

    private void markAutoFailure() {
        if (this.currentOrder == null || this.currentOrder.origin == OrderOrigin.MANUAL) return;
        if (this.isTotemAutoOrder()) {
            if (!this.totemBelowThreshold.getValue() || this.totemOrderRecovered) return;
            if (this.currentOrder.origin == OrderOrigin.AUTO_PRIMARY) {
                Night.LOGGER.warn("[AutoShop] TOTEM_AUTO_RETRY cooldownSeconds=5");
            } else {
                Night.LOGGER.warn("[AutoShop] TOTEM_AUTO_RETRY_EXHAUSTED");
            }
        }
        AutoControl control = this.autoControls[this.currentOrder.mode.ordinal()];
        if (this.currentOrder.origin == OrderOrigin.AUTO_PRIMARY) {
            control.phase = AutoPhase.RETRY_COOLDOWN;
            control.retryAtNanos = System.nanoTime() + 5_000_000_000L;
            Night.LOGGER.warn("[AutoShop] AUTO_RETRY_SCHEDULED mode={} cooldownSeconds=5", this.currentOrder.mode);
        } else {
            control.phase = AutoPhase.EXHAUSTED;
            Night.LOGGER.warn("[AutoShop] AUTO_RETRY_EXHAUSTED mode={}", this.currentOrder.mode);
        }
    }

    private void markAutoServiced() {
        if (this.currentOrder == null || this.currentOrder.origin == OrderOrigin.MANUAL) return;
        if (this.isTotemAutoOrder() && (!this.totemBelowThreshold.getValue() || this.totemOrderRecovered)) return;
        AutoControl control = this.autoControls[this.currentOrder.mode.ordinal()];
        control.phase = AutoPhase.SERVICED;
        int stock = this.stockCount(itemFor(this.currentOrder.mode));
        control.peakStock = Math.max(control.startStock, stock);
        Night.LOGGER.info("[AutoShop] AUTO_SERVICED mode={} observedStock={} startStock={}", this.currentOrder.mode, stock, control.startStock);
    }

    private static Item itemFor(ShopMode mode) {
        return switch (mode) {
            case Totem -> Items.TOTEM_OF_UNDYING;
            case Crystal -> Items.END_CRYSTAL;
            case Exp -> Items.EXPERIENCE_BOTTLE;
            case Obsidian -> Items.OBSIDIAN;
            case EnderPearl -> Items.ENDER_PEARL;
        };
    }

    private int stockCount(Item item) {
        if (AutoShopModule.mc.player == null) return 0;
        int count = 0;
        for (int slot = 0; slot < 36; ++slot) {
            ItemStack stack = AutoShopModule.mc.player.getInventory().getItem(slot);
            if (stack.getItem() == item) count += stack.getCount();
        }
        return count;
    }

    private boolean tryStartAutomaticTotemOrder() {
        if (!this.totemBelowThreshold.getValue() || !this.totemTrackingInitialized || this.totemContainerResyncPending ||
                AutoShopModule.mc.player == null || AutoShopModule.mc.level == null || mc.getConnection() == null ||
                !AutoShopModule.mc.player.isAlive() || AutoShopModule.mc.player.isDeadOrDying() ||
                AutoShopModule.mc.player.getHealth() <= 0.0f) return false;
        AutoControl control = this.totemAutoControl();
        if (this.state != State.IDLE || !this.orderQueue.isEmpty()) return false;
        if (control.phase != AutoPhase.READY && control.phase != AutoPhase.RETRY_COOLDOWN) return false;
        int stock = this.previousObservedTotems;
        int lost = Math.max(0, this.baselineTotems - stock);
        int configuredLoss = this.totemLostCount.getValue().intValue();
        boolean lossTrigger = lost >= configuredLoss;
        boolean zeroTrigger = this.baselineTotems > 0 && stock == 0 && this.totemZeroFailsafePending && !this.totemZeroFailsafeUsed;
        if ((control.phase != AutoPhase.RETRY_COOLDOWN && !lossTrigger && !zeroTrigger) ||
                !this.autoStartSafe(Items.TOTEM_OF_UNDYING)) return false;
        if (control.phase == AutoPhase.RETRY_COOLDOWN && System.nanoTime() - control.retryAtNanos < 0) return false;
        OrderOrigin origin = control.phase == AutoPhase.READY ? OrderOrigin.AUTO_PRIMARY : OrderOrigin.AUTO_RETRY;
        if (origin == OrderOrigin.AUTO_PRIMARY) control.startStock = stock;
        control.phase = origin == OrderOrigin.AUTO_PRIMARY ? AutoPhase.RUNNING_PRIMARY : AutoPhase.RUNNING_RETRY;
        control.peakStock = Math.max(control.peakStock, stock);
        this.totemOrderRecovered = false;
        if (origin == OrderOrigin.AUTO_PRIMARY) {
            this.totemEpisodeTriggerReason = lossTrigger ? "LOSS" : "ZERO_FAILSAFE";
            if (zeroTrigger && !lossTrigger) {
                this.totemZeroFailsafePending = false;
                this.totemZeroFailsafeUsed = true;
            }
        }
        int amount = this.totemAmount.getValue().intValue();
        Night.LOGGER.info("[AutoShop] TOTEM_AUTO_TRIGGER reason={} lost={} configured={} amount={} attempt={}", this.totemEpisodeTriggerReason, lost, configuredLoss, amount, origin);
        this.enqueuePurchaseOrder(new PurchaseOrder(ShopMode.Totem, amount, origin));
        return true;
    }

    private void tryStartAutomaticOrder() {
        if (AutoShopModule.mc.player == null || AutoShopModule.mc.level == null || mc.getConnection() == null) return;
        ShopMode[] modes = ShopMode.values();
        for (ShopMode mode : modes) {
            if (mode == ShopMode.Totem) {
                if (this.tryStartAutomaticTotemOrder()) return;
                continue;
            }
            int i = mode.ordinal();
            AutoControl control = this.autoControls[i];
            boolean enabled = this.autoEnabled(mode);
            if (!enabled) {
                control.wasEnabled = false;
                continue;
            }
            if (!control.wasEnabled) {
                control.reset();
                control.wasEnabled = true;
                Night.LOGGER.info("[AutoShop] AUTO_REARM mode={} reason=TOGGLE_ON", mode);
            }
            int stock = this.stockCount(itemFor(mode));
            int threshold = this.thresholdCount(mode, itemFor(mode));
            if (control.phase == AutoPhase.SERVICED || control.phase == AutoPhase.EXHAUSTED) {
                control.peakStock = Math.max(control.peakStock, stock);
                if (stock > threshold || control.peakStock > control.startStock && stock < control.peakStock && stock <= threshold) {
                    control.phase = AutoPhase.READY;
                    Night.LOGGER.info("[AutoShop] AUTO_REARM mode={} reason={}", mode, stock > threshold ? "ABOVE_THRESHOLD" : "RECOVERY_THEN_CONSUMPTION");
                }
            } else if (control.phase == AutoPhase.RETRY_COOLDOWN) {
                if (stock > control.startStock) {
                    control.phase = AutoPhase.SERVICED;
                    control.peakStock = stock;
                    Night.LOGGER.info("[AutoShop] AUTO_SERVICED mode={} reason=STOCK_INCREASE_DURING_COOLDOWN", mode);
                } else if (stock > threshold) {
                    control.phase = AutoPhase.READY;
                    Night.LOGGER.info("[AutoShop] AUTO_REARM mode={} reason=ABOVE_THRESHOLD", mode);
                }
            }
            if (this.state != State.IDLE || !this.orderQueue.isEmpty()) continue;
            if (control.phase != AutoPhase.READY && control.phase != AutoPhase.RETRY_COOLDOWN) continue;
            if (stock > threshold || !this.autoStartSafe(itemFor(mode))) continue;
            if (control.phase == AutoPhase.RETRY_COOLDOWN && System.nanoTime() - control.retryAtNanos < 0) continue;
            OrderOrigin origin = control.phase == AutoPhase.READY ? OrderOrigin.AUTO_PRIMARY : OrderOrigin.AUTO_RETRY;
            if (origin == OrderOrigin.AUTO_PRIMARY) control.startStock = stock;
            control.phase = origin == OrderOrigin.AUTO_PRIMARY ? AutoPhase.RUNNING_PRIMARY : AutoPhase.RUNNING_RETRY;
            control.peakStock = Math.max(control.peakStock, stock);
            int amount = this.manualAmount(mode);
            Night.LOGGER.info("[AutoShop] {} mode={} attempt={} stock={} threshold={} amount={}", origin == OrderOrigin.AUTO_PRIMARY ? "AUTO_TRIGGER" : "AUTO_RETRY", mode, origin == OrderOrigin.AUTO_PRIMARY ? "PRIMARY" : "RETRY", stock, threshold, amount);
            this.enqueuePurchaseOrder(new PurchaseOrder(mode, amount, origin));
            return;
        }
    }

    private boolean autoStartSafe(Item item) {
        if (AutoShopModule.mc.gui.screen() != null || AutoShopModule.mc.player.hasContainerOpen()) return false;
        for (int slot = 0; slot < 36; ++slot) {
            ItemStack stack = AutoShopModule.mc.player.getInventory().getItem(slot);
            if (stack.isEmpty() || stack.getItem() == item && stack.getCount() < stack.getMaxStackSize()) return true;
        }
        return false;
    }

    private boolean autoEnabled(ShopMode mode) {
        return switch (mode) {
            case Totem -> this.totemBelowThreshold.getValue();
            case Crystal -> this.crystalBelowThreshold.getValue();
            case Exp -> this.expBelowThreshold.getValue();
            case Obsidian -> this.obsidianBelowThreshold.getValue();
            case EnderPearl -> this.enderPearlBelowThreshold.getValue();
        };
    }

    private int thresholdCount(ShopMode mode, Item item) {
        int threshold = switch (mode) {
            case Totem -> throw new IllegalArgumentException("Totem automatic buying uses loss tracking");
            case Crystal -> this.crystalThreshold.getValue().intValue();
            case Exp -> this.expThreshold.getValue().intValue();
            case Obsidian -> this.obsidianThreshold.getValue().intValue();
            case EnderPearl -> this.enderPearlThreshold.getValue().intValue();
        };
        return mode == ShopMode.Totem ? threshold : threshold * item.getDefaultInstance().getMaxStackSize();
    }

    private int manualAmount(ShopMode mode) {
        return switch (mode) {
            case Totem -> this.totemAmount.getValue().intValue();
            case Crystal -> this.crystalStacks.getValue().intValue();
            case Exp -> this.expStacks.getValue().intValue();
            case Obsidian -> this.obsidianStacks.getValue().intValue();
            case EnderPearl -> this.enderPearlStacks.getValue().intValue();
        };
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientDisconnectEvent event) {
        this.state = State.IDLE;
        this.orderQueue.clear();
        this.currentOrder = null;
        this.lastWorld = null;
        this.lastPlayer = null;
        this.ticks = this.timeoutTicks = 0;
        this.quantitySet = false;
        this.generation++;
        this.clearSilentState();
        this.resetAutoControls();
        this.clearTotemTracking();
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        boolean screenOpen;
        if (this.lastWorld != AutoShopModule.mc.level || this.lastPlayer != AutoShopModule.mc.player) {
            if (this.lastWorld != null && this.state != State.IDLE) this.abort("Player changed world/session");
            this.lastWorld = AutoShopModule.mc.level;
            this.lastPlayer = AutoShopModule.mc.player;
            this.generation++;
            this.clearSilentState();
            this.resetAutoControls();
            this.clearTotemTracking();
        }
        if (AutoShopModule.mc.player != null && (AutoShopModule.mc.player.isDeadOrDying() ||
                !AutoShopModule.mc.player.isAlive() || AutoShopModule.mc.player.getHealth() <= 0.0f) && this.isTotemAutoOrder()) {
            this.abort("Player dead during automatic Totem order");
            this.clearTotemTracking();
        }
        this.observeTotemStock();
        this.tryStartAutomaticOrder();
        if (this.state == State.IDLE) return;
        if (AutoShopModule.mc.player == null || AutoShopModule.mc.level == null) {
            this.abort("Player left world");
            return;
        }
        this.expireExpectedResponse();
        if (this.ticks < this.actionDelay.getValue().intValue()) {
            ++this.ticks;
            return;
        }
        this.ticks = 0;
        Screen currentScreen = AutoShopModule.mc.gui.screen();
        boolean bl = screenOpen = currentScreen instanceof AbstractContainerScreen && !(currentScreen instanceof InventoryScreen);
        if (this.silent.getValue() && this.silentSession) screenOpen = screenOpen || AutoShopModule.mc.player.hasContainerOpen();
        if (this.state != State.WAIT_SHOP && !screenOpen) {
            this.abort("Shop GUI closed unexpectedly.");
            return;
        }
        switch (this.state.ordinal()) {
            case 1: {
                if (screenOpen || this.silent.getValue() && this.realShopScreenIntercepted) {
                    this.realShopScreenIntercepted = false;
                    Night.LOGGER.info("[AutoShop] SHOP_ACCEPTED mode={} generation={}", this.currentOrder == null ? "unknown" : this.currentOrder.mode, this.generation);
                    this.state = State.CLICK_ITEM;
                    this.timeoutTicks = 0;
                    break;
                }
                ++this.timeoutTicks;
                if (this.timeoutTicks % 20 == 0) {
                    this.sendShopCommand();
                }
                if (this.timeoutTicks <= 100) break;
                this.abort("Shop GUI never opened.");
                break;
            }
            case 2: {
                if (this.currentOrder == null) {
                    this.state = State.DECIDE;
                    return;
                }
                int slot = switch (this.currentOrder.mode.ordinal()) {
                    default -> throw new MatchException(null, null);
                    case 0 -> 13;
                    case 1 -> 10;
                    case 2 -> 16;
                    case 3 -> 9;
                    case 4 -> 14;
                };
                if (this.silent.getValue() && this.silentSession) this.armExpectedResponse(ResponseReason.ITEM_SELECTION);
                Night.LOGGER.info("[AutoShop] ITEM_CLICK mode={} slot={}", this.currentOrder.mode, slot);
                this.click(slot);
                this.state = State.DECIDE;
                break;
            }
            case 3: {
                if (this.currentOrder == null || this.currentOrder.remaining <= 0) {
                    if (!this.orderQueue.isEmpty()) {
                        if (this.isTotemAutoOrder()) {
                            this.markAutoServiced();
                            this.totemOrderRecovered = false;
                        }
                        this.currentOrder = this.orderQueue.poll();
                        this.beginOrder();
                        return;
                    }
                    this.finish();
                    return;
                }
                if (this.currentOrder.mode == ShopMode.Totem) {
                    this.state = State.CONFIRM;
                    break;
                }
                if (!this.quantitySet) {
                    this.state = State.SET_11;
                    break;
                }
                this.state = State.CONFIRM;
                break;
            }
            case 4: {
                int slot = this.currentOrder != null && this.currentOrder.mode == ShopMode.EnderPearl ? 15 : 16;
                if (this.silent.getValue() && this.silentSession) this.armExpectedResponse(ResponseReason.QUANTITY_SELECTION);
                Night.LOGGER.info("[AutoShop] QUANTITY_CLICK mode={} slot={}", this.currentOrder == null ? "unknown" : this.currentOrder.mode, slot);
                this.click(slot);
                this.quantitySet = true;
                this.state = State.CONFIRM;
                break;
            }
            case 5: {
                Night.LOGGER.info("[AutoShop] CONFIRM mode={} remaining={}", this.currentOrder == null ? "unknown" : this.currentOrder.mode, this.currentOrder == null ? 0 : this.currentOrder.remaining);
                this.click(23);
                if (this.currentOrder != null) {
                    --this.currentOrder.remaining;
                }
                this.state = State.DECIDE;
                break;
            }
        }
    }

    private void finish() {
        Night.LOGGER.info("[AutoShop] FINISH mode={}", this.currentOrder == null ? "unknown" : this.currentOrder.mode);
        this.markAutoServiced();
        this.totemOrderRecovered = false;
        this.state = State.IDLE;
        this.orderQueue.clear();
        this.currentOrder = null;
        this.generation++;
        this.clearSilentState();
        if (AutoShopModule.mc.player != null) {
            AutoShopModule.mc.player.closeContainer();
        }
        Night.CHAT_MANAGER.tagged("Done.", this.getName());
    }

    private void abort(String reason) {
        Night.LOGGER.warn("[AutoShop] ABORT reason={} state={}", reason, this.state);
        this.markAutoFailure();
        this.totemOrderRecovered = false;
        this.state = State.IDLE;
        this.orderQueue.clear();
        this.currentOrder = null;
        this.generation++;
        this.clearSilentState();
        if (AutoShopModule.mc.player != null) {
            AutoShopModule.mc.player.closeContainer();
        }
        Night.CHAT_MANAGER.tagged("Aborted: " + reason, this.getName());
    }

    private void click(int slot) {
        InventoryUtils.click(slot, 0, ContainerInput.PICKUP);
    }

    @Override
    public String getMetaData() {
        if (this.currentOrder != null) {
            return this.currentOrder.mode.name() + " " + this.currentOrder.remaining + (String)(this.orderQueue.isEmpty() ? "" : " (+" + this.orderQueue.size() + ")");
        }
        return "";
    }

    private static enum State {
        IDLE,
        WAIT_SHOP,
        CLICK_ITEM,
        DECIDE,
        SET_11,
        CONFIRM;

    }

    public static enum ScreenDecision {
        PASS,
        SUPPRESS_EXPECTED,
        FAIL_OPEN_UNEXPECTED
    }

    private static enum ResponseReason {
        NONE,
        SHOP_COMMAND,
        ITEM_SELECTION,
        QUANTITY_SELECTION
    }

    private static enum OrderOrigin {
        MANUAL,
        AUTO_PRIMARY,
        AUTO_RETRY
    }

    private static enum AutoPhase {
        READY,
        RUNNING_PRIMARY,
        RETRY_COOLDOWN,
        RUNNING_RETRY,
        SERVICED,
        EXHAUSTED
    }

    private static final class AutoControl {
        private AutoPhase phase = AutoPhase.READY;
        private boolean wasEnabled;
        private int startStock;
        private int peakStock;
        private long retryAtNanos;

        private void reset() {
            this.phase = AutoPhase.READY;
            this.wasEnabled = false;
            this.startStock = 0;
            this.peakStock = 0;
            this.retryAtNanos = 0;
        }
    }

    public static class PurchaseOrder {
        public final ShopMode mode;
        public int remaining;
        private final OrderOrigin origin;

        public PurchaseOrder(ShopMode mode, int remaining) {
            this(mode, remaining, OrderOrigin.MANUAL);
        }

        private PurchaseOrder(ShopMode mode, int remaining, OrderOrigin origin) {
            this.mode = mode;
            this.remaining = remaining;
            this.origin = origin;
        }
    }

    public static enum ShopMode {
        Totem,
        Crystal,
        Exp,
        Obsidian,
        EnderPearl;

    }
}

