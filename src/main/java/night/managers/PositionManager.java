/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
 *  net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket
 */
package night.managers;

import lombok.Generated;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PacketSendEvent;
import night.utils.IMinecraft;

public class PositionManager
implements IMinecraft {
    private double serverX;
    private double serverY;
    private double serverZ;
    private boolean serverOnGround;
    private boolean serverSprinting;
    private boolean serverSneaking;
    private int serverSlot;

    public PositionManager() {
        Night.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
   public void onPacketSend(PacketSendEvent event) {
      if (mc.player != null) {
         if (event.getPacket() instanceof ServerboundMovePlayerPacket packet) {
            if (packet.hasPosition()) {
               this.serverX = packet.getX(mc.player.getX());
               this.serverY = packet.getY(mc.player.getY());
               this.serverZ = packet.getZ(mc.player.getZ());
            }

            this.serverOnGround = packet.isOnGround();
         }

         if (event.getPacket() instanceof ServerboundSetCarriedItemPacket packet) {
            this.serverSlot = packet.getSlot();
         }

         if (event.getPacket() instanceof ServerboundPlayerCommandPacket packet) {
            switch (packet.getAction()) {
               case START_SPRINTING:
                  this.serverSprinting = true;
                  break;
               case STOP_SPRINTING:
                  this.serverSprinting = false;
            }
         }

         if (event.getPacket() instanceof ServerboundPlayerInputPacket packet) {
            this.serverSneaking = packet.input().shift();
         }
      }
   }

    public double getServerX() {
        return PositionManager.mc.player != null ? PositionManager.mc.player.getX() : this.serverX;
    }

    public double getServerY() {
        return PositionManager.mc.player != null ? PositionManager.mc.player.getY() : this.serverY;
    }

    public double getServerZ() {
        return PositionManager.mc.player != null ? PositionManager.mc.player.getZ() : this.serverZ;
    }

    public boolean isServerOnGround() {
        return PositionManager.mc.player != null ? PositionManager.mc.player.onGround() : this.serverOnGround;
    }

    @Generated
    public boolean isServerSprinting() {
        return this.serverSprinting;
    }

    @Generated
    public boolean isServerSneaking() {
        return this.serverSneaking;
    }

    @Generated
    public int getServerSlot() {
        return this.serverSlot;
    }
}

