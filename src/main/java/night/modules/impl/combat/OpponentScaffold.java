/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.combat;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PlayerUpdateEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.movement.HitboxDesyncModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.minecraft.EntityUtils;
import night.utils.minecraft.InventoryUtils;
import night.utils.minecraft.PositionUtils;
import night.utils.minecraft.WorldUtils;
import night.utils.system.ThreadExecutor;

@RegisterModule(name="OpponentScaffold", description="Automatically places blocks under non-friended players to prevent them from falling.", category=Module.Category.COMBAT, proxyEnhanced=true)
public class OpponentScaffold
extends Module {
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
    public BooleanSetting asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
    public NumberSetting limit = new NumberSetting("Limit", "The number of blocks that can be placed per tick.", 4, 1, 20);
    public NumberSetting delay = new NumberSetting("Delay", "The amount of ticks that have to be waited for between placements.", 0, 0, 20);
    public NumberSetting range = new NumberSetting("Range", "The maximum range at which blocks will be placed at.", 5.0, 0.0, 12.0);
    public NumberSetting enemyRange = new NumberSetting("EnemyRange", "The maximum distance at which the target should be at.", Float.valueOf(10.0f), Float.valueOf(0.0f), Float.valueOf(20.0f));
    public NumberSetting extrapolation = new NumberSetting("Extrapolation", "Extrapolates the target's position to predict where they will move.", 0, 0, 20);
    public BooleanSetting await = new BooleanSetting("Await", "Waits for blocks to be registered by the client before placing on them.", false);
    public BooleanSetting rotate = new BooleanSetting("Rotate", "Whether or not you should rotate when you place blocks.", true);
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
    public BooleanSetting crystalDestruction = new BooleanSetting("CrystalDestruction", "Destroys any crystals that interfere with block placement.", true);
    public BooleanSetting whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
    public BooleanSetting selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module once it is finished with placing.", false);
    public BooleanSetting itemDisable = new BooleanSetting("ItemDisable", "Toggles off the module whenever you run out of items to place with.", true);
    public BooleanSetting render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
    private List<BlockPos> positions = new ArrayList<BlockPos>();
    private int ticks = 0;
    private int blocksPlaced = 0;

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (OpponentScaffold.mc.player == null || OpponentScaffold.mc.level == null) {
            return;
        }
        if (!this.whileEating.getValue() && EntityUtils.isEating()) {
            return;
        }
        List players = OpponentScaffold.mc.level.players();
        if (players == null || players.isEmpty()) {
            return;
        }
        Runnable runnable = () -> {
            int previousSlot;
            if (OpponentScaffold.mc.player == null || OpponentScaffold.mc.level == null) {
                return;
            }
            this.blocksPlaced = 0;
            if (this.ticks < this.delay.getValue().intValue()) {
                ++this.ticks;
                return;
            }
            if (this.autoSwitch.getValue().equalsIgnoreCase("None") && (OpponentScaffold.mc.player.getMainHandItem() == null || !(OpponentScaffold.mc.player.getMainHandItem().getItem() instanceof BlockItem))) {
                if (this.itemDisable.getValue()) {
                    Night.CHAT_MANAGER.tagged("You are currently not holding any blocks.", this.getName());
                    this.setToggled(false);
                }
                this.positions = new ArrayList<BlockPos>();
                return;
            }
            int slot = InventoryUtils.findHardestBlock(0, this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
            int n = previousSlot = OpponentScaffold.mc.player.getInventory() != null ? OpponentScaffold.mc.player.getInventory().getSelectedSlot() : 0;
            if (slot == -1) {
                if (this.itemDisable.getValue()) {
                    Night.CHAT_MANAGER.tagged("No blocks could be found in your hotbar.", this.getName());
                    this.setToggled(false);
                }
                this.positions = new ArrayList<BlockPos>();
                return;
            }
            Target target = this.getTarget(players);
            if (target == null) {
                if (this.selfDisable.getValue()) {
                    this.setToggled(false);
                }
                this.positions = new ArrayList<BlockPos>();
                return;
            }
            this.positions = target.positions();
            Night.ROTATION_MANAGER.beginBatchRotation();
            try {
                InventoryUtils.switchSlot(this.autoSwitch.getValue(), slot, previousSlot);
                ArrayList<BlockPos> placedPositions = new ArrayList<BlockPos>();
                for (BlockPos position : this.positions) {
                    if (this.blocksPlaced >= this.limit.getValue().intValue()) break;
                    Direction direction = WorldUtils.getDirection(position, placedPositions, this.strictDirection.getValue());
                    if (direction == null) {
                        Direction supportDirection;
                        BlockPos supportPosition = position.offset(0, -1, 0);
                        if (!WorldUtils.isPlaceable(supportPosition) || (supportDirection = WorldUtils.getDirection(supportPosition, placedPositions, this.strictDirection.getValue())) == null) continue;
                        WorldUtils.placeBlock(supportPosition, supportDirection, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), this.render.getValue());
                        placedPositions.add(supportPosition);
                        ++this.blocksPlaced;
                        if (this.blocksPlaced >= this.limit.getValue().intValue()) break;
                        if (this.await.getValue() || (direction = WorldUtils.getDirection(position, placedPositions, this.strictDirection.getValue())) == null) continue;
                    }
                    WorldUtils.placeBlock(position, direction, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), this.render.getValue());
                    placedPositions.add(position);
                    ++this.blocksPlaced;
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

    @Override
    public void onEnable() {
        if (OpponentScaffold.mc.player == null || OpponentScaffold.mc.level == null) {
            this.setToggled(false);
        }
    }

    @Override
    public void onDisable() {
        this.positions = new ArrayList<BlockPos>();
    }

    @Override
    public String getMetaData() {
        return String.valueOf(this.positions.size());
    }

    private Target getTarget(List<AbstractClientPlayer> players) {
        double distToPlayer;
        boolean hitboxEnabled;
        if (OpponentScaffold.mc.player == null) {
            return null;
        }
        AbstractClientPlayer closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (AbstractClientPlayer player : players) {
            double distance;
            if (player == OpponentScaffold.mc.player || player.isInvisible() || Night.FRIEND_MANAGER.contains(player.getName().getString()) || player.getHealth() <= 0.0f || (distance = OpponentScaffold.mc.player.distanceToSqr((Entity)player)) > (double)Mth.square((float)this.enemyRange.getValue().floatValue()) || !(distance < closestDistance)) continue;
            closestDistance = distance;
            closest = player;
        }
        if (closest == null) {
            return null;
        }
        BlockPos basePos = this.getTargetPosition((Player)closest);
        if (basePos == null) {
            return null;
        }
        HitboxDesyncModule module = Night.MODULE_MANAGER.getModule(HitboxDesyncModule.class);
        ArrayList<BlockPos> targetPositions = new ArrayList<BlockPos>();
        BlockPos underPlayer = basePos.below();
        boolean bl = hitboxEnabled = module != null && module.isToggled() && !Boolean.TRUE.equals(module.close.getValue());
        if (WorldUtils.isPlaceable(underPlayer, hitboxEnabled) && (distToPlayer = OpponentScaffold.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)underPlayer))) <= Mth.square((double)this.range.getValue().doubleValue())) {
            targetPositions.add(underPlayer);
        }
        if (targetPositions.isEmpty()) {
            return null;
        }
        return new Target((Player)closest, (List<BlockPos>)targetPositions);
    }

    private BlockPos getTargetPosition(Player player) {
        if (player == null) {
            return null;
        }
        int extrapolationTicks = this.extrapolation.getValue().intValue();
        if (extrapolationTicks > 0) {
            double deltaX = player.getX() - player.xo;
            double deltaZ = player.getZ() - player.zo;
            double predictedX = player.getX() + deltaX * (double)extrapolationTicks;
            double predictedZ = player.getZ() + deltaZ * (double)extrapolationTicks;
            return new BlockPos((int)Math.floor(predictedX), (int)Math.floor(player.getY()), (int)Math.floor(predictedZ));
        }
        return PositionUtils.getFlooredPosition((Entity)player);
    }

    private record Target(Player player, List<BlockPos> positions) {
    }
}

