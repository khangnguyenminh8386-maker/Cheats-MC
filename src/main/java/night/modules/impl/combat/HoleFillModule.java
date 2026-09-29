/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundAttackPacket
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.combat;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
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
import night.utils.minecraft.HoleUtils;
import night.utils.minecraft.InventoryUtils;
import night.utils.minecraft.PositionUtils;
import night.utils.minecraft.WorldUtils;
import night.utils.rotations.RotationUtils;
import night.utils.system.ThreadExecutor;

@RegisterModule(name="HoleFill", description="Automatically places blocks inside of holes to prevent others from getting inside of them.", category=Module.Category.COMBAT, proxyEnhanced=true)
public class HoleFillModule
extends Module {
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
    public ModeSetting mode = new ModeSetting("Mode", "The offsets that will be used when trapping.", "Normal", new String[]{"Normal", "Smart"});
    public BooleanSetting webs = new BooleanSetting("Webs", "Use webs to holefill instead of blocks.", false);
    public BooleanSetting selfWeb = new BooleanSetting("SelfWeb", "Includes your own hole when holefilling with webs.", false);
    public BooleanSetting refill = new BooleanSetting("Refill", "Fills the hole you just exited.", false);
    public BooleanSetting waitUntilExited = new BooleanSetting("WaitUntilExited", "Waits until your hitbox completely leaves the hole before refilling.", new BooleanSetting.Visibility(this.refill, true), true);
    public BooleanSetting asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
    public NumberSetting bpt = new NumberSetting("BPT", "The maximum number of blocks to place per tick.", 4, 1, 8);
    public NumberSetting range = new NumberSetting("Range", "The maximum range at which the blocks will be placed at.", 5.0, 0.0, 12.0);
    public NumberSetting enemyRange = new NumberSetting("EnemyRange", "The maximum distance at which the target should be at.", new ModeSetting.Visibility(this.mode, "Smart"), (Number)Float.valueOf(8.0f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(16.0f));
    public NumberSetting smartRange = new NumberSetting("SmartRange", "The distance at which the holes will have to be away from the target.", new ModeSetting.Visibility(this.mode, "Smart"), (Number)Float.valueOf(3.0f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(6.0f));
    public BooleanSetting safety = new BooleanSetting("Safety", "Prevents holes close to you from getting filled.", true);
    public NumberSetting safetyRange = new NumberSetting("SafetyRange", "The maximum distance the holes can be at to be prevented from getting filled.", new BooleanSetting.Visibility(this.safety, true), (Number)Float.valueOf(2.0f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(6.0f));
    public BooleanSetting rotate = new BooleanSetting("Rotate", "Sends a packet rotation whenever placing a block.", true);
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
    public BooleanSetting attack = new BooleanSetting("Attack", "Attacks any crystal that interferes with hole filling.", false);
    public BooleanSetting attackRotate = new BooleanSetting("AttackRotate", "Rotate", "Rotates toward the crystal before attacking it.", new BooleanSetting.Visibility(this.attack, true), true);
    public NumberSetting attackRange = new NumberSetting("AttackRange", "Range", "The maximum range at which crystals will be attacked.", new BooleanSetting.Visibility(this.attack, true), Float.valueOf(3.0f), Float.valueOf(1.0f), Float.valueOf(6.0f));
    public NumberSetting attackAge = new NumberSetting("AttackAge", "Age", "The minimum age (in ticks) a crystal must reach before being attacked.", new BooleanSetting.Visibility(this.attack, true), 5, 0, 20);
    public BooleanSetting attackMultiTask = new BooleanSetting("AttackMultiTask", "Multitask", "Allows attacking while an item is already in use.", new BooleanSetting.Visibility(this.attack, true), true);
    public BooleanSetting attackSwing = new BooleanSetting("AttackSwing", "Swing", "Sends a swing packet whenever attacking a crystal.", new BooleanSetting.Visibility(this.attack, true), true);
    public BooleanSetting doubleHoles = new BooleanSetting("DoubleHoles", "Whether or not to fill double holes.", false);
    public BooleanSetting holeCheck = new BooleanSetting("HoleCheck", "Checks if the target isn't in a hole before placing.", true);
    public BooleanSetting whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
    public BooleanSetting selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module once it is finished with placing.", false);
    public BooleanSetting itemDisable = new BooleanSetting("ItemDisable", "Toggles off the module whenever you run out of items to place with.", true);
    public BooleanSetting render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
    private List<BlockPos> positions = new ArrayList<BlockPos>();
    private int blocksPlaced = 0;
    private BlockPos lastHolePos = null;
    private BlockPos refillPos = null;
    private boolean wasInHole = false;

    @Override
    public void onEnable() {
        BlockPos currentPos;
        this.lastHolePos = null;
        this.refillPos = null;
        this.wasInHole = false;
        if (!this.getNull() && HoleFillModule.mc.player != null && this.isHole(currentPos = PositionUtils.getFlooredPosition((Entity)HoleFillModule.mc.player))) {
            this.lastHolePos = currentPos.immutable();
            this.wasInHole = true;
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (!this.whileEating.getValue() && EntityUtils.isEating()) {
            return;
        }
        List players = HoleFillModule.mc.level.players();
        Runnable runnable = () -> {
            Target target;
            boolean flag;
            this.blocksPlaced = 0;
            flag = this.webs.getValue() ? !HoleFillModule.mc.player.getMainHandItem().getItem().equals(Items.COBWEB) : !(HoleFillModule.mc.player.getMainHandItem().getItem() instanceof BlockItem);
            if (this.autoSwitch.getValue().equalsIgnoreCase("None") && flag) {
                if (this.itemDisable.getValue()) {
                    Night.CHAT_MANAGER.tagged("You are currently not holding any " + (this.webs.getValue() ? "cobwebs." : "blocks."), this.getName());
                    this.setToggled(false);
                }
                this.positions = new ArrayList<BlockPos>();
                return;
            }
            int slot = this.webs.getValue() ? InventoryUtils.find(Items.COBWEB, 0, this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8) : InventoryUtils.findHardestBlock(0, this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
            int previousSlot = HoleFillModule.mc.player.getInventory().getSelectedSlot();
            if (slot == -1) {
                if (this.itemDisable.getValue()) {
                    Night.CHAT_MANAGER.tagged("No " + (this.webs.getValue() ? "cobwebs" : "blocks") + " could be found in your hotbar.", this.getName());
                    this.setToggled(false);
                }
                this.positions = new ArrayList<BlockPos>();
                return;
            }
            this.positions = this.mode.getValue().equalsIgnoreCase("Smart") ? ((target = this.getTarget(players)) == null ? new ArrayList<BlockPos>() : target.positions()) : this.getPositions(null);
            if (this.refill.getValue()) {
                this.updateExitedHole(HoleFillModule.mc.player);
                if (this.refillPos != null) {
                    if (!WorldUtils.isPlaceable(this.refillPos)) {
                        this.refillPos = null;
                    } else if (!this.waitUntilExited.getValue() || !this.playerIntersects(this.refillPos, (Player)HoleFillModule.mc.player)) {
                        if (HoleFillModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)this.refillPos)) <= Mth.square((double)this.range.getValue().doubleValue())) {
                            if (!this.positions.contains(this.refillPos)) {
                                this.positions.add(0, this.refillPos);
                            }
                        } else {
                            this.refillPos = null;
                        }
                    }
                }
            } else {
                this.refillPos = null;
                this.wasInHole = false;
                this.lastHolePos = null;
            }
            if (this.attack.getValue()) {
                this.attackHoleCrystals();
            }
            if (this.positions.isEmpty()) {
                if (this.selfDisable.getValue() && !this.mode.getValue().equalsIgnoreCase("Smart")) {
                    this.setToggled(false);
                }
                return;
            }
            Night.ROTATION_MANAGER.beginBatchRotation();
            try {
                InventoryUtils.switchSlot(this.autoSwitch.getValue(), slot, previousSlot);
                for (BlockPos position : this.positions) {
                    if (this.blocksPlaced >= this.bpt.getValue().intValue()) break;
                    Direction direction = WorldUtils.getDirection(position, this.strictDirection.getValue());
                    if (direction == null || !WorldUtils.placeBlock(position, direction, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue())) continue;
                    ++this.blocksPlaced;
                    if (!position.equals((Object)this.refillPos)) continue;
                    this.refillPos = null;
                }
                InventoryUtils.switchBack(this.autoSwitch.getValue(), slot, previousSlot);
            }
            finally {
                Night.ROTATION_MANAGER.endBatchRotation();
            }
        };
        if (this.asynchronous.getValue()) {
            ThreadExecutor.execute(runnable);
        } else {
            runnable.run();
        }
    }

    private void attackHoleCrystals() {
        Night.ROTATION_MANAGER.beginBatchRotation();
        for (EndCrystal crystal : HoleFillModule.mc.level.getEntitiesOfClass(EndCrystal.class, new AABB(HoleFillModule.mc.player.blockPosition()).inflate(this.attackRange.getValue().doubleValue() + 3.0))) {
            if (!crystal.isAlive() || crystal.isRemoved() || crystal.tickCount < this.attackAge.getValue().intValue()) continue;
            AABB crystalBox = crystal.getBoundingBox();
            boolean threatens = false;
            for (BlockPos pos : this.positions) {
                if (!HoleFillModule.mc.level.getBlockState(pos).canBeReplaced() || !new AABB(pos).intersects(crystalBox)) continue;
                threatens = true;
                break;
            }
            if (!threatens || crystalBox.distanceToSqr(HoleFillModule.mc.player.getEyePosition()) > this.attackRange.getValue().doubleValue() * this.attackRange.getValue().doubleValue() || !this.attackMultiTask.getValue() && HoleFillModule.mc.player.isUsingItem()) continue;
            if (this.attackRotate.getValue()) {
                float[] rotations = RotationUtils.getRotations(crystal.getBoundingBox().getCenter());
                Night.ROTATION_MANAGER.silentRotate(rotations[0], rotations[1]);
                boolean insideBox = crystalBox.contains(HoleFillModule.mc.player.getEyePosition());
                if (!insideBox && !WorldUtils.canSee(crystal.getBoundingBox().getCenter())) continue;
            }
            mc.getConnection().send((Packet)new ServerboundAttackPacket(crystal.getId()));
            if (!this.attackSwing.getValue()) continue;
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
        Night.ROTATION_MANAGER.endBatchRotation();
    }

    private void updateExitedHole(LocalPlayer player) {
        BlockPos currentPos = PositionUtils.getFlooredPosition((Entity)player);
        boolean inHole = this.isHole(currentPos);
        if (inHole) {
            this.lastHolePos = currentPos.immutable();
            this.wasInHole = true;
            return;
        }
        if (!this.wasInHole || this.lastHolePos == null) {
            this.wasInHole = false;
            return;
        }
        if (!currentPos.equals((Object)this.lastHolePos)) {
            this.refillPos = this.lastHolePos.immutable();
        }
        this.wasInHole = false;
    }

    private boolean isHole(BlockPos pos) {
        if (HoleFillModule.mc.level == null || pos == null) {
            return false;
        }
        if (HoleUtils.getSingleHole(pos, 1.0, false) != null) {
            return true;
        }
        return this.doubleHoles.getValue() && HoleUtils.getDoubleHole(pos, 1.0) != null;
    }

    private boolean playerIntersects(BlockPos pos, Player player) {
        return player.getBoundingBox().intersects(new AABB(pos));
    }

    @Override
    public void onDisable() {
        this.positions = new ArrayList<BlockPos>();
        this.lastHolePos = null;
        this.refillPos = null;
        this.wasInHole = false;
    }

    @Override
    public String getMetaData() {
        return String.valueOf(this.positions.size());
    }

    private Target getTarget(List<AbstractClientPlayer> players) {
        Target optimalTarget = null;
        for (Player player : players) {
            List<BlockPos> positions;
            if (player == HoleFillModule.mc.player || !player.isAlive() || player.getHealth() <= 0.0f || HoleFillModule.mc.player.distanceToSqr((Entity)player) > Mth.square((double)this.enemyRange.getValue().doubleValue()) || Night.FRIEND_MANAGER.contains(player.getName().getString()) || this.holeCheck.getValue() && HoleUtils.isPlayerInHole(player) || (positions = this.getPositions(player)).isEmpty()) continue;
            if (optimalTarget == null) {
                optimalTarget = new Target(player, positions);
                continue;
            }
            if (!(HoleFillModule.mc.player.distanceToSqr((Entity)player) < HoleFillModule.mc.player.distanceToSqr((Entity)optimalTarget.player()))) continue;
            optimalTarget = new Target(player, positions);
        }
        return optimalTarget;
    }

    private List<BlockPos> getPositions(Player player) {
        ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
        int r = this.range.getValue().intValue();
        BlockPos center = HoleFillModule.mc.player.blockPosition();
        for (BlockPos position : BlockPos.betweenClosed((BlockPos)center.offset(-r, -r, -r), (BlockPos)center.offset(r, r, r))) {
            if (HoleFillModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)position)) > Mth.square((double)this.range.getValue().doubleValue()) || !WorldUtils.isPlaceable(position) || this.safety.getValue() && HoleFillModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)position)) <= Mth.square((double)this.safetyRange.getValue().doubleValue())) continue;
            if (this.selfWeb.getValue() && HoleUtils.isPlayerInHole((Player)HoleFillModule.mc.player) && HoleFillModule.mc.player.blockPosition().equals((Object)position)) {
                positions.add(position.immutable());
                continue;
            }
            if (HoleUtils.getSingleHole(position, 1.0) != null) {
                if (player != null && player.distanceToSqr(Vec3.atCenterOf((Vec3i)position)) > Mth.square((double)this.smartRange.getValue().doubleValue())) continue;
                positions.add(position.immutable());
                continue;
            }
            if (!this.doubleHoles.getValue() || HoleUtils.getDoubleHole(position, 1.0) == null || player != null && player.distanceToSqr(Vec3.atCenterOf((Vec3i)position)) > Mth.square((double)this.smartRange.getValue().doubleValue())) continue;
            positions.add(position.immutable());
        }
        return positions;
    }

    private record Target(Player player, List<BlockPos> positions) {
    }
}

