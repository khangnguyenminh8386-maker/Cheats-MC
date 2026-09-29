/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Plane
 *  net.minecraft.core.Position
 *  net.minecraft.core.Vec3i
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.combat;
import java.util.Set;
import net.minecraft.core.Direction.Plane;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.ClientDisconnectEvent;
import night.events.impl.PlayerUpdateEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.combat.AutoCrystalModule;
import night.modules.impl.player.SpeedMineModule;
import night.pingbypass.PingBypassFlags;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.utils.minecraft.DamageUtils;
import night.utils.minecraft.EntityUtils;
import night.utils.minecraft.HoleUtils;
import night.utils.minecraft.InventoryUtils;
import night.utils.minecraft.PositionUtils;
import night.utils.minecraft.WorldUtils;

@RegisterModule(name="AutoMine", description="Manages which blocks SpeedMine should mine to defeat your opponents.", category=Module.Category.COMBAT)
public class AutoMineModule
extends Module {
    public ModeSetting priority = new ModeSetting("Priority", "Which area of the target to prioritize mining.", "Leg", new String[]{"Leg", "Head"});
    public ModeSetting logic = new ModeSetting("Logic", "Auto mining targeting logic.", "Grim", new String[]{"Grim", "NCP"});
    public BooleanSetting raytrace = new BooleanSetting("Raytrace", "Only mines blocks that are in line of sight (visible).", false);
    public BooleanSetting upFallback = new BooleanSetting("CivBreak", "Priority=Leg only: when Raytrace can't see any leg-level surround block, mines the block above it instead. Turn on AutoCrystal's own MineIgnore setting for the crystal pre-place/detonate combo -- this module only builds+mines the obsidian.", new BooleanSetting.Visibility(this.raytrace, true), false);
    public ModeSetting upFallbackRotate = new ModeSetting("Rotate", "The rotation mode when placing the Up-fallback obsidian.", new BooleanSetting.Visibility(this.upFallback, true), "Grim", new String[]{"None", "Normal", "Grim"});
    public BooleanSetting ignoreNaked = new BooleanSetting("IgnoreNaked", "Ignore Naked, even that player wears elytra only", false);
    public BooleanSetting avoidSharing = new BooleanSetting("AvoidSharing", "Avoids mining blocks that are part of your own surround or safety ring.", true);
    public BooleanSetting terrain = new BooleanSetting("Terrain", "Automatically mines terrain blocks to place obsidian when target has no crystal base or is fluid-covered.", false);
    public BooleanSetting terrainPlace = new BooleanSetting("TerrainPlace", "Automatically places obsidian once the terrain block is broken.", new BooleanSetting.Visibility(this.terrain, true), true);
    public BooleanSetting ironPickaxe = new BooleanSetting("IronPickaxe", "Uses iron pickaxe after obsidian matures to prevent obsidian item drops.", false);
    public BooleanSetting antiCrawl = new BooleanSetting("AntiCrawl", "While crawling, mines the block above/below your feet to stand back up instead of staying trapped.", true);
    public BooleanSetting face = new BooleanSetting("Face", "Mines the block stuck in your face to rescue you from face traps or phased blocks.", true);
    private final List<BlockPos> pendingTerrainPlacements = new ArrayList<BlockPos>();
    private TerrainPair activeTerrainPair = null;
    private UUID activeTerrainTarget = null;
    private BlockPos upComboPos = null;

    public boolean isRaytrace() {
        return this.raytrace.getValue();
    }

    public boolean isUpComboActive(BlockPos pos) {
        return this.upFallback.getValue() && this.upComboPos != null && this.upComboPos.equals((Object)pos);
    }

    private void resetUpCombo() {
        this.upComboPos = null;
    }

    private SpeedMineModule speedMine() {
        return Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientDisconnectEvent event) {
        this.pendingTerrainPlacements.clear();
        this.activeTerrainPair = null;
        this.activeTerrainTarget = null;
        this.resetUpCombo();
    }

    @Override
    public void onEnable() {
        this.pendingTerrainPlacements.clear();
        this.activeTerrainPair = null;
        this.activeTerrainTarget = null;
        this.resetUpCombo();
    }

    @Override
    public void onDisable() {
        this.pendingTerrainPlacements.clear();
        this.activeTerrainPair = null;
        this.activeTerrainTarget = null;
        this.resetUpCombo();
        SpeedMineModule speedMine = this.speedMine();
        if (speedMine != null) {
            speedMine.setDoubleEngaged(false);
            speedMine.restoreIronSwap();
        }
    }

    private boolean isRealTerrainBase(BlockPos pos) {
        if (pos == null || AutoMineModule.mc.level == null) {
            return false;
        }
        Block block = AutoMineModule.mc.level.getBlockState(pos).getBlock();
        return block == Blocks.OBSIDIAN || block == Blocks.BEDROCK;
    }

    private boolean isMinableLegCandidate(BlockPos pos, SpeedMineModule speedMine) {
        BlockState state = AutoMineModule.mc.level.getBlockState(pos);
        if (state.canBeReplaced()) {
            return false;
        }
        return !(state.getBlock().defaultDestroyTime() < 0.0f) || speedMine.sixB.getValue() && state.getBlock() == Blocks.BEDROCK;
    }

    private boolean isPhased(Player target) {
        return EntityUtils.isPhased((Entity)target);
    }

    private boolean isTargetPhasedInBedrock(Player target) {
        if (target == null || AutoMineModule.mc.level == null) {
            return false;
        }
        for (BlockPos pos : EntityUtils.getPhasedBlocks((Entity)target)) {
            if (!AutoMineModule.mc.level.getBlockState(pos).is(Blocks.BEDROCK)) continue;
            return true;
        }
        return false;
    }

    private BlockPos getGrimBedrockHeadTarget(Player target, SpeedMineModule speedMine) {
        BlockPos eyeTop;
        if (target == null || AutoMineModule.mc.level == null) {
            return null;
        }
        if (!this.logic.getValue().equalsIgnoreCase("Grim")) {
            return null;
        }
        if (!this.isTargetPhasedInBedrock(target)) {
            return null;
        }
        int targetFeetY = Mth.floor((double)target.getY());
        int headY = target.isVisuallyCrawling() || target.getBoundingBox().getYsize() <= 1.0 ? targetFeetY + 1 : targetFeetY + 2;
        SpeedMineModule.Action camping = speedMine.getPrimary();
        if (camping != null && camping.isInstantMine() && camping.getPosition().getY() == headY && speedMine.isTargetSurroundPosition(camping.getPosition(), target)) {
            return camping.getPosition();
        }
        ArrayList<BlockPos> candidates = new ArrayList<BlockPos>();
        for (BlockPos insidePos : HoleUtils.getInsidePositions((Entity)target)) {
            BlockPos headPos = new BlockPos(insidePos.getX(), headY, insidePos.getZ());
            if (candidates.contains(headPos)) continue;
            candidates.add(headPos);
        }
        BlockPos directHead = target.blockPosition().above(headY - targetFeetY);
        if (!candidates.contains(directHead)) {
            candidates.add(directHead);
        }
        if (!candidates.contains(eyeTop = BlockPos.containing((double)target.getX(), (double)(target.getBoundingBox().maxY + 0.2), (double)target.getZ()))) {
            candidates.add(eyeTop);
        }
        candidates.sort(Comparator.comparingDouble(pos -> {
            double dist = AutoMineModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)pos));
            if (this.isRaytrace() && WorldUtils.canSeeBlock(pos)) {
                dist -= 1000.0;
            }
            return dist;
        }));
        for (BlockPos pos2 : candidates) {
            BlockState state;
            if (speedMine.isOutOfRange(pos2) || this.isRaytrace() && !WorldUtils.canSeeBlock(pos2) || (state = AutoMineModule.mc.level.getBlockState(pos2)).canBeReplaced() || !WorldUtils.canBreak(pos2) || state.is(Blocks.COBWEB) || !speedMine.isValid(pos2)) continue;
            return pos2;
        }
        return null;
    }

    private BlockPos getSelfFaceBlock() {
        if (AutoMineModule.mc.player == null || AutoMineModule.mc.level == null) {
            return null;
        }
        if (AutoMineModule.mc.player.isVisuallyCrawling()) {
            return null;
        }
        BlockPos eyePos = BlockPos.containing((Position)AutoMineModule.mc.player.getEyePosition());
        if (WorldUtils.canBreak(eyePos) && !WorldUtils.isReplaceable(eyePos)) {
            return eyePos;
        }
        BlockPos headPos = AutoMineModule.mc.player.blockPosition().above();
        if (WorldUtils.canBreak(headPos) && !WorldUtils.isReplaceable(headPos)) {
            return headPos;
        }
        return null;
    }

    public boolean isProtectedTerrainBase(BlockPos pos) {
        return this.terrain.getValue() && this.activeTerrainPair != null && pos != null && pos.equals((Object)this.activeTerrainPair.basePos()) && this.isRealTerrainBase(pos);
    }

    private boolean allSidesTerraformed(Player target, SpeedMineModule speedMine) {
        BlockPos feet = target.blockPosition();
        boolean sawCandidate = false;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockState surroundState;
            BlockPos surroundPos = feet.relative(dir);
            BlockPos basePos = surroundPos.below();
            if (speedMine.isOutOfRange(surroundPos) || speedMine.isOutOfRange(basePos) || (surroundState = AutoMineModule.mc.level.getBlockState(surroundPos)).getBlock().defaultDestroyTime() < 0.0f && (!speedMine.sixB.getValue() || surroundState.getBlock() != Blocks.BEDROCK)) continue;
            sawCandidate = true;
            BlockState baseState = AutoMineModule.mc.level.getBlockState(basePos);
            boolean hasRealBase = baseState.getBlock() == Blocks.OBSIDIAN || baseState.getBlock() == Blocks.BEDROCK;
            if (hasRealBase) continue;
            return false;
        }
        return sawCandidate;
    }

    public TerrainPair getBestTerrainPair(Player target) {
        SpeedMineModule speedMine = this.speedMine();
        if (target == null || AutoMineModule.mc.level == null || AutoMineModule.mc.player == null || speedMine == null) {
            this.activeTerrainPair = null;
            this.activeTerrainTarget = null;
            return null;
        }
        if (this.allSidesTerraformed(target, speedMine)) {
            this.activeTerrainPair = null;
            this.activeTerrainTarget = null;
            return null;
        }
        UUID targetUUID = target.getUUID();
        BlockPos targetFeet = HoleUtils.getTargetFeetPosition(target);
        if (this.activeTerrainPair != null && targetUUID.equals(this.activeTerrainTarget)) {
            boolean baseBreakable;
            BlockPos currentSurround = this.activeTerrainPair.surroundPos();
            BlockPos currentBase = this.activeTerrainPair.basePos();
            boolean inRange = !speedMine.isOutOfRange(currentSurround) && !speedMine.isOutOfRange(currentBase);
            boolean nearTarget = targetFeet.distManhattan((Vec3i)currentSurround) <= 2;
            BlockState surroundState = AutoMineModule.mc.level.getBlockState(currentSurround);
            BlockState baseState = AutoMineModule.mc.level.getBlockState(currentBase);
            boolean surroundBreakable = surroundState.getBlock().defaultDestroyTime() >= 0.0f || speedMine.sixB.getValue() && surroundState.getBlock() == Blocks.BEDROCK || speedMine.isMining(currentSurround);
            boolean bl = baseBreakable = baseState.getBlock().defaultDestroyTime() >= 0.0f || baseState.getBlock() == Blocks.BEDROCK || baseState.getBlock() == Blocks.OBSIDIAN || this.pendingTerrainPlacements.contains(currentBase);
            if (inRange && nearTarget && surroundBreakable && baseBreakable) {
                return this.activeTerrainPair;
            }
        }
        HashSet<BlockPos> selfSurround = new HashSet<BlockPos>();
        if (this.avoidSharing.getValue()) {
            selfSurround.addAll(HoleUtils.getFeetPositions((Player)AutoMineModule.mc.player, true, false, true));
            for (BlockPos p : HoleUtils.getInsidePositions((Entity)AutoMineModule.mc.player)) {
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    selfSurround.add(p.relative(dir));
                }
            }
        }
        TerrainPair bestPair = null;
        double bestScore = Double.MAX_VALUE;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            boolean isMiningPair;
            boolean hasRealBase;
            BlockPos surroundPos = targetFeet.relative(dir);
            BlockPos basePos = surroundPos.below();
            if (speedMine.isOutOfRange(surroundPos) || speedMine.isOutOfRange(basePos)) continue;
            BlockState surroundState = AutoMineModule.mc.level.getBlockState(surroundPos);
            BlockState baseState = AutoMineModule.mc.level.getBlockState(basePos);
            if (surroundState.getBlock().defaultDestroyTime() < 0.0f && !speedMine.isMining(surroundPos) || baseState.getBlock().defaultDestroyTime() < 0.0f && baseState.getBlock() != Blocks.BEDROCK && baseState.getBlock() != Blocks.OBSIDIAN && !this.pendingTerrainPlacements.contains(basePos)) continue;
            double score = 0.0;
            if (this.avoidSharing.getValue() && (selfSurround.contains(surroundPos) || selfSurround.contains(basePos))) {
                score += 100000.0;
            }
            boolean bl = hasRealBase = baseState.getBlock() == Blocks.OBSIDIAN || baseState.getBlock() == Blocks.BEDROCK;
            if (hasRealBase) {
                score -= 20000.0;
            } else if (!baseState.canBeReplaced()) {
                score += (double)baseState.getBlock().defaultDestroyTime() * 10.0;
            }
            if (!surroundState.canBeReplaced()) {
                score -= 10000.0;
            }
            boolean bl2 = isMiningPair = speedMine.isMining(surroundPos) || speedMine.isMining(basePos);
            if (isMiningPair) {
                score -= 50000.0;
            }
            if (!((score += AutoMineModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)surroundPos))) < bestScore)) continue;
            bestScore = score;
            bestPair = new TerrainPair(dir, surroundPos, basePos, score);
        }
        this.activeTerrainPair = bestPair;
        this.activeTerrainTarget = bestPair != null ? targetUUID : null;
        return bestPair;
    }

    public void placePendingTerrain() {
        int obsidianSlot;
        if (!this.terrainPlace.getValue() || this.pendingTerrainPlacements.isEmpty()) {
            return;
        }
        if (AutoMineModule.mc.player == null || AutoMineModule.mc.level == null) {
            return;
        }
        SpeedMineModule speedMine = this.speedMine();
        if (speedMine == null) {
            return;
        }
        if (!speedMine.whileEating.getValue() && EntityUtils.isEating()) {
            return;
        }
        obsidianSlot = speedMine.switchMode.getValue().equalsIgnoreCase("None") ? -1 : InventoryUtils.find(Items.OBSIDIAN, 0, speedMine.switchMode.getValue().equalsIgnoreCase("AltSwap") || speedMine.switchMode.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END);
        if (obsidianSlot == -1) {
            obsidianSlot = InventoryUtils.findHardestBlock(0, 8);
        }
        if (obsidianSlot == -1) {
            return;
        }
        ArrayList<BlockPos> toRemove = new ArrayList<BlockPos>();
        for (BlockPos pos : new ArrayList<BlockPos>(this.pendingTerrainPlacements)) {
            if (speedMine.isOutOfRange(pos)) {
                toRemove.add(pos);
                continue;
            }
            BlockState state = AutoMineModule.mc.level.getBlockState(pos);
            if (state.getBlock() == Blocks.OBSIDIAN) {
                toRemove.add(pos);
                speedMine.dropPrimaryIfInvalid(p -> !p.equals((Object)pos));
                speedMine.dropSecondaryIfInvalid(p -> !p.equals((Object)pos));
                continue;
            }
            if (!state.canBeReplaced() || !WorldUtils.isPlaceable(pos)) continue;
            Direction direction = WorldUtils.getDirection(pos, false);
            if (direction == null) {
                direction = WorldUtils.getClosestDirection(pos, true);
            }
            if (direction == null) continue;
            int previousSlot = AutoMineModule.mc.player.getInventory().getSelectedSlot();
            InventoryUtils.switchSlot(speedMine.switchMode.getValue(), obsidianSlot, previousSlot);
            boolean placed = WorldUtils.placeBlock(pos, direction, InteractionHand.MAIN_HAND, speedMine.rotate.getValue().equalsIgnoreCase("Packet") || speedMine.rotate.getValue().equalsIgnoreCase("Normal") || speedMine.rotate.getValue().equalsIgnoreCase("Silent"), true, speedMine.render.getValue().equalsIgnoreCase("Both") || speedMine.render.getValue().equalsIgnoreCase("Fill"));
            InventoryUtils.switchBack(speedMine.switchMode.getValue(), obsidianSlot, previousSlot);
            if (!placed) continue;
            toRemove.add(pos);
            speedMine.dropPrimaryIfInvalid(p -> !p.equals((Object)pos));
            speedMine.dropSecondaryIfInvalid(p -> !p.equals((Object)pos));
        }
        toRemove.forEach(this.pendingTerrainPlacements::remove);
    }

    @SubscribeEvent
   public void onPlayerUpdate(PlayerUpdateEvent event) {
      if (!PingBypassFlags.isPingBypassActive()) {
         if (mc.player != null && mc.level != null) {
            SpeedMineModule speedMine = this.speedMine();
            if (speedMine != null && speedMine.isToggled()) {
               if (this.antiCrawl.getValue() && mc.player.isVisuallyCrawling()) {
                  BlockPos playerPosition = mc.player.blockPosition();
                  BlockPos position;
                  if (!WorldUtils.canBreak(playerPosition.below())
                     || WorldUtils.isReplaceable(playerPosition.below())
                     || WorldUtils.isReplaceable(playerPosition.below(2)) && HoleUtils.getSingleHole(playerPosition.below(2), 1.0, false) == null) {
                     position = playerPosition.above();
                  } else {
                     position = playerPosition.below();
                  }

                  if (speedMine.isValid(position) && !speedMine.isOutOfRange(position)) {
                     if (speedMine.isMining(position)) {
                        return;
                     }

                     speedMine.handle(position, 0);
                     return;
                  }
               }

               if (this.face.getValue()) {
                  BlockPos facePosition = this.getSelfFaceBlock();
                  if (facePosition != null && speedMine.isValid(facePosition) && !speedMine.isOutOfRange(facePosition)) {
                     if (speedMine.isMining(facePosition)) {
                        return;
                     }

                     speedMine.handle(facePosition, 0);
                     return;
                  }
               }

               if (!this.tickUpFallback(speedMine)) {
                  SpeedMineModule.Action primary = speedMine.getPrimary();
                  SpeedMineModule.Secondary secondary = speedMine.getSecondary();
                  BlockPos selfEye = BlockPos.containing(mc.player.getEyePosition());
                  BlockPos selfHead = mc.player.blockPosition().above();
                  BlockPos selfFeet = mc.player.blockPosition();
                  if (primary != null
                     && primary.getPriority() == 0
                     && (
                        primary.getPosition().equals(selfEye)
                           || primary.getPosition().equals(selfHead)
                           || primary.getPosition().equals(selfFeet)
                           || primary.getPosition().equals(selfFeet.below())
                     )
                     && mc.level.getBlockState(primary.getPosition()).canBeReplaced()) {
                     speedMine.dropPrimary();
                     primary = null;
                  }

                  if (secondary != null
                     && secondary.getPriority() == 0
                     && (
                        secondary.getPosition().equals(selfEye)
                           || secondary.getPosition().equals(selfHead)
                           || secondary.getPosition().equals(selfFeet)
                           || secondary.getPosition().equals(selfFeet.below())
                     )
                     && mc.level.getBlockState(secondary.getPosition()).canBeReplaced()) {
                     speedMine.dropSecondary();
                     secondary = null;
                  }

                  SpeedMineModule.Target rawTarget = speedMine.getTarget();
                  SpeedMineModule.Target target = rawTarget == null || this.ignoreNaked.getValue() && EntityUtils.isNaked(rawTarget.player())
                     ? null
                     : rawTarget;
                  BlockPos secondaryPos = secondary != null ? secondary.getPosition() : null;
                  int secondaryPriority = secondary != null ? secondary.getPriority() : 0;
                  if ((primary == null || primary.getPriority() <= 0 || WorldUtils.isReplaceable(primary.getPosition()))
                     && (secondaryPos == null || secondaryPriority <= 0 || WorldUtils.isReplaceable(secondaryPos))) {
                     if (primary == null
                        || primary.getPriority() != 0
                        || !speedMine.sixB.getValue()
                        || primary.getState().getBlock() != Blocks.BEDROCK
                        || target == null
                        || !speedMine.isTargetSurroundPosition(primary.getPosition(), target.player())) {
                        if (speedMine.doubleMine.getValue()) {
                           if (!speedMine.isMineTimerReady()) {
                              return;
                           }

                           if (target == null) {
                              speedMine.setDoubleEngaged(false);
                           }

                           if (target != null) {
                              List<BlockPos> validBlocks = new ArrayList<>();
                              List<BlockPos> insidePositions = HoleUtils.getInsidePositions(target.player());
                              validBlocks.addAll(insidePositions);
                              HashSet<BlockPos> feetPositions = HoleUtils.getFeetPositions(target.player(), true, false, true);
                              validBlocks.addAll(feetPositions);

                              for (BlockPos pos : feetPositions) {
                                 validBlocks.add(pos.below());

                                 for (Direction dir : Plane.HORIZONTAL) {
                                    validBlocks.add(pos.relative(dir).below());
                                 }
                              }

                              for (BlockPos pos : insidePositions) {
                                 for (Direction dir : Plane.HORIZONTAL) {
                                    validBlocks.add(pos.relative(dir));
                                    validBlocks.add(pos.relative(dir).below());
                                 }

                                 validBlocks.add(pos.above());
                                 validBlocks.add(pos.below());
                              }

                              AutoMineModule.TerrainPair bestPair = this.terrain.getValue() ? this.getBestTerrainPair(target.player()) : null;
                              if (bestPair != null) {
                                 validBlocks.add(bestPair.surroundPos());
                                 validBlocks.add(bestPair.basePos());
                              }

                              BlockPos grimBedrockHead = this.logic.getValue().equalsIgnoreCase("Grim")
                                 ? this.getGrimBedrockHeadTarget(target.player(), speedMine)
                                 : null;
                              if (grimBedrockHead != null) {
                                 validBlocks.add(grimBedrockHead);
                              }

                              if (primary != null && primary.isTerrainBase() && this.isRealTerrainBase(primary.getPosition())) {
                                 speedMine.dropPrimary();
                                 primary = null;
                              }

                              speedMine.setDoubleEngaged(true);
                              if (primary != null && primary.getPriority() == 0 && !primary.isTerrainBase()) {
                                 boolean stillSurrounded = speedMine.isTargetSurroundPosition(primary.getPosition(), target.player());
                                 if (!stillSurrounded) {
                                    speedMine.dropPrimary();
                                    SpeedMineModule.Action var25 = null;
                                 } else if (!primary.isInstantMine()) {
                                    BlockPos pos = primary.getPosition();
                                    boolean insideAir = HoleUtils.getInsidePositions(target.player()).contains(pos)
                                       && mc.level.getBlockState(pos).canBeReplaced();
                                    if (!validBlocks.contains(pos) || insideAir) {
                                       speedMine.dropPrimary();
                                       SpeedMineModule.Action var24 = null;
                                    }
                                 }
                              }

                              if (secondary != null && secondary.getPriority() == 0 && !secondary.isHolding()) {
                                 BlockPos pos = secondary.getPosition();
                                 boolean insideAir = HoleUtils.getInsidePositions(target.player()).contains(pos) && mc.level.getBlockState(pos).canBeReplaced();
                                 if (!validBlocks.contains(pos) || insideAir) {
                                    speedMine.dropSecondary();
                                    SpeedMineModule.Secondary var27 = null;
                                 }
                              }

                              AutoMineModule.TerrainPair finalBestPair = bestPair;
                              Runnable terrainTask = () -> {
                                 if (this.terrain.getValue()) {
                                    if (finalBestPair != null) {
                                       BlockPos basePos = finalBestPair.basePos();
                                       BlockPos surroundPos = finalBestPair.surroundPos();
                                       BlockState baseState = mc.level.getBlockState(basePos);
                                       BlockState surroundState = mc.level.getBlockState(surroundPos);
                                       boolean hasRealBasex = baseState.getBlock() == Blocks.OBSIDIAN || baseState.getBlock() == Blocks.BEDROCK;
                                       if (!hasRealBasex && this.terrainPlace.getValue() && !this.pendingTerrainPlacements.contains(basePos)) {
                                          this.pendingTerrainPlacements.add(basePos);
                                       }

                                       if (this.isPhased(target.player())) {
                                          if (speedMine.primaryCamping()) {
                                             BlockPos campPos = speedMine.getPrimary().getPosition();
                                             if (!campPos.equals(basePos) && !campPos.equals(surroundPos)) {
                                                return;
                                             }
                                          }

                                          boolean canTakePrimary = speedMine.getPrimary() == null || speedMine.getSecondary() == null;
                                          if (!hasRealBasex && !baseState.canBeReplaced()) {
                                             if (!speedMine.isMining(basePos) && canTakePrimary) {
                                                speedMine.handle(basePos, 0);
                                                SpeedMineModule.Action newPrimary = speedMine.getPrimary();
                                                if (newPrimary != null && newPrimary.getPosition().equals(basePos)) {
                                                   newPrimary.setTerrainBase(true);
                                                }
                                             }
                                          } else if (hasRealBasex && !surroundState.canBeReplaced()) {
                                             SpeedMineModule.Action currentPrimary = speedMine.getPrimary();
                                             boolean surroundAlreadyPrimary = currentPrimary != null && currentPrimary.getPosition().equals(surroundPos);
                                             if (!surroundAlreadyPrimary) {
                                                speedMine.handle(surroundPos, 0);
                                                SpeedMineModule.Action newPrimary = speedMine.getPrimary();
                                                if (newPrimary != null && newPrimary.getPosition().equals(surroundPos)) {
                                                   newPrimary.setTerrainSurround(true);
                                                }
                                             }
                                          }
                                       } else {
                                          if (!hasRealBasex && !baseState.canBeReplaced() && !speedMine.isMining(basePos)) {
                                             speedMine.handle(basePos, 0);
                                          }

                                          if (!surroundState.canBeReplaced() && !speedMine.isMining(surroundPos)) {
                                             if (hasRealBasex && speedMine.doubleMine.getValue()) {
                                                BlockPos partner = HoleUtils.getFeetPositions(target.player(), true, false, true)
                                                   .stream()
                                                   .filter(pos -> !pos.equals(surroundPos))
                                                   .filter(pos -> !mc.level.getBlockState(pos).canBeReplaced())
                                                   .filter(pos -> !speedMine.isMining(pos) && !speedMine.isInvalid(pos) && !speedMine.isOutOfRange(pos))
                                                   .min(Comparator.comparingDouble(pos -> mc.player.distanceToSqr(Vec3.atCenterOf(pos))))
                                                   .orElse(null);
                                                if (partner != null) {
                                                   speedMine.handle(partner, 0);
                                                }
                                             }

                                             speedMine.handle(surroundPos, 0);
                                             SpeedMineModule.Action newPrimary = speedMine.getPrimary();
                                             if (newPrimary != null && newPrimary.getPosition().equals(surroundPos)) {
                                                newPrimary.setTerrainSurround(true);
                                             }
                                          }
                                       }
                                    }
                                 }
                              };
                              Runnable outside = () -> {
                                 SpeedMineModule.Action livePrimary = speedMine.getPrimary();
                                 boolean campingAbandonedPhase = livePrimary != null
                                    && speedMine.primaryCamping()
                                    && HoleUtils.getInsidePositions(target.player()).contains(livePrimary.getPosition());
                                 if (!speedMine.primaryCamping() || campingAbandonedPhase) {
                                    if (livePrimary == null || !speedMine.sixB.getValue() || livePrimary.getState().getBlock() != Blocks.BEDROCK) {
                                       List<BlockPos> remainingPhase = HoleUtils.getInsidePositions(target.player())
                                          .stream()
                                          .filter(pos -> !mc.level.getBlockState(pos).canBeReplaced())
                                          .toList();
                                       if (remainingPhase.size() < 2) {
                                          if (!this.terrain.getValue() || livePrimary == null || !livePrimary.isTerrainSurround() || livePrimary.isStarted()) {
                                             if (this.terrain.getValue() && finalBestPair != null) {
                                                BlockState pairBaseState = mc.level.getBlockState(finalBestPair.basePos());
                                                boolean pairHasRealBase = pairBaseState.getBlock() == Blocks.OBSIDIAN
                                                   || pairBaseState.getBlock() == Blocks.BEDROCK;
                                                if (!pairHasRealBase) {
                                                   return;
                                                }

                                                if ((speedMine.isMining(finalBestPair.basePos()) || speedMine.isMining(finalBestPair.surroundPos()))
                                                   && speedMine.slotsFull()) {
                                                   return;
                                                }
                                             }

                                             if (this.logic.getValue().equals("NCP")) {
                                                for (BlockPos positionxx : HoleUtils.getFeetPositions(target.player(), true, false, true)
                                                   .stream()
                                                   .filter(pos -> !mc.level.getBlockState(pos).canBeReplaced())
                                                   .filter(pos -> !this.isRaytrace() || WorldUtils.canSeeBlock(pos))
                                                   .toList()) {
                                                   if (speedMine.slotsFull()) {
                                                      break;
                                                   }

                                                   if (!speedMine.isMining(positionxx)
                                                      && !speedMine.isInvalid(positionxx)
                                                      && !speedMine.isOutOfRange(positionxx)) {
                                                      speedMine.handle(positionxx, 0);
                                                   }
                                                }
                                             } else {
                                                Set<BlockPos> selfSurround = new HashSet<>();
                                                if (this.avoidSharing.getValue() && mc.player != null) {
                                                   selfSurround.addAll(HoleUtils.getFeetPositions(mc.player, true, false, true));

                                                   for (BlockPos p : HoleUtils.getInsidePositions(mc.player)) {
                                                      for (Direction dir : Plane.HORIZONTAL) {
                                                         selfSurround.add(p.relative(dir));
                                                      }
                                                   }
                                                }

                                                List<BlockPos> surroundPositions = HoleUtils.getFeetPositions(target.player(), true, false, true)
                                                   .stream()
                                                   .filter(pos -> !mc.level.getBlockState(pos).canBeReplaced())
                                                   .filter(pos -> !this.isRaytrace() || WorldUtils.canSeeBlock(pos))
                                                   .sorted(Comparator.comparingDouble(pos -> {
                                                      double penalty = this.avoidSharing.getValue() && selfSurround.contains(pos) ? 100000.0 : 0.0;
                                                      if (this.isRaytrace() && !WorldUtils.canSeeBlock(pos)) {
                                                         penalty += 500000.0;
                                                      } else if (WorldUtils.canSeeBlock(pos)) {
                                                         penalty -= 50000.0;
                                                      }

                                                      if (speedMine.sixB.getValue() && mc.level.getBlockState(pos).getBlock() == Blocks.BEDROCK) {
                                                         penalty -= 200000.0;
                                                      }

                                                      if (this.terrain.getValue() && finalBestPair != null && pos.equals(finalBestPair.surroundPos())) {
                                                         penalty -= 100000.0;
                                                      } else {
                                                         BlockPos basePos = pos.below();
                                                         BlockState baseState = mc.level.getBlockState(basePos);
                                                         boolean hasBase = baseState.getBlock() == Blocks.OBSIDIAN || baseState.getBlock() == Blocks.BEDROCK;
                                                         boolean isMiningBase = speedMine.isMining(basePos);
                                                         if (isMiningBase) {
                                                            penalty -= 50000.0;
                                                         } else if (hasBase) {
                                                            penalty -= 20000.0;
                                                         } else if (this.terrain.getValue()) {
                                                            penalty += 50000.0;
                                                         }
                                                      }

                                                      return penalty + mc.player.distanceToSqr(Vec3.atCenterOf(pos));
                                                   }))
                                                   .toList();
                                                boolean override = campingAbandonedPhase;

                                                for (BlockPos positionx : surroundPositions) {
                                                   if (!override && speedMine.slotsFull()) {
                                                      break;
                                                   }

                                                   if (!speedMine.isMining(positionx)) {
                                                      if (speedMine.isInvalid(positionx) || speedMine.isOutOfRange(positionx)) {
                                                         continue;
                                                      }

                                                      boolean isBedrockPick = speedMine.sixB.getValue()
                                                         && mc.level.getBlockState(positionx).getBlock() == Blocks.BEDROCK;
                                                      if (isBedrockPick && speedMine.doubleMine.getValue() && speedMine.getPrimary() == null) {
                                                         BlockPos partner = surroundPositions.stream()
                                                            .filter(p -> !p.equals(positionx))
                                                            .filter(p -> mc.level.getBlockState(p).getBlock() != Blocks.BEDROCK)
                                                            .filter(p -> !speedMine.isMining(p) && !speedMine.isInvalid(p) && !speedMine.isOutOfRange(p))
                                                            .findFirst()
                                                            .orElse(null);
                                                         if (partner != null) {
                                                            speedMine.handle(partner, 0);
                                                         }
                                                      }

                                                      speedMine.handle(positionx, 0);
                                                      override = false;
                                                   }

                                                   if (!override && speedMine.slotsFull()) {
                                                      break;
                                                   }
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              };
                              Runnable inside = () -> {
                                 SpeedMineModule.Action livePrimary = speedMine.getPrimary();
                                 if (!this.terrain.getValue() || livePrimary == null || !livePrimary.isTerrainSurround() || livePrimary.isStarted()) {
                                    int targetFeetY = Mth.floor(target.player().getY());

                                    for (BlockPos positionx : HoleUtils.getInsidePositions(target.player())
                                       .stream()
                                       .filter(insidePosition -> !mc.level.getBlockState(insidePosition).canBeReplaced())
                                       .filter(pos -> !this.isRaytrace() || WorldUtils.canSeeBlock(pos))
                                       .sorted(Comparator.comparingDouble(pos -> pos.getY() > targetFeetY ? 1.0 : 0.0))
                                       .toList()) {
                                       if (!speedMine.isInvalid(positionx) && !speedMine.isOutOfRange(positionx)) {
                                          if (speedMine.phaseSlotsFull()) {
                                             break;
                                          }

                                          speedMine.handle(positionx, 0);
                                          if (speedMine.phaseSlotsFull()) {
                                             break;
                                          }
                                       }
                                    }
                                 }
                              };
                              BlockPos prioritizedTarget = null;
                              if (this.priority.getValue().equalsIgnoreCase("Head")) {
                                 prioritizedTarget = this.getHeadTarget(target.player(), speedMine);
                              }

                              if (prioritizedTarget == null && this.logic.getValue().equalsIgnoreCase("Grim")) {
                                 prioritizedTarget = this.getGrimBedrockHeadTarget(target.player(), speedMine);
                              }

                              if (prioritizedTarget != null && !speedMine.isMining(prioritizedTarget)) {
                                 speedMine.handle(prioritizedTarget, 0);
                              }

                              if (prioritizedTarget == null) {
                                 inside.run();
                              }

                              if (prioritizedTarget == null && this.terrain.getValue()) {
                                 terrainTask.run();
                              }

                              if (prioritizedTarget == null) {
                                 outside.run();
                              }
                           }
                        } else {
                           BlockPos position = null;
                           BlockPos grimBedrockHead = null;
                           boolean terrainBasePick = false;
                           if (target == null) {
                              return;
                           }

                           if (primary != null && primary.getPriority() == 0 && !primary.isTerrainBase()) {
                              boolean targetStillSurrounded = speedMine.isTargetSurroundPosition(primary.getPosition(), target.player());
                              boolean insideAir = HoleUtils.getInsidePositions(target.player()).contains(primary.getPosition())
                                 && mc.level.getBlockState(primary.getPosition()).canBeReplaced();
                              if (!targetStillSurrounded) {
                                 speedMine.dropPrimary();
                                 primary = null;
                              } else if (insideAir) {
                                 speedMine.dropPrimary();
                                 primary = null;
                              } else if (speedMine.sixB.getValue() && primary.getState().getBlock() == Blocks.BEDROCK) {
                                 return;
                              }
                           }

                           if (this.priority.getValue().equalsIgnoreCase("Head")) {
                              position = this.getHeadTarget(target.player(), speedMine);
                           }

                           if (position == null && this.logic.getValue().equalsIgnoreCase("Grim")) {
                              grimBedrockHead = this.getGrimBedrockHeadTarget(target.player(), speedMine);
                              position = grimBedrockHead;
                           }

                           if (position == null && this.terrain.getValue()) {
                              AutoMineModule.TerrainPair bestPair = this.getBestTerrainPair(target.player());
                              if (bestPair != null) {
                                 BlockState baseState = mc.level.getBlockState(bestPair.basePos());
                                 BlockState surroundState = mc.level.getBlockState(bestPair.surroundPos());
                                 boolean hasRealBase = baseState.getBlock() == Blocks.OBSIDIAN || baseState.getBlock() == Blocks.BEDROCK;
                                 if (!hasRealBase && !baseState.canBeReplaced()) {
                                    position = bestPair.basePos();
                                    terrainBasePick = true;
                                    if (this.terrainPlace.getValue() && !this.pendingTerrainPlacements.contains(position)) {
                                       this.pendingTerrainPlacements.add(position);
                                    }
                                 } else if (!surroundState.canBeReplaced()) {
                                    position = bestPair.surroundPos();
                                 }
                              }
                           }

                           if (position == null) {
                              int targetFeetY = Mth.floor(target.player().getY());
                              List<BlockPos> insidePositions = HoleUtils.getInsidePositions(target.player())
                                 .stream()
                                 .filter(pos -> pos.getY() <= targetFeetY)
                                 .filter(pos -> !mc.level.getBlockState(pos).canBeReplaced())
                                 .filter(pos -> !this.isRaytrace() || WorldUtils.canSeeBlock(pos))
                                 .toList();
                              position = !insidePositions.isEmpty() ? insidePositions.get(0) : target.position();
                              if (position != null && this.isRaytrace() && !WorldUtils.canSeeBlock(position)) {
                                 position = null;
                              }
                           }

                           if (position == null) {
                              return;
                           }

                           if (primary != null && position.equals(primary.getPosition())) {
                              return;
                           }

                           if (primary != null
                              && primary.getPriority() == 0
                              && !primary.isTerrainBase()
                              && speedMine.isTargetSurroundPosition(primary.getPosition(), target.player())
                              && (grimBedrockHead == null || !position.equals(grimBedrockHead))) {
                              return;
                           }

                           speedMine.handle(position, 0);
                           if (terrainBasePick) {
                              SpeedMineModule.Action newPrimary = speedMine.getPrimary();
                              if (newPrimary != null && newPrimary.getPosition().equals(position)) {
                                 newPrimary.setTerrainBase(true);
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

    private BlockPos getHeadTarget(Player target, SpeedMineModule speedMine) {
        if (target == null || AutoMineModule.mc.level == null) {
            return null;
        }
        int targetFeetY = Mth.floor((double)target.getY());
        int headY = targetFeetY + 2;
        SpeedMineModule.Action camping = speedMine.getPrimary();
        if (camping != null && camping.isInstantMine() && camping.getPosition().getY() == headY && speedMine.isTargetSurroundPosition(camping.getPosition(), target)) {
            return camping.getPosition();
        }
        for (BlockPos insidePos : HoleUtils.getInsidePositions((Entity)target)) {
            BlockPos headPos = new BlockPos(insidePos.getX(), headY, insidePos.getZ());
            if (!speedMine.isValid(headPos) || speedMine.isOutOfRange(headPos) || this.isRaytrace() && !WorldUtils.canSeeBlock(headPos) || AutoMineModule.mc.level.getBlockState(headPos).canBeReplaced()) continue;
            return headPos;
        }
        BlockPos directHead = target.blockPosition().above(2);
        if (!(!speedMine.isValid(directHead) || speedMine.isOutOfRange(directHead) || this.isRaytrace() && !WorldUtils.canSeeBlock(directHead) || AutoMineModule.mc.level.getBlockState(directHead).canBeReplaced())) {
            return directHead;
        }
        return null;
    }

    private boolean tickUpFallback(SpeedMineModule speedMine) {
        Player targetPlayer;
        if (!(this.priority.getValue().equalsIgnoreCase("Leg") && this.raytrace.getValue() && this.upFallback.getValue())) {
            this.resetUpCombo();
            return false;
        }
        SpeedMineModule.Target rawTarget = speedMine.getTarget();
        Player player = targetPlayer = rawTarget != null ? rawTarget.player() : null;
        if (targetPlayer == null || EntityUtils.isGhost((Entity)targetPlayer)) {
            this.resetUpCombo();
            return false;
        }
        if (!HoleUtils.isPlayerInHole(targetPlayer)) {
            this.resetUpCombo();
            return false;
        }
        HashSet<BlockPos> legRing = HoleUtils.getFeetPositions(targetPlayer, true, false, true);
        if (this.upComboPos != null) {
            if (!legRing.contains(this.upComboPos.below())) {
                this.resetUpCombo();
            } else {
                if (this.isPhased(targetPlayer) || this.elsewhereDealsMoreDamage(speedMine, targetPlayer, this.upComboPos)) {
                    this.resetUpCombo();
                    return false;
                }
                return this.tickUpComboCycle(speedMine, this.upComboPos);
            }
        }
        boolean anySolidLeg = legRing.stream().anyMatch(pos -> !AutoMineModule.mc.level.getBlockState(pos).canBeReplaced());
        boolean anyMineableVisibleLeg = legRing.stream().anyMatch(pos -> this.isMinableLegCandidate((BlockPos)pos, speedMine) && WorldUtils.canSeeBlock(pos));
        if (!anySolidLeg || anyMineableVisibleLeg) {
            return false;
        }
        if (this.isPhased(targetPlayer)) {
            return false;
        }
        BlockPos chosen = null;
        for (BlockPos leg : legRing) {
            BlockPos up = leg.above();
            if (speedMine.isOutOfRange(up) || !WorldUtils.canSeeBlock(up)) continue;
            chosen = up;
            break;
        }
        if (chosen == null) {
            return false;
        }
        this.upComboPos = chosen;
        return this.tickUpComboCycle(speedMine, chosen);
    }

    private boolean elsewhereDealsMoreDamage(SpeedMineModule speedMine, Player targetPlayer, BlockPos chosen) {
        AutoCrystalModule autoCrystal = Night.MODULE_MANAGER.getModule(AutoCrystalModule.class);
        if (autoCrystal == null) {
            return false;
        }
        float elsewhere = autoCrystal.getBestPlaceDamage();
        if (elsewhere <= 0.0f) {
            return false;
        }
        BlockPos crystalCell = chosen.above();
        float ours = DamageUtils.getCrystalDamage((Entity)targetPlayer, PositionUtils.extrapolate(targetPlayer, autoCrystal.extrapolation.getValue().intValue()), crystalCell, chosen, autoCrystal.ignoreTerrain.getValue());
        return elsewhere > ours;
    }

    private boolean tickUpComboCycle(SpeedMineModule speedMine, BlockPos chosen) {
        if (AutoMineModule.mc.level.getBlockState(chosen).canBeReplaced()) {
            this.placeUpComboObsidian(speedMine, chosen);
            return true;
        }
        if (!speedMine.isMining(chosen) && speedMine.isValid(chosen) && !speedMine.isOutOfRange(chosen)) {
            speedMine.handle(chosen, 0);
        }
        return true;
    }

    private void placeUpComboObsidian(SpeedMineModule speedMine, BlockPos pos) {
        if (!WorldUtils.isPlaceable(pos)) {
            return;
        }
        AutoCrystalModule autoCrystal = Night.MODULE_MANAGER.getModule(AutoCrystalModule.class);
        String switchMode = autoCrystal != null ? autoCrystal.autoSwitch.getValue() : "Silent";
        boolean fullInventory = switchMode.equalsIgnoreCase("AltSwap");
        int obsidianSlot = InventoryUtils.find(Items.OBSIDIAN, 0, fullInventory ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END);
        if (obsidianSlot == -1) {
            obsidianSlot = InventoryUtils.findHardestBlock(0, 8);
        }
        if (obsidianSlot == -1) {
            return;
        }
        Direction direction = WorldUtils.getDirection(pos, false);
        if (direction == null) {
            direction = WorldUtils.getClosestDirection(pos, true);
        }
        if (direction == null) {
            return;
        }
        int previousSlot = AutoMineModule.mc.player.getInventory().getSelectedSlot();
        InventoryUtils.switchSlot(switchMode, obsidianSlot, previousSlot);
        WorldUtils.placeBlock(pos, direction, InteractionHand.MAIN_HAND, this.upFallbackRotate.getValue(), true, speedMine.render.getValue().equalsIgnoreCase("Both") || speedMine.render.getValue().equalsIgnoreCase("Fill"));
        InventoryUtils.switchBack(switchMode, obsidianSlot, previousSlot);
    }

    public record TerrainPair(Direction direction, BlockPos surroundPos, BlockPos basePos, double score) {
    }
}

