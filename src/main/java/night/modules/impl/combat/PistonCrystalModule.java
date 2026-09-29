/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.core.Direction$Plane
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundAttackPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.BannerBlock
 *  net.minecraft.world.level.block.BaseRailBlock
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.ButtonBlock
 *  net.minecraft.world.level.block.CarpetBlock
 *  net.minecraft.world.level.block.FlowerBlock
 *  net.minecraft.world.level.block.FlowerPotBlock
 *  net.minecraft.world.level.block.LadderBlock
 *  net.minecraft.world.level.block.LeverBlock
 *  net.minecraft.world.level.block.MushroomBlock
 *  net.minecraft.world.level.block.PressurePlateBlock
 *  net.minecraft.world.level.block.ScaffoldingBlock
 *  net.minecraft.world.level.block.SignBlock
 *  net.minecraft.world.level.block.TallFlowerBlock
 *  net.minecraft.world.level.block.TorchBlock
 *  net.minecraft.world.level.block.TripWireBlock
 *  net.minecraft.world.level.block.TripWireHookBlock
 *  net.minecraft.world.level.block.VineBlock
 *  net.minecraft.world.level.block.WebBlock
 *  net.minecraft.world.level.block.piston.PistonBaseBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package night.modules.impl.combat;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.TickEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.player.SpeedMineModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.minecraft.DamageUtils;
import night.utils.minecraft.EntityUtils;
import night.utils.minecraft.InventoryUtils;
import night.utils.minecraft.NetworkUtils;
import night.utils.minecraft.PistonHelper;
import night.utils.minecraft.WorldUtils;
import night.utils.miscellaneous.RenderPosition;
import night.utils.rotations.RotationUtils;

@RegisterModule(name="PistonCrystal", description="Places piston + redstone to push a crystal into targets.", category=Module.Category.COMBAT)
public class PistonCrystalModule
extends Module {
    public NumberSetting minDamage = new NumberSetting("MinDamage", "Minimum damage to attempt a sequence", Float.valueOf(6.0f), Float.valueOf(1.0f), Float.valueOf(20.0f));
    public NumberSetting maxLocalDamage = new NumberSetting("MaxLocalDamage", "Max self-damage allowed", Float.valueOf(12.0f), Float.valueOf(1.0f), Float.valueOf(20.0f));
    public NumberSetting delay = new NumberSetting("Delay", "Delay between place actions (ticks)", 0, 0, 10);
    public ModeSetting sort = new ModeSetting("Sort", "How to pick target", "LowestDistance", new String[]{"LowestDistance", "LowestHealth", "CloseAngle"});
    public NumberSetting targetRange = new NumberSetting("TargetRange", "Max target range", Float.valueOf(6.0f), Float.valueOf(1.0f), Float.valueOf(20.0f));
    public BooleanSetting inAirTarget = new BooleanSetting("InAirTarget", "Target players in the air", true);
    public BooleanSetting breakCrystal = new BooleanSetting("BreakCrystal", "Attack crystal after placing it", true);
    public BooleanSetting autoBreak = new BooleanSetting("AutoBreak", "Automatically breaks leftover redstone/piston with pickaxe after crystal explosion.", true);
    public NumberSetting placeRange = new NumberSetting("PlaceRange", "Range to place blocks", Float.valueOf(4.5f), Float.valueOf(0.1f), Float.valueOf(6.0f));
    public NumberSetting breakRange = new NumberSetting("BreakRange", "Range to break crystals", Float.valueOf(4.5f), Float.valueOf(0.1f), Float.valueOf(6.0f));
    public ModeSetting rotate = new ModeSetting("Rotate", "Silently rotate to placed blocks", "Silent", new String[]{"None", "Normal", "Silent"});
    public BooleanSetting grim = new BooleanSetting("Grim", "YawDeceive - filter dir by player facing & override piston yaw", true);
    public ModeSetting swapAction = new ModeSetting("Swap", "How to swap to required items", "Silent", InventoryUtils.SWITCH_MODES);
    public BooleanSetting airPlace = new BooleanSetting("AirPlace", "Allow placing when not on ground", false);
    public BooleanSetting safety = new BooleanSetting("Safety", "Skip if crystal would kill self", true);
    public BooleanSetting blockDestruction = new BooleanSetting("BlockDestruction", "Account for explosion block destruction", false);
    public BooleanSetting selfExtrapolate = new BooleanSetting("SelfExtrapolate", "Extrapolate self position for self-damage calc", false);
    public NumberSetting extrapolationTicks = new NumberSetting("ExtrapolationTicks", "Ticks to extrapolate enemy position", 0, 0, 10);
    public BooleanSetting assumeBestArmor = new BooleanSetting("AssumeBestArmor", "Assume target has Prot 4 netherite", false);
    public BooleanSetting autoSupport = new BooleanSetting("AutoSupport", "Automatically places obsidian support blocks when no platform exists.", true);
    public BooleanSetting pauseOnEat = new BooleanSetting("PauseOnEat", "Pauses the module while eating or using an item.", true);
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Use raytrace to verify line-of-sight before placing piston/redstone (required on servers that check raytrace).", false);
    private BlockPos pistonPos;
    private BlockPos redstonePos;
    private BlockPos crystalPos;
    private boolean crystalForcedPush;
    private BlockPos crystalSupportPos;
    private BlockPos pistonSupportPos;
    private BlockPos lastPiston;
    private BlockPos lastRedstone;
    private final Set<BlockPos> placedRedstone = new LinkedHashSet<BlockPos>();
    private final Set<BlockPos> placedPistons = new LinkedHashSet<BlockPos>();
    private int preferredHeight = -1;
    private Direction face;
    private boolean usingTorch;
    private Player target;
    private long lastActionMs = 0L;
    private long breakTimerMs = 0L;
    private int preSwapSlot = -1;

    @Override
    public void onEnable() {
        this.resetState();
        this.placedRedstone.clear();
        this.placedPistons.clear();
        this.breakTimerMs = System.currentTimeMillis() + 99999999L;
    }

    @Override
    public void onDisable() {
        this.resetState();
        this.placedRedstone.clear();
        this.placedPistons.clear();
    }

    /*
     * Unable to fully structure code
     */
    @SubscribeEvent
   public void onTick(TickEvent event) {
      if (mc.player != null && mc.level != null) {
         if (!this.pauseOnEat.getValue() || !EntityUtils.isEating()) {
            long nowMs = System.currentTimeMillis();
            if (nowMs - this.lastActionMs >= this.delay.getValue().longValue() * 50L) {
               int crystalSlot = this.findItem(Items.END_CRYSTAL);
               int pistonSlot = this.findPiston();
               int redstoneSlot = this.findRedstone();
               if (crystalSlot != -1 && pistonSlot != -1 && redstoneSlot != -1) {
                  this.target = this.pickTarget();
                  if (this.target == null) {
                     this.resetState();
                  } else {
                     if (this.pistonPos != null && this.redstonePos != null && this.crystalPos != null) {
                        boolean isPlacedSetup = mc.level.getBlockState(this.pistonPos).getBlock() instanceof PistonBaseBlock
                           && (
                              mc.level.getBlockState(this.redstonePos).getBlock() == Blocks.REDSTONE_BLOCK
                                 || mc.level.getBlockState(this.redstonePos).getBlock() == Blocks.REDSTONE_TORCH
                           );
                        if (!isPlacedSetup) {
                           boolean air = this.airPlace.getValue();
                           boolean pistonOk = PistonHelper.canPlace(this.pistonPos, null, air, true)
                              || mc.level.getBlockState(this.pistonPos).getBlock() instanceof PistonBaseBlock
                              || !air
                                 && this.pistonSupportPos != null
                                 && (PistonHelper.canPlace(this.pistonSupportPos, null, air, true) || this.isSolidBlock(this.pistonSupportPos));
                           boolean redstoneOk = PistonHelper.canPlace(this.redstonePos, null, air, true)
                              || mc.level.getBlockState(this.redstonePos).getBlock() == Blocks.REDSTONE_BLOCK
                              || mc.level.getBlockState(this.redstonePos).getBlock() == Blocks.REDSTONE_TORCH
                              || this.redstonePos.equals(this.pistonPos.above())
                              || this.redstonePos.equals(this.pistonPos.relative(this.face));
                           boolean crystalOk = (
                                 this.crystalForcedPush
                                    ? PistonHelper.canPlaceCrystalIgnoreHeadroom(this.crystalPos, air)
                                    : PistonHelper.canPlaceCrystal(this.crystalPos, air)
                              )
                              || this.crystalSupportPos != null
                                 && (PistonHelper.canPlace(this.crystalSupportPos, null, false, true) || this.isObsidian(this.crystalSupportPos));
                           if (!pistonOk && !air && this.autoSupport.getValue() && this.pistonSupportPos == null) {
                              BlockPos repaired = this.findPistonSupportRepair(this.pistonPos, this.face);
                              if (repaired != null) {
                                 this.pistonSupportPos = repaired;
                                 pistonOk = true;
                              }
                           }

                           if (!crystalOk && this.autoSupport.getValue() && this.crystalSupportPos == null) {
                              BlockPos repairBase = this.crystalPos.below();
                              if (this.canSupportCrystal(this.crystalPos, repairBase)) {
                                 this.crystalSupportPos = repairBase;
                                 crystalOk = true;
                              }
                           }

                           if (!pistonOk || !redstoneOk || !crystalOk) {
                              this.resetState();
                           }
                        }
                     }

                     if (this.pistonPos == null && this.crystalPos == null && this.redstonePos == null) {
                        this.doPistonCrystal(this.target);
                     }

                     if (this.autoSupport.getValue()
                        && this.crystalSupportPos != null
                        && !this.isObsidian(this.crystalSupportPos)
                        && PistonHelper.canPlace(this.crystalSupportPos, null, false, true)) {
                        int supportSlot = this.findSupportBlock();
                        if (supportSlot != -1) {
                           if (!this.placeBlock(supportSlot, this.crystalSupportPos, null)) {
                              this.resetState();
                              return;
                           }

                           this.lastActionMs = nowMs;
                           return;
                        }
                     }

                     if (!this.airPlace.getValue()
                        && this.autoSupport.getValue()
                        && this.pistonSupportPos != null
                        && !this.isSolidBlock(this.pistonSupportPos)
                        && PistonHelper.canPlace(this.pistonSupportPos, null, false, true)) {
                        int supportSlot = this.findSupportBlock();
                        if (supportSlot != -1) {
                           if (!this.placeBlock(supportSlot, this.pistonSupportPos, null)) {
                              this.resetState();
                              return;
                           }

                           this.lastActionMs = nowMs;
                           return;
                        }
                     }

                     if (this.pistonPos != null && PistonHelper.canPlace(this.pistonPos, null, this.airPlace.getValue(), true)) {
                        boolean pistonPlaced = this.placeBlock(pistonSlot, this.pistonPos, this.face);
                        if (!pistonPlaced && this.strictDirection.getValue()) {
                           this.resetState();
                           return;
                        }

                        this.lastPiston = this.pistonPos;
                        this.placedPistons.add(this.pistonPos);
                        this.lastActionMs = nowMs;
                     }

                     if (this.redstonePos != null && PistonHelper.canPlace(this.redstonePos, null, this.airPlace.getValue(), true)) {
                        if (this.usingTorch) {
                           this.placeTorch(redstoneSlot, this.redstonePos);
                        } else {
                           this.placeBlock(redstoneSlot, this.redstonePos, null);
                        }

                        this.lastRedstone = this.redstonePos;
                        this.placedRedstone.add(this.redstonePos);
                        this.lastActionMs = nowMs;
                     }

                     if (this.crystalPos == null
                        || (
                           this.crystalForcedPush
                              ? !PistonHelper.canPlaceCrystalIgnoreHeadroom(this.crystalPos, this.airPlace.getValue())
                              : !PistonHelper.canPlaceCrystal(this.crystalPos, this.airPlace.getValue())
                        )) {
                        if (this.breakCrystal.getValue() && nowMs - this.breakTimerMs >= this.delay.getValue().longValue() * 50L) {
                           for (int yOff = -1; yOff <= 4; yOff++) {
                              BlockPos check = this.target.blockPosition().above(yOff);
                              if (PistonHelper.hasCrystal(check) && (this.crystalPos == null || !this.crystalPos.equals(check))) {
                                 this.attackCrystalAt(check);
                                 this.lastActionMs = nowMs;
                                 this.breakTimerMs = nowMs;
                                 this.resetState();
                                 return;
                              }
                           }
                        }

                        SpeedMineModule speedMine = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(SpeedMineModule.class) : null;
                        boolean speedMineDigging = speedMine != null && speedMine.isToggled() && speedMine.primaryDigging();
                        boolean rotatedThisTick = this.lastActionMs == nowMs;
                        if (this.autoBreak.getValue() && !speedMineDigging && !rotatedThisTick) {
                           Iterator<BlockPos> it = this.placedRedstone.iterator();

                           while (it.hasNext()) {
                              BlockPos rPos = it.next();
                              if (!this.isRedstone(rPos)) {
                                 it.remove();
                              } else if (!rPos.equals(this.redstonePos) && this.inRange(rPos, this.placeRange.getValue().floatValue())) {
                                 this.breakBlock(rPos);
                                 it.remove();
                                 this.lastActionMs = nowMs;
                                 return;
                              }
                           }

                           it = this.placedPistons.iterator();

                           while (it.hasNext()) {
                              BlockPos pPos = it.next();
                              if (!(mc.level.getBlockState(pPos).getBlock() instanceof PistonBaseBlock)) {
                                 it.remove();
                              } else if (!pPos.equals(this.pistonPos) && this.inRange(pPos, this.placeRange.getValue().floatValue())) {
                                 this.breakBlock(pPos);
                                 it.remove();
                                 this.lastActionMs = nowMs;
                                 return;
                              }
                           }
                        }

                        long timeoutMs = (2L + this.delay.getValue().longValue()) * 50L;
                        if (this.autoBreak.getValue() && !speedMineDigging && nowMs - this.lastActionMs >= timeoutMs) {
                           Iterator<BlockPos> it = this.placedRedstone.iterator();

                           while (it.hasNext()) {
                              BlockPos rPos = it.next();
                              if (this.isRedstone(rPos)) {
                                 if (this.inRange(rPos, this.placeRange.getValue().floatValue())) {
                                    this.breakBlock(rPos);
                                    it.remove();
                                    this.lastActionMs = nowMs;
                                    this.resetState();
                                    return;
                                 }
                              } else {
                                 it.remove();
                              }
                           }

                           it = this.placedPistons.iterator();

                           while (it.hasNext()) {
                              BlockPos pPos = it.next();
                              if (mc.level.getBlockState(pPos).getBlock() instanceof PistonBaseBlock) {
                                 if (this.inRange(pPos, this.placeRange.getValue().floatValue())) {
                                    this.breakBlock(pPos);
                                    it.remove();
                                    this.lastActionMs = nowMs;
                                    this.resetState();
                                    return;
                                 }
                              } else {
                                 it.remove();
                              }
                           }
                        }
                     } else {
                        this.placeCrystal(crystalSlot, this.crystalPos);
                        this.lastActionMs = nowMs;
                        this.breakTimerMs = nowMs;
                     }
                  }
               } else {
                  this.resetState();
               }
            }
         }
      }
   }

    private void doPistonCrystal(Player tgt) {
        boolean ceilingSolidBodyGap;
        int holeDepth;
        if (this.isTargetPhased(tgt)) {
            return;
        }
        int ticks = this.extrapolationTicks.getValue().intValue();
        double ox = (tgt.getX() - tgt.xOld) * (double)ticks;
        double oy = (tgt.getY() - tgt.yOld) * (double)ticks * 0.3;
        double oz = (tgt.getZ() - tgt.zOld) * (double)ticks;
        double ex = tgt.getX() + ox;
        double ey = tgt.getY() + oy;
        double ez = tgt.getZ() + oz;
        BlockPos base = PistonCrystalModule.getEntityFeetPos((Entity)tgt, ex, ey, ez);
        AABB targetBox = tgt.getBoundingBox().move(ox, oy, oz);
        AABB selfBox = this.selfExtrapolate.getValue() ? PistonCrystalModule.mc.player.getBoundingBox().move((PistonCrystalModule.mc.player.getX() - PistonCrystalModule.mc.player.xOld) * (double)ticks, (PistonCrystalModule.mc.player.getY() - PistonCrystalModule.mc.player.yOld) * (double)ticks * 0.3, (PistonCrystalModule.mc.player.getZ() - PistonCrystalModule.mc.player.zOld) * (double)ticks) : PistonCrystalModule.mc.player.getBoundingBox();
        boolean isCrawling = tgt.getPose() == Pose.SWIMMING || tgt.isVisuallyCrawling() || tgt.getEyeHeight() <= 1.0f || tgt.getBoundingBox().getYsize() <= 1.0;
        for (holeDepth = 0; holeDepth <= 4 && !this.hasHorizontalOpening(base.above(holeDepth)); ++holeDepth) {
        }
        boolean isCrawlTrapped = isCrawling && PistonCrystalModule.mc.level != null && !PistonCrystalModule.mc.level.getBlockState(base.above(1)).isAir() && !this.hasHorizontalOpening(base.above(0));
        boolean isNormalTrapped = !isCrawling && PistonCrystalModule.mc.level != null && !PistonCrystalModule.mc.level.getBlockState(base.above(2)).isAir() && !this.hasHorizontalOpening(base.above(1));
        boolean isDeepHole = !isCrawling && holeDepth >= 2;
        boolean ceilingBlocksH2 = !PistonCrystalModule.mc.level.getBlockState(base.above(2)).isAir();
        boolean h2NeighborsWalled = !this.hasHorizontalOpening(base.above(2));
        boolean bl = ceilingSolidBodyGap = !isCrawling && PistonCrystalModule.mc.level != null && (ceilingBlocksH2 || h2NeighborsWalled) && this.hasHorizontalOpening(base.above(1));
        int[] heights = isCrawlTrapped ? new int[]{1, 0, 2} : (holeDepth >= 3 ? new int[]{holeDepth, holeDepth - 1, 2, 1, 0} : (isNormalTrapped || holeDepth == 2 ? new int[]{2, 1, 0, 3} : (ceilingSolidBodyGap ? new int[]{1, 0, 2, 3} : new int[]{0, 1, 2, 3})));
        boolean preferredHeightEligible = false;
        for (int h : heights) {
            if (h != this.preferredHeight) continue;
            preferredHeightEligible = true;
            break;
        }
        if (this.preferredHeight != -1 && preferredHeightEligible && this.preferredHeight != heights[0]) {
            int[] reordered = new int[heights.length];
            reordered[0] = this.preferredHeight;
            int idx = 1;
            for (int h : heights) {
                if (h == this.preferredHeight) continue;
                reordered[idx++] = h;
            }
            heights = reordered;
        }
        for (int h : heights) {
            boolean trappedAtHeight;
            BlockPos standPos = base.above(h);
            BlockPos exception = isCrawlTrapped && h == 1 || ceilingSolidBodyGap && h == 1 ? base.above(1) : (!(!isNormalTrapped && !isDeepHole || h != 2 && h != holeDepth) ? base.above(h) : null);
            float dmg = DamageUtils.getCrystalDamage((Entity)tgt, targetBox, standPos, exception, this.blockDestruction.getValue());
            float selfDmg = DamageUtils.getCrystalDamage((Entity)PistonCrystalModule.mc.player, selfBox, standPos, null, this.blockDestruction.getValue());
            boolean bl2 = trappedAtHeight = h == 1 && isCrawlTrapped || h == 1 && ceilingSolidBodyGap || h == 2 && isNormalTrapped || h == holeDepth && isDeepHole;
            if (trappedAtHeight) {
                if (dmg <= this.minDamage.getValue().floatValue() || selfDmg > this.maxLocalDamage.getValue().floatValue() || this.safety.getValue() && selfDmg >= PistonCrystalModule.mc.player.getHealth() + PistonCrystalModule.mc.player.getAbsorptionAmount()) continue;
                if (!this.trySearch(base, h, true)) {
                    this.tryPreBuildCrystalSupport(base, h);
                    continue;
                }
                this.preferredHeight = h;
                return;
            }
            if (dmg <= this.minDamage.getValue().floatValue() || selfDmg > this.maxLocalDamage.getValue().floatValue() || this.safety.getValue() && selfDmg >= PistonCrystalModule.mc.player.getHealth() + PistonCrystalModule.mc.player.getAbsorptionAmount()) continue;
            if (!this.trySearch(base, h, isNormalTrapped || isCrawlTrapped || isDeepHole)) {
                if (!this.tryPreBuildCrystalSupport(base, h)) continue;
                return;
            }
            this.preferredHeight = h;
            return;
        }
    }

    private boolean tryPreBuildCrystalSupport(BlockPos base, int height) {
        if (!this.autoSupport.getValue()) {
            return false;
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos supportBase;
            BlockPos temp1 = base.relative(dir).above(height);
            if (!this.inRange(temp1, this.placeRange.getValue().floatValue()) || (!PistonHelper.canPlaceCrystal(temp1, false) ? this.isObsidian(supportBase = temp1.below()) || !this.canSupportCrystal(temp1, supportBase) : !this.inRange(supportBase = temp1.relative(dir).below(), this.placeRange.getValue().floatValue()) || !PistonHelper.canPlace(supportBase, null, false, true))) continue;
            int supportSlot = this.findSupportBlock();
            if (supportSlot == -1) {
                return false;
            }
            this.placeBlock(supportSlot, supportBase, null);
            this.lastActionMs = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public static BlockPos getEntityFeetPos(Entity entity, double ex, double ey, double ez) {
        double topY;
        VoxelShape shape;
        if (PistonCrystalModule.mc.level == null) {
            return new BlockPos(Mth.floor((double)ex), Mth.floor((double)ey), Mth.floor((double)ez));
        }
        int floorY = Mth.floor((double)ey);
        BlockPos floorPos = new BlockPos(Mth.floor((double)ex), floorY, Mth.floor((double)ez));
        BlockState state = PistonCrystalModule.mc.level.getBlockState(floorPos);
        if (!state.isAir() && !state.canBeReplaced() && !(shape = state.getCollisionShape((BlockGetter)PistonCrystalModule.mc.level, floorPos)).isEmpty() && ey >= (topY = (double)floorPos.getY() + shape.max(Direction.Axis.Y)) - 0.05) {
            return floorPos.above();
        }
        return floorPos;
    }

    private boolean isTargetPhased(Player tgt) {
        if (PistonCrystalModule.mc.level == null || tgt == null) {
            return false;
        }
        BlockPos feetPos = PistonCrystalModule.getEntityFeetPos((Entity)tgt, tgt.getX(), tgt.getY(), tgt.getZ());
        BlockState feetState = PistonCrystalModule.mc.level.getBlockState(feetPos);
        return this.isHardPhasedBlock(feetState);
    }

    private boolean hasHorizontalOpening(BlockPos cell) {
        if (PistonCrystalModule.mc.level == null) {
            return true;
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockState state = PistonCrystalModule.mc.level.getBlockState(cell.relative(dir));
            if (!state.isAir() && !state.canBeReplaced()) continue;
            return true;
        }
        return false;
    }

    private boolean isDecoyOrPassableBlock(BlockState state) {
        if (state == null || state.isAir() || state.canBeReplaced()) {
            return true;
        }
        Block b = state.getBlock();
        return b instanceof WebBlock || b instanceof ButtonBlock || b instanceof MushroomBlock || b instanceof LadderBlock || b instanceof ScaffoldingBlock || b instanceof FlowerBlock || b instanceof FlowerPotBlock || b instanceof TallFlowerBlock || b instanceof VineBlock || b instanceof TorchBlock || b instanceof LeverBlock || b instanceof PressurePlateBlock || b instanceof SignBlock || b instanceof BannerBlock || b instanceof CarpetBlock || b instanceof TripWireBlock || b instanceof TripWireHookBlock || b instanceof BaseRailBlock || b == Blocks.COBWEB || b == Blocks.LADDER || b == Blocks.SCAFFOLDING || b == Blocks.VINE || b == Blocks.BROWN_MUSHROOM || b == Blocks.RED_MUSHROOM || b == Blocks.TORCH || b == Blocks.SOUL_TORCH || b == Blocks.REDSTONE_TORCH;
    }

    private boolean isHardPhasedBlock(BlockState state) {
        if (this.isDecoyOrPassableBlock(state)) {
            return false;
        }
        Block b = state.getBlock();
        if (b == Blocks.OBSIDIAN || b == Blocks.BEDROCK || b == Blocks.ENDER_CHEST || b == Blocks.RESPAWN_ANCHOR || b == Blocks.CRYING_OBSIDIAN || b == Blocks.ANVIL || b == Blocks.CHIPPED_ANVIL || b == Blocks.DAMAGED_ANVIL || b == Blocks.REINFORCED_DEEPSLATE || b == Blocks.NETHERITE_BLOCK || b == Blocks.ANCIENT_DEBRIS) {
            return true;
        }
        return state.isSolid() && state.blocksMotion();
    }

    private boolean trySearch(BlockPos base, int height, boolean isTrapped) {
        ArrayList<Direction> dirs = new ArrayList<Direction>();
        Direction facing = PistonCrystalModule.mc.player.getDirection();
        dirs.add(facing);
        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (d == facing) continue;
            dirs.add(d);
        }
        boolean airPlaceActive = this.airPlace.getValue();
        for (Direction dir : dirs) {
            ArrayList<Direction> dir2Candidates;
            BlockPos temp1 = base.relative(dir).above(height);
            if (!this.inRange(temp1, this.placeRange.getValue().floatValue()) || this.strictDirection.getValue() && !WorldUtils.canSeeBlock(temp1)) continue;
            BlockPos needCrystalSupport = null;
            if (!(PistonHelper.canPlaceCrystal(temp1, airPlaceActive) || isTrapped && PistonHelper.canPlaceCrystalIgnoreHeadroom(temp1, airPlaceActive))) {
                BlockPos supportBase;
                if (airPlaceActive || !this.autoSupport.getValue() || !this.canSupportCrystal(temp1, supportBase = temp1.below())) continue;
                needCrystalSupport = supportBase;
            }
            BlockPos tempPiston = null;
            BlockPos tempRedstone = null;
            BlockPos needPistonSupport = null;
            if (isTrapped) {
                dir2Candidates = new ArrayList<Direction>(4);
                dir2Candidates.add(dir);
                dir2Candidates.add(dir.getClockWise());
                dir2Candidates.add(dir.getCounterClockWise());
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    if (d == dir.getOpposite() || dir2Candidates.contains(d)) continue;
                    dir2Candidates.add(d);
                }
            } else {
                dir2Candidates = new ArrayList(3);
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    if (d == dir.getOpposite()) continue;
                    dir2Candidates.add(d);
                }
            }
            block4: for (Direction dir2 : dir2Candidates) {
                boolean pistonCanAttachToCrystalSupport;
                boolean pistonAlreadyThere;
                BlockPos extCheck;
                BlockPos temp2 = temp1.relative(dir).relative(dir2);
                if ((!isTrapped || dir2 == dir) && !PistonCrystalModule.mc.level.getBlockState(extCheck = temp2.relative(dir.getOpposite())).isAir() && !PistonCrystalModule.mc.level.getBlockState(extCheck).canBeReplaced() || !(pistonAlreadyThere = PistonCrystalModule.mc.level.getBlockState(temp2).getBlock() instanceof PistonBaseBlock) && this.strictDirection.getValue() && !WorldUtils.canSeeBlock(temp2)) continue;
                needPistonSupport = null;
                boolean pistonPlaceableNow = PistonHelper.canPlace(temp2, null, airPlaceActive, true) || pistonAlreadyThere;
                boolean bl = pistonCanAttachToCrystalSupport = !airPlaceActive && needCrystalSupport != null && temp2.distManhattan((Vec3i)needCrystalSupport) == 1;
                if (!pistonPlaceableNow && !pistonCanAttachToCrystalSupport) {
                    BlockPos foundSupport;
                    if (airPlaceActive || !this.autoSupport.getValue() || (foundSupport = this.findSupportForPiston(temp2, dir, dir2, needCrystalSupport)) == null) continue;
                    needPistonSupport = foundSupport;
                }
                if (!this.inRange(temp2, this.placeRange.getValue().floatValue())) continue;
                tempPiston = temp2;
                for (Direction dir3 : Direction.values()) {
                    if (dir3 == dir.getOpposite()) continue;
                    BlockPos temp3 = temp2.relative(dir3);
                    if (needPistonSupport != null && temp3.equals((Object)needPistonSupport) || needCrystalSupport != null && temp3.equals((Object)needCrystalSupport)) continue;
                    if (PistonCrystalModule.mc.level.getBlockState(temp3).getBlock() == Blocks.REDSTONE_BLOCK || PistonCrystalModule.mc.level.getBlockState(temp3).getBlock() == Blocks.REDSTONE_TORCH) {
                        tempRedstone = temp3;
                        break block4;
                    }
                    if (!PistonHelper.canPlace(temp3, null, airPlaceActive, true) || !this.inRange(temp3, this.placeRange.getValue().floatValue()) || this.strictDirection.getValue() && !WorldUtils.canSeeBlock(temp3)) continue;
                    tempRedstone = temp3;
                    break block4;
                }
            }
            if (tempPiston == null || tempRedstone == null) continue;
            this.face = dir;
            this.crystalPos = temp1;
            this.crystalForcedPush = isTrapped;
            this.pistonPos = tempPiston;
            this.redstonePos = tempRedstone;
            this.crystalSupportPos = needCrystalSupport;
            this.pistonSupportPos = needPistonSupport;
            return true;
        }
        return false;
    }

    private boolean placeBlock(int slot, BlockPos pos, Direction pistonDir) {
        Direction side;
        if (slot == -1) {
            return false;
        }
        if (pistonDir != null) {
            if (this.strictDirection.getValue()) {
                List<Direction> visible = WorldUtils.getDirections(pos, null, true);
                side = visible.contains(pistonDir) ? pistonDir : (visible.isEmpty() ? null : visible.get(0));
                if (side == null && !this.airPlace.getValue()) {
                    return false;
                }
            } else {
                side = PistonHelper.getPlaceSide(pos, d -> true);
                if (side == null && !this.airPlace.getValue()) {
                    return false;
                }
            }
        } else if (this.strictDirection.getValue() ? (side = WorldUtils.getDirection(pos, true)) == null : (side = PistonHelper.getPlaceSide(pos, d -> true)) == null && !this.airPlace.getValue()) {
            return false;
        }
        int prevSlot = PistonCrystalModule.mc.player.getInventory().getSelectedSlot();
        this.doSwap(slot, prevSlot);
        if (this.shouldRotate()) {
            if (pistonDir != null) {
                this.pistonFacingSnap(pistonDir);
            } else {
                Direction rotSide = side != null ? side : Direction.UP;
                this.doRotate(pos, rotSide);
            }
        }
        if (side != null) {
            PistonHelper.placeBlock(pos, side, false);
        } else {
            PistonHelper.clickBlock(pos, Direction.UP, false);
        }
        this.restoreSlot(slot, prevSlot);
        Night.RENDER_MANAGER.getRenderPositions().removeIf(p -> p.getPos().equals((Object)pos));
        Night.RENDER_MANAGER.getRenderPositions().add(new RenderPosition(pos));
        return true;
    }

    private void placeCrystal(int slot, BlockPos cp) {
        BlockPos base = cp.below();
        Direction side = PistonHelper.getClickSide(base);
        if (side == null) {
            if (this.airPlace.getValue()) {
                base = cp;
                side = Direction.UP;
            } else {
                side = Direction.UP;
            }
        }
        if (slot == -1) {
            return;
        }
        int prevSlot = PistonCrystalModule.mc.player.getInventory().getSelectedSlot();
        this.doSwap(slot, prevSlot);
        if (this.shouldRotate()) {
            this.doRotate(base, side);
        }
        PistonHelper.clickBlock(base, side, false);
        this.restoreSlot(slot, prevSlot);
        Night.RENDER_MANAGER.getRenderPositions().removeIf(p -> p.getPos().equals((Object)cp));
        Night.RENDER_MANAGER.getRenderPositions().add(new RenderPosition(cp));
    }

    private void placeTorch(int slot, BlockPos pos) {
        if (!PistonHelper.canPlace(pos, null, this.airPlace.getValue(), true) || slot == -1) {
            return;
        }
        List<Direction> sides = PistonHelper.getPlaceSides(pos, null);
        if (sides.isEmpty()) {
            if (this.airPlace.getValue()) {
                int prevSlot = PistonCrystalModule.mc.player.getInventory().getSelectedSlot();
                this.doSwap(slot, prevSlot);
                if (this.shouldRotate()) {
                    this.doRotate(pos, Direction.UP);
                }
                PistonHelper.clickBlock(pos, Direction.UP, false);
                this.restoreSlot(slot, prevSlot);
                Night.RENDER_MANAGER.getRenderPositions().removeIf(p -> p.getPos().equals((Object)pos));
                Night.RENDER_MANAGER.getRenderPositions().add(new RenderPosition(pos));
                return;
            }
            return;
        }
        int prevSlot = PistonCrystalModule.mc.player.getInventory().getSelectedSlot();
        for (Direction side : sides) {
            if (PistonCrystalModule.mc.level.getBlockState(pos.relative(side)).getBlock() instanceof PistonBaseBlock || side == Direction.UP) continue;
            this.doSwap(slot, prevSlot);
            if (this.shouldRotate()) {
                this.doRotate(pos, side);
            }
            PistonHelper.placeBlock(pos, side, false);
            this.restoreSlot(slot, prevSlot);
            Night.RENDER_MANAGER.getRenderPositions().removeIf(p -> p.getPos().equals((Object)pos));
            Night.RENDER_MANAGER.getRenderPositions().add(new RenderPosition(pos));
            return;
        }
    }

   private void attackCrystalAt(BlockPos pos) {
      List<EndCrystal> crystals = mc.level.getEntitiesOfClass(EndCrystal.class, new AABB(pos).inflate(1.0), e -> e.isAlive());
      if (!crystals.isEmpty()) {
         double brSq = this.breakRange.getValue().floatValue() * this.breakRange.getValue().floatValue();
         EndCrystal crystal = crystals.stream()
            .filter(e -> mc.player.distanceToSqr(e) <= brSq)
            .min(Comparator.comparingDouble(e -> mc.player.distanceToSqr(e)))
            .orElse(null);
         if (crystal != null) {
            if (this.shouldRotate()) {
               Vec3 hitVec = crystal.position().add(0.0, 0.5, 0.0);
               float[] rots = RotationUtils.getRotations(hitVec);
               Night.ROTATION_MANAGER.silentRotate(rots[0], rots[1]);
            }

            mc.getConnection().send(new ServerboundAttackPacket(crystal.getId()));
            mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
         }
      }
   }

    private void breakBlock(BlockPos pos) {
        if (PistonCrystalModule.mc.player == null || mc.getConnection() == null || PistonCrystalModule.mc.level == null || pos == null) {
            return;
        }
        Direction side = PistonHelper.getClickSide(pos);
        if (side == null) {
            side = Direction.UP;
        }
        int pickSlot = this.findPickaxe();
        int prevSlot = PistonCrystalModule.mc.player.getInventory().getSelectedSlot();
        if (pickSlot != -1) {
            this.doSwap(pickSlot, prevSlot);
        }
        if (this.shouldRotate()) {
            this.doRotate(pos, side);
        }
        Direction finalSide = side;
        NetworkUtils.sendSequencedPacket(seq -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, finalSide, seq));
        NetworkUtils.sendSequencedPacket(seq -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, pos, finalSide, seq));
        mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        if (pickSlot != -1) {
            this.restoreSlot(pickSlot, prevSlot);
        }
    }

    private int findPickaxe() {
        int slot = this.findItem(Items.NETHERITE_PICKAXE);
        if (slot != -1) {
            return slot;
        }
        slot = this.findItem(Items.DIAMOND_PICKAXE);
        if (slot != -1) {
            return slot;
        }
        slot = this.findItem(Items.IRON_PICKAXE);
        if (slot != -1) {
            return slot;
        }
        return -1;
    }

    private boolean shouldRotate() {
        return !this.rotate.getValue().equalsIgnoreCase("None");
    }

    private void doRotate(BlockPos pos, Direction side) {
        Vec3 hitVec = Vec3.atCenterOf((Vec3i)pos).add((double)side.getStepX() * 0.5, (double)side.getStepY() * 0.5, (double)side.getStepZ() * 0.5);
        float[] rots = RotationUtils.getRotations(hitVec);
        Night.ROTATION_MANAGER.silentRotate(rots[0], rots[1]);
    }

    private void pistonFacingSnap(Direction dir) {
        if (dir == Direction.EAST) {
            Night.ROTATION_MANAGER.silentRotate(-90.0f, 0.0f);
        } else if (dir == Direction.WEST) {
            Night.ROTATION_MANAGER.silentRotate(90.0f, 0.0f);
        } else if (dir == Direction.NORTH) {
            Night.ROTATION_MANAGER.silentRotate(180.0f, 0.0f);
        } else {
            Night.ROTATION_MANAGER.silentRotate(0.0f, 0.0f);
        }
    }

    private void doSwap(int slot, int previousSlot) {
        InventoryUtils.switchSlot(this.swapAction.getValue(), slot, previousSlot);
    }

    private void restoreSlot(int slot, int previousSlot) {
        InventoryUtils.switchBack(this.swapAction.getValue(), slot, previousSlot);
    }

    private int findItem(Item item) {
        if (this.swapAction.getValue().equalsIgnoreCase("None")) {
            ItemStack h = PistonCrystalModule.mc.player.getMainHandItem();
            return !h.isEmpty() && h.getItem() == item ? PistonCrystalModule.mc.player.getInventory().getSelectedSlot() : -1;
        }
        int end = this.swapAction.getValue().equalsIgnoreCase("AltSwap") || this.swapAction.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END;
        for (int i = 0; i <= end; ++i) {
            ItemStack s = PistonCrystalModule.mc.player.getInventory().getItem(i);
            if (s.isEmpty() || s.getItem() != item) continue;
            return i;
        }
        return -1;
    }

    private int findPiston() {
        int slot = this.findBlock(Blocks.PISTON);
        if (slot != -1) {
            return slot;
        }
        return this.findBlock(Blocks.STICKY_PISTON);
    }

    private int findBlock(Block block) {
        if (this.swapAction.getValue().equalsIgnoreCase("None")) {
            BlockItem bi;
            Item item;
            ItemStack h = PistonCrystalModule.mc.player.getMainHandItem();
            if (!h.isEmpty() && (item = h.getItem()) instanceof BlockItem && (bi = (BlockItem)item).getBlock() == block) {
                return PistonCrystalModule.mc.player.getInventory().getSelectedSlot();
            }
            return -1;
        }
        int end = this.swapAction.getValue().equalsIgnoreCase("AltSwap") || this.swapAction.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END;
        for (int i = 0; i <= end; ++i) {
            BlockItem bi;
            Item item;
            ItemStack s = PistonCrystalModule.mc.player.getInventory().getItem(i);
            if (s.isEmpty() || !((item = s.getItem()) instanceof BlockItem) || (bi = (BlockItem)item).getBlock() != block) continue;
            return i;
        }
        return -1;
    }

    private int findRedstone() {
        int slot = this.findItem(Items.REDSTONE_BLOCK);
        if (slot != -1) {
            this.usingTorch = false;
            return slot;
        }
        slot = this.findItem(Items.REDSTONE_TORCH);
        if (slot != -1) {
            this.usingTorch = true;
            return slot;
        }
        return -1;
    }

    private Player pickTarget() {
        double r = this.targetRange.getValue().floatValue();
        return PistonCrystalModule.mc.level.players().stream().filter(p -> p != PistonCrystalModule.mc.player).filter(p -> !EntityUtils.isGhost((Entity)p)).filter(p -> !p.isSpectator() && p.isAlive()).filter(p -> PistonCrystalModule.mc.player.distanceToSqr((Entity)p) <= r * r).filter(p -> !Night.FRIEND_MANAGER.contains(p.getName().getString())).filter(p -> this.inAirTarget.getValue() || p.onGround()).min(this.getComparator()).orElse(null);
    }

    private Comparator<Player> getComparator() {
        return switch (this.sort.getValue()) {
            case "LowestHealth" -> Comparator.comparingDouble(e -> e.getHealth() + e.getAbsorptionAmount());
            default -> Comparator.comparingDouble(e -> PistonCrystalModule.mc.player.distanceToSqr((Entity)e));
        };
    }

    private boolean inRange(BlockPos pos, double r) {
        return PistonCrystalModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)pos)) <= r * r;
    }

    private boolean canSupportCrystal(BlockPos crystalPos, BlockPos supportBase) {
        if (PistonCrystalModule.mc.level == null || supportBase == null || crystalPos == null) {
            return false;
        }
        if (!this.inRange(supportBase, this.placeRange.getValue().floatValue())) {
            return false;
        }
        if (!PistonCrystalModule.mc.level.getBlockState(crystalPos).isAir() && !PistonCrystalModule.mc.level.getBlockState(crystalPos).canBeReplaced()) {
            return false;
        }
        if (!PistonCrystalModule.mc.level.getBlockState(crystalPos.above()).isAir() && !PistonCrystalModule.mc.level.getBlockState(crystalPos.above()).canBeReplaced()) {
            return false;
        }
        if (PistonHelper.hasEntityBlockCrystal(crystalPos, false) || PistonHelper.hasEntityBlockCrystal(crystalPos.above(), false)) {
            return false;
        }
        if (!PistonCrystalModule.mc.level.getBlockState(supportBase).isAir() && !PistonCrystalModule.mc.level.getBlockState(supportBase).canBeReplaced()) {
            return false;
        }
        if (!PistonHelper.canPlace(supportBase, null, false, true)) {
            return false;
        }
        return !this.strictDirection.getValue() || WorldUtils.getDirection(supportBase, true) != null;
    }

    private BlockPos findSupportForPiston(BlockPos pistonPos, Direction pushDir, Direction lateralDir, BlockPos pendingCrystalSupport) {
        Direction[] candidates;
        if (PistonCrystalModule.mc.level == null || pistonPos == null) {
            return null;
        }
        if (pendingCrystalSupport != null && pistonPos.distManhattan((Vec3i)pendingCrystalSupport) == 1) {
            return null;
        }
        for (Direction d : candidates = new Direction[]{Direction.DOWN, lateralDir, lateralDir.getOpposite()}) {
            BlockPos help = pistonPos.relative(d);
            if (!this.inRange(help, this.placeRange.getValue().floatValue()) || !PistonCrystalModule.mc.level.getBlockState(help).isAir() && !PistonCrystalModule.mc.level.getBlockState(help).canBeReplaced() || !PistonHelper.canPlace(help, null, false, true) || this.strictDirection.getValue() && WorldUtils.getDirection(help, true) == null) continue;
            return help;
        }
        return null;
    }

    private BlockPos findPistonSupportRepair(BlockPos pistonPos, Direction pushDir) {
        if (PistonCrystalModule.mc.level == null || pistonPos == null) {
            return null;
        }
        BlockPos down = pistonPos.below();
        if (!this.isReservedForSetup(down) && this.inRange(down, this.placeRange.getValue().floatValue()) && (PistonCrystalModule.mc.level.getBlockState(down).isAir() || PistonCrystalModule.mc.level.getBlockState(down).canBeReplaced()) && PistonHelper.canPlace(down, null, false, true)) {
            return down;
        }
        for (Direction d : Direction.Plane.HORIZONTAL) {
            BlockPos help;
            if (pushDir != null && d == pushDir.getOpposite() || this.isReservedForSetup(help = pistonPos.relative(d)) || !this.inRange(help, this.placeRange.getValue().floatValue()) || !PistonCrystalModule.mc.level.getBlockState(help).isAir() && !PistonCrystalModule.mc.level.getBlockState(help).canBeReplaced() || !PistonHelper.canPlace(help, null, false, true)) continue;
            return help;
        }
        return null;
    }

    private boolean isReservedForSetup(BlockPos pos) {
        return pos.equals((Object)this.redstonePos) || pos.equals((Object)this.crystalPos) || pos.equals((Object)this.crystalSupportPos);
    }

    public int findSupportBlock() {
        int slot = this.findBlock(Blocks.OBSIDIAN);
        if (slot == -1) {
            slot = this.findBlock(Blocks.CRYING_OBSIDIAN);
        }
        if (slot == -1) {
            slot = this.findBlock(Blocks.ENDER_CHEST);
        }
        if (slot == -1) {
            slot = this.findBlock(Blocks.NETHERITE_BLOCK);
        }
        if (slot == -1) {
            slot = this.findBlock(Blocks.COBBLESTONE);
        }
        if (slot == -1) {
            slot = this.findBlock(Blocks.STONE);
        }
        return slot;
    }

    private boolean isObsidian(BlockPos pos) {
        if (PistonCrystalModule.mc.level == null || pos == null) {
            return false;
        }
        Block b = PistonCrystalModule.mc.level.getBlockState(pos).getBlock();
        return b == Blocks.OBSIDIAN || b == Blocks.CRYING_OBSIDIAN || b == Blocks.BEDROCK || b == Blocks.ENDER_CHEST;
    }

    private boolean isSolidBlock(BlockPos pos) {
        if (PistonCrystalModule.mc.level == null || pos == null) {
            return false;
        }
        BlockState state = PistonCrystalModule.mc.level.getBlockState(pos);
        return !state.isAir() && state.isSolid();
    }

    private boolean isRedstone(BlockPos pos) {
        if (PistonCrystalModule.mc.level == null || pos == null) {
            return false;
        }
        Block b = PistonCrystalModule.mc.level.getBlockState(pos).getBlock();
        return b == Blocks.REDSTONE_BLOCK || b == Blocks.REDSTONE_TORCH;
    }

    private void resetState() {
        this.pistonPos = null;
        this.redstonePos = null;
        this.crystalPos = null;
        this.crystalForcedPush = false;
        this.crystalSupportPos = null;
        this.pistonSupportPos = null;
        this.lastPiston = null;
        this.face = null;
        this.usingTorch = false;
    }
}

