/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
 *  net.minecraft.world.entity.EntityTypes
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.player;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.events.SubscribeEvent;
import night.events.impl.KeyInputEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PacketSendEvent;
import night.events.impl.RenderWorldEvent;
import night.events.impl.TickEvent;
import night.modules.Module;
import night.settings.impl.BindSetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.utils.color.ColorUtils;
import night.utils.graphics.Renderer3D;

public class ChorusControlModule
extends Module {
    public BindSetting confirm = new BindSetting("Confirm", "The key that will be used to confirm the teleportation.", 340).disableHoldModes();
    public ModeSetting mode = new ModeSetting("Mode", "The rendering that will be applied to the target block.", "Both", new String[]{"None", "Fill", "Outline", "Both"});
    public ColorSetting fillColor = new ColorSetting("FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(this.mode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(this.mode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    private ClientboundPlayerPositionPacket packet;
    private boolean cancel = false;

    @SubscribeEvent
    public void onTick(TickEvent event) {
        ItemStack stack;
        if (ChorusControlModule.mc.player == null || ChorusControlModule.mc.level == null) {
            return;
        }
        if (!this.cancel && ChorusControlModule.mc.player.isUsingItem() && (stack = ChorusControlModule.mc.player.getItemInHand(ChorusControlModule.mc.player.getUsedItemHand())).getItem() == Items.CHORUS_FRUIT && stack.getUseDuration((LivingEntity)ChorusControlModule.mc.player) - ChorusControlModule.mc.player.getTicksUsingItem() <= 1) {
            this.cancel = true;
        }
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (ChorusControlModule.mc.player == null || ChorusControlModule.mc.level == null || ChorusControlModule.mc.gui != null && ChorusControlModule.mc.gui.screen() != null) {
            return;
        }
        if (this.packet == null) {
            return;
        }
        if (event.getKey() != this.confirm.getValue()) {
            return;
        }
        this.packet.handle((ClientGamePacketListener)mc.getConnection());
        this.packet = null;
        this.cancel = false;
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent event) {
        ServerboundMovePlayerPacket packet;
        if (ChorusControlModule.mc.player == null || ChorusControlModule.mc.level == null) {
            return;
        }
        if (!this.cancel) {
            return;
        }
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof ServerboundMovePlayerPacket && (packet = (ServerboundMovePlayerPacket)packet2).hasPosition()) {
            event.setCancelled(true);
        }
        if (event.getPacket() instanceof ServerboundAcceptTeleportationPacket) {
            event.setCancelled(true);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (ChorusControlModule.mc.player == null || ChorusControlModule.mc.level == null) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof ClientboundPlayerPositionPacket) {
            ClientboundPlayerPositionPacket packet2 = (ClientboundPlayerPositionPacket)packet;
            if (this.cancel) {
                event.setCancelled(true);
                this.packet = packet2;
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (ChorusControlModule.mc.player == null || ChorusControlModule.mc.level == null || ChorusControlModule.mc.gui != null && ChorusControlModule.mc.gui.screen() != null) {
            return;
        }
        if (this.packet == null) {
            return;
        }
        Vec3 vec3d = new Vec3(this.packet.change().position().x(), this.packet.change().position().y(), this.packet.change().position().z());
        AABB box = EntityTypes.PLAYER.getDimensions().makeBoundingBox(vec3d);
        if (this.mode.getValue().equalsIgnoreCase("Fill") || this.mode.getValue().equalsIgnoreCase("Both")) {
            Renderer3D.renderBox(event.getMatrices(), box, this.fillColor.getColor());
        }
        if (this.mode.getValue().equalsIgnoreCase("Outline") || this.mode.getValue().equalsIgnoreCase("Both")) {
            Renderer3D.renderBoxOutline(event.getMatrices(), box, this.outlineColor.getColor());
        }
    }

    @Override
    public void onDisable() {
        if (mc.getConnection() != null && this.packet != null) {
            this.packet.handle((ClientGamePacketListener)mc.getConnection());
            this.packet = null;
            this.cancel = false;
        }
    }
}

