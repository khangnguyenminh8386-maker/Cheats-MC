/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.multiplayer.MultiPlayerGameMode
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Plane
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PlayerUpdateEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.player.SpeedMineModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.minecraft.EntityUtils;
import night.utils.minecraft.InventoryUtils;
import night.utils.minecraft.WorldUtils;
import night.utils.rotations.RotationUtils;
import night.utils.system.Timer;

@RegisterModule(name="ObsidianFarmer", description="Places and mines ender chests using SpeedMine.", category=Module.Category.COMBAT, proxyEnhanced=true)
public class ObsidianFarmerModule
extends Module {
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
    public ModeSetting rotate = new ModeSetting("Rotate", "The rotation mode when placing and mining blocks.", "Grim", new String[]{"None", "Normal", "Grim"});
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places and mines using directions that face you.", false);
    public BooleanSetting airPlace = new BooleanSetting("AirPlace", "Places blocks in the air without needing neighboring blocks.", false);
    public BooleanSetting crystalDestruction = new BooleanSetting("CrystalDestruction", "Destroys any crystals that interfere with block placement.", true);
    public NumberSetting range = new NumberSetting("Range", "The maximum range at which blocks will be placed.", 5.0, 1.0, 6.0);
    public NumberSetting mineRange = new NumberSetting("MineRange", "The maximum range at which ender chests will be mined.", 5.0, 1.0, 8.0);
    public NumberSetting wallsRange = new NumberSetting("WallsRange", "The maximum range through walls for mining.", 3.5, 1.0, 8.0);
    public NumberSetting delay = new NumberSetting("Delay", "The delay in ticks between placements.", 2, 0, 20);
    public NumberSetting mineDelay = new NumberSetting("MineDelay", "The delay in ticks before mining.", 1, 0, 20);
    public NumberSetting retryDelay = new NumberSetting("RetryDelay", "The delay in ticks before retrying a failed mine attempt.", 1, 1, 20);
    public BooleanSetting requirePacketMine = new BooleanSetting("RequirePacketMine", "Only mines when SpeedMine is enabled.", true);
    public BooleanSetting support = new BooleanSetting("Support", "Places a supporting block under the ender chest if needed.", true);
    public BooleanSetting swing = new BooleanSetting("Swing", "Swings your hand when starting to break blocks.", true);
    public BooleanSetting whileEating = new BooleanSetting("WhileEating", "Places and mines blocks while eating.", true);
    public BooleanSetting onlyOnGround = new BooleanSetting("OnlyGround", "Only works while on ground.", false);
    public BooleanSetting render = new BooleanSetting("Render", "Whether or not to render place and mine positions.", true);
    private final Timer placeTimer = new Timer();
    private final Timer mineTimer = new Timer();
    private final Timer retryTimer = new Timer();
    private BlockPos enderChestPos;
    private Phase phase = Phase.Place;

    @Override
    public void onEnable() {
        this.enderChestPos = null;
        this.phase = Phase.Place;
        this.placeTimer.reset();
        this.mineTimer.reset();
        this.retryTimer.reset();
    }

    @Override
    public void onDisable() {
        this.enderChestPos = null;
        this.phase = Phase.Place;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (this.getNull()) {
            return;
        }
        LocalPlayer player = ObsidianFarmerModule.mc.player;
        ClientLevel level = ObsidianFarmerModule.mc.level;
        MultiPlayerGameMode gameMode = ObsidianFarmerModule.mc.gameMode;
        if (player == null || level == null || gameMode == null) {
            return;
        }
        if (!this.whileEating.getValue() && EntityUtils.isEating()) {
            return;
        }
        if (this.onlyOnGround.getValue() && !player.onGround()) {
            return;
        }
        this.updateCurrentEnderChest(player, level);
        if (this.enderChestPos != null && level.getBlockState(this.enderChestPos).is(Blocks.ENDER_CHEST)) {
            this.phase = Phase.Mine;
            if (this.requirePacketMine.getValue() && !this.isPacketMineActive()) {
                return;
            }
            this.targetEnderChest(player, level, gameMode, this.enderChestPos);
            return;
        }
        this.phase = Phase.Place;
        this.enderChestPos = null;
        this.placeEnderChest(player, level);
    }

    private boolean isPacketMineActive() {
        if (Night.MODULE_MANAGER == null) {
            return false;
        }
        SpeedMineModule speedMine = Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
        return speedMine != null && speedMine.isToggled();
    }

    private void updateCurrentEnderChest(LocalPlayer player, ClientLevel level) {
        if (this.enderChestPos != null) {
            if (!level.getBlockState(this.enderChestPos).is(Blocks.ENDER_CHEST)) {
                this.enderChestPos = null;
            }
            return;
        }
        this.enderChestPos = this.findExistingEnderChest(player, level);
    }

    private BlockPos findExistingEnderChest(LocalPlayer player, ClientLevel level) {
        double maxRange = Math.max(this.range.getValue().doubleValue(), this.mineRange.getValue().doubleValue());
        double rangeSq = maxRange * maxRange;
        int radius = (int)Math.ceil(maxRange);
        BlockPos base = player.blockPosition();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int x = -radius; x <= radius; ++x) {
            for (int y = -radius; y <= radius; ++y) {
                for (int z = -radius; z <= radius; ++z) {
                    double distance;
                    BlockPos pos = base.offset(x, y, z);
                    if (!level.getBlockState(pos).is(Blocks.ENDER_CHEST) || (distance = player.distanceToSqr(Vec3.atCenterOf((Vec3i)pos))) > rangeSq || !(distance < bestDistance)) continue;
                    bestDistance = distance;
                    best = pos.immutable();
                }
            }
        }
        return best;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void placeEnderChest(LocalPlayer player, ClientLevel level) {
        BlockPos placed;
        if (!this.placeTimer.hasTimeElapsed((long)this.delay.getValue().intValue() * 50L)) {
            return;
        }
        BlockPos placePos = this.findBestPlacePosition(player, level);
        if (placePos == null) {
            return;
        }
        int enderChestSlot = this.findEnderChestSlot(player);
        if (enderChestSlot == -1) {
            return;
        }
        int previousSlot = player.getInventory().getSelectedSlot();
        Night.ROTATION_MANAGER.beginBatchRotation();
        boolean success = false;
        try {
            int supportSlot;
            BlockPos supportPos;
            InventoryUtils.switchSlot(this.autoSwitch.getValue(), enderChestSlot, previousSlot);
            success = this.tryPlaceAt(placePos);
            if (!success && this.support.getValue() && this.isValidPlaceSpot(player, level, supportPos = placePos.below()) && (supportSlot = InventoryUtils.findHardestBlock(0, this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8)) != -1) {
                InventoryUtils.switchSlot(this.autoSwitch.getValue(), supportSlot, previousSlot);
                boolean supportPlaced = this.tryPlaceAt(supportPos);
                if (supportPlaced && this.isValidPlaceSpot(player, level, placePos)) {
                    InventoryUtils.switchSlot(this.autoSwitch.getValue(), enderChestSlot, previousSlot);
                    success = this.tryPlaceAt(placePos);
                }
            }
            InventoryUtils.switchBack(this.autoSwitch.getValue(), enderChestSlot, previousSlot);
        }
        finally {
            Night.ROTATION_MANAGER.endBatchRotation();
        }
        this.placeTimer.reset();
        if (!success) {
            return;
        }
        BlockPos blockPos = placed = level.getBlockState(placePos).is(Blocks.ENDER_CHEST) ? placePos.immutable() : this.findExistingEnderChest(player, level);
        if (placed == null) {
            return;
        }
        this.enderChestPos = placed;
        this.phase = Phase.Mine;
        this.mineTimer.reset();
        this.retryTimer.reset();
    }

    private boolean tryPlaceAt(BlockPos pos) {
        Direction direction = WorldUtils.getDirection(pos, this.strictDirection.getValue());
        if (direction != null) {
            return WorldUtils.placeBlock(pos, direction, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), this.render.getValue());
        }
        if (this.airPlace.getValue()) {
            WorldUtils.airPlaceBlock(pos, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.render.getValue());
            return true;
        }
        return false;
    }

    private void targetEnderChest(LocalPlayer player, ClientLevel level, MultiPlayerGameMode gameMode, BlockPos pos) {
        boolean shouldRotate;
        if (!this.mineTimer.hasTimeElapsed((long)this.mineDelay.getValue().intValue() * 50L)) {
            return;
        }
        if (!this.retryTimer.hasTimeElapsed((long)this.retryDelay.getValue().intValue() * 50L)) {
            return;
        }
        if (!this.canTargetEnderChest(player, level, pos)) {
            this.enderChestPos = null;
            this.phase = Phase.Place;
            return;
        }
        Direction side = this.getMineSide(player, level, pos);
        if (this.strictDirection.getValue() && side == null) {
            return;
        }
        Direction mineSide = side == null ? Direction.UP : side;
        Vec3 hitVec = this.getHitVec(pos, mineSide);
        boolean bl = shouldRotate = !this.rotate.getValue().equalsIgnoreCase("None");
        if (shouldRotate) {
            float[] rots = RotationUtils.getRotations(hitVec);
            if (Night.ROTATION_MANAGER.isBatchingRotations()) {
                Night.ROTATION_MANAGER.batchRotate(rots[0], rots[1]);
            } else {
                Night.ROTATION_MANAGER.silentRotate(rots[0], rots[1]);
            }
        }
        gameMode.startDestroyBlock(pos, mineSide);
        if (this.swing.getValue()) {
            player.swing(InteractionHand.MAIN_HAND);
        }
        this.retryTimer.reset();
        this.mineTimer.reset();
    }

    private BlockPos findBestPlacePosition(LocalPlayer player, ClientLevel level) {
        List<BlockPos> positions = this.getPlaceCandidates(player);
        positions.removeIf(pos -> !this.isValidPlaceSpot(player, level, (BlockPos)pos));
        positions.sort(Comparator.comparingDouble(pos -> player.distanceToSqr(Vec3.atCenterOf((Vec3i)pos))));
        return positions.isEmpty() ? null : positions.getFirst().immutable();
    }

    private List<BlockPos> getPlaceCandidates(LocalPlayer player) {
        LinkedHashSet<BlockPos> positions = new LinkedHashSet<BlockPos>();
        BlockPos base = player.blockPosition();
        Direction facing = player.getDirection();
        positions.add(base.relative(facing));
        positions.add(base.relative(facing).below());
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            positions.add(base.relative(dir));
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            positions.add(base.relative(dir).below());
        }
        positions.add(base.below());
        return new ArrayList<BlockPos>(positions);
    }

    private boolean isValidPlaceSpot(LocalPlayer player, ClientLevel level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (!state.isAir() && !state.canBeReplaced()) {
            return false;
        }
        if (player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)pos)) > this.range.getValue().doubleValue() * this.range.getValue().doubleValue()) {
            return false;
        }
        AABB box = new AABB(pos);
        if (player.getBoundingBox().intersects(box)) {
            return false;
        }
        if (!level.getEntitiesOfClass(Entity.class, box, entity -> !(entity instanceof ItemEntity)).isEmpty()) {
            return false;
        }
        return this.airPlace.getValue() || WorldUtils.getDirection(pos, this.strictDirection.getValue()) != null;
    }

    private boolean canTargetEnderChest(LocalPlayer player, ClientLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).is(Blocks.ENDER_CHEST)) {
            return false;
        }
        if (!this.canReach(player, level, pos, this.mineRange.getValue().doubleValue(), this.wallsRange.getValue().doubleValue())) {
            return false;
        }
        return !this.strictDirection.getValue() || this.getMineSide(player, level, pos) != null;
    }

    private boolean canReach(LocalPlayer player, ClientLevel level, BlockPos pos, double visibleRange, double wallRange) {
        Vec3 hit;
        Vec3 eye = player.getEyePosition();
        double distance = eye.distanceTo(hit = Vec3.atCenterOf((Vec3i)pos));
        if (distance <= visibleRange && this.hasLineOfSight(player, level, eye, hit)) {
            return true;
        }
        return distance <= wallRange;
    }

    private boolean hasLineOfSight(LocalPlayer player, ClientLevel level, Vec3 from, Vec3 to) {
        BlockHitResult result = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player));
        return result.getType() == HitResult.Type.MISS;
    }

    private Direction getMineSide(LocalPlayer player, ClientLevel level, BlockPos pos) {
        Vec3 eye = player.getEyePosition();
        Direction bestSide = null;
        double bestDistance = Double.MAX_VALUE;
        for (Direction direction : Direction.values()) {
            Vec3 hit = this.getHitVec(pos, direction);
            double distance = eye.distanceTo(hit);
            if (distance >= bestDistance || this.strictDirection.getValue() && !this.hasLineOfSight(player, level, eye, hit)) continue;
            bestDistance = distance;
            bestSide = direction;
        }
        return bestSide;
    }

    private Vec3 getHitVec(BlockPos pos, Direction direction) {
        return Vec3.atCenterOf((Vec3i)pos).add((double)direction.getStepX() * 0.5, (double)direction.getStepY() * 0.5, (double)direction.getStepZ() * 0.5);
    }

    private int findEnderChestSlot(LocalPlayer player) {
        int maxSlot = this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8;
        int selected = player.getInventory().getSelectedSlot();
        if (player.getInventory().getItem(selected).is(Items.ENDER_CHEST)) {
            return selected;
        }
        return InventoryUtils.find(Items.ENDER_CHEST, 0, maxSlot);
    }

    @Override
    public String getMetaData() {
        if (this.enderChestPos != null) {
            return String.valueOf(ChatFormatting.GREEN) + this.phase.name() + String.valueOf(ChatFormatting.RESET);
        }
        return "Place";
    }

    private static enum Phase {
        Place,
        Mine;

    }
}

