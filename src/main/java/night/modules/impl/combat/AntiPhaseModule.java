/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.ExperienceOrb
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.arrow.AbstractArrow
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.phys.AABB
 */
package night.modules.impl.combat;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PlayerUpdateEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.minecraft.EntityUtils;
import night.utils.minecraft.HoleUtils;
import night.utils.minecraft.InventoryUtils;
import night.utils.minecraft.WorldUtils;

@RegisterModule(name="AntiPhase", description="Places a climbable block (Scaffolding, Ladder, Vines) directly at a target enemy's feet.", category=Module.Category.COMBAT)
public class AntiPhaseModule
extends Module {
    public ModeSetting block = new ModeSetting("Block", "The item placed at the enemy's feet.", "Scaffolding", new String[]{"Scaffolding", "Ladder", "Vines"});
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to the selected item.", "Silent", InventoryUtils.SWITCH_MODES);
    public ModeSetting rotate = new ModeSetting("Rotate", "The rotation mode when placing.", "Grim", new String[]{"None", "Normal", "Grim"});
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
    public BooleanSetting render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
    public NumberSetting range = new NumberSetting("Range", "The maximum distance at which enemies will be targeted.", Float.valueOf(5.0f), Float.valueOf(1.0f), Float.valueOf(6.0f));
    public NumberSetting wallRange = new NumberSetting("WallRange", "The maximum distance at which enemies will be targeted through walls (no line of sight required).", Float.valueOf(3.0f), Float.valueOf(0.0f), Float.valueOf(6.0f));
    public BooleanSetting onlyHole = new BooleanSetting("OnlyHole", "Only places while the enemy is already standing in a hole (feet surrounded by blocks).", false);
    public BooleanSetting pauseOnEat = new BooleanSetting("PauseOnEat", "Pauses placing while eating or using items.", false);
    private Player target;

    @Override
    public String getMetaData() {
        return this.target != null ? this.target.getName().getString() : "None";
    }

    @Override
    public void onDisable() {
        this.target = null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (AntiPhaseModule.mc.player == null || AntiPhaseModule.mc.level == null) {
            return;
        }
        if (this.pauseOnEat.getValue() && (AntiPhaseModule.mc.player.isUsingItem() || AntiPhaseModule.mc.player.getUseItemRemainingTicks() > 0)) {
            return;
        }
        this.target = this.findTarget();
        if (this.target == null) {
            return;
        }
        if (this.onlyHole.getValue() && !HoleUtils.isPlayerInHole(this.target)) {
            return;
        }
        int slot = this.findItemSlot(this.resolveItem());
        if (slot == -1) {
            return;
        }
        Set<BlockPos> feetCells = this.footCells(this.target);
        int previousSlot = AntiPhaseModule.mc.player.getInventory().getSelectedSlot();
        Night.ROTATION_MANAGER.beginBatchRotation();
        try {
            if (!InventoryUtils.switchSlot(this.autoSwitch.getValue(), slot, previousSlot)) {
                return;
            }
            for (BlockPos position : feetCells) {
                if (!this.isPlaceable(position, this.target)) continue;
                boolean placed = false;
                for (Direction direction : WorldUtils.getDirections(position, List.of(), this.strictDirection.getValue())) {
                    if (!WorldUtils.placeBlock(position, direction, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue())) continue;
                    placed = true;
                    break;
                }
                if (placed) continue;
                WorldUtils.airPlaceBlock(position, InteractionHand.MAIN_HAND, "None", this.render.getValue());
            }
            InventoryUtils.switchBack(this.autoSwitch.getValue(), slot, previousSlot);
        }
        finally {
            Night.ROTATION_MANAGER.endBatchRotation();
        }
    }

    private Item resolveItem() {
        return switch (this.block.getValue()) {
            case "Ladder" -> Items.LADDER;
            case "Vines" -> Items.VINE;
            default -> Items.SCAFFOLDING;
        };
    }

    private int findItemSlot(Item item) {
        for (int i = 0; i <= 8; ++i) {
            if (!AntiPhaseModule.mc.player.getInventory().getItem(i).is(item)) continue;
            return i;
        }
        return -1;
    }

    private Player findTarget() {
        return AntiPhaseModule.mc.level.players().stream().filter(this::isValidTarget).min(Comparator.comparingDouble(p -> AntiPhaseModule.mc.player.distanceToSqr((Entity)p))).orElse(null);
    }

    private boolean isValidTarget(Player player) {
        if (player == AntiPhaseModule.mc.player || !player.isAlive() || player.isRemoved() || player.isSpectator()) {
            return false;
        }
        if (EntityUtils.isGhost((Entity)player)) {
            return false;
        }
        if (Night.FRIEND_MANAGER.contains(player.getName().getString())) {
            return false;
        }
        double distSq = AntiPhaseModule.mc.player.distanceToSqr((Entity)player);
        if (distSq > Mth.square((double)this.range.getValue().doubleValue())) {
            return false;
        }
        boolean needLos = distSq > Mth.square((double)this.wallRange.getValue().doubleValue());
        return !needLos || WorldUtils.canSee((Entity)player);
    }

    private boolean isPlaceable(BlockPos position, Player targetPlayer) {
        if (AntiPhaseModule.mc.level == null) {
            return false;
        }
        if (AntiPhaseModule.mc.level.isOutsideBuildHeight(position)) {
            return false;
        }
        if (!AntiPhaseModule.mc.level.getBlockState(position).canBeReplaced()) {
            return false;
        }
        return AntiPhaseModule.mc.level.getEntities((Entity)null, new AABB(position), e -> true).stream().noneMatch(e -> e != targetPlayer && !(e instanceof ExperienceOrb) && !(e instanceof ItemEntity) && !(e instanceof AbstractArrow) && !EntityUtils.isGhost(e));
    }

    private Set<BlockPos> footCells(Player player) {
        HashSet<BlockPos> cells = new HashSet<BlockPos>();
        AABB box = player.getBoundingBox();
        int y = player.blockPosition().getY();
        int x = (int)Math.floor(box.minX);
        while ((double)x < Math.ceil(box.maxX)) {
            int z = (int)Math.floor(box.minZ);
            while ((double)z < Math.ceil(box.maxZ)) {
                cells.add(new BlockPos(x, y, z));
                ++z;
            }
            ++x;
        }
        return cells;
    }
}

