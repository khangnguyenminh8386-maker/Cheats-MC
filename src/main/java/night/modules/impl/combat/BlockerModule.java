/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.combat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PlayerMineEvent;
import night.events.impl.PlayerUpdateEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.player.SpeedMineModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.minecraft.EntityUtils;
import night.utils.minecraft.HoleUtils;
import night.utils.minecraft.InventoryUtils;
import night.utils.minecraft.WorldUtils;
import night.utils.system.ThreadExecutor;
import night.utils.system.Timer;

@RegisterModule(name="Blocker", description="Places blocks to stop enemies from placing crystals.", category=Module.Category.COMBAT, proxyEnhanced=true)
public class BlockerModule
extends Module {
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
    public BooleanSetting asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
    public NumberSetting limit = new NumberSetting("Limit", "The number of blocks that can be placed per tick.", 4, 1, 20);
    public NumberSetting delay = new NumberSetting("Delay", "The amount of ticks that have to be waited for between placements.", 0, 0, 20);
    public NumberSetting range = new NumberSetting("Range", "The maximum range at which the blocks will be placed at.", 5.0, 0.0, 12.0);
    public BooleanSetting rotate = new BooleanSetting("Rotate", "Sends a packet rotation whenever placing a block.", true);
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
    public BooleanSetting crystalDestruction = new BooleanSetting("CrystalDestruction", "Destroys any crystals that interfere with block placement.", true);
    public BooleanSetting whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
    public BooleanSetting feet = new BooleanSetting("Feet", "Places on feet level blocks.", true);
    public BooleanSetting head = new BooleanSetting("Head", "Places on head level blocks.", true);
    public BooleanSetting render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
    private final CopyOnWriteArrayList<Position> targetPositions = new CopyOnWriteArrayList();
    private Mine mine = null;
    private int ticks = 0;

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (!this.whileEating.getValue() && EntityUtils.isEating()) {
            return;
        }
        Runnable runnable = () -> {
            if (BlockerModule.mc.player == null || BlockerModule.mc.level == null) {
                return;
            }
            SpeedMineModule module = Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
            if (this.mine != null && module.getPrimary() != null && this.mine.position().equals((Object)module.getPrimary().getPosition()) || module.getSecondary() != null && this.mine.position().equals((Object)module.getSecondary().getPosition())) {
                this.mine = null;
                return;
            }
            int blocksPlaced = 0;
            if (this.ticks < this.delay.getValue().intValue()) {
                ++this.ticks;
                return;
            }
            HashSet<BlockPos> feetPositions = HoleUtils.getFeetPositions((Player)BlockerModule.mc.player, true, false, false);
            List<BlockPos> insidePositions = HoleUtils.getInsidePositions((Entity)BlockerModule.mc.player);
            if (this.mine != null && this.mine.timer().hasTimeElapsed(Float.valueOf(Math.max(this.mine.breakTime() - 200.0f, 0.0f)))) {
                BlockPos position2 = this.mine.position();
                if (this.mine.type() == MineType.FEET && this.feet.getValue()) {
                    if (feetPositions.contains(this.mine.position())) {
                        this.targetPositions.add(new Position(position2, position2.above()));
                        for (Direction direction : Direction.values()) {
                            if (!direction.getAxis().isHorizontal()) continue;
                            this.targetPositions.add(new Position(position2, position2.relative(direction)));
                        }
                    }
                    this.mine = null;
                } else if ((this.mine.type() == MineType.HEAD || this.mine.type() == MineType.SIDE) && this.head.getValue()) {
                    if (this.mine.type() == MineType.HEAD && insidePositions.contains(this.mine.position().below().below()) || this.mine.type() == MineType.SIDE && feetPositions.contains(this.mine.position().below())) {
                        this.targetPositions.add(new Position(position2, position2.above()));
                    }
                    this.mine = null;
                }
            }
            this.targetPositions.removeIf(position -> !WorldUtils.isPlaceable(position.position()));
            this.targetPositions.removeIf(position -> BlockerModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)position.position())) > Mth.square((double)this.range.getValue().doubleValue()));
            this.targetPositions.removeIf(position -> !feetPositions.contains(position.original()) && !feetPositions.contains(position.original().below()) && !insidePositions.contains(position.original().below().below()));
            if (this.targetPositions.isEmpty()) {
                return;
            }
            int slot = InventoryUtils.findHardestBlock(0, this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
            int previousSlot = BlockerModule.mc.player.getInventory().getSelectedSlot();
            if (slot == -1) {
                this.targetPositions.clear();
                return;
            }
            Night.ROTATION_MANAGER.beginBatchRotation();
            try {
                InventoryUtils.switchSlot(this.autoSwitch.getValue(), slot, previousSlot);
                for (Position position3 : new ArrayList<Position>(this.targetPositions)) {
                    Direction direction;
                    if (blocksPlaced >= this.limit.getValue().intValue()) break;
                    direction = WorldUtils.getDirection(position3.position(), null, this.strictDirection.getValue());
                    if (direction == null) continue;
                    WorldUtils.placeBlock(position3.position(), direction, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), this.render.getValue());
                    this.targetPositions.remove(position3);
                    ++blocksPlaced;
                }
                InventoryUtils.switchBack(this.autoSwitch.getValue(), slot, previousSlot);
            }
            finally {
                Night.ROTATION_MANAGER.endBatchRotation();
            }
            this.ticks = 0;
        };
        if (this.asynchronous.getValue()) {
            ThreadExecutor.execute(runnable);
        } else {
            runnable.run();
        }
    }

    @SubscribeEvent
    public void onPlayerMine(PlayerMineEvent event) {
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (BlockerModule.mc.player == null || BlockerModule.mc.level == null) {
            return;
        }
        if (this.mine != null && this.mine.position().equals((Object)event.getPosition())) {
            return;
        }
        SpeedMineModule module = Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
        if (module.getPrimary() != null && event.getPosition().equals((Object)module.getPrimary().getPosition()) || module.getSecondary() != null && event.getPosition().equals((Object)module.getSecondary().getPosition())) {
            return;
        }
        Entity entity = BlockerModule.mc.level.getEntity(event.getActorID());
        if (entity == BlockerModule.mc.player) {
            return;
        }
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player)entity;
        if (Night.FRIEND_MANAGER.contains(player.getName().getString())) {
            return;
        }
        HashSet<BlockPos> feetPositions = HoleUtils.getFeetPositions((Player)BlockerModule.mc.player, true, false, false);
        List<BlockPos> insidePositions = HoleUtils.getInsidePositions((Entity)BlockerModule.mc.player);
        if (this.feet.getValue() && feetPositions.contains(event.getPosition())) {
            this.mine = new Mine(event.getPosition(), new Timer(), WorldUtils.getBreakTime(player, BlockerModule.mc.level.getBlockState(event.getPosition())), MineType.FEET);
            return;
        }
        if (this.head.getValue()) {
            if (feetPositions.contains(event.getPosition().below())) {
                this.mine = new Mine(event.getPosition(), new Timer(), WorldUtils.getBreakTime(player, BlockerModule.mc.level.getBlockState(event.getPosition())), MineType.SIDE);
            }
            if (insidePositions.contains(event.getPosition().below().below())) {
                this.mine = new Mine(event.getPosition(), new Timer(), WorldUtils.getBreakTime(player, BlockerModule.mc.level.getBlockState(event.getPosition())), MineType.HEAD);
            }
        }
    }

    @Override
    public void onDisable() {
        this.targetPositions.clear();
    }

    @Override
    public String getMetaData() {
        if (this.targetPositions == null) {
            return "0";
        }
        return String.valueOf(this.targetPositions.size());
    }

    private record Mine(BlockPos position, Timer timer, float breakTime, MineType type) {
    }

    private static enum MineType {
        FEET,
        HEAD,
        SIDE;

    }

    private record Position(BlockPos original, BlockPos position) {
    }
}

