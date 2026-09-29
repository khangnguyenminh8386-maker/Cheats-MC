/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.ShulkerBoxBlock
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package night.utils.minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.modules.impl.movement.HitboxDesyncModule;
import night.utils.IMinecraft;
import night.utils.minecraft.PositionUtils;
import night.utils.minecraft.WorldUtils;

public class HoleUtils
implements IMinecraft {
    private static final Vec3i[] holeOffsets = new Vec3i[]{new Vec3i(0, -1, 0), new Vec3i(1, 0, 0), new Vec3i(-1, 0, 0), new Vec3i(0, 0, 1), new Vec3i(0, 0, -1)};
    private static final Vec3i[] singleOffsets = new Vec3i[]{new Vec3i(-1, 0, 0), new Vec3i(1, 0, 0), new Vec3i(0, 0, -1), new Vec3i(0, 0, 1), new Vec3i(0, -1, 0)};
    private static final Vec3i[] doubleXOffsets = new Vec3i[]{new Vec3i(-1, 0, 0), new Vec3i(0, 0, -1), new Vec3i(0, 0, 1), new Vec3i(0, -1, 0), new Vec3i(2, 0, 0), new Vec3i(1, 0, -1), new Vec3i(1, 0, 1), new Vec3i(1, -1, 0)};
    private static final Vec3i[] doubleZOffsets = new Vec3i[]{new Vec3i(0, 0, -1), new Vec3i(-1, 0, 0), new Vec3i(1, 0, 0), new Vec3i(0, -1, 0), new Vec3i(0, 0, 2), new Vec3i(-1, 0, 1), new Vec3i(1, 0, 1), new Vec3i(0, -1, 1)};
    private static final Vec3i[] quadOffsets = new Vec3i[]{new Vec3i(-1, 0, 0), new Vec3i(0, 0, -1), new Vec3i(0, -1, 0), new Vec3i(2, 0, 0), new Vec3i(1, 0, -1), new Vec3i(1, -1, 0), new Vec3i(-1, 0, 1), new Vec3i(0, 0, 2), new Vec3i(0, -1, 1), new Vec3i(2, 0, 1), new Vec3i(1, 0, 2), new Vec3i(1, -1, 1)};

    public static boolean isPlayerInHole(Player player) {
        return HoleUtils.getFeetPositions(player, true, true, false).stream().noneMatch(position -> HoleUtils.mc.level.getBlockState(position).canBeReplaced());
    }

    public static List<BlockPos> getInsidePositions(Entity targetEntity) {
        LivingEntity living;
        boolean isHorizontal;
        if (targetEntity == null || HoleUtils.mc.level == null) {
            return Collections.emptyList();
        }
        ArrayList<BlockPos> targetPositions = new ArrayList<BlockPos>();
        AABB box = targetEntity.getBoundingBox();
        boolean bl = isHorizontal = targetEntity.isSwimming() || targetEntity.isVisuallySwimming() || targetEntity.isVisuallyCrawling() || targetEntity instanceof LivingEntity && (living = (LivingEntity)targetEntity).isFallFlying() || targetEntity.getPose() == Pose.SWIMMING || targetEntity.getPose() == Pose.FALL_FLYING || box.maxY - box.minY <= 1.0;
        if (isHorizontal) {
            float yaw = targetEntity.getYRot();
            double rad = Math.toRadians(yaw);
            double dirX = -Math.sin(rad);
            double dirZ = Math.cos(rad);
            double posX = targetEntity.getX();
            double posY = targetEntity.getY();
            double posZ = targetEntity.getZ();
            for (double dist = -0.9; dist <= 0.9; dist += 0.2) {
                for (double width = -0.3; width <= 0.3; width += 0.3) {
                    double sx = posX + dirX * dist - dirZ * width;
                    double sz = posZ + dirZ * dist + dirX * width;
                    BlockPos pos = new BlockPos((int)Math.floor(sx), (int)Math.floor(posY), (int)Math.floor(sz));
                    if (targetPositions.contains(pos)) continue;
                    targetPositions.add(pos);
                }
            }
            int minX = (int)Math.floor(box.minX);
            int maxX = (int)Math.ceil(box.maxX);
            int minY = (int)Math.floor(box.minY);
            int maxY = (int)Math.ceil(box.maxY);
            int minZ = (int)Math.floor(box.minZ);
            int maxZ = (int)Math.ceil(box.maxZ);
            for (int x = minX; x < maxX; ++x) {
                for (int y = minY; y < maxY; ++y) {
                    for (int z = minZ; z < maxZ; ++z) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (targetPositions.contains(pos)) continue;
                        targetPositions.add(pos);
                    }
                }
            }
        } else {
            int minX = (int)Math.floor(box.minX);
            int maxX = (int)Math.ceil(box.maxX);
            int minY = (int)Math.floor(box.minY);
            int minZ = (int)Math.floor(box.minZ);
            int maxZ = (int)Math.ceil(box.maxZ);
            int scanMaxY = (int)Math.ceil(box.maxY);
            for (int x = minX; x < maxX; ++x) {
                for (int y = minY; y < scanMaxY; ++y) {
                    for (int z = minZ; z < maxZ; ++z) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (targetPositions.contains(pos)) continue;
                        targetPositions.add(pos);
                    }
                }
            }
        }
        if (targetPositions.isEmpty()) {
            targetPositions.add(PositionUtils.getFlooredPosition(targetEntity));
        }
        return targetPositions;
    }

    public static BlockPos getTargetFeetPosition(Player target) {
        if (target == null || HoleUtils.mc.level == null) {
            return BlockPos.ZERO;
        }
        BlockPos floored = PositionUtils.getFlooredPosition((Entity)target);
        if (HoleUtils.mc.level.getBlockState(floored.below()).canBeReplaced()) {
            for (int dy = 2; dy <= 3; ++dy) {
                BlockPos below = floored.below(dy);
                if (HoleUtils.mc.level.getBlockState(below).canBeReplaced()) continue;
                return below.above();
            }
        }
        return floored;
    }

    public static HashSet<BlockPos> getFeetPositions(Player target, boolean extension, boolean floor, boolean targetOnly) {
        return HoleUtils.getFeetPositions(target, extension, floor, floor, targetOnly);
    }

   public static HashSet<BlockPos> getFeetPositions(Player target, boolean extension, boolean floor, boolean underFeet, boolean targetOnly) {
      HashSet<BlockPos> positions = new HashSet<>();
      HashSet<BlockPos> blacklist = new HashSet<>();
      HitboxDesyncModule hitboxDesyncModule = Night.MODULE_MANAGER.getModule(HitboxDesyncModule.class);
      AABB bounds = target.getBoundingBox();
      double fracY = bounds.minY - Math.floor(bounds.minY);
      int feetY = (int)(fracY > 0.8 ? Math.floor(bounds.minY) + 1.0 : Math.floor(bounds.minY));
      int minX = (int)Math.floor(bounds.minX);
      int maxX = (int)Math.floor(bounds.maxX);
      int minZ = (int)Math.floor(bounds.minZ);
      int maxZ = (int)Math.floor(bounds.maxZ);
      BlockPos centerPos = new BlockPos((int)Math.floor(target.getX()), feetY, (int)Math.floor(target.getZ()));
      if (mc.level != null && !target.onGround() && mc.level.getBlockState(centerPos.below()).canBeReplaced()) {
         for (int dy = 2; dy <= 3; dy++) {
            BlockPos below = centerPos.below(dy);
            if (!mc.level.getBlockState(below).canBeReplaced()) {
               feetY = below.above().getY();
               break;
            }
         }
      }

      BlockPos floored = PositionUtils.getFlooredPosition(target);
      blacklist.add(new BlockPos(floored.getX(), feetY, floored.getZ()));

      for (int x = minX; x <= maxX; x++) {
         for (int z = minZ; z <= maxZ; z++) {
            if (bounds.intersects(new AABB(x, feetY, z, x + 1, feetY + 1, z + 1))) {
               blacklist.add(new BlockPos(x, feetY, z));
            }
         }
      }

      if (extension && mc.level != null) {
         for (BlockPos cell : Set.copyOf(blacklist)) {
            for (Direction dir : Direction.values()) {
               if (!dir.getAxis().isVertical()) {
                  BlockPos off = cell.relative(dir);
                  if (mc.level.getBlockState(off).canBeReplaced()) {
                     List<Player> collisions = WorldUtils.getCollisions(off);
                     if (!collisions.isEmpty()) {
                        for (Player player : collisions) {
                           if (player != target
                              && (player != mc.player || target != mc.player)
                              && (!targetOnly || player == target)
                              && (target != mc.player || Night.FRIEND_MANAGER.contains(player.getName().getString()))
                              && !(Math.abs(player.getY() - target.getY()) > 1.0)) {
                              AABB pBox = player.getBoundingBox();
                              int pMinX = (int)Math.floor(pBox.minX + 0.001);
                              int pMaxX = (int)Math.floor(pBox.maxX - 0.001);
                              int pMinZ = (int)Math.floor(pBox.minZ + 0.001);
                              int pMaxZ = (int)Math.floor(pBox.maxZ - 0.001);

                              for (int px = pMinX; px <= pMaxX; px++) {
                                 for (int pz = pMinZ; pz <= pMaxZ; pz++) {
                                    blacklist.add(new BlockPos(px, feetY, pz));
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      for (BlockPos pos : blacklist) {
         if (floor && underFeet) {
            positions.add(pos.below());
         }

         for (Direction dir : Direction.values()) {
            if (dir.getAxis().isHorizontal()) {
               BlockPos off = pos.relative(dir);
               if (!blacklist.contains(off)) {
                  positions.add(off);
                  if (floor && mc.level != null && mc.level.getBlockState(off).canBeReplaced()) {
                     positions.add(off.below());
                  }
               }
            }
         }
      }

      int targetFeetY = feetY;
      positions.removeIf(p -> p.getY() == targetFeetY && target.getBoundingBox().intersects(new AABB(p)));
      if (target == mc.player && hitboxDesyncModule.isToggled() && hitboxDesyncModule.close.getValue()) {
         List<BlockPos> desyncPositions = new ArrayList<>();
         Vec3 vec3d = Vec3.atCenterOf(mc.player.blockPosition());
         boolean flagX = vec3d.x - mc.player.getX() > 0.0;
         boolean flagZ = vec3d.z - mc.player.getZ() > 0.0;
         if (flagX && flagZ) {
            desyncPositions.add(new BlockPos(mc.player.blockPosition().offset(-1, 0, 0)));
            desyncPositions.add(new BlockPos(mc.player.blockPosition().offset(0, 0, -1)));
         }

         if (!flagX && flagZ) {
            desyncPositions.add(new BlockPos(mc.player.blockPosition().offset(1, 0, 0)));
            desyncPositions.add(new BlockPos(mc.player.blockPosition().offset(0, 0, -1)));
         }

         if (flagX && !flagZ) {
            desyncPositions.add(new BlockPos(mc.player.blockPosition().offset(-1, 0, 0)));
            desyncPositions.add(new BlockPos(mc.player.blockPosition().offset(0, 0, 1)));
         }

         if (!flagX && !flagZ) {
            desyncPositions.add(new BlockPos(mc.player.blockPosition().offset(1, 0, 0)));
            desyncPositions.add(new BlockPos(mc.player.blockPosition().offset(0, 0, 1)));
         }

         positions.removeIf(desyncPositions::contains);
      }

      return positions;
   }

    public static List<BlockPos> getTrapPositions(Player player, boolean partial, boolean head, boolean antiStep, boolean antiBomb, boolean strictDirection) {
        return HoleUtils.getTrapPositions(player, partial, head, antiStep, antiBomb, strictDirection, false, false);
    }

    public static List<BlockPos> getTrapPositions(Player player, boolean partial, boolean head, boolean antiStep, boolean antiBomb, boolean strictDirection, boolean airPlace) {
        return HoleUtils.getTrapPositions(player, null, partial, head, antiStep, antiBomb, strictDirection, false, airPlace);
    }

    public static List<BlockPos> getTrapPositions(Player player, BlockPos basePos, boolean partial, boolean head, boolean antiStep, boolean antiBomb, boolean strictDirection, boolean airPlace) {
        return HoleUtils.getTrapPositions(player, basePos, partial, head, antiStep, antiBomb, strictDirection, false, airPlace);
    }

    public static List<BlockPos> getTrapPositions(Player player, boolean partial, boolean head, boolean antiStep, boolean antiBomb, boolean strictDirection, boolean feet, boolean airPlace) {
        return HoleUtils.getTrapPositions(player, null, partial, head, antiStep, antiBomb, strictDirection, feet, airPlace);
    }

   public static List<BlockPos> getTrapPositions(
      Player player,
      BlockPos basePos,
      boolean partial,
      boolean head,
      boolean antiStep,
      boolean antiBomb,
      boolean strictDirection,
      boolean feet,
      boolean airPlace
   ) {
      LinkedHashSet<BlockPos> positions = new LinkedHashSet<>();
      HashSet<BlockPos> occupiedCells;
      if (basePos != null && !player.onGround()) {
         occupiedCells = new HashSet<>();
         occupiedCells.add(basePos);
      } else {
         occupiedCells = getOccupiedFloorCells(player);
      }

      for (BlockPos position : occupiedCells) {
         Direction wallDirection = null;

         for (Direction dir : Direction.values()) {
            if (dir.getAxis().isHorizontal() && !occupiedCells.contains(position.relative(dir))) {
               if (wallDirection == null) {
                  wallDirection = dir;
               }

               if (WorldUtils.getDirection(position.relative(dir).above(), strictDirection) != null) {
                  wallDirection = dir;
                  break;
               }
            }
         }

         if (wallDirection == null) {
            wallDirection = Direction.EAST;
         }

         if (antiStep) {
            addWallOffset(positions, occupiedCells, position, new Vec3i(1, 2, 0));
            addWallOffset(positions, occupiedCells, position, new Vec3i(-1, 2, 0));
            addWallOffset(positions, occupiedCells, position, new Vec3i(0, 2, 1));
            addWallOffset(positions, occupiedCells, position, new Vec3i(0, 2, -1));
         }

         if (antiBomb) {
            positions.add(position.offset(0, 3, 0));
         }

         if (feet) {
            for (Direction dir : Direction.values()) {
               if (dir.getAxis().isHorizontal() && !occupiedCells.contains(position.relative(dir))) {
                  positions.add(position.relative(dir));
               }
            }
         }

         if (partial) {
            BlockPos headPosition = position.offset(0, 2, 0);
            if (mc.level != null && !mc.level.getBlockState(headPosition).canBeReplaced()) {
               positions.add(headPosition);
            } else if (!airPlace && WorldUtils.getDirection(headPosition, strictDirection) == null) {
               positions.add(position.relative(wallDirection).above());
               positions.add(position.relative(wallDirection).above(2));
               positions.add(headPosition);
            } else {
               positions.add(headPosition);
            }
         } else {
            for (Direction dir : Direction.values()) {
               if (dir.getAxis().isHorizontal() && !occupiedCells.contains(position.relative(dir))) {
                  positions.add(position.relative(dir).above());
               }
            }

            if (head) {
               BlockPos headPosition = position.offset(0, 2, 0);
               if (!airPlace && (mc.level == null || mc.level.getBlockState(headPosition).canBeReplaced())) {
                  positions.add(position.relative(wallDirection).above(2));
               }

               positions.add(headPosition);
            }
         }
      }

      return new ArrayList<>(positions);
   }

    private static void addWallOffset(Set<BlockPos> positions, HashSet<BlockPos> occupiedCells, BlockPos base, Vec3i vec3i) {
        BlockPos floorBelow;
        if ((vec3i.getX() != 0 || vec3i.getZ() != 0) && occupiedCells.contains(floorBelow = base.offset(vec3i.getX(), 0, vec3i.getZ()))) {
            return;
        }
        positions.add(base.offset(vec3i));
    }

    private static HashSet<BlockPos> getOccupiedFloorCells(Player target) {
        HashSet<BlockPos> cells = new HashSet<BlockPos>();
        BlockPos feetPos = PositionUtils.getFlooredPosition((Entity)target);
        cells.add(feetPos);
        for (Direction dir : Direction.values()) {
            if (dir.getAxis().isVertical()) continue;
            AABB box = target.getBoundingBox();
            BlockPos off = feetPos.relative(dir);
            if (!(box.minX < (double)(off.getX() + 1) && box.maxX > (double)off.getX() && box.minZ < (double)(off.getZ() + 1) && box.maxZ > (double)off.getZ())) continue;
            int x = (int)Math.floor(box.minX);
            while ((double)x < Math.ceil(box.maxX)) {
                int z = (int)Math.floor(box.minZ);
                while ((double)z < Math.ceil(box.maxZ)) {
                    cells.add(new BlockPos(x, feetPos.getY(), z));
                    ++z;
                }
                ++x;
            }
        }
        return cells;
    }

    private static HoleSafety classifyWalls(BlockPos position, Vec3i[] offsets) {
        boolean allBedrock = true;
        boolean allBlastProof = true;
        for (Vec3i offset : offsets) {
            boolean otherBlastProof;
            BlockPos pos = position.offset(offset);
            Block block = HoleUtils.mc.level.getBlockState(pos).getBlock();
            boolean bedrock = block.equals(Blocks.BEDROCK);
            boolean bl = otherBlastProof = block.equals(Blocks.OBSIDIAN) || block.equals(Blocks.RESPAWN_ANCHOR) || block.equals(Blocks.ENDER_CHEST);
            if (!bedrock && !otherBlastProof) {
                allBlastProof = false;
                if (block instanceof ShulkerBoxBlock || !HoleUtils.mc.level.getBlockState(pos).getCollisionShape((BlockGetter)HoleUtils.mc.level, pos).isEmpty()) continue;
                return null;
            }
            if (bedrock) continue;
            allBedrock = false;
        }
        if (!allBlastProof) {
            return HoleSafety.NONE;
        }
        return allBedrock ? HoleSafety.SAFE : HoleSafety.UNSAFE;
    }

    public static Hole getSingleHole(BlockPos position, double height) {
        return HoleUtils.getSingleHole(position, height, true);
    }

    public static Hole getSingleHole(BlockPos position, double height, boolean reachable) {
        if (!HoleUtils.mc.level.getBlockState(position).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(position.above()).getBlock().equals(Blocks.AIR) && reachable) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(position.above().above()).getBlock().equals(Blocks.AIR) && reachable) {
            return null;
        }
        HoleSafety safety = HoleUtils.classifyWalls(position, singleOffsets);
        if (safety == null) {
            return null;
        }
        return new Hole(new AABB((double)position.getX(), (double)position.getY(), (double)position.getZ(), (double)(position.getX() + 1), (double)position.getY() + height, (double)(position.getZ() + 1)), HoleType.SINGLE, safety);
    }

    public static Hole getDoubleHole(BlockPos position, double height) {
        HoleSafety s;
        boolean z;
        if (!HoleUtils.mc.level.getBlockState(position).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(position.above()).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(position.above().above()).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        boolean x = HoleUtils.mc.level.getBlockState(position.offset(new Vec3i(1, 0, 0))).getBlock().equals(Blocks.AIR) && HoleUtils.mc.level.getBlockState(position.offset(new Vec3i(1, 0, 0)).above()).getBlock().equals(Blocks.AIR) && HoleUtils.mc.level.getBlockState(position.offset(new Vec3i(1, 0, 0)).above().above()).getBlock().equals(Blocks.AIR);
        boolean bl = z = HoleUtils.mc.level.getBlockState(position.offset(new Vec3i(0, 0, 1))).getBlock().equals(Blocks.AIR) && HoleUtils.mc.level.getBlockState(position.offset(new Vec3i(0, 0, 1)).above()).getBlock().equals(Blocks.AIR) && HoleUtils.mc.level.getBlockState(position.offset(new Vec3i(0, 0, 1)).above().above()).getBlock().equals(Blocks.AIR);
        if (!x && !z) {
            return null;
        }
        AABB box = null;
        HoleSafety safety = null;
        if (x && (s = HoleUtils.classifyWalls(position, doubleXOffsets)) != null) {
            box = new AABB((double)position.getX(), (double)position.getY(), (double)position.getZ(), (double)(position.getX() + 2), (double)position.getY() + height, (double)(position.getZ() + 1));
            safety = s;
        }
        if (z && box == null && (s = HoleUtils.classifyWalls(position, doubleZOffsets)) != null) {
            box = new AABB((double)position.getX(), (double)position.getY(), (double)position.getZ(), (double)(position.getX() + 1), (double)position.getY() + height, (double)(position.getZ() + 2));
            safety = s;
        }
        if (box == null) {
            return null;
        }
        return new Hole(box, HoleType.DOUBLE, safety);
    }

    public static Hole getQuadHole(BlockPos position, double height) {
        if (!HoleUtils.mc.level.getBlockState(position).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(position.above()).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(position.offset(new Vec3i(1, 0, 0))).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(position.offset(new Vec3i(0, 0, 1))).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(position.offset(new Vec3i(1, 0, 1))).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(position.above().above()).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(position.offset(new Vec3i(1, 0, 0)).above()).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(position.offset(new Vec3i(1, 0, 0)).above().above()).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(position.offset(new Vec3i(0, 0, 1)).above()).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(position.offset(new Vec3i(0, 0, 1)).above().above()).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(position.offset(new Vec3i(1, 0, 1)).above()).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(position.offset(new Vec3i(1, 0, 1)).above().above()).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        HoleSafety safety = HoleUtils.classifyWalls(position, quadOffsets);
        if (safety == null) {
            return null;
        }
        return new Hole(new AABB((double)position.getX(), (double)position.getY(), (double)position.getZ(), (double)(position.getX() + 2), (double)position.getY() + height, (double)(position.getZ() + 2)), HoleType.QUAD, safety);
    }

    public static Hole getPartialHole(BlockPos position, double height) {
        if (HoleUtils.mc.level == null) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(position).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(position.above()).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(position.above().above()).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (HoleUtils.getSingleHole(position, height) != null) {
            return null;
        }
        if (HoleUtils.getDoubleHole(position, height) != null) {
            return null;
        }
        if (HoleUtils.getQuadHole(position, height) != null) {
            return null;
        }
        if (HoleUtils.mc.level.getBlockState(position.below()).canBeReplaced()) {
            return null;
        }
        int solidSides = 0;
        BlockPos openSide = null;
        for (Direction dir : Direction.values()) {
            if (!dir.getAxis().isHorizontal()) continue;
            BlockPos neighbor = position.relative(dir);
            if (!HoleUtils.mc.level.getBlockState(neighbor).canBeReplaced() && !HoleUtils.mc.level.getBlockState(neighbor).getCollisionShape((BlockGetter)HoleUtils.mc.level, neighbor).isEmpty()) {
                ++solidSides;
                continue;
            }
            openSide = neighbor;
        }
        if (solidSides != 3) {
            return null;
        }
        if (openSide == null || HoleUtils.mc.level.getBlockState(openSide.below()).canBeReplaced()) {
            return null;
        }
        return new Hole(new AABB((double)position.getX(), (double)position.getY(), (double)position.getZ(), (double)(position.getX() + 1), (double)position.getY() + height, (double)(position.getZ() + 1)), HoleType.PARTIAL, HoleSafety.PARTIAL);
    }

    public static enum HoleSafety {
        SAFE,
        UNSAFE,
        PARTIAL,
        NONE;

    }

    public record Hole(AABB box, HoleType type, HoleSafety safety) {
    }

    public static enum HoleType {
        SINGLE,
        DOUBLE,
        QUAD,
        PARTIAL;

    }
}

