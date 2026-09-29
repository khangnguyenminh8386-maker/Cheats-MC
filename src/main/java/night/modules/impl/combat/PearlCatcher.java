/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.combat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.EntitySpawnEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.minecraft.EntityUtils;
import night.utils.minecraft.HoleUtils;
import night.utils.minecraft.InventoryUtils;
import night.utils.minecraft.WorldUtils;

@RegisterModule(name="PearlCatcher", description="Places obsidian in the air to stop players pearls from reaching their destination.", category=Module.Category.COMBAT)
public class PearlCatcher
extends Module {
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
    public BooleanSetting rotate = new BooleanSetting("Rotate", "Sends a packet rotation whenever placing a block.", true);
    public NumberSetting range = new NumberSetting("Range", "The maximum range at which the blocks will be placed at.", 5.0, 0.0, 12.0);
    public NumberSetting enemyRange = new NumberSetting("EnemyRange", "The maximum distance at which the target should be at.", Float.valueOf(8.0f), Float.valueOf(0.0f), Float.valueOf(16.0f));
    public NumberSetting distance = new NumberSetting("Distance", "The distance at which the obisidan block will be placed from the player.", Float.valueOf(4.0f), Float.valueOf(3.0f), Float.valueOf(6.0f));
    public BooleanSetting holeCheck = new BooleanSetting("HoleCheck", "Only self traps whenever you are in a hole.", false);
    public BooleanSetting whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
    public BooleanSetting itemDisable = new BooleanSetting("ItemDisable", "Toggles off the module whenever you run out of items to place with.", true);
    public BooleanSetting render = new BooleanSetting("Render", "Whether or not to render the place position.", true);

    @SubscribeEvent
    public void onEntitySpawn(EntitySpawnEvent event) {
        Player owner;
        ThrownEnderpearl pearl;
        block5: {
            block4: {
                Entity entity = event.getEntity();
                if (!(entity instanceof ThrownEnderpearl)) break block4;
                pearl = (ThrownEnderpearl)entity;
                if (this.whileEating.getValue() || !EntityUtils.isEating()) break block5;
            }
            return;
        }
        Entity entity = pearl.getOwner();
        if (!(entity instanceof Player) || !this.validTarget(owner = (Player)entity)) {
            return;
        }
        this.catchPearl(owner);
    }

    private void catchPearl(Player player) {
        if (this.autoSwitch.getValue().equalsIgnoreCase("None") && !(PearlCatcher.mc.player.getMainHandItem().getItem() instanceof BlockItem)) {
            if (this.itemDisable.getValue()) {
                Night.CHAT_MANAGER.tagged("You are currently not holding any blocks.", this.getName());
                this.setToggled(false);
            }
            return;
        }
        int slot = InventoryUtils.findHardestBlock(0, this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
        int previousSlot = PearlCatcher.mc.player.getInventory().getSelectedSlot();
        if (slot == -1) {
            if (this.itemDisable.getValue()) {
                Night.CHAT_MANAGER.tagged("No blocks could be found in your hotbar.", this.getName());
                this.setToggled(false);
            }
            return;
        }
        HitResult hitResult = player.pick((double)this.distance.getValue().floatValue(), 0.0f, false);
        if (!(hitResult instanceof BlockHitResult)) {
            return;
        }
        BlockHitResult blockHitResult = (BlockHitResult)hitResult;
        BlockPos pos = blockHitResult.getBlockPos();
        if (PearlCatcher.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)pos)) > Mth.square((double)this.range.getValue().doubleValue())) {
            return;
        }
        if (PearlCatcher.mc.level.getBlockState(pos).getBlock().equals(Blocks.AIR)) {
            InventoryUtils.switchSlot(this.autoSwitch.getValue(), slot, previousSlot);
            WorldUtils.placeBlock(pos, WorldUtils.getDirection(pos, false), InteractionHand.MAIN_HAND, this.rotate.getValue(), false, this.render.getValue());
            InventoryUtils.switchBack(this.autoSwitch.getValue(), slot, previousSlot);
        }
    }

    private boolean validTarget(Player player) {
        if (player == PearlCatcher.mc.player) {
            return false;
        }
        if (PearlCatcher.mc.player.distanceToSqr((Entity)player) > Mth.square((double)this.enemyRange.getValue().doubleValue())) {
            return false;
        }
        if (Night.FRIEND_MANAGER.contains(player.getName().getString())) {
            return false;
        }
        return !this.holeCheck.getValue() || HoleUtils.isPlayerInHole(player);
    }
}

