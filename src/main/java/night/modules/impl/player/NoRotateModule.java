/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Relative
 */
package night.modules.impl.player;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PacketReceiveEvent;
import night.mixins.accessors.PlayerPositionAccessor;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.utils.minecraft.PositionUtils;

@RegisterModule(name="NoRotate", description="Prevents the server from forcing rotations on you.", category=Module.Category.PLAYER)
public class NoRotateModule
extends Module {
    public BooleanSetting inBlocks = new BooleanSetting("InBlocks", "Whether or not to stop rotations whenever inside of a block.", false);
    public BooleanSetting spoof = new BooleanSetting("Spoof", "Sends rotation packets once you have been rubberbanded.", false);

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (NoRotateModule.mc.player == null || NoRotateModule.mc.level == null) {
            return;
        }
        if (!this.inBlocks.getValue() && !NoRotateModule.mc.level.getBlockState(PositionUtils.getFlooredPosition((Entity)NoRotateModule.mc.player)).canBeReplaced()) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof ClientboundPlayerPositionPacket) {
            ClientboundPlayerPositionPacket packet2 = (ClientboundPlayerPositionPacket)packet;
            if (this.spoof.getValue()) {
                Night.ROTATION_MANAGER.packetRotate(packet2.change().yRot(), packet2.change().xRot());
                Night.ROTATION_MANAGER.packetRotate(NoRotateModule.mc.player.getYRot(), NoRotateModule.mc.player.getXRot());
            }
            ((PlayerPositionAccessor)(Object)packet2.change()).setYaw(NoRotateModule.mc.player.getYRot());
            ((PlayerPositionAccessor)(Object)packet2.change()).setPitch(NoRotateModule.mc.player.getXRot());
            packet2.relatives().remove(Relative.X_ROT);
            packet2.relatives().remove(Relative.Y_ROT);
        }
    }
}

