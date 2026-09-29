/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.ExperienceOrb
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package night.utils.minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.utils.IMinecraft;
import night.utils.minecraft.NetworkUtils;
import night.utils.minecraft.WorldUtils;
import night.utils.rotations.RotationUtils;

public class PistonHelper
implements IMinecraft {
    public static final CopyOnWriteArrayList<BlockPos> placeList = new CopyOnWriteArrayList();

    public static boolean isGrimDirection(BlockPos pos, Direction direction) {
        if (PistonHelper.mc.player == null || PistonHelper.mc.level == null) {
            return false;
        }
        AABB combined = PistonHelper.getCombinedBox(pos);
        AABB eyeBox = new AABB(PistonHelper.mc.player.getX(), PistonHelper.mc.player.getY() + 0.4, PistonHelper.mc.player.getZ(), PistonHelper.mc.player.getX(), PistonHelper.mc.player.getY() + 1.62, PistonHelper.mc.player.getZ()).inflate(2.0E-4);
        if (PistonHelper.isIntersected(eyeBox, combined)) {
            return true;
        }
        return switch (direction) {
            default -> throw new MatchException(null, null);
            case Direction.NORTH -> {
                if (eyeBox.minZ <= combined.minZ) {
                    yield true;
                }
                yield false;
            }
            case Direction.SOUTH -> {
                if (eyeBox.maxZ >= combined.maxZ) {
                    yield true;
                }
                yield false;
            }
            case Direction.EAST -> {
                if (eyeBox.maxX >= combined.maxX) {
                    yield true;
                }
                yield false;
            }
            case Direction.WEST -> {
                if (eyeBox.minX <= combined.minX) {
                    yield true;
                }
                yield false;
            }
            case Direction.UP -> {
                if (eyeBox.maxY >= combined.maxY) {
                    yield true;
                }
                yield false;
            }
            case Direction.DOWN -> eyeBox.minY <= combined.minY;
        };
    }

    public static Direction getClickSideStrict(BlockPos pos) {
        if (PistonHelper.mc.player == null) {
            return null;
        }
        Direction best = null;
        double minDist = Double.MAX_VALUE;
        for (Direction dir : Direction.values()) {
            double dist;
            if (!PistonHelper.isGrimDirection(pos, dir) || !((dist = PistonHelper.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)pos.relative(dir)))) < minDist)) continue;
            minDist = dist;
            best = dir;
        }
        return best;
    }

    public static Direction getClickSide(BlockPos pos) {
        double dist;
        if (PistonHelper.mc.player == null) {
            return null;
        }
        double minDist = Double.MAX_VALUE;
        Direction best = null;
        for (Direction dir : Direction.values()) {
            if (!PistonHelper.canSee(pos, dir) || !((dist = PistonHelper.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)pos.relative(dir)))) < minDist)) continue;
            minDist = dist;
            best = dir;
        }
        if (best != null) {
            return best;
        }
        minDist = Double.MAX_VALUE;
        best = Direction.UP;
        for (Direction dir : Direction.values()) {
            if (!PistonHelper.isGrimDirection(pos, dir) || !((dist = PistonHelper.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)pos.relative(dir)))) < minDist)) continue;
            minDist = dist;
            best = dir;
        }
        return best;
    }

    public static Direction getPlaceSide(BlockPos pos, Predicate<Direction> filter) {
        return PistonHelper.getPlaceSide(pos, filter, false);
    }

    public static Direction getPlaceSide(BlockPos pos, Predicate<Direction> filter, boolean ignoreSneak) {
        double dist;
        Vec3 clickPoint;
        BlockState neighborState;
        BlockPos neighbor;
        if (pos == null || PistonHelper.mc.player == null || PistonHelper.mc.level == null) {
            return null;
        }
        double bestDist = Double.MAX_VALUE;
        Direction best = null;
        for (Direction dir : Direction.values()) {
            if (filter != null && !filter.test(dir)) continue;
            neighbor = pos.relative(dir);
            neighborState = PistonHelper.mc.level.getBlockState(neighbor);
            if (!PistonHelper.canClick(neighbor, ignoreSneak) || neighborState.canBeReplaced() || !PistonHelper.isGrimDirection(neighbor, dir.getOpposite())) continue;
            clickPoint = Vec3.atCenterOf((Vec3i)pos).add((double)dir.getStepX() * 0.5, (double)dir.getStepY() * 0.5, (double)dir.getStepZ() * 0.5);
            dist = PistonHelper.mc.player.getEyePosition().distanceToSqr(clickPoint);
            if (!(dist < bestDist)) continue;
            bestDist = dist;
            best = dir;
        }
        if (best != null) {
            return best;
        }
        for (Direction dir : Direction.values()) {
            if (filter != null && !filter.test(dir)) continue;
            neighbor = pos.relative(dir);
            neighborState = PistonHelper.mc.level.getBlockState(neighbor);
            if (!PistonHelper.canClick(neighbor, ignoreSneak) || neighborState.canBeReplaced()) continue;
            clickPoint = Vec3.atCenterOf((Vec3i)pos).add((double)dir.getStepX() * 0.5, (double)dir.getStepY() * 0.5, (double)dir.getStepZ() * 0.5);
            dist = PistonHelper.mc.player.getEyePosition().distanceToSqr(clickPoint);
            if (!(dist < bestDist)) continue;
            bestDist = dist;
            best = dir;
        }
        return best;
    }

    public static List<Direction> getPlaceSides(BlockPos pos, Predicate<Direction> filter) {
        return PistonHelper.getPlaceSides(pos, filter, false);
    }

    public static List<Direction> getPlaceSides(BlockPos pos, Predicate<Direction> filter, boolean ignoreSneak) {
        ArrayList<Direction> sides = new ArrayList<Direction>();
        if (pos == null || PistonHelper.mc.level == null) {
            return sides;
        }
        for (Direction dir : Direction.values()) {
            if (filter != null && !filter.test(dir)) continue;
            BlockPos neighbor = pos.relative(dir);
            BlockState neighborState = PistonHelper.mc.level.getBlockState(neighbor);
            if (!PistonHelper.canClick(neighbor, ignoreSneak) || neighborState.canBeReplaced() || !PistonHelper.isGrimDirection(neighbor, dir.getOpposite())) continue;
            sides.add(dir);
        }
        return sides;
    }

    public static boolean canClick(BlockPos pos) {
        return PistonHelper.canClick(pos, false);
    }

    public static boolean canClick(BlockPos pos, boolean ignoreSneak) {
        if (PistonHelper.mc.level == null || PistonHelper.mc.player == null) {
            return false;
        }
        BlockState state = PistonHelper.mc.level.getBlockState(pos);
        boolean needsShift = WorldUtils.isInteractable(state);
        return state.isSolid() && (!needsShift || PistonHelper.mc.player.isShiftKeyDown() || ignoreSneak);
    }

    public static boolean canReplace(BlockPos pos) {
        if (PistonHelper.mc.level == null || pos == null || pos.getY() >= 320 || pos.getY() < -64) {
            return false;
        }
        return PistonHelper.mc.level.getBlockState(pos).canBeReplaced();
    }

    public static boolean canPlace(BlockPos pos) {
        return PistonHelper.canPlace(pos, null, false, false);
    }

    public static boolean canPlace(BlockPos pos, Predicate<Direction> filter) {
        return PistonHelper.canPlace(pos, filter, false, false);
    }

    public static boolean canPlace(BlockPos pos, Predicate<Direction> filter, boolean airPlace) {
        return PistonHelper.canPlace(pos, filter, airPlace, false);
    }

    public static boolean canPlace(BlockPos pos, Predicate<Direction> filter, boolean airPlace, boolean ignoreCrystal) {
        if (!PistonHelper.canReplace(pos)) {
            return false;
        }
        if (PistonHelper.hasEntity(pos, ignoreCrystal)) {
            return false;
        }
        if (airPlace) {
            return true;
        }
        return PistonHelper.getPlaceSide(pos, filter) != null;
    }

    public static boolean canPlaceCrystal(BlockPos pos) {
        return PistonHelper.canPlaceCrystal(pos, false);
    }

    public static boolean canPlaceCrystal(BlockPos pos, boolean airPlace) {
        if (PistonHelper.mc.level == null) {
            return false;
        }
        if (!PistonHelper.mc.level.getBlockState(pos).isAir()) {
            return false;
        }
        BlockPos abovePos = pos.above();
        if (!PistonHelper.mc.level.getBlockState(abovePos).isAir()) {
            return false;
        }
        if (PistonHelper.hasEntityBlockCrystal(pos, false) || PistonHelper.hasEntityBlockCrystal(abovePos, false)) {
            return false;
        }
        BlockPos basePos = pos.below();
        Block base = PistonHelper.mc.level.getBlockState(basePos).getBlock();
        if (base != Blocks.OBSIDIAN && base != Blocks.BEDROCK) {
            return false;
        }
        if (airPlace) {
            return true;
        }
        return PistonHelper.getClickSide(basePos) != null;
    }

    public static boolean canPlaceCrystalIgnoreHeadroom(BlockPos pos, boolean airPlace) {
        if (PistonHelper.mc.level == null) {
            return false;
        }
        if (!PistonHelper.mc.level.getBlockState(pos).isAir()) {
            return false;
        }
        if (PistonHelper.hasEntityBlockCrystal(pos, false)) {
            return false;
        }
        BlockPos basePos = pos.below();
        Block base = PistonHelper.mc.level.getBlockState(basePos).getBlock();
        if (base != Blocks.OBSIDIAN && base != Blocks.BEDROCK) {
            return false;
        }
        if (airPlace) {
            return true;
        }
        return PistonHelper.getClickSide(basePos) != null;
    }

    public static boolean hasEntity(BlockPos pos, boolean ignoreCrystal) {
        if (PistonHelper.mc.level == null) {
            return false;
        }
        AABB box = new AABB(pos).deflate(0.01);
        for (Entity entity : PistonHelper.mc.level.getEntitiesOfClass(Entity.class, box)) {
            if (!entity.isAlive() || entity instanceof ItemEntity || entity instanceof ExperienceOrb || entity instanceof Projectile || ignoreCrystal && entity instanceof EndCrystal) continue;
            return true;
        }
        return false;
    }

    public static boolean hasEntityBlockCrystal(BlockPos pos, boolean ignoreCrystal) {
        if (PistonHelper.mc.level == null) {
            return false;
        }
        AABB box = new AABB(pos);
        for (Entity entity : PistonHelper.mc.level.getEntitiesOfClass(Entity.class, box)) {
            if (!entity.isAlive() || ignoreCrystal && entity instanceof EndCrystal) continue;
            return true;
        }
        return false;
    }

    public static boolean hasCrystal(BlockPos pos) {
        if (PistonHelper.mc.level == null) {
            return false;
        }
        AABB box = new AABB(pos);
        for (EndCrystal crystal : PistonHelper.mc.level.getEntitiesOfClass(EndCrystal.class, box)) {
            if (!crystal.isAlive()) continue;
            return true;
        }
        return false;
    }

    public static boolean hasCrystalAt(BlockPos pos) {
        if (PistonHelper.mc.level == null) {
            return false;
        }
        AABB box = new AABB(pos);
        for (EndCrystal crystal : PistonHelper.mc.level.getEntitiesOfClass(EndCrystal.class, box)) {
            if (!crystal.isAlive() || !crystal.blockPosition().equals((Object)pos)) continue;
            return true;
        }
        return false;
    }

    public static void placeBlockDirect(BlockPos pos, Direction side) {
        if (PistonHelper.mc.player == null || mc.getConnection() == null) {
            return;
        }
        BlockPos neighbor = pos.relative(side);
        Vec3 hitVec = new Vec3((double)neighbor.getX() + 0.5 + (double)side.getOpposite().getStepX() * 0.5, (double)neighbor.getY() + 0.5 + (double)side.getOpposite().getStepY() * 0.5, (double)neighbor.getZ() + 0.5 + (double)side.getOpposite().getStepZ() * 0.5);
        BlockHitResult result = new BlockHitResult(hitVec, side.getOpposite(), neighbor, false);
        NetworkUtils.sendSequencedPacket(seq -> new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, result, seq));
        mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        placeList.add(pos);
    }

    public static void clickBlock(BlockPos pos, Direction face, boolean rotate) {
        if (PistonHelper.mc.player == null || mc.getConnection() == null) {
            return;
        }
        Vec3 hitVec = new Vec3((double)pos.getX() + 0.5 + (double)face.getStepX() * 0.5, (double)pos.getY() + 0.5 + (double)face.getStepY() * 0.5, (double)pos.getZ() + 0.5 + (double)face.getStepZ() * 0.5);
        if (rotate) {
            float[] rots = RotationUtils.getRotations(hitVec);
            Night.ROTATION_MANAGER.silentRotate(rots[0], rots[1]);
        }
        BlockHitResult result = new BlockHitResult(hitVec, face, pos, false);
        NetworkUtils.sendSequencedPacket(seq -> new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, result, seq));
        mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        placeList.add(pos);
    }

    public static void placeBlock(BlockPos pos, Direction side, boolean rotate) {
        PistonHelper.clickBlock(pos.relative(side), side.getOpposite(), rotate);
    }

    public static boolean canSee(BlockPos pos, Direction side) {
        if (PistonHelper.mc.level == null || PistonHelper.mc.player == null) {
            return false;
        }
        Vec3 target = Vec3.atCenterOf((Vec3i)pos).add((double)side.getStepX() * 0.5, (double)side.getStepY() * 0.5, (double)side.getStepZ() * 0.5);
        BlockHitResult result = PistonHelper.mc.level.clip(new ClipContext(PistonHelper.mc.player.getEyePosition(), target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)PistonHelper.mc.player));
        return result == null || result.getType() == HitResult.Type.MISS;
    }

    public static Vec3 getClosestPointToBox(Vec3 pos, AABB box) {
        return new Vec3(Mth.clamp((double)pos.x, (double)box.minX, (double)box.maxX), Mth.clamp((double)pos.y, (double)box.minY, (double)box.maxY), Mth.clamp((double)pos.z, (double)box.minZ, (double)box.maxZ));
    }

    private static AABB getCombinedBox(BlockPos pos) {
        if (PistonHelper.mc.level == null) {
            return new AABB(pos);
        }
        AABB combined = new AABB(pos);
        for (AABB box : PistonHelper.mc.level.getBlockState(pos).getCollisionShape((BlockGetter)PistonHelper.mc.level, pos).toAabbs()) {
            AABB offsetBox = box.move((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
            double minX = Math.max(offsetBox.minX, combined.minX);
            double minY = Math.max(offsetBox.minY, combined.minY);
            double minZ = Math.max(offsetBox.minZ, combined.minZ);
            double maxX = Math.min(offsetBox.maxX, combined.maxX);
            double maxY = Math.min(offsetBox.maxY, combined.maxY);
            double maxZ = Math.min(offsetBox.maxZ, combined.maxZ);
            combined = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        }
        return combined;
    }

    private static boolean isIntersected(AABB a, AABB b) {
        return b.maxX - 1.0E-7 > a.minX && b.minX + 1.0E-7 < a.maxX && b.maxY - 1.0E-7 > a.minY && b.minY + 1.0E-7 < a.maxY && b.maxZ - 1.0E-7 > a.minZ && b.minZ + 1.0E-7 < a.maxZ;
    }
}

