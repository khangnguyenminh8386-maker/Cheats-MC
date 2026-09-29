/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.combat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import night.utils.IMinecraft;

public class CrystalPlacementHelper
implements IMinecraft {
    public static PlacementResult getVisiblePlacement(BlockPos position) {
        if (CrystalPlacementHelper.mc.player == null || CrystalPlacementHelper.mc.level == null) {
            return new PlacementResult(Direction.UP, Vec3.atCenterOf((Vec3i)position).add(0.0, 0.5, 0.0), false);
        }
        Vec3 eye = CrystalPlacementHelper.mc.player.getEyePosition();
        Direction[] faces = new Direction[]{Direction.UP, Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST, Direction.DOWN};
        PlacementResult bestResult = null;
        double bestDistSq = Double.MAX_VALUE;
        double[][] directions = new double[][]{{0.0, 0.5}, {1.0, 0.5}, {0.5, 0.0}, {0.5, 1.0}, {0.0, 0.0}, {1.0, 0.0}, {0.0, 1.0}, {1.0, 1.0}};
        int maxIter = 10;
        for (Direction face : faces) {
            BlockPos adjacent = position.relative(face);
            if (face != Direction.UP && !CrystalPlacementHelper.mc.level.getBlockState(adjacent).canBeReplaced() && CrystalPlacementHelper.mc.level.getBlockState(adjacent).isSolidRender()) continue;
            PlacementResult centerHit = CrystalPlacementHelper.testFacePoint(position, face, eye, 0.5, 0.5);
            if (centerHit != null) {
                double distSq = eye.distanceToSqr(centerHit.hitVec);
                if (!(distSq < bestDistSq)) continue;
                bestDistSq = distSq;
                bestResult = centerHit;
                continue;
            }
            block1: for (double[] dir : directions) {
                for (int iter = 1; iter <= maxIter; ++iter) {
                    double t = 1.0 - 1.0 / (double)(1 << iter);
                    double ox = 0.5 + (dir[0] - 0.5) * t;
                    double oy = 0.5 + (dir[1] - 0.5) * t;
                    PlacementResult hit = CrystalPlacementHelper.testFacePoint(position, face, eye, ox, oy);
                    if (hit == null) continue;
                    double distSq = eye.distanceToSqr(hit.hitVec);
                    if (!(distSq < bestDistSq)) continue block1;
                    bestDistSq = distSq;
                    bestResult = hit;
                    continue block1;
                }
            }
        }
        if (bestResult != null) {
            return bestResult;
        }
        Direction closestExposed = null;
        double minDistance = Double.MAX_VALUE;
        for (Direction face : faces) {
            Vec3 hitVec;
            double dist;
            BlockPos adjacent = position.relative(face);
            BlockState state = CrystalPlacementHelper.mc.level.getBlockState(adjacent);
            if (!state.canBeReplaced() || !((dist = eye.distanceToSqr(hitVec = CrystalPlacementHelper.getSurfacePoint(position, face, 0.5, 0.5))) < minDistance)) continue;
            minDistance = dist;
            closestExposed = face;
        }
        if (closestExposed != null) {
            return new PlacementResult(closestExposed, CrystalPlacementHelper.getSurfacePoint(position, closestExposed, 0.5, 0.5), false);
        }
        return new PlacementResult(Direction.UP, Vec3.atCenterOf((Vec3i)position).add(0.0, 0.5, 0.0), false);
    }

    private static PlacementResult testFacePoint(BlockPos position, Direction face, Vec3 eye, double ox, double oy) {
        Vec3 point = CrystalPlacementHelper.getPointOnFace(position, face, ox, oy);
        BlockHitResult result = CrystalPlacementHelper.mc.level.clip(new ClipContext(eye, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)CrystalPlacementHelper.mc.player));
        if (result == null) {
            return null;
        }
        if (result.getType() == HitResult.Type.MISS) {
            return new PlacementResult(face, CrystalPlacementHelper.getSurfacePoint(position, face, ox, oy), true);
        }
        if (result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals((Object)position)) {
            return new PlacementResult(result.getDirection(), result.getLocation(), true);
        }
        return null;
    }

    public static boolean isPlacementVisible(BlockPos position) {
        if (CrystalPlacementHelper.mc.player == null || CrystalPlacementHelper.mc.level == null) {
            return false;
        }
        PlacementResult result = CrystalPlacementHelper.getVisiblePlacement(position);
        return result != null && result.visible;
    }

    public static Vec3 getPointOnFace(BlockPos pos, Direction face, double off1, double off2) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        switch (face) {
            case UP: {
                return new Vec3(x + off1, y + 0.999, z + off2);
            }
            case DOWN: {
                return new Vec3(x + off1, y + 0.001, z + off2);
            }
            case SOUTH: {
                return new Vec3(x + off1, y + off2, z + 0.999);
            }
            case NORTH: {
                return new Vec3(x + off1, y + off2, z + 0.001);
            }
            case EAST: {
                return new Vec3(x + 0.999, y + off1, z + off2);
            }
            case WEST: {
                return new Vec3(x + 0.001, y + off1, z + off2);
            }
        }
        return new Vec3(x + 0.5, y + 0.5, z + 0.5);
    }

    public static Vec3 getSurfacePoint(BlockPos pos, Direction face, double off1, double off2) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        switch (face) {
            case UP: {
                return new Vec3(x + off1, y + 1.0, z + off2);
            }
            case DOWN: {
                return new Vec3(x + off1, y + 0.0, z + off2);
            }
            case SOUTH: {
                return new Vec3(x + off1, y + off2, z + 1.0);
            }
            case NORTH: {
                return new Vec3(x + off1, y + off2, z + 0.0);
            }
            case EAST: {
                return new Vec3(x + 1.0, y + off1, z + off2);
            }
            case WEST: {
                return new Vec3(x + 0.0, y + off1, z + off2);
            }
        }
        return new Vec3(x + 0.5, y + 0.5, z + 0.5);
    }

    public static class PlacementResult {
        public Direction direction;
        public Vec3 hitVec;
        public boolean visible;

        public PlacementResult(Direction direction, Vec3 hitVec) {
            this(direction, hitVec, true);
        }

        public PlacementResult(Direction direction, Vec3 hitVec, boolean visible) {
            this.direction = direction;
            this.hitVec = hitVec;
            this.visible = visible;
        }
    }
}

