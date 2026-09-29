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
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
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
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.TickEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.minecraft.DamageUtils;
import night.utils.minecraft.EntityUtils;
import night.utils.minecraft.InventoryUtils;
import night.utils.minecraft.PistonHelper;
import night.utils.minecraft.WorldUtils;
import night.utils.miscellaneous.RenderPosition;
import night.utils.rotations.RotationUtils;

@RegisterModule(name="PistonKick", description="Pushes players out of holes or solid blocks using pistons.", category=Module.Category.COMBAT)
public final class PistonKickModule
extends Module {
    public static PistonKickModule INSTANCE;
    public NumberSetting delay = new NumberSetting("Delay", "Delay between pushes (ticks)", 1, 0, 10);
    public NumberSetting pushDelay = new NumberSetting("PushDelay", "Duration to hold piston extended before retracting (ms)", 150, 50, 500);
    public ModeSetting power = new ModeSetting("Power", "How the piston is powered.", "Redstone", new String[]{"Redstone", "Lever"});
    public BooleanSetting airPlace = new BooleanSetting("AirPlace", "Allow placing when not on ground.", false);
    public BooleanSetting oneTwelve = new BooleanSetting("1.12", "Place piston and power in the same tick.", true);
    public NumberSetting targetRange = new NumberSetting("TargetRange", "Target range.", Float.valueOf(5.0f), Float.valueOf(1.0f), Float.valueOf(6.0f));
    public BooleanSetting inAirTarget = new BooleanSetting("InAirTarget", "Target players in the air.", true);
    public ModeSetting rotate = new ModeSetting("Rotate", "Silently rotate when placing.", "Silent", new String[]{"None", "Normal", "Silent"});
    public BooleanSetting breakCrystal = new BooleanSetting("BreakCrystal", "Break end crystals blocking piston path.", false);
    public NumberSetting placeRange = new NumberSetting("PlaceRange", "Block place range.", Float.valueOf(5.0f), Float.valueOf(1.0f), Float.valueOf(6.0f));
    public NumberSetting breakRange = new NumberSetting("BreakRange", "Crystal break range.", new BooleanSetting.Visibility(this.breakCrystal, true), (Number)Float.valueOf(4.5f), (Number)Float.valueOf(1.0f), (Number)Float.valueOf(6.0f));
    public NumberSetting maxLocalDamage = new NumberSetting("MaxLocalDamage", "Max self-damage allowed to break a crystal.", new BooleanSetting.Visibility(this.breakCrystal, true), (Number)Float.valueOf(8.0f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(20.0f));
    public BooleanSetting safety = new BooleanSetting("Safety", "Never break a crystal that would kill you.", new BooleanSetting.Visibility(this.breakCrystal, true), true);
    public ModeSetting swapAction = new ModeSetting("Swap", "How to swap to required items.", "Silent", InventoryUtils.SWITCH_MODES);
    public BooleanSetting consecutive = new BooleanSetting("Consecutive", "Keep targeting the same player consecutively.", true);
    public BooleanSetting merge = new BooleanSetting("Merge", "Burst piston and power packets together.", false);
    public BooleanSetting pauseOnEat = new BooleanSetting("PauseOnEat", "Pause while eating or using items.", false);
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Use raytrace to verify line-of-sight before placing piston/redstone (required on servers that check raytrace).", false);
    public BooleanSetting render = new BooleanSetting("Render", "Render placed blocks using RendersModule.", true);
    private BlockPos targetPhasedPos;
    private BlockPos pistonPos;
    private BlockPos redstonePos;
    private BlockPos supportPos;
    private final List<BlockPos> supportPositions = new ArrayList<BlockPos>();
    private Direction pushDir;
    private Player currentTarget;
    private long lastActionMs = 0L;
    private long extendedSinceMs = 0L;
    private long leverTurnedOnMs = 0L;

    public PistonKickModule() {
        INSTANCE = this;
    }

    public static PistonKickModule getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        this.resetState();
    }

    @Override
    public void onDisable() {
        this.resetState();
    }

    private void resetState() {
        this.targetPhasedPos = null;
        this.pistonPos = null;
        this.redstonePos = null;
        this.supportPos = null;
        this.supportPositions.clear();
        this.pushDir = null;
        this.currentTarget = null;
        this.extendedSinceMs = 0L;
        this.leverTurnedOnMs = 0L;
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        boolean needPiston;
        boolean isSupportRedstone;
        int supportSlot;
        if (PistonKickModule.mc.player == null || PistonKickModule.mc.level == null) {
            return;
        }
        if (this.pauseOnEat.getValue() && EntityUtils.isEating()) {
            return;
        }
        long nowMs = System.currentTimeMillis();
        if (nowMs - this.lastActionMs < this.delay.getValue().longValue() * 50L) {
            return;
        }
        int pistonSlot = this.findPiston();
        int powerSlot = this.findPower();
        if (pistonSlot == -1 || powerSlot == -1) {
            this.resetState();
            return;
        }
        if (this.currentTarget != null) {
            if (!this.currentTarget.isAlive() || this.currentTarget.isSpectator() || PistonKickModule.mc.player.distanceToSqr((Entity)this.currentTarget) > (double)(this.targetRange.getValue().floatValue() * this.targetRange.getValue().floatValue())) {
                this.resetState();
            } else {
                List<BlockPos> phasedList = this.getPhasedSolidBlocks(this.currentTarget);
                if (phasedList.isEmpty()) {
                    this.resetState();
                } else if (this.targetPhasedPos == null || !phasedList.contains(this.targetPhasedPos)) {
                    this.targetPhasedPos = phasedList.get(0);
                }
            }
        }
        if (this.currentTarget == null) {
            List<Player> enemies = this.getEnemies();
            for (Player enemy : enemies) {
                List<BlockPos> phasedList;
                if (!this.inAirTarget.getValue() && !enemy.onGround() || (phasedList = this.getPhasedSolidBlocks(enemy)).isEmpty()) continue;
                this.currentTarget = enemy;
                this.targetPhasedPos = phasedList.get(0);
                break;
            }
        }
        if (this.currentTarget == null || this.targetPhasedPos == null) {
            this.resetState();
            return;
        }
        if (this.breakCrystal.getValue()) {
            this.breakNearbyCrystals(this.currentTarget);
        }
        if (this.pistonPos != null && this.pushDir != null) {
            boolean pistonPlaced = this.isPistonPlacedAndFacing(this.pistonPos, this.pushDir);
            boolean pistonExtended = this.isPistonExtended(this.pistonPos);
            boolean isPowered = this.isPistonPowered(this.pistonPos);
            boolean isLeverMode = this.power.getValue().equalsIgnoreCase("Lever");
            if (pistonExtended) {
                long pushDuration;
                if (this.extendedSinceMs == 0L) {
                    this.extendedSinceMs = nowMs;
                }
                if (nowMs - this.extendedSinceMs < (pushDuration = Math.max(50L, this.pushDelay.getValue().longValue())) || this.leverTurnedOnMs > 0L && nowMs - this.leverTurnedOnMs < 50L) {
                    this.lastActionMs = nowMs;
                    return;
                }
                if (isLeverMode && this.redstonePos != null && this.isLever(this.redstonePos) && this.isLeverPowered(this.redstonePos)) {
                    this.clickLever(this.redstonePos);
                    this.leverTurnedOnMs = 0L;
                    this.lastActionMs = nowMs;
                    return;
                }
                if (isLeverMode && this.redstonePos != null && !this.isLeverPowered(this.redstonePos) && pistonExtended) {
                    this.lastActionMs = nowMs;
                    return;
                }
                List<BlockPos> remaining = this.getPhasedSolidBlocks(this.currentTarget);
                if (remaining.isEmpty()) {
                    this.resetState();
                    return;
                }
                this.pistonPos = null;
                this.redstonePos = null;
                this.supportPos = null;
                this.pushDir = null;
                this.extendedSinceMs = 0L;
                this.leverTurnedOnMs = 0L;
                this.lastActionMs = nowMs;
            } else {
                this.extendedSinceMs = 0L;
            }
            if (pistonPlaced && isPowered && !pistonExtended) {
                this.lastActionMs = nowMs;
                return;
            }
            boolean air = this.airPlace.getValue();
            if (!pistonPlaced && !PistonHelper.canPlace(this.pistonPos, null, air, true)) {
                boolean hasSupport;
                boolean bl = hasSupport = !isLeverMode && this.redstonePos != null && this.redstonePos.equals((Object)this.pistonPos.below()) && (PistonHelper.canPlace(this.redstonePos, null, air, true) || this.isPowerSource(this.redstonePos)) || !this.supportPositions.isEmpty() && this.supportPositions.stream().anyMatch(p -> PistonHelper.canPlace(p, null, air, true) || this.isSolidBlock((BlockPos)p)) || this.supportPos != null && (PistonHelper.canPlace(this.supportPos, null, air, true) || this.isSolidBlock(this.supportPos));
                if (!hasSupport) {
                    this.resetState();
                }
            }
        }
        if (!(this.pistonPos != null && this.pushDir != null || this.searchPlacement(this.currentTarget))) {
            return;
        }
        if (this.pistonPos == null || this.pushDir == null) {
            return;
        }
        boolean air = this.airPlace.getValue();
        boolean isLeverMode = this.power.getValue().equalsIgnoreCase("Lever");
        for (BlockPos sPos : this.supportPositions) {
            int supportSlot2;
            if (this.isSolidBlock(sPos) || !PistonHelper.canPlace(sPos, null, air, true) || (supportSlot2 = this.findSupportBlock()) == -1) continue;
            this.placeBlock(supportSlot2, sPos, null);
            this.lastActionMs = nowMs;
            if (this.oneTwelve.getValue() || this.merge.getValue()) continue;
            return;
        }
        if (this.supportPos != null && !this.isSolidBlock(this.supportPos) && !this.supportPositions.contains(this.supportPos) && PistonHelper.canPlace(this.supportPos, null, air, true) && (supportSlot = this.findSupportBlock()) != -1) {
            this.placeBlock(supportSlot, this.supportPos, null);
            this.lastActionMs = nowMs;
            if (!this.oneTwelve.getValue() && !this.merge.getValue()) {
                return;
            }
        }
        boolean bl = isSupportRedstone = !isLeverMode && this.redstonePos != null && this.redstonePos.equals((Object)this.pistonPos.below());
        if (isSupportRedstone && !this.isPowerSource(this.redstonePos) && PistonHelper.canPlace(this.redstonePos, null, air, true)) {
            this.placeBlock(powerSlot, this.redstonePos, null);
            this.lastActionMs = nowMs;
            if (!this.oneTwelve.getValue() && !this.merge.getValue()) {
                return;
            }
        }
        boolean bl2 = needPiston = !this.isPistonPlacedAndFacing(this.pistonPos, this.pushDir);
        if (needPiston && PistonHelper.canPlace(this.pistonPos, null, air, true)) {
            this.placeBlock(pistonSlot, this.pistonPos, this.pushDir);
            this.lastActionMs = nowMs;
            if (!this.oneTwelve.getValue() && !this.merge.getValue()) {
                return;
            }
        }
        if (this.redstonePos != null && !this.isPistonPowered(this.pistonPos) && !this.isPowerSource(this.redstonePos)) {
            boolean canPlacePower;
            boolean bl3 = canPlacePower = !isLeverMode || this.isPistonPlacedAndFacing(this.pistonPos, this.pushDir) || air;
            if (canPlacePower && PistonHelper.canPlace(this.redstonePos, null, air, true)) {
                this.placeBlock(powerSlot, this.redstonePos, null);
                this.lastActionMs = nowMs;
                if (isLeverMode) {
                    this.clickLever(this.redstonePos);
                    this.leverTurnedOnMs = nowMs;
                }
            }
        } else if (isLeverMode && this.redstonePos != null && this.isLever(this.redstonePos) && !this.isLeverPowered(this.redstonePos) && !this.isPistonExtended(this.pistonPos) && this.extendedSinceMs == 0L) {
            this.clickLever(this.redstonePos);
            this.leverTurnedOnMs = nowMs;
            this.lastActionMs = nowMs;
        }
    }

    private boolean searchPlacement(Player target) {
        List<BlockPos> phasedBlocks = this.getPhasedSolidBlocks(target);
        if (phasedBlocks.isEmpty()) {
            return false;
        }
        if (phasedBlocks.size() >= 2) {
            Direction playerFacing = PistonKickModule.mc.player.getDirection();
            Direction.Axis verticalAxis = playerFacing.getAxis();
            phasedBlocks.sort((b1, b2) -> {
                boolean b1IsVertical = verticalAxis == Direction.Axis.Z && b1.getZ() != b2.getZ() || verticalAxis == Direction.Axis.X && b1.getX() != b2.getX();
                return b1IsVertical ? -1 : 1;
            });
        }
        for (BlockPos phasedPos : phasedBlocks) {
            if (!this.searchPlacementForBlock(target, phasedPos)) continue;
            this.targetPhasedPos = phasedPos;
            return true;
        }
        return false;
    }

    private boolean searchPlacementForBlock(Player target, BlockPos phasedPos) {
        ArrayList<Direction> dirs = new ArrayList<Direction>();
        Direction playerFacing = PistonKickModule.mc.player.getDirection();
        dirs.add(playerFacing);
        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (d == playerFacing) continue;
            dirs.add(d);
        }
        BlockPos headPos = phasedPos.above();
        boolean air = this.airPlace.getValue();
        boolean isLeverMode = this.power.getValue().equalsIgnoreCase("Lever");
        for (Direction pDir : dirs) {
            BlockPos underPiston;
            boolean canPistonDirect;
            boolean isPPosAir;
            if (!this.isDestinationOpen(phasedPos, pDir)) continue;
            BlockPos pPos = headPos.relative(pDir.getOpposite());
            if (!PistonKickModule.mc.level.getBlockState(headPos).isAir() && !PistonKickModule.mc.level.getBlockState(headPos).canBeReplaced() || !this.inRange(pPos)) continue;
            boolean isPistonReady = this.isPistonPlacedAndFacing(pPos, pDir);
            boolean bl = isPPosAir = PistonKickModule.mc.level.getBlockState(pPos).isAir() || PistonKickModule.mc.level.getBlockState(pPos).canBeReplaced();
            if (!isPistonReady && !isPPosAir) continue;
            boolean bl2 = canPistonDirect = isPistonReady || PistonHelper.canPlace(pPos, null, air, true);
            if (canPistonDirect) {
                if (this.isPistonPowered(pPos)) {
                    this.supportPos = null;
                    this.pistonPos = pPos;
                    this.redstonePos = null;
                    this.pushDir = pDir;
                    return true;
                }
                if (isLeverMode) {
                    BlockPos pistonSupport = pPos.below();
                    boolean needPistonSupport = !air && !this.isSolidBlock(pistonSupport);
                    ArrayList<LeverCandidate> candidates = new ArrayList<LeverCandidate>();
                    BlockPos backPos = pPos.relative(pDir.getOpposite());
                    BlockPos backSupport = backPos.below();
                    ArrayList<BlockPos> backSupports = new ArrayList<BlockPos>();
                    if (needPistonSupport) {
                        backSupports.add(pistonSupport);
                    }
                    if (!air && !this.isSolidBlock(backSupport)) {
                        backSupports.add(backSupport);
                    }
                    candidates.add(new LeverCandidate(backPos, backSupports));
                    BlockPos leftPos = pPos.relative(pDir.getCounterClockWise());
                    BlockPos leftSupport = leftPos.below();
                    ArrayList<BlockPos> leftSupports = new ArrayList<BlockPos>();
                    if (needPistonSupport) {
                        leftSupports.add(pistonSupport);
                    }
                    if (!air && !this.isSolidBlock(leftSupport)) {
                        leftSupports.add(leftSupport);
                    }
                    candidates.add(new LeverCandidate(leftPos, leftSupports));
                    if (this.isSolidBlock(leftPos)) {
                        BlockPos leftAbovePos = leftPos.above();
                        ArrayList<BlockPos> leftAboveSupports = new ArrayList<BlockPos>();
                        if (needPistonSupport) {
                            leftAboveSupports.add(pistonSupport);
                        }
                        candidates.add(new LeverCandidate(leftAbovePos, leftAboveSupports));
                    }
                    BlockPos rightPos = pPos.relative(pDir.getClockWise());
                    BlockPos rightSupport = rightPos.below();
                    ArrayList<BlockPos> rightSupports = new ArrayList<BlockPos>();
                    if (needPistonSupport) {
                        rightSupports.add(pistonSupport);
                    }
                    if (!air && !this.isSolidBlock(rightSupport)) {
                        rightSupports.add(rightSupport);
                    }
                    candidates.add(new LeverCandidate(rightPos, rightSupports));
                    if (this.isSolidBlock(rightPos)) {
                        BlockPos rightAbovePos = rightPos.above();
                        ArrayList<BlockPos> rightAboveSupports = new ArrayList<BlockPos>();
                        if (needPistonSupport) {
                            rightAboveSupports.add(pistonSupport);
                        }
                        candidates.add(new LeverCandidate(rightAbovePos, rightAboveSupports));
                    }
                    if (this.isSolidBlock(backPos)) {
                        BlockPos back2Pos = pPos.relative(pDir.getOpposite(), 2);
                        ArrayList<BlockPos> back2Supports = new ArrayList<BlockPos>();
                        if (needPistonSupport) {
                            back2Supports.add(pistonSupport);
                        }
                        candidates.add(new LeverCandidate(back2Pos, back2Supports));
                        BlockPos backAbovePos = backPos.above();
                        ArrayList<BlockPos> backAboveSupports = new ArrayList<BlockPos>();
                        if (needPistonSupport) {
                            backAboveSupports.add(pistonSupport);
                        }
                        candidates.add(new LeverCandidate(backAbovePos, backAboveSupports));
                    }
                    BlockPos backBelowPos = pPos.relative(pDir.getOpposite()).below();
                    ArrayList<BlockPos> backBelowSupports = new ArrayList<BlockPos>();
                    if (needPistonSupport) {
                        backBelowSupports.add(pistonSupport);
                    }
                    candidates.add(new LeverCandidate(backBelowPos, backBelowSupports));
                    for (LeverCandidate candidate : candidates) {
                        boolean leverPlaceable;
                        BlockPos candidateRPos = candidate.leverPos;
                        if (candidateRPos.equals((Object)pPos.relative(pDir)) || candidateRPos.equals((Object)headPos) || candidateRPos.equals((Object)headPos.below()) || candidateRPos.equals((Object)phasedPos.relative(pDir)) || candidateRPos.equals((Object)headPos.relative(pDir)) || !this.inRange(candidateRPos)) continue;
                        boolean bl3 = leverPlaceable = this.isLever(candidateRPos) || PistonKickModule.mc.level.getBlockState(candidateRPos).isAir() || PistonKickModule.mc.level.getBlockState(candidateRPos).canBeReplaced();
                        if (!leverPlaceable) continue;
                        boolean supportsValid = true;
                        for (BlockPos s : candidate.neededSupports) {
                            if (s.equals((Object)headPos) || s.equals((Object)headPos.below()) || s.equals((Object)phasedPos.relative(pDir)) || s.equals((Object)headPos.relative(pDir))) {
                                supportsValid = false;
                                break;
                            }
                            if (!this.inRange(s)) {
                                supportsValid = false;
                                break;
                            }
                            if (this.isSolidBlock(s) || PistonHelper.canPlace(s, null, air, true)) continue;
                            supportsValid = false;
                            break;
                        }
                        if (!supportsValid) continue;
                        this.supportPositions.clear();
                        this.supportPositions.addAll(candidate.neededSupports);
                        this.supportPos = candidate.neededSupports.isEmpty() ? null : candidate.neededSupports.get(0);
                        this.pistonPos = pPos;
                        this.redstonePos = candidateRPos;
                        this.pushDir = pDir;
                        return true;
                    }
                } else {
                    BlockPos rPos = this.findBestPowerPos(pPos, headPos, phasedPos.relative(pDir), pDir);
                    if (rPos != null) {
                        this.supportPos = null;
                        this.pistonPos = pPos;
                        this.redstonePos = rPos;
                        this.pushDir = pDir;
                        return true;
                    }
                    for (Direction rDir : new Direction[]{Direction.UP, pDir.getOpposite(), pDir.getClockWise(), pDir.getCounterClockWise(), Direction.DOWN}) {
                        BlockPos candidateRPos = pPos.relative(rDir);
                        if (candidateRPos.equals((Object)pPos.relative(pDir)) || candidateRPos.equals((Object)headPos) || candidateRPos.equals((Object)headPos.below()) || candidateRPos.equals((Object)phasedPos.relative(pDir)) || candidateRPos.equals((Object)headPos.relative(pDir)) || !this.inRange(candidateRPos) || !PistonKickModule.mc.level.getBlockState(candidateRPos).isAir() && !PistonKickModule.mc.level.getBlockState(candidateRPos).canBeReplaced() || !air && !PistonHelper.canPlace(candidateRPos, null, false, true)) continue;
                        this.supportPos = null;
                        this.pistonPos = pPos;
                        this.redstonePos = candidateRPos;
                        this.pushDir = pDir;
                        return true;
                    }
                }
            }
            if (air || !isPPosAir || !this.inRange(underPiston = pPos.below())) continue;
            if (isLeverMode) {
                if (!PistonHelper.canPlace(underPiston, null, false, true) && !this.isSolidBlock(underPiston)) continue;
                ArrayList<LeverCandidate> bCandidates = new ArrayList<LeverCandidate>();
                BlockPos backPos = pPos.relative(pDir.getOpposite());
                BlockPos backSupport = backPos.below();
                ArrayList<BlockPos> backSupports = new ArrayList<BlockPos>();
                if (!this.isSolidBlock(underPiston)) {
                    backSupports.add(underPiston);
                }
                if (!this.isSolidBlock(backSupport)) {
                    backSupports.add(backSupport);
                }
                bCandidates.add(new LeverCandidate(backPos, backSupports));
                BlockPos leftPos = pPos.relative(pDir.getCounterClockWise());
                BlockPos leftSupport = leftPos.below();
                ArrayList<BlockPos> leftSupports = new ArrayList<BlockPos>();
                if (!this.isSolidBlock(underPiston)) {
                    leftSupports.add(underPiston);
                }
                if (!this.isSolidBlock(leftSupport)) {
                    leftSupports.add(leftSupport);
                }
                bCandidates.add(new LeverCandidate(leftPos, leftSupports));
                BlockPos rightPos = pPos.relative(pDir.getClockWise());
                BlockPos rightSupport = rightPos.below();
                ArrayList<BlockPos> rightSupports = new ArrayList<BlockPos>();
                if (!this.isSolidBlock(underPiston)) {
                    rightSupports.add(underPiston);
                }
                if (!this.isSolidBlock(rightSupport)) {
                    rightSupports.add(rightSupport);
                }
                bCandidates.add(new LeverCandidate(rightPos, rightSupports));
                for (LeverCandidate candidate : bCandidates) {
                    boolean leverPlaceable;
                    BlockPos candidateRPos = candidate.leverPos;
                    if (candidateRPos.equals((Object)pPos.relative(pDir)) || candidateRPos.equals((Object)headPos) || candidateRPos.equals((Object)headPos.below()) || candidateRPos.equals((Object)phasedPos.relative(pDir)) || candidateRPos.equals((Object)headPos.relative(pDir)) || !this.inRange(candidateRPos)) continue;
                    boolean bl4 = leverPlaceable = this.isLever(candidateRPos) || PistonKickModule.mc.level.getBlockState(candidateRPos).isAir() || PistonKickModule.mc.level.getBlockState(candidateRPos).canBeReplaced();
                    if (!leverPlaceable) continue;
                    boolean supportsValid = true;
                    for (BlockPos s : candidate.neededSupports) {
                        if (s.equals((Object)headPos) || s.equals((Object)headPos.below()) || s.equals((Object)phasedPos.relative(pDir)) || s.equals((Object)headPos.relative(pDir))) {
                            supportsValid = false;
                            break;
                        }
                        if (this.inRange(s) && (this.isSolidBlock(s) || PistonHelper.canPlace(s, null, false, true))) continue;
                        supportsValid = false;
                        break;
                    }
                    if (!supportsValid) continue;
                    this.supportPositions.clear();
                    this.supportPositions.addAll(candidate.neededSupports);
                    this.supportPos = candidate.neededSupports.isEmpty() ? null : candidate.neededSupports.get(0);
                    this.pistonPos = pPos;
                    this.redstonePos = candidateRPos;
                    this.pushDir = pDir;
                    return true;
                }
                continue;
            }
            if (!PistonHelper.canPlace(underPiston, null, false, true) && !this.isPowerSource(underPiston)) continue;
            this.supportPositions.clear();
            this.supportPos = null;
            this.pistonPos = pPos;
            this.redstonePos = underPiston;
            this.pushDir = pDir;
            return true;
        }
        return false;
    }

    private BlockPos findBestPowerPos(BlockPos pPos, BlockPos headPos, BlockPos destFeet, Direction pDir) {
        Direction[] tryDirs;
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        boolean air = this.airPlace.getValue();
        for (Direction d : tryDirs = new Direction[]{Direction.DOWN, pDir.getOpposite(), pDir.getClockWise(), pDir.getCounterClockWise(), Direction.UP}) {
            double dist;
            BlockPos pos = pPos.relative(d);
            if (pos.equals((Object)headPos) || pos.equals((Object)headPos.below()) || pos.equals((Object)destFeet) || pos.equals((Object)destFeet.above()) || pos.equals((Object)pPos.relative(pDir))) continue;
            if (this.isPowerSource(pos)) {
                return pos;
            }
            if (!PistonHelper.canPlace(pos, null, air, true) || !this.inRange(pos) || !((dist = PistonKickModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)pos))) < bestDist)) continue;
            bestDist = dist;
            best = pos;
        }
        return best;
    }

    private void placeBlock(int slot, BlockPos pos, Direction pistonPushDir) {
        Direction side;
        if (slot == -1 || PistonKickModule.mc.player == null) {
            return;
        }
        Direction direction = side = this.strictDirection.getValue() ? WorldUtils.getDirection(pos, true) : PistonHelper.getPlaceSide(pos, d -> true);
        if (side == null) {
            if (this.airPlace.getValue()) {
                if (this.power.getValue().equalsIgnoreCase("Lever") && this.pistonPos != null && this.pushDir != null && pos.equals((Object)this.pistonPos.relative(this.pushDir.getOpposite()))) {
                    side = this.pushDir;
                } else {
                    side = PistonHelper.getClickSide(pos);
                    if (side == null) {
                        side = Direction.UP;
                    }
                }
            } else {
                return;
            }
        }
        int prevSlot = PistonKickModule.mc.player.getInventory().getSelectedSlot();
        this.doSwap(slot, prevSlot);
        if (this.shouldRotate()) {
            this.doRotate(pos, side);
            if (pistonPushDir != null) {
                this.snapPistonFacing(pistonPushDir);
            }
        }
        PistonHelper.placeBlock(pos, side, false);
        this.restoreSlot(slot, prevSlot);
        if (this.render.getValue()) {
            Night.RENDER_MANAGER.getRenderPositions().removeIf(p -> p.getPos().equals((Object)pos));
            Night.RENDER_MANAGER.getRenderPositions().add(new RenderPosition(pos));
        }
    }

    private void snapPistonFacing(Direction pushDir) {
        float yaw = switch (pushDir) {
            case Direction.SOUTH -> 180.0f;
            case Direction.NORTH -> 0.0f;
            case Direction.EAST -> 90.0f;
            case Direction.WEST -> -90.0f;
            default -> 0.0f;
        };
        Night.ROTATION_MANAGER.silentRotate(yaw, 0.0f);
    }

    private boolean isPistonPlacedAndFacing(BlockPos pos, Direction pushDir) {
        if (PistonKickModule.mc.level == null) {
            return false;
        }
        BlockState state = PistonKickModule.mc.level.getBlockState(pos);
        if (state.getBlock() instanceof PistonBaseBlock) {
            return state.getValue((Property)PistonBaseBlock.FACING) == pushDir;
        }
        return false;
    }

    private boolean isPistonExtended(BlockPos pos) {
        if (PistonKickModule.mc.level == null) {
            return false;
        }
        BlockState state = PistonKickModule.mc.level.getBlockState(pos);
        if (state.getBlock() instanceof PistonBaseBlock) {
            return (Boolean)state.getValue((Property)PistonBaseBlock.EXTENDED);
        }
        return false;
    }

    private boolean isPistonPowered(BlockPos pPos) {
        if (PistonKickModule.mc.level == null) {
            return false;
        }
        if (this.power.getValue().equalsIgnoreCase("Lever")) {
            for (Direction d : Direction.values()) {
                BlockPos check = pPos.relative(d);
                if (!this.isLever(check) || !this.isLeverPowered(check)) continue;
                return true;
            }
            return PistonKickModule.mc.level.hasNeighborSignal(pPos);
        }
        for (Direction d : Direction.values()) {
            if (!this.isRedstone(pPos.relative(d))) continue;
            return true;
        }
        return false;
    }

    private boolean isPowerSource(BlockPos pos) {
        if (PistonKickModule.mc.level == null || pos == null) {
            return false;
        }
        Block b = PistonKickModule.mc.level.getBlockState(pos).getBlock();
        if (this.power.getValue().equalsIgnoreCase("Lever")) {
            return b == Blocks.LEVER;
        }
        return b == Blocks.REDSTONE_BLOCK || b == Blocks.REDSTONE_TORCH;
    }

    private boolean isRedstone(BlockPos pos) {
        if (PistonKickModule.mc.level == null) {
            return false;
        }
        Block b = PistonKickModule.mc.level.getBlockState(pos).getBlock();
        return b == Blocks.REDSTONE_BLOCK || b == Blocks.REDSTONE_TORCH;
    }

    private boolean isLever(BlockPos pos) {
        if (PistonKickModule.mc.level == null || pos == null) {
            return false;
        }
        return PistonKickModule.mc.level.getBlockState(pos).getBlock() == Blocks.LEVER;
    }

    private boolean isLeverPowered(BlockPos pos) {
        if (PistonKickModule.mc.level == null || pos == null) {
            return false;
        }
        BlockState state = PistonKickModule.mc.level.getBlockState(pos);
        if (state.getBlock() == Blocks.LEVER) {
            return (Boolean)state.getValue((Property)LeverBlock.POWERED);
        }
        return false;
    }

    private void clickLever(BlockPos pos) {
        if (PistonKickModule.mc.player == null || PistonKickModule.mc.level == null) {
            return;
        }
        Direction clickFace = PistonHelper.getClickSide(pos);
        if (clickFace == null) {
            clickFace = Direction.UP;
        }
        PistonHelper.clickBlock(pos, clickFace, this.shouldRotate());
    }

    private boolean isDestinationOpen(BlockPos phasedPos, Direction pDir) {
        if (PistonKickModule.mc.level == null) {
            return false;
        }
        BlockPos destFeet = phasedPos.relative(pDir);
        BlockPos destHead = destFeet.above();
        BlockState feet = PistonKickModule.mc.level.getBlockState(destFeet);
        BlockState head = PistonKickModule.mc.level.getBlockState(destHead);
        if (!feet.isAir() && (!feet.canBeReplaced() || feet.blocksMotion() || this.isImmovable(feet))) {
            return false;
        }
        return head.isAir() || head.canBeReplaced() && !head.blocksMotion() && !this.isImmovable(head);
    }

    private boolean isImmovable(BlockState state) {
        Block b = state.getBlock();
        return b == Blocks.OBSIDIAN || b == Blocks.BEDROCK || b == Blocks.CRYING_OBSIDIAN || b == Blocks.RESPAWN_ANCHOR || b == Blocks.ENDER_CHEST || state.isSolid();
    }

    public List<BlockPos> getPhasedSolidBlocks(Player target) {
        ArrayList<BlockPos> solidPhased = new ArrayList<BlockPos>();
        if (target == null || PistonKickModule.mc.level == null) {
            return solidPhased;
        }
        List<BlockPos> allPhased = EntityUtils.getPhasedBlocks((Entity)target);
        for (BlockPos pos : allPhased) {
            BlockState state = PistonKickModule.mc.level.getBlockState(pos);
            if (!this.isHardPhasedBlock(state)) continue;
            solidPhased.add(pos);
        }
        if (solidPhased.isEmpty()) {
            BlockPos feetPos = BlockPos.containing((double)target.getX(), (double)(target.getY() + 0.1), (double)target.getZ());
            if (this.isHardPhasedBlock(PistonKickModule.mc.level.getBlockState(feetPos))) {
                solidPhased.add(feetPos);
            } else {
                BlockPos blockPos = target.blockPosition();
                if (this.isHardPhasedBlock(PistonKickModule.mc.level.getBlockState(blockPos))) {
                    solidPhased.add(blockPos);
                } else {
                    BlockPos torsoPos = feetPos.above();
                    if (this.isHardPhasedBlock(PistonKickModule.mc.level.getBlockState(torsoPos))) {
                        solidPhased.add(torsoPos);
                    }
                }
            }
        }
        return solidPhased;
    }

    public BlockPos getPhasedSolidBlock(Player target) {
        List<BlockPos> list = this.getPhasedSolidBlocks(target);
        return list.isEmpty() ? null : list.get(0);
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

    private boolean inRange(BlockPos pos) {
        double r = this.placeRange.getValue().floatValue();
        return PistonKickModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)pos)) <= r * r;
    }

    private void breakNearbyCrystals(Player target) {
        double r = this.breakRange.getValue().floatValue();
        AABB box = target.getBoundingBox().inflate(r);
        PistonKickModule.mc.level.getEntitiesOfClass(EndCrystal.class, box, e -> e.isAlive() && PistonKickModule.mc.player.distanceToSqr((Entity)e) <= r * r).stream().min(Comparator.comparingDouble(e -> PistonKickModule.mc.player.distanceToSqr((Entity)e))).ifPresent(crystal -> {
            float selfDmg = DamageUtils.getCrystalDamage((Entity)PistonKickModule.mc.player, null, crystal, false);
            if (selfDmg > this.maxLocalDamage.getValue().floatValue()) {
                return;
            }
            if (this.safety.getValue() && selfDmg >= PistonKickModule.mc.player.getHealth() + PistonKickModule.mc.player.getAbsorptionAmount()) {
                return;
            }
            mc.getConnection().send((Packet)new ServerboundAttackPacket(crystal.getId()));
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        });
    }

    private boolean shouldRotate() {
        return !this.rotate.getValue().equalsIgnoreCase("None");
    }

    private void doRotate(BlockPos pos, Direction side) {
        Vec3 hitVec = Vec3.atCenterOf((Vec3i)pos).add((double)side.getStepX() * 0.5, (double)side.getStepY() * 0.5, (double)side.getStepZ() * 0.5);
        float[] rots = RotationUtils.getRotations(hitVec);
        Night.ROTATION_MANAGER.silentRotate(rots[0], rots[1]);
    }

    private void doSwap(int slot, int previousSlot) {
        InventoryUtils.switchSlot(this.swapAction.getValue(), slot, previousSlot);
    }

    private void restoreSlot(int slot, int previousSlot) {
        InventoryUtils.switchBack(this.swapAction.getValue(), slot, previousSlot);
    }

    private int findPower() {
        if (this.power.getValue().equalsIgnoreCase("Lever")) {
            return this.findBlock(Blocks.LEVER);
        }
        int slot = this.findBlock(Blocks.REDSTONE_BLOCK);
        if (slot != -1) {
            return slot;
        }
        return this.findBlock(Blocks.REDSTONE_TORCH);
    }

    private int findSupportBlock() {
        int slot = this.findBlock(Blocks.OBSIDIAN);
        if (slot != -1) {
            return slot;
        }
        slot = this.findBlock(Blocks.CRYING_OBSIDIAN);
        if (slot != -1) {
            return slot;
        }
        slot = this.findBlock(Blocks.ENDER_CHEST);
        if (slot != -1) {
            return slot;
        }
        slot = this.findBlock(Blocks.NETHERITE_BLOCK);
        if (slot != -1) {
            return slot;
        }
        slot = this.findBlock(Blocks.COBBLESTONE);
        if (slot != -1) {
            return slot;
        }
        return this.findPower();
    }

    private boolean isSolidBlock(BlockPos pos) {
        if (PistonKickModule.mc.level == null || pos == null) {
            return false;
        }
        BlockState state = PistonKickModule.mc.level.getBlockState(pos);
        return !state.isAir() && !state.canBeReplaced() && state.isSolid();
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
            ItemStack held = PistonKickModule.mc.player.getMainHandItem();
            if (!held.isEmpty() && (item = held.getItem()) instanceof BlockItem && (bi = (BlockItem)item).getBlock() == block) {
                return PistonKickModule.mc.player.getInventory().getSelectedSlot();
            }
            return -1;
        }
        int end = this.swapAction.getValue().equalsIgnoreCase("AltSwap") || this.swapAction.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END;
        for (int i = 0; i <= end; ++i) {
            BlockItem bi;
            Item item;
            ItemStack s = PistonKickModule.mc.player.getInventory().getItem(i);
            if (s.isEmpty() || !((item = s.getItem()) instanceof BlockItem) || (bi = (BlockItem)item).getBlock() != block) continue;
            return i;
        }
        return -1;
    }

    private List<Player> getEnemies() {
        double r = this.targetRange.getValue().floatValue();
        return PistonKickModule.mc.level.players().stream().filter(p -> p != PistonKickModule.mc.player).filter(p -> !EntityUtils.isGhost((Entity)p)).filter(p -> !p.isSpectator() && p.isAlive()).filter(p -> !Night.FRIEND_MANAGER.contains(p.getName().getString())).filter(p -> PistonKickModule.mc.player.distanceToSqr((Entity)p) <= r * r).sorted(Comparator.comparingDouble(p -> PistonKickModule.mc.player.distanceToSqr((Entity)p))).collect(Collectors.toList());
    }

    private record LeverCandidate(BlockPos leverPos, List<BlockPos> neededSupports) {
    }
}

