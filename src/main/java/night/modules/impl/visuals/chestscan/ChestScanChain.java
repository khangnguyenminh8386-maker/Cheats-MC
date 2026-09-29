/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.ChestBlock
 *  net.minecraft.world.level.block.HopperBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 */
package night.modules.impl.visuals.chestscan;
import java.util.Deque;
import java.util.Map.Entry;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import night.modules.impl.visuals.chestscan.ChestScanStore;

public final class ChestScanChain {
    private ChestScanChain() {
    }

    public static Map<BlockPos, BlockPos> findEdges(Level level, Set<BlockPos> trackedChests, BlockPos center, int radiusBlocks) {
        HashMap<BlockPos, BlockPos> edges = new HashMap<BlockPos, BlockPos>();
        double radiusSq = (double)radiusBlocks * (double)radiusBlocks;
        ArrayDeque<BlockPos> frontier = new ArrayDeque<BlockPos>(trackedChests);
        HashSet<BlockPos> visited = new HashSet<BlockPos>(trackedChests);
        while (!frontier.isEmpty()) {
            Direction facing;
            BlockPos dest;
            BlockPos chestPos = (BlockPos)frontier.poll();
            if (chestPos.distSqr((Vec3i)center) > radiusSq) continue;
            BlockPos belowPos = chestPos.below();
            BlockState belowState = level.getBlockState(belowPos);
            if (belowState.getBlock() instanceof HopperBlock && ChestScanChain.isChest(level.getBlockState(dest = belowPos.relative(facing = (Direction)belowState.getValue((Property)HopperBlock.FACING))))) {
                edges.put(chestPos, dest);
                if (visited.add(dest)) {
                    frontier.add(dest);
                }
            }
            for (Direction dir : Direction.values()) {
                BlockPos sourcePos;
                BlockPos hopperPos = chestPos.relative(dir);
                BlockState hopperState = level.getBlockState(hopperPos);
                if (!(hopperState.getBlock() instanceof HopperBlock) || hopperState.getValue((Property)HopperBlock.FACING) != dir.getOpposite() || !ChestScanChain.isChest(level.getBlockState(sourcePos = hopperPos.above()))) continue;
                edges.put(sourcePos, chestPos);
                if (!visited.add(sourcePos)) continue;
                frontier.add(sourcePos);
            }
        }
        return edges;
    }

    private static boolean isChest(BlockState state) {
        return state.getBlock() instanceof ChestBlock;
    }

   public static Set<BlockPos> inferEmpty(Map<BlockPos, BlockPos> edges, Map<BlockPos, ChestScanStore.ChestStatus> realStatuses) {
      Map<BlockPos, List<BlockPos>> reverse = new HashMap<>();

      for (Entry<BlockPos, BlockPos> e : edges.entrySet()) {
         reverse.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(e.getKey());
      }

      Set<BlockPos> inferred = new HashSet<>();
      Set<BlockPos> visited = new HashSet<>();

      for (Entry<BlockPos, ChestScanStore.ChestStatus> e : realStatuses.entrySet()) {
         if (e.getValue() != ChestScanStore.ChestStatus.FULL) {
            BlockPos start = e.getKey();
            if (visited.add(start)) {
               Deque<BlockPos> queue = new ArrayDeque<>();
               queue.add(start);

               while (!queue.isEmpty()) {
                  BlockPos cur = queue.poll();

                  for (BlockPos parent : reverse.getOrDefault(cur, List.of())) {
                     if (visited.add(parent)) {
                        if (!realStatuses.containsKey(parent)) {
                           inferred.add(parent);
                        }

                        queue.add(parent);
                     }
                  }
               }
            }
         }
      }

      return inferred;
   }
}

