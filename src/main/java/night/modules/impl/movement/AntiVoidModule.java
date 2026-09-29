/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Pos
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.movement;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import night.events.SubscribeEvent;
import night.events.impl.PlayerMoveEvent;
import night.modules.Module;
import night.modules.RegisterModule;

@RegisterModule(name="AntiVoid", description="Prevents you from falling into the void.", category=Module.Category.MOVEMENT)
public class AntiVoidModule
extends Module {
    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent event) {
        if (AntiVoidModule.mc.player == null || AntiVoidModule.mc.level == null) {
            return;
        }
        if (this.falling()) {
            event.setCancelled(true);
            event.setMovement(new Vec3(event.getMovement().x, 0.0, event.getMovement().z));
            mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.Pos(AntiVoidModule.mc.player.getX(), AntiVoidModule.mc.player.getY(), AntiVoidModule.mc.player.getZ(), true, AntiVoidModule.mc.player.horizontalCollision));
        }
    }

    private boolean falling() {
        if (AntiVoidModule.mc.player == null || AntiVoidModule.mc.level == null) {
            return false;
        }
        int minY = AntiVoidModule.mc.level.getMinY();
        for (int i = (int)AntiVoidModule.mc.player.getY(); i >= minY; --i) {
            BlockPos pos = BlockPos.containing((double)AntiVoidModule.mc.player.getX(), (double)i, (double)AntiVoidModule.mc.player.getZ());
            BlockState state = AntiVoidModule.mc.level.getBlockState(pos);
            if (state.isAir() || state.getCollisionShape((BlockGetter)AntiVoidModule.mc.level, pos).isEmpty()) continue;
            return false;
        }
        return AntiVoidModule.mc.player.fallDistance > 0.0;
    }
}

