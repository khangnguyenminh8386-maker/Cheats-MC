/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec2
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.movement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import night.events.SubscribeEvent;
import night.events.impl.PlayerMoveEvent;
import night.mixins.accessors.ClientInputAccessor;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.NumberSetting;
import night.utils.minecraft.HoleUtils;
import night.utils.minecraft.PositionUtils;

@RegisterModule(name="Anchor", description="Pulls you toward your nearest hole when looking down.", category=Module.Category.MOVEMENT)
public class Anchor
extends Module {
    public NumberSetting pitch = new NumberSetting("Pitch", "Minimum pitch angle to activate anchoring.", 60, 0, 90);
    public BooleanSetting doubles = new BooleanSetting("Doubles", "Include 2x1 holes when anchoring.", true);
    public BooleanSetting stopMotion = new BooleanSetting("StopMotion", "Stop horizontal movement while anchoring.", true);
    public BooleanSetting fastFall = new BooleanSetting("FastFall", "Accelerate fall speed while anchoring.", true);
    public NumberSetting speed = new NumberSetting("FallSpeed", "Speed multiplier for falling.", Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(5.0f));
    public BooleanSetting checkAlreadyInHole = new BooleanSetting("AlreadyInHole", "Don't anchor if already in a hole.", true);
    public BooleanSetting checkTerrainHole = new BooleanSetting("CheckTerrainHole", "Don't anchor toward terrain holes.", false);
    public BooleanSetting checkBurrowed = new BooleanSetting("CheckBurrowed", "Don't anchor if burrowed.", true);

    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent event) {
        if (this.getNull() || Anchor.mc.player == null || Anchor.mc.level == null) {
            return;
        }
        if (this.checkAlreadyInHole.getValue() && this.isAlreadyInHole()) {
            return;
        }
        if (this.checkBurrowed.getValue() && this.isBurrowed()) {
            return;
        }
        float playerPitch = Anchor.mc.player.getXRot();
        if (playerPitch < this.pitch.getValue().floatValue()) {
            return;
        }
        BlockPos playerPos = PositionUtils.getFlooredPosition((Entity)Anchor.mc.player);
        if (Anchor.mc.player.isFallFlying()) {
            return;
        }
        int searchHeight = 5;
        for (int i = 0; i <= searchHeight; ++i) {
            BlockPos checkPos = playerPos.below(i + 1);
            if (Anchor.mc.level.getBlockState(checkPos).isAir()) {
                return;
            }
            HoleUtils.Hole hole = HoleUtils.getSingleHole(checkPos, 1.0);
            if (hole != null) {
                if (this.checkTerrainHole.getValue() && this.isTerrainHole(hole)) continue;
                this.applyAnchorLogic(event, hole);
                return;
            }
            if (this.doubles.getValue() && (hole = HoleUtils.getDoubleHole(checkPos, 1.0)) != null) {
                if (this.checkTerrainHole.getValue() && this.isTerrainHole(hole)) continue;
                this.applyAnchorLogic(event, hole);
                return;
            }
            hole = HoleUtils.getQuadHole(checkPos, 1.0);
            if (hole == null || this.checkTerrainHole.getValue() && this.isTerrainHole(hole)) continue;
            this.applyAnchorLogic(event, hole);
            return;
        }
    }

    private void applyAnchorLogic(PlayerMoveEvent event, HoleUtils.Hole hole) {
        Vec3 velocity;
        if (Anchor.mc.player == null) {
            return;
        }
        Vec3 holeCenter = hole.box().getCenter();
        if (this.stopMotion.getValue()) {
            velocity = Anchor.mc.player.getDeltaMovement();
            if (velocity != null) {
                Anchor.mc.player.setDeltaMovement(0.0, velocity.y, 0.0);
            }
            ((ClientInputAccessor)Anchor.mc.player.input).setMoveVector(Vec2.ZERO);
        }
        if (this.fastFall.getValue() && (velocity = Anchor.mc.player.getDeltaMovement()) != null && velocity.y > -0.1) {
            double fallSpeed = this.speed.getValue().doubleValue() / 10.0;
            Anchor.mc.player.setDeltaMovement(velocity.x, -fallSpeed, velocity.z);
        }
        double xSpeed = holeCenter.x - Anchor.mc.player.getX();
        double zSpeed = holeCenter.z - Anchor.mc.player.getZ();
        event.setX(xSpeed / 2.0);
        event.setZ(zSpeed / 2.0);
    }

    private boolean isAlreadyInHole() {
        return HoleUtils.isPlayerInHole((Player)Anchor.mc.player);
    }

    private boolean isBurrowed() {
        BlockPos playerPos = PositionUtils.getFlooredPosition((Entity)Anchor.mc.player);
        Direction[] horizontalDirections = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        int solidSides = 0;
        for (Direction direction : horizontalDirections) {
            BlockPos checkPos = playerPos.relative(direction);
            if (Anchor.mc.level.getBlockState(checkPos).isAir() || Anchor.mc.level.getBlockState(checkPos).canBeReplaced()) continue;
            ++solidSides;
        }
        return solidSides == 4;
    }

    private boolean isTerrainHole(HoleUtils.Hole hole) {
        if (hole == null) {
            return false;
        }
        return hole.safety() == HoleUtils.HoleSafety.UNSAFE || hole.safety() == HoleUtils.HoleSafety.SAFE;
    }
}

