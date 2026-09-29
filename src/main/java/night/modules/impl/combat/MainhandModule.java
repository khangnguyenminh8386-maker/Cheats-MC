/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.item.Items
 */
package night.modules.impl.combat;

import java.util.ArrayDeque;
import java.util.Iterator;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.ClientDisconnectEvent;
import night.events.impl.PlayerPopEvent;
import night.events.impl.PlayerUpdateEvent;
import night.mixins.accessors.ClientPlayerInteractionManagerAccessor;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.combat.SuicideModule;
import night.modules.impl.miscellaneous.FakePlayerModule;
import night.pingbypass.PingBypassFlags;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.minecraft.EntityUtils;
import night.utils.minecraft.InventoryUtils;

@RegisterModule(name="Mainhand", description="Automatically manages totems and golden apples in your mainhand.", category=Module.Category.COMBAT)
public class MainhandModule
extends Module {
    public NumberSetting slot = new NumberSetting("Slot", "The hotbar slot (1-9) to keep a totem in.", 1, 1, 9);
    public NumberSetting delay = new NumberSetting("Delay", "Delay (ms) before refilling a totem to hotbar.", 0, 0, 2000);
    public ModeSetting refillMode = new ModeSetting("RefillMode", "Refill Mode", "When to refill the configured hotbar slot.", "Immediate", new String[]{"Immediate", "After Pops"});
    public NumberSetting popsBeforeRefill = new NumberSetting("PopsBeforeRefill", "Pops Before Refill", "Confirmed main-hand Totem pops per inventory refill.", new ModeSetting.Visibility(this.refillMode, "After Pops"), 4, 1, 9);
    public BooleanSetting useGapple = new BooleanSetting("UseGapple", "Switches to golden apple when right clicking while holding a totem in mainhand.", true);
    public BooleanSetting lethalOverride = new BooleanSetting("LethalOverride", "Allows eating a golden apple regardless of health when no enemies are in visual range.", new BooleanSetting.Visibility(this.useGapple, true), false);
    public NumberSetting health = new NumberSetting("Health", "The health at which a totem will be prioritized.", 16, 0, 36);
    public BooleanSetting elytraCheck = new BooleanSetting("ElytraCheck", "Prioritizes a totem whenever you're wearing an elytra.", true);
    public NumberSetting fallDistance = new NumberSetting("FallDistance", "The fall distance at which the module will prioritize a totem.", Float.valueOf(20.0f), Float.valueOf(0.0f), Float.valueOf(80.0f));
    public BooleanSetting antiMace = new BooleanSetting("AntiMace", "Switches to a totem if a player near you is trying to smash attack you with a mace.", false);
    public NumberSetting maceRange = new NumberSetting("MaceRange", "The distance at which an enemy has to be in with a mace in order to swap to a totem.", new BooleanSetting.Visibility(this.antiMace, true), (Number)Float.valueOf(12.0f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(24.0f));
    private long pendingRefillTime = 0L;
    private static final int POP_WINDOW_UPDATES = 8;
    private static final int MAX_POP_CANDIDATES = 32;
    private final ArrayDeque<PopCandidate> popCandidates = new ArrayDeque<>();
    private final boolean[] observedTotems = new boolean[9];
    private int updateSequence;
    private int confirmedPopDebt;
    private int refillSource = -1;
    private int refillTarget = -1;
    private int refillAttemptUpdate;
    private String observedMode;
    private Object observedPlayer;
    private Object observedWorld;
    private String pendingReason;
    private boolean eatingHotbar = false;
    private int previousHotbarSlot = -1;
    private boolean eatingInventory = false;
    private int swappedInvSlot = -1;
    private int swappedHotbarSlot = -1;
    public static volatile boolean hidingLegitInventory = false;

    @Override
    public void onEnable() {
        this.pendingRefillTime = 0L;
        this.resetAfterPops("enable");
        this.observedMode = this.refillMode.getValue();
    }

    private static final class PopCandidate {
        private final int slot;
        private final int createdUpdate;

        private PopCandidate(int slot, int createdUpdate) {
            this.slot = slot;
            this.createdUpdate = createdUpdate;
        }
    }

    private boolean afterPops() {
        return "After Pops".equals(this.refillMode.getValue());
    }

    private void resetAfterPops(String reason) {
        if (!this.popCandidates.isEmpty() || this.confirmedPopDebt != 0 || this.pendingRefillTime != 0L || this.refillSource != -1) {
            Night.LOGGER.info("[Mainhand] RESET reason={}", reason);
        }
        this.popCandidates.clear();
        this.confirmedPopDebt = 0;
        this.pendingRefillTime = 0L;
        this.refillSource = -1;
        this.refillTarget = -1;
        this.pendingReason = null;
        this.updateSequence = 0;
        for (int i = 0; i < this.observedTotems.length; i++) this.observedTotems[i] = false;
    }

    private void observeModeAndSession() {
        String mode = this.refillMode.getValue();
        if (this.observedMode == null || !this.observedMode.equals(mode)) {
            this.resetAfterPops("mode_change");
            this.observedMode = mode;
            Night.LOGGER.info("[Mainhand] MODE mode={}", mode);
        }
        if (MainhandModule.mc.player == null || MainhandModule.mc.level == null || MainhandModule.mc.player.isDeadOrDying()) {
            this.resetAfterPops("invalid_session");
            this.observedPlayer = MainhandModule.mc.player;
            this.observedWorld = MainhandModule.mc.level;
            return;
        }
        if (this.observedPlayer != MainhandModule.mc.player || this.observedWorld != MainhandModule.mc.level) {
            this.resetAfterPops("session_change");
            this.observedPlayer = MainhandModule.mc.player;
            this.observedWorld = MainhandModule.mc.level;
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientDisconnectEvent event) {
        this.resetAfterPops("disconnect");
        this.observedPlayer = null;
        this.observedWorld = null;
    }

    @SubscribeEvent(priority=0x7FFFFFFF)
    public void onPlayerPop(PlayerPopEvent event) {
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        if (event.getPlayer() != MainhandModule.mc.player || Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
            return;
        }
        if (!this.isToggled()) {
            return;
        }
        mc.execute(() -> {
            if (MainhandModule.mc.player == null || MainhandModule.mc.level == null) {
                return;
            }
            this.observeModeAndSession();
            if (this.afterPops()) {
                if (MainhandModule.mc.player.isDeadOrDying()) return;
                int selectedSlot = MainhandModule.mc.player.getInventory().getSelectedSlot();
                if (selectedSlot < 0 || selectedSlot > 8 || !MainhandModule.mc.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING)
                        || !MainhandModule.mc.player.getInventory().getItem(selectedSlot).is(Items.TOTEM_OF_UNDYING)) {
                    Night.LOGGER.info("[Mainhand] POP_REJECTED reason=offhand_or_no_mainhand");
                    return;
                }
                if (this.popCandidates.size() == MAX_POP_CANDIDATES) this.popCandidates.removeFirst();
                this.popCandidates.addLast(new PopCandidate(selectedSlot, this.updateSequence));
                this.observedTotems[selectedSlot] = true;
                Night.LOGGER.info("[Mainhand] POP_CANDIDATE slot={} age=0", selectedSlot + 1);
                return;
            }
            int targetSlot = this.slot.getValue().intValue() - 1;
            long d = this.delay.getValue().longValue();
            if (d <= 0L) {
                this.refill(targetSlot);
            } else {
                this.pendingRefillTime = System.currentTimeMillis() + d;
            }
        });
    }

    private void refill(int targetHotbarSlot) {
        if (MainhandModule.mc.player == null || MainhandModule.mc.level == null) {
            return;
        }
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        Screen currentScreen = MainhandModule.mc.gui.screen();
        if (currentScreen instanceof AbstractContainerScreen && !(currentScreen instanceof InventoryScreen) && !MainhandModule.mc.player.containerMenu.getCarried().isEmpty()) {
            return;
        }
        if (MainhandModule.mc.player.getInventory().getItem(targetHotbarSlot).getItem() == Items.TOTEM_OF_UNDYING) {
            return;
        }
        int totemSlot = this.findRefillTotem(targetHotbarSlot);
        if (totemSlot != -1) {
            InventoryUtils.swap("Swap", totemSlot, targetHotbarSlot);
        }
    }

    public boolean legitOwnsTotems() {
        return this.isToggled();
    }

    @SubscribeEvent(priority=0x7FFFFFFF)
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        this.observeModeAndSession();
        if (this.afterPops() && MainhandModule.mc.player != null && MainhandModule.mc.level != null && !MainhandModule.mc.player.isDeadOrDying()) {
            this.observeAfterPops();
        }
        this.updateMainhand();
    }

    private void observeAfterPops() {
        this.updateSequence++;
        boolean[] current = new boolean[9];
        for (int i = 0; i < current.length; i++) {
            current[i] = MainhandModule.mc.player.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING);
        }
        Iterator<PopCandidate> iterator = this.popCandidates.iterator();
        while (iterator.hasNext()) {
            PopCandidate candidate = iterator.next();
            if (this.updateSequence - candidate.createdUpdate > POP_WINDOW_UPDATES) {
                iterator.remove();
                Night.LOGGER.info("[Mainhand] POP_REJECTED reason=timeout slot={}", candidate.slot + 1);
            } else if (this.observedTotems[candidate.slot] && !current[candidate.slot]) {
                iterator.remove();
                this.confirmedPopDebt++;
                Night.LOGGER.info("[Mainhand] POP_CONFIRMED slot={} debt={} threshold={}", candidate.slot + 1, this.confirmedPopDebt, this.popsBeforeRefill.getValue().intValue());
                this.observedTotems[candidate.slot] = false;
            }
        }
        System.arraycopy(current, 0, this.observedTotems, 0, current.length);
    }

    private void logPending(String reason) {
        if (!reason.equals(this.pendingReason)) {
            Night.LOGGER.info("[Mainhand] REFILL_PENDING reason={}", reason);
            this.pendingReason = reason;
        }
    }

    private void updateAfterPopsRefill(int targetSlot, long now) {
        int threshold = this.popsBeforeRefill.getValue().intValue();
        if (this.refillSource != -1) {
            boolean targetHasTotem = MainhandModule.mc.player.getInventory().getItem(this.refillTarget).is(Items.TOTEM_OF_UNDYING);
            boolean sourceLostTotem = !MainhandModule.mc.player.getInventory().getItem(this.refillSource).is(Items.TOTEM_OF_UNDYING);
            if (targetHasTotem && sourceLostTotem) {
                this.confirmedPopDebt = Math.max(0, this.confirmedPopDebt - threshold);
                Night.LOGGER.info("[Mainhand] REFILL_SUCCESS sourceSlot={} targetSlot={} remainingDebt={}", this.refillSource, this.refillTarget + 1, this.confirmedPopDebt);
                this.refillSource = -1;
                this.refillTarget = -1;
                this.pendingRefillTime = 0L;
                this.pendingReason = null;
                return;
            }
            if (this.updateSequence - this.refillAttemptUpdate <= POP_WINDOW_UPDATES) return;
            this.refillSource = -1;
            this.refillTarget = -1;
            this.pendingRefillTime = 0L;
            this.logPending("swap_not_observed");
            return;
        }
        if (this.confirmedPopDebt < threshold) {
            this.pendingRefillTime = 0L;
            this.pendingReason = null;
            return;
        }
        if (MainhandModule.mc.player.getInventory().getItem(targetSlot).is(Items.TOTEM_OF_UNDYING)) {
            this.pendingRefillTime = 0L;
            this.logPending("target_still_totem");
            return;
        }
        if (this.pendingRefillTime == 0L) {
            long d = this.delay.getValue().longValue();
            this.pendingRefillTime = now + Math.max(1L, d);
            Night.LOGGER.info("[Mainhand] THRESHOLD_REACHED debt={} threshold={} targetSlot={}", this.confirmedPopDebt, threshold, targetSlot + 1);
            if (d > 0L) Night.LOGGER.info("[Mainhand] REFILL_DELAY ms={}", d);
        }
        if (now < this.pendingRefillTime || PingBypassFlags.isPingBypassActive()) return;
        int sourceSlot = InventoryUtils.find(Items.TOTEM_OF_UNDYING, InventoryUtils.INVENTORY_START, InventoryUtils.INVENTORY_END);
        if (sourceSlot == -1) {
            this.logPending("no_inventory_totem");
            return;
        }
        this.pendingReason = null;
        this.refillSource = sourceSlot;
        this.refillTarget = targetSlot;
        this.refillAttemptUpdate = this.updateSequence;
        InventoryUtils.swap("Swap", sourceSlot, targetSlot);
    }

    private void updateMainhand() {
        boolean canGapple;
        boolean isUsing;
        boolean isContainerScreen;
        if (MainhandModule.mc.player == null || MainhandModule.mc.level == null) {
            return;
        }
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        Screen currentScreen = MainhandModule.mc.gui.screen();
        boolean bl = isContainerScreen = currentScreen instanceof AbstractContainerScreen && !(currentScreen instanceof InventoryScreen);
        if (isContainerScreen && !MainhandModule.mc.player.containerMenu.getCarried().isEmpty()) {
            return;
        }
        boolean bl2 = isUsing = MainhandModule.mc.options.keyUse.isDown() || MainhandModule.mc.player.isUsingItem();
        if (this.eatingHotbar) {
            if (!isUsing) {
                if (this.previousHotbarSlot != -1) {
                    MainhandModule.mc.player.getInventory().setSelectedSlot(this.previousHotbarSlot);
                    ((ClientPlayerInteractionManagerAccessor)MainhandModule.mc.gameMode).invokeSyncSelectedSlot();
                }
                this.eatingHotbar = false;
                this.previousHotbarSlot = -1;
            }
            return;
        }
        if (this.eatingInventory) {
            if (!isUsing) {
                if (this.swappedInvSlot != -1 && this.swappedHotbarSlot != -1) {
                    InventoryUtils.swap("Swap", this.swappedInvSlot, this.swappedHotbarSlot);
                }
                this.eatingInventory = false;
                this.swappedInvSlot = -1;
                this.swappedHotbarSlot = -1;
            }
            return;
        }
        boolean bl3 = canGapple = !this.needsTotem() || this.lethalOverride.getValue() && !this.hasEnemyInVisualRange();
        if (this.useGapple.getValue() && MainhandModule.mc.options.keyUse.isDown() && MainhandModule.mc.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING) && canGapple) {
            int hotbarGapple = this.findGappleHotbar();
            if (hotbarGapple != -1) {
                this.previousHotbarSlot = MainhandModule.mc.player.getInventory().getSelectedSlot();
                MainhandModule.mc.player.getInventory().setSelectedSlot(hotbarGapple);
                ((ClientPlayerInteractionManagerAccessor)MainhandModule.mc.gameMode).invokeSyncSelectedSlot();
                this.eatingHotbar = true;
                return;
            }
            int invGapple = this.findGappleInventory();
            if (invGapple != -1) {
                this.swappedInvSlot = invGapple;
                this.swappedHotbarSlot = MainhandModule.mc.player.getInventory().getSelectedSlot();
                InventoryUtils.swap("Swap", invGapple, this.swappedHotbarSlot);
                this.eatingInventory = true;
                return;
            }
        }
        int targetSlot = this.slot.getValue().intValue() - 1;
        long now = System.currentTimeMillis();
        if (this.afterPops()) {
            this.updateAfterPopsRefill(targetSlot, now);
        } else if (this.pendingRefillTime > 0L) {
            if (now >= this.pendingRefillTime) {
                this.pendingRefillTime = 0L;
                this.refill(targetSlot);
            }
        } else {
            boolean needRefill;
            boolean bl4 = needRefill = MainhandModule.mc.player.getInventory().getItem(targetSlot).getItem() != Items.TOTEM_OF_UNDYING;
            if (needRefill) {
                long d = this.delay.getValue().longValue();
                if (d <= 0L) {
                    this.refill(targetSlot);
                } else {
                    this.pendingRefillTime = now + d;
                }
            }
        }
        if (this.needsTotem()) {
            boolean slotHasTotem;
            int currentSelected = MainhandModule.mc.player.getInventory().getSelectedSlot();
            boolean bl5 = slotHasTotem = MainhandModule.mc.player.getInventory().getItem(targetSlot).getItem() == Items.TOTEM_OF_UNDYING;
            if (currentSelected != targetSlot || !slotHasTotem) {
                if (slotHasTotem) {
                    MainhandModule.mc.player.getInventory().setSelectedSlot(targetSlot);
                    ((ClientPlayerInteractionManagerAccessor)MainhandModule.mc.gameMode).invokeSyncSelectedSlot();
                } else {
                    int anyHotbarTotem = InventoryUtils.find(Items.TOTEM_OF_UNDYING, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
                    if (anyHotbarTotem != -1 && currentSelected != anyHotbarTotem) {
                        MainhandModule.mc.player.getInventory().setSelectedSlot(anyHotbarTotem);
                        ((ClientPlayerInteractionManagerAccessor)MainhandModule.mc.gameMode).invokeSyncSelectedSlot();
                    }
                }
            }
        }
    }

    private int findRefillTotem(int target) {
        int invTotem = InventoryUtils.findInventory(Items.TOTEM_OF_UNDYING);
        if (invTotem != -1) {
            return invTotem;
        }
        for (int i = InventoryUtils.HOTBAR_START; i <= InventoryUtils.HOTBAR_END; ++i) {
            if (i == target || MainhandModule.mc.player.getInventory().getItem(i).getItem() != Items.TOTEM_OF_UNDYING) continue;
            return i;
        }
        return -1;
    }

    private int findGappleHotbar() {
        int egap = InventoryUtils.find(Items.ENCHANTED_GOLDEN_APPLE, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
        if (egap != -1) {
            return egap;
        }
        return InventoryUtils.find(Items.GOLDEN_APPLE, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
    }

    private int findGappleInventory() {
        int egap = InventoryUtils.find(Items.ENCHANTED_GOLDEN_APPLE, InventoryUtils.INVENTORY_START, InventoryUtils.INVENTORY_END);
        if (egap != -1) {
            return egap;
        }
        return InventoryUtils.find(Items.GOLDEN_APPLE, InventoryUtils.INVENTORY_START, InventoryUtils.INVENTORY_END);
    }

    public boolean shouldLockSlot() {
        if (!this.isToggled()) {
            return false;
        }
        if (MainhandModule.mc.player == null) {
            return false;
        }
        if (this.eatingHotbar || this.eatingInventory) {
            return false;
        }
        if (InventoryUtils.find(Items.TOTEM_OF_UNDYING) == -1) {
            return false;
        }
        return this.needsTotem();
    }

    private boolean needsTotem() {
        if (Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && Night.MODULE_MANAGER.getModule(SuicideModule.class).offhandOverride.getValue()) {
            return false;
        }
        if (MainhandModule.mc.player.getHealth() + MainhandModule.mc.player.getAbsorptionAmount() <= this.health.getValue().floatValue()) {
            return true;
        }
        if (MainhandModule.mc.player.fallDistance > (double)this.fallDistance.getValue().floatValue()) {
            return true;
        }
        if (this.elytraCheck.getValue() && MainhandModule.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
            return true;
        }
        return this.antiMace.getValue() && MainhandModule.mc.level.players().stream().anyMatch(entity -> entity != MainhandModule.mc.player && !Night.FRIEND_MANAGER.contains(entity.getName().getString()) && MainhandModule.mc.player.distanceToSqr((Entity)entity) <= (double)Mth.square((float)this.maceRange.getValue().floatValue()) && entity.fallDistance >= 1.5 && entity.getMainHandItem().getItem().equals(Items.MACE));
    }

    private boolean hasEnemyInVisualRange() {
        if (MainhandModule.mc.level == null || MainhandModule.mc.player == null) {
            return false;
        }
        FakePlayerModule fakePlayer = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FakePlayerModule.class) : null;
        return MainhandModule.mc.level.players().stream().anyMatch(player -> player != MainhandModule.mc.player && !player.isDeadOrDying() && !EntityUtils.isGhost((Entity)player) && (fakePlayer == null || !fakePlayer.isToggled() || player != fakePlayer.getPlayer()) && !Night.FRIEND_MANAGER.contains(player.getName().getString()));
    }

    @Override
    public void onDisable() {
        if (this.eatingHotbar && MainhandModule.mc.player != null && this.previousHotbarSlot != -1) {
            MainhandModule.mc.player.getInventory().setSelectedSlot(this.previousHotbarSlot);
            ((ClientPlayerInteractionManagerAccessor)MainhandModule.mc.gameMode).invokeSyncSelectedSlot();
        }
        if (this.eatingInventory && MainhandModule.mc.player != null && this.swappedInvSlot != -1 && this.swappedHotbarSlot != -1) {
            InventoryUtils.swap("Swap", this.swappedInvSlot, this.swappedHotbarSlot);
        }
        this.eatingHotbar = false;
        this.previousHotbarSlot = -1;
        this.eatingInventory = false;
        this.swappedInvSlot = -1;
        this.swappedHotbarSlot = -1;
        this.pendingRefillTime = 0L;
        this.resetAfterPops("disable");
        this.observedMode = null;
        this.observedPlayer = null;
        this.observedWorld = null;
    }

    @Override
    public String getMetaData() {
        return "Slot " + this.slot.getValue().intValue();
    }
}

