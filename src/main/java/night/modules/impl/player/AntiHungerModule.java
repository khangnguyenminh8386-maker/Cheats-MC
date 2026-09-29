/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket$Action
 */
package night.modules.impl.player;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;


import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import night.events.SubscribeEvent;
import night.events.impl.PacketSendEvent;
import night.events.impl.UpdateMovementEvent;
import night.mixins.accessors.PlayerMoveC2SPacketAccessor;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;

@RegisterModule(name="AntiHunger", description="Reduces the amount of hunger consumption.", category=Module.Category.PLAYER)
public class AntiHungerModule
extends Module {
    public BooleanSetting ground = new BooleanSetting("Ground", "Modifies movement packets to decrease hunger consumption.", true);
    public BooleanSetting sprint = new BooleanSetting("Sprint", "Spoofs sprinting packets to decrease hunger consumption.", true);
    private boolean lastOnGround = false;
    private boolean ignore = false;

    @SubscribeEvent
   public void onPacketSend(PacketSendEvent event) {
      if (mc.player != null && mc.level != null) {
         if (this.ignore && event.getPacket() instanceof ServerboundMovePlayerPacket) {
            this.ignore = false;
         } else if (!mc.player.isPassenger() && !mc.player.isInWater() && !mc.player.isUnderWater()) {
            if (event.getPacket() instanceof ServerboundMovePlayerPacket packet
               && this.ground.getValue()
               && mc.player.onGround()
               && mc.player.fallDistance <= 0.0
               && !mc.gameMode.isDestroying()) {
               ((PlayerMoveC2SPacketAccessor)packet).setOnGround(false);
            }

            if (event.getPacket() instanceof ServerboundPlayerCommandPacket packet && this.sprint.getValue() && packet.getAction() == Action.START_SPRINTING) {
               event.setCancelled(true);
            }
         }
      }
   }

    @SubscribeEvent
    public void onUpdateMovement(UpdateMovementEvent event) {
        if (AntiHungerModule.mc.player == null || AntiHungerModule.mc.level == null) {
            return;
        }
        if (AntiHungerModule.mc.player.onGround() && !this.lastOnGround && this.ground.getValue()) {
            this.ignore = true;
        }
        this.lastOnGround = AntiHungerModule.mc.player.onGround();
    }

    @Override
    public void onEnable() {
        if (AntiHungerModule.mc.player == null) {
            return;
        }
        this.lastOnGround = AntiHungerModule.mc.player.onGround();
    }
}

