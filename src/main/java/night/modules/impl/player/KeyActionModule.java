/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemPacket
 *  net.minecraft.tags.ItemTags
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package night.modules.impl.player;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.KeyInputEvent;
import night.events.impl.MouseInputEvent;
import night.events.impl.PlayerUpdateEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.movement.SprintModule;
import night.modules.impl.player.MultiTaskModule;
import night.pingbypass.server.ProxyServerTickListener;
import night.settings.impl.BindSetting;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.input.KeyboardUtils;
import night.utils.minecraft.InventoryUtils;
import night.utils.minecraft.NetworkUtils;

@RegisterModule(name="KeyAction", description="Keybinds for throwing fireworks, pearls and experience bottles.", category=Module.Category.PLAYER, persistent=true, proxyEnhanced=true)
public class KeyActionModule
extends Module {
    public CategorySetting fireworkCategory = new CategorySetting("FireWork", "Keybind and settings for throwing a firework.");
    public BindSetting fireworkBind = new BindSetting("FireWorkBind", "Bind", "The key that throws a firework. Hold/ReverseHold repeat it every tick the bind is (or isn't) held.", new CategorySetting.Visibility(this.fireworkCategory), 0);
    public ModeSetting fireworkSwitch = new ModeSetting("FireWorkSwitch", "Switch", "The mode that will be used for automatically switching to fireworks.", new CategorySetting.Visibility(this.fireworkCategory), "Silent", InventoryUtils.SWITCH_MODES);
    public CategorySetting pearlsCategory = new CategorySetting("Pearls", "Keybind and settings for throwing an ender pearl.");
    public BindSetting pearlsBind = new BindSetting("PearlsBind", "Bind", "The key that throws an ender pearl. Hold/ReverseHold repeat it every tick the bind is (or isn't) held.", new CategorySetting.Visibility(this.pearlsCategory), 0);
    public ModeSetting pearlsSwitch = new ModeSetting("PearlsSwitch", "Switch", "The mode that will be used for automatically switching to pearls.", new CategorySetting.Visibility(this.pearlsCategory), "Silent", InventoryUtils.SWITCH_MODES);
    public BooleanSetting pearlsRotate = new BooleanSetting("PearlsRotate", "Rotate", "Sends a packet rotation right before throwing the pearl.", new CategorySetting.Visibility(this.pearlsCategory), true);
    public CategorySetting xpCategory = new CategorySetting("XP", "Keybind and settings for throwing experience bottles.");
    public BindSetting xpBind = new BindSetting("XPBind", "Bind", "The key that starts/stops throwing experience bottles. Hold/ReverseHold keep the loop matched to whether the bind is (or isn't) held instead of toggling it.", new CategorySetting.Visibility(this.xpCategory), 0);
    public ModeSetting xpSwitch = new ModeSetting("XPSwitch", "Switch", "The mode that will be used for automatically switching to experience bottles.", new CategorySetting.Visibility(this.xpCategory), "Silent", InventoryUtils.SWITCH_MODES);
    public BooleanSetting xpRotate = new BooleanSetting("XPRotate", "Rotate", "Rotates down to your feet when throwing experience bottles.", new CategorySetting.Visibility(this.xpCategory), true);
    public NumberSetting xpDelay = new NumberSetting("XPDelay", "Delay", "The delay in ticks between throwing experience bottles.", new CategorySetting.Visibility(this.xpCategory), 1, 0, 20);
    public NumberSetting xpRepeat = new NumberSetting("XPRepeat", "Repeat", "Allows you to throw a lot more XP bottles at once.", new CategorySetting.Visibility(this.xpCategory), 1, 1, 15);
    public ModeSetting xpAntiWaste = new ModeSetting("XPAntiWaste", "AntiWaste", "How wasting of experience bottles should be prevented.", new CategorySetting.Visibility(this.xpCategory), "Avoid", new String[]{"None", "Avoid", "Disable"});
    public BooleanSetting xpItemDisable = new BooleanSetting("XPItemDisable", "ItemDisable", "Automatically stops the XP loop when you run out of XP.", new CategorySetting.Visibility(this.xpCategory), true);
    private boolean xpActive = false;
    private int xpTicks = 0;
    private long lastPearlTime = 0L;

    public boolean isXpActive() {
        return this.xpActive;
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (this.shouldRunOnProxy() || this.getNull() || KeyActionModule.mc.gui != null && KeyActionModule.mc.gui.screen() != null) {
            return;
        }
        if (this.fireworkBind.getValue() != 0 && event.getKey() == this.fireworkBind.getValue() && this.fireworkBind.getMode().equals("Bind")) {
            this.throwFirework();
        }
        if (this.pearlsBind.getValue() != 0 && event.getKey() == this.pearlsBind.getValue() && this.pearlsBind.getMode().equals("Bind")) {
            this.throwPearl();
        }
        if (this.xpBind.getValue() != 0 && event.getKey() == this.xpBind.getValue() && this.xpBind.getMode().equals("Bind")) {
            this.xpActive = !this.xpActive;
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseInputEvent event) {
        if (this.shouldRunOnProxy() || this.getNull() || KeyActionModule.mc.gui != null && KeyActionModule.mc.gui.screen() != null) {
            return;
        }
        int key = -event.getButton() - 1;
        if (this.fireworkBind.getValue() != 0 && key == this.fireworkBind.getValue() && this.fireworkBind.getMode().equals("Bind")) {
            this.throwFirework();
        }
        if (this.pearlsBind.getValue() != 0 && key == this.pearlsBind.getValue() && this.pearlsBind.getMode().equals("Bind")) {
            this.throwPearl();
        }
        if (this.xpBind.getValue() != 0 && key == this.xpBind.getValue() && this.xpBind.getMode().equals("Bind")) {
            this.xpActive = !this.xpActive;
        }
    }

    private void throwFirework() {
        if (KeyActionModule.mc.player == null || KeyActionModule.mc.level == null) {
            return;
        }
        if (this.fireworkSwitch.getValue().equalsIgnoreCase("None") && KeyActionModule.mc.player.getMainHandItem().getItem() != Items.FIREWORK_ROCKET) {
            Night.CHAT_MANAGER.tagged("You are currently not holding any fireworks.", this.getName());
            return;
        }
        if (KeyActionModule.mc.player.getCooldowns().isOnCooldown(new ItemStack((ItemLike)Items.FIREWORK_ROCKET))) {
            return;
        }
        int slot = InventoryUtils.find(Items.FIREWORK_ROCKET, 0, this.fireworkSwitch.getValue().equalsIgnoreCase("AltSwap") || this.fireworkSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
        int previousSlot = KeyActionModule.mc.player.getInventory().getSelectedSlot();
        if (slot == -1) {
            Night.CHAT_MANAGER.tagged("No fireworks could be found in your hotbar.", this.getName());
            return;
        }
        InventoryUtils.switchSlot(this.fireworkSwitch.getValue(), slot, previousSlot);
        NetworkUtils.sendSequencedPacket(sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, KeyActionModule.mc.player.getYRot(), KeyActionModule.mc.player.getXRot()));
        InventoryUtils.switchBack(this.fireworkSwitch.getValue(), slot, previousSlot);
    }

    public boolean isPearlActive() {
        return System.currentTimeMillis() - this.lastPearlTime < 100L;
    }

    private void throwPearl() {
        if (KeyActionModule.mc.player == null || KeyActionModule.mc.level == null) {
            return;
        }
        if (this.pearlsSwitch.getValue().equalsIgnoreCase("None") && KeyActionModule.mc.player.getMainHandItem().getItem() != Items.ENDER_PEARL) {
            Night.CHAT_MANAGER.tagged("You are currently not holding any pearls.", this.getName());
            return;
        }
        if (KeyActionModule.mc.player.getCooldowns().isOnCooldown(new ItemStack((ItemLike)Items.ENDER_PEARL))) {
            return;
        }
        this.lastPearlTime = System.currentTimeMillis();
        MultiTaskModule multiTask = Night.MODULE_MANAGER.getModule(MultiTaskModule.class);
        boolean keepEating = multiTask != null && multiTask.isToggled() && multiTask.pearl.getValue() && KeyActionModule.mc.player.isUsingItem();
        String pearlMode = keepEating ? "AltSwap" : this.pearlsSwitch.getValue();
        int slot = InventoryUtils.find(Items.ENDER_PEARL, 0, pearlMode.equalsIgnoreCase("AltSwap") || pearlMode.equalsIgnoreCase("AltPickup") ? 35 : 8);
        int previousSlot = KeyActionModule.mc.player.getInventory().getSelectedSlot();
        if (slot == -1) {
            Night.CHAT_MANAGER.tagged("No pearls could be found in your hotbar.", this.getName());
            return;
        }
        if (this.pearlsRotate.getValue()) {
            Night.ROTATION_MANAGER.packetRotate(KeyActionModule.mc.player.getYRot(), KeyActionModule.mc.player.getXRot());
        }
        InventoryUtils.switchSlot(pearlMode, slot, previousSlot);
        NetworkUtils.sendSequencedPacket(sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, KeyActionModule.mc.player.getYRot(), KeyActionModule.mc.player.getXRot()), this::serverSend);
        this.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        InventoryUtils.switchBack(pearlMode, slot, previousSlot);
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (this.shouldRunOnProxy() || this.getNull() || KeyActionModule.mc.gui != null && KeyActionModule.mc.gui.screen() != null) {
            return;
        }
        this.checkHoldBind(this.fireworkBind, this::throwFirework);
        this.checkHoldBind(this.pearlsBind, this::throwPearl);
        if (this.xpBind.getValue() != 0 && !this.xpBind.getMode().equals("Bind")) {
            boolean held = KeyboardUtils.isBindDown(this.xpBind.getValue());
            this.xpActive = this.xpBind.getMode().equals("Hold") == held;
        }
        this.tickXp();
    }

    private void checkHoldBind(BindSetting bind, Runnable action) {
        if (bind.getValue() == 0 || bind.getMode().equals("Bind")) {
            return;
        }
        boolean held = KeyboardUtils.isBindDown(bind.getValue());
        if (bind.getMode().equals("Hold") == held) {
            action.run();
        }
    }

    private void tickXp() {
        float pitch;
        SprintModule sprint;
        if (!this.xpActive) {
            return;
        }
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (KeyActionModule.mc.player == null || KeyActionModule.mc.level == null) {
            return;
        }
        if (this.xpSwitch.getValue().equalsIgnoreCase("None") && !(KeyActionModule.mc.player.getMainHandItem().getItem() instanceof BlockItem)) {
            Night.CHAT_MANAGER.tagged("You are currently not holding any experience bottles.", this.getName());
            this.xpActive = false;
            return;
        }
        if (this.xpTicks < this.xpDelay.getValue().intValue()) {
            ++this.xpTicks;
            return;
        }
        if (!this.needsExperience() && !this.xpAntiWaste.getValue().equals("None")) {
            if (this.xpAntiWaste.getValue().equals("Disable")) {
                this.xpActive = false;
            }
            return;
        }
        int slot = InventoryUtils.find(Items.EXPERIENCE_BOTTLE, 0, this.xpSwitch.getValue().equalsIgnoreCase("AltSwap") || this.xpSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
        int previousSlot = KeyActionModule.mc.player.getInventory().getSelectedSlot();
        if (slot == -1) {
            Night.CHAT_MANAGER.tagged("No experience bottles could be found in your hotbar.", this.getName());
            this.xpActive = false;
            return;
        }
        SprintModule sprintModule = sprint = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(SprintModule.class) : null;
        float yaw = sprint != null && sprint.isToggled() && sprint.isGrimCompensating() && sprint.getPendingYaw() != null ? sprint.getPendingYaw().floatValue() : (Night.ROTATION_MANAGER != null && Night.ROTATION_MANAGER.getRotation() != null ? Night.ROTATION_MANAGER.getRotation().getYaw() : KeyActionModule.mc.player.getYRot());
        float f = pitch = this.xpRotate.getValue() ? 90.0f : KeyActionModule.mc.player.getXRot();
        if (this.xpRotate.getValue() && Night.ROTATION_MANAGER != null) {
            Night.ROTATION_MANAGER.silentRotate(yaw, pitch);
        }
        InventoryUtils.switchSlot(this.xpSwitch.getValue(), slot, previousSlot);
        for (int i = 0; i < this.xpRepeat.getValue().intValue(); ++i) {
            NetworkUtils.sendSequencedPacket(sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, yaw, pitch), this::serverSend);
        }
        this.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        InventoryUtils.switchBack(this.xpSwitch.getValue(), slot, previousSlot);
        this.xpTicks = 0;
    }

    private void serverSend(Packet<?> packet) {
        Connection serverConn;
        if (this.isRunningOnProxy() && Night.PROXY_SERVER != null && (serverConn = Night.PROXY_SERVER.getServerConnection()) != null && serverConn.isConnected()) {
            ProxyServerTickListener.allowSend(() -> serverConn.send(packet));
            return;
        }
        mc.getConnection().send(packet);
    }

    private boolean needsExperience() {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
            ItemStack stack = KeyActionModule.mc.player.getItemBySlot(slot);
            if (stack.isEmpty() || !stack.is(ItemTags.FOOT_ARMOR) && !stack.is(ItemTags.LEG_ARMOR) && !stack.is(ItemTags.CHEST_ARMOR) && !stack.is(ItemTags.HEAD_ARMOR) || !((float)Math.round((float)(stack.getMaxDamage() - stack.getDamageValue()) * 100.0f / (float)stack.getMaxDamage()) < 100.0f)) continue;
            return true;
        }
        return false;
    }

    @Override
    public void onDisable() {
        this.xpActive = false;
        this.xpTicks = 0;
    }
}

