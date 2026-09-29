/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$PosRot
 *  net.minecraft.world.entity.ai.attributes.Attributes
 */
package night.modules.impl.movement;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.ai.attributes.Attributes;
import night.events.SubscribeEvent;
import night.events.impl.PacketSendEvent;
import night.events.impl.PlayerUpdateEvent;
import night.mixins.accessors.PlayerMoveC2SPacketAccessor;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.ModeSetting;

@RegisterModule(name="NoFall", description="Prevents you from taking fall damage.", category=Module.Category.MOVEMENT)
public class NoFallModule
extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "The method that will be used in preventing fall damage.", "Packet", new String[]{"Packet", "Grim"});

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (!this.isFalling()) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("Grim")) {
            mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.PosRot(NoFallModule.mc.player.getX(), NoFallModule.mc.player.getY() + 1.0E-9, NoFallModule.mc.player.getZ(), NoFallModule.mc.player.getYRot(), NoFallModule.mc.player.getXRot(), false, NoFallModule.mc.player.horizontalCollision));
            NoFallModule.mc.player.resetFallDistance();
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent event) {
        if (NoFallModule.mc.player == null || NoFallModule.mc.level == null) {
            return;
        }
        if (!this.mode.getValue().equalsIgnoreCase("Packet")) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof ServerboundMovePlayerPacket) {
            ServerboundMovePlayerPacket packet2 = (ServerboundMovePlayerPacket)packet;
            if (this.isFalling()) {
                ((PlayerMoveC2SPacketAccessor)packet2).setOnGround(true);
            }
        }
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue();
    }

    private boolean isFalling() {
        return NoFallModule.mc.player.fallDistance > NoFallModule.mc.player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE) && !NoFallModule.mc.player.onGround() && !NoFallModule.mc.player.isFallFlying();
    }
}

