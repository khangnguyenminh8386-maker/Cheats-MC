/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Position
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.InterpolationHandler
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PlayerUpdateEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.minecraft.EntityUtils;
import night.utils.minecraft.InventoryUtils;
import night.utils.minecraft.WorldUtils;

@RegisterModule(name="CombatPlace", description="Automatically places blocks in front of moving targets to block them.", category=Module.Category.COMBAT, proxyEnhanced=true)
public class CombatPlace
extends Module {
    public BooleanSetting flatten = new BooleanSetting("Flatten", "Place blocks under the target.", true);
    public ModeSetting mode = new ModeSetting("Mode", "Prediction mode.", "Two", new String[]{"None", "One", "Two", "Three"});
    public ModeSetting targetPriority = new ModeSetting("Priority", "Target selection priority.", "Closest", new String[]{"Closest", "Health"});
    public NumberSetting predictTicks = new NumberSetting("PredictTicks", "Prediction scale.", 2, 1, 8);
    public NumberSetting minKmh = new NumberSetting("MinKMH", "Min speed to trigger blocker.", 20, 1, 40);
    public NumberSetting limit = new NumberSetting("Limit", "The maximum number of blocks that can be placed per tick.", 4, 1, 20);
    public NumberSetting delay = new NumberSetting("Delay", "The delay in ticks between placements.", 0, 0, 20);
    public BooleanSetting strict = new BooleanSetting("Strict", "Prevent placement if entities intersect.", true);
    public BooleanSetting safety = new BooleanSetting("Safety", "Prevent trapping yourself.", true);
    public NumberSetting range = new NumberSetting("Range", "The maximum range at which blocks will be placed.", 5.0, 0.0, 12.0);
    public BooleanSetting rotate = new BooleanSetting("Rotate", "Sends a packet rotation whenever placing a block.", true);
    public BooleanSetting crystalDestruction = new BooleanSetting("CrystalDestruction", "Destroys any crystals that interfere with block placement.", true);
    private int delayTicks = 0;

    @Override
    public void onEnable() {
        this.delayTicks = 0;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (this.getNull()) {
            return;
        }
        Player target = this.findTarget();
        if (target == null) {
            return;
        }
        if (this.delayTicks < this.delay.getValue().intValue()) {
            ++this.delayTicks;
            return;
        }
        List<BlockPos> blocksToPlace = this.getPlacePositions(target);
        if (blocksToPlace.isEmpty()) {
            return;
        }
        double rangeSq = Math.pow(this.range.getValue().doubleValue(), 2.0);
        blocksToPlace.removeIf(pos -> CombatPlace.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)pos)) > rangeSq);
        if (blocksToPlace.isEmpty()) {
            return;
        }
        this.placeBlocks(blocksToPlace);
        this.delayTicks = 0;
    }

    private void placeBlocks(List<BlockPos> positions) {
        if (CombatPlace.mc.player == null) {
            return;
        }
        int blockSlot = InventoryUtils.findHardestBlock(0, 8);
        if (blockSlot == -1) {
            return;
        }
        int previousSlot = CombatPlace.mc.player.getInventory().getSelectedSlot();
        InventoryUtils.switchSlot("Silent", blockSlot, previousSlot);
        int placed = 0;
        for (BlockPos position : positions) {
            if (placed >= this.limit.getValue().intValue()) break;
            Direction direction = WorldUtils.getDirection(position, false);
            if (direction == null) continue;
            WorldUtils.placeBlock(position, direction, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), false);
            ++placed;
        }
        InventoryUtils.switchBack("Silent", blockSlot, previousSlot);
    }

    private List<BlockPos> getPlacePositions(Player target) {
        double speedKmh;
        String currentMode;
        BlockPos under;
        ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
        if (this.flatten.getValue() && this.isValidSpot(under = BlockPos.containing((Position)target.position()).below())) {
            positions.add(under);
        }
        if (!(currentMode = this.mode.getValue()).equals("None") && (speedKmh = EntityUtils.getSpeed((Entity)target, EntityUtils.SpeedUnit.KILOMETERS)) >= this.minKmh.getValue().doubleValue()) {
            double dz;
            InterpolationHandler interpolation = target.getInterpolation();
            Vec3 lerpTarget = interpolation != null ? interpolation.position() : target.position();
            double targetX = lerpTarget.x;
            double targetZ = lerpTarget.z;
            double dx = targetX - target.getX();
            double len = Math.sqrt(dx * dx + (dz = targetZ - target.getZ()) * dz);
            if (len > 0.001) {
                double scale;
                double dirX = dx / len;
                double dirZ = dz / len;
                double tickSpeed = Math.sqrt(Math.pow(target.getX() - target.xo, 2.0) + Math.pow(target.getZ() - target.zo, 2.0));
                BlockPos feetPos = BlockPos.containing((double)(targetX + dirX * tickSpeed * (scale = this.predictTicks.getValue().doubleValue())), (double)target.getY(), (double)(targetZ + dirZ * tickSpeed * scale));
                if (this.isValidSpot(feetPos)) {
                    BlockPos topPos;
                    BlockPos headPos;
                    positions.add(feetPos);
                    if ((currentMode.equals("Two") || currentMode.equals("Three")) && this.isValidSpot(headPos = feetPos.above())) {
                        positions.add(headPos);
                    }
                    if (currentMode.equals("Three") && this.isValidSpot(topPos = feetPos.above(2))) {
                        positions.add(topPos);
                    }
                }
            }
        }
        return positions;
    }

    private boolean isValidSpot(BlockPos pos) {
        AABB box;
        if (CombatPlace.mc.level == null) {
            return false;
        }
        if (CombatPlace.mc.level.isOutsideBuildHeight(pos)) {
            return false;
        }
        if (!CombatPlace.mc.level.getBlockState(pos).canBeReplaced()) {
            return false;
        }
        if (this.strict.getValue() && CombatPlace.mc.level.getEntities((Entity)null, box = new AABB(pos), e -> true).stream().anyMatch(e -> e instanceof Player)) {
            return false;
        }
        return !this.safety.getValue() || CombatPlace.mc.player == null || !CombatPlace.mc.player.getBoundingBox().intersects(new AABB(pos));
    }

    private Player findTarget() {
        if (CombatPlace.mc.level == null || CombatPlace.mc.player == null) {
            return null;
        }
        return CombatPlace.mc.level.players().stream().filter(p -> p != CombatPlace.mc.player && !p.isRemoved() && !Night.FRIEND_MANAGER.contains(p.getName().getString())).filter(p -> (double)CombatPlace.mc.player.distanceTo((Entity)p) <= this.range.getValue().doubleValue() + 4.0).min(this.getComparator()).orElse(null);
    }

    private Comparator<Player> getComparator() {
        if (this.targetPriority.getValue().equals("Health")) {
            return Comparator.comparingDouble(p -> p.getHealth() + p.getAbsorptionAmount());
        }
        return Comparator.comparingDouble(p -> CombatPlace.mc.player != null ? (double)CombatPlace.mc.player.distanceTo((Entity)p) : 0.0);
    }
}

