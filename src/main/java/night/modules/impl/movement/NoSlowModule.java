/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundContainerClickPacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Pos
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$PosRot
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Rot
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$StatusOnly
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket$Action
 *  net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
 *  net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemPacket
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Input
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 */
package night.modules.impl.movement;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PacketSendEvent;
import night.events.impl.UpdateMovementEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.utils.minecraft.NetworkUtils;

@RegisterModule(name="NoSlow", description="Removes the slowness effect that you receive when doing certain actions.", category=Module.Category.MOVEMENT)
public class NoSlowModule
extends Module {
    public BooleanSetting items = new BooleanSetting("Items", "Removes the slowness effect from eating or using items.", true);
    public ModeSetting itemMode = new ModeSetting("Mode", "Item slowdown bypass mode.", "Normal", new String[]{"Normal", "Strict", "GrimV2", "GrimV3"});
    public BooleanSetting crawl = new BooleanSetting("Crawl", "Removes the slowness effect from crawling.", false);
    public BooleanSetting sneak = new BooleanSetting("Sneak", "Removes the slowness effect from sneaking.", false);
    public BooleanSetting soulSand = new BooleanSetting("SoulSand", "Removes the slowness effect from walking on soul sand.", false);
    public BooleanSetting slimeBlocks = new BooleanSetting("SlimeBlocks", "Removes the slowness effect from walking on slime blocks.", false);
    public BooleanSetting honeyBlocks = new BooleanSetting("HoneyBlocks", "Removes the slowness effect from walking on honey blocks.", false);

    @SubscribeEvent
    public void onUpdateMovementPost(UpdateMovementEvent.Post event) {
        float pitch;
        if (NoSlowModule.mc.player == null || NoSlowModule.mc.level == null) {
            return;
        }
        if (!this.items.getValue() || !NoSlowModule.mc.player.isUsingItem() || NoSlowModule.mc.player.isPassenger() || NoSlowModule.mc.player.isFallFlying()) {
            return;
        }
        float yaw = Night.ROTATION_MANAGER.getRotation() != null ? Night.ROTATION_MANAGER.getRotation().getYaw() : NoSlowModule.mc.player.getYRot();
        float f = pitch = Night.ROTATION_MANAGER.getRotation() != null ? Night.ROTATION_MANAGER.getRotation().getPitch() : NoSlowModule.mc.player.getXRot();
        if (this.itemMode.getValue().equals("GrimV2")) {
            if (!NoSlowModule.mc.player.isShiftKeyDown() && (NoSlowModule.mc.player.getUseItem().has(DataComponents.FOOD) || this.canUseItem(NoSlowModule.mc.player.getUseItem()))) {
                InteractionHand distraction = NoSlowModule.mc.player.getUsedItemHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
                NetworkUtils.sendSequencedPacket(id -> new ServerboundUseItemPacket(distraction, id, yaw, pitch));
            }
        } else if (this.itemMode.getValue().equals("Strict") && !NetworkUtils.isLegacyProtocol()) {
            if (NoSlowModule.mc.player.getUsedItemHand() == InteractionHand.OFF_HAND) {
                int slot = NoSlowModule.mc.player.getInventory().getSelectedSlot();
                mc.getConnection().send((Packet)new ServerboundSetCarriedItemPacket((slot + 1) % 9));
                mc.getConnection().send((Packet)new ServerboundSetCarriedItemPacket(slot));
            } else {
                NetworkUtils.sendSequencedPacket(id -> new ServerboundUseItemPacket(InteractionHand.OFF_HAND, id, yaw, pitch));
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent event) {
        if (NoSlowModule.mc.player == null || NoSlowModule.mc.level == null) {
            return;
        }
        if (this.itemMode.getValue().equals("Strict")) {
            if (event.getPacket() instanceof ServerboundMovePlayerPacket.PosRot || event.getPacket() instanceof ServerboundMovePlayerPacket.Pos || event.getPacket() instanceof ServerboundMovePlayerPacket.Rot || event.getPacket() instanceof ServerboundMovePlayerPacket.StatusOnly) {
                NoSlowModule.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(NoSlowModule.mc.player.getInventory().getSelectedSlot()));
            }
            if (event.getPacket() instanceof ServerboundContainerClickPacket) {
                if (NoSlowModule.mc.player.isUsingItem()) {
                    NoSlowModule.mc.player.stopUsingItem();
                }
                if (NoSlowModule.mc.player.isSprinting()) {
                    NoSlowModule.mc.player.connection.send((Packet)new ServerboundPlayerCommandPacket((Entity)NoSlowModule.mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
                }
                if (NoSlowModule.mc.player.isShiftKeyDown()) {
                    NoSlowModule.mc.player.connection.send((Packet)new ServerboundPlayerInputPacket(new Input(false, false, false, false, false, false, false)));
                }
            }
        }
    }

    @Override
    public void onDisable() {
        Night.WORLD_MANAGER.setTimerMultiplier(1.0f);
    }

    public boolean canUseItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.has(DataComponents.CONSUMABLE) || stack.has(DataComponents.FOOD) || stack.is(Items.BOW) || stack.is(Items.CROSSBOW) || stack.is(Items.SHIELD) || stack.is(Items.TRIDENT) || stack.is(Items.SPYGLASS) || stack.is(Items.GOAT_HORN);
    }

    public boolean canBypassGrimUseTime() {
        if (NoSlowModule.mc.player == null) {
            return false;
        }
        return NoSlowModule.mc.player.getTicksUsingItem() > 1 && NoSlowModule.mc.player.getTicksUsingItem() % 2 != 0;
    }

    public boolean shouldSlow() {
        if (NoSlowModule.mc.player == null || !this.items.getValue()) {
            return true;
        }
        if (this.itemMode.getValue().equals("GrimV3")) {
            return !this.canBypassGrimUseTime();
        }
        return false;
    }
}

