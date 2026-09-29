/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.player.RemotePlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundAddEntityPacket
 *  net.minecraft.network.protocol.game.ServerboundAttackPacket
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityTypes
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.combat;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PlayerUpdateEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.combat.AutoCrystalModule;
import night.modules.impl.visuals.LogoutSpotModule;
import night.modules.impl.visuals.PopChamsModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.minecraft.EntityUtils;
import night.utils.minecraft.HoleUtils;
import night.utils.minecraft.InventoryUtils;
import night.utils.minecraft.WorldUtils;
import night.utils.rotations.RotationUtils;
import night.utils.system.ThreadExecutor;

@RegisterModule(name="AutoTrap", description="Automatically places blocks around you to prevent other people from getting inside your hole.", category=Module.Category.COMBAT)
public class AutoTrapModule
extends Module {
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
    public ModeSetting mode = new ModeSetting("Mode", "The offsets that will be used when trapping.", "Full", new String[]{"Partial", "Full"});
    public BooleanSetting head = new BooleanSetting("Head", "Whether or not to cover the block on the players head.", new ModeSetting.Visibility(this.mode, "Full"), true);
    public BooleanSetting noFeet = new BooleanSetting("NoFeet", "Skips the new floor-level ring around the target's feet entirely -- walls and head (if enabled) still trap as before.", false);
    public BooleanSetting disableAfterFeet = new BooleanSetting("DisableAfterFeet", "Stops re-trapping the floor-level feet ring once it has successfully placed there at least once since the module was enabled -- walls/head keep trapping normally.", new BooleanSetting.Visibility(this.noFeet, false), false);
    public BooleanSetting asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
    public NumberSetting bpt = new NumberSetting("BPT", "The maximum number of blocks to place per tick.", 4, 1, 8);
    public NumberSetting range = new NumberSetting("Range", "The maximum range at which the blocks will be placed at.", 5.0, 0.0, 12.0);
    public NumberSetting enemyRange = new NumberSetting("EnemyRange", "The maximum distance at which the target should be at.", Float.valueOf(8.0f), Float.valueOf(0.0f), Float.valueOf(16.0f));
    public BooleanSetting await = new BooleanSetting("Await", "Waits for blocks to be registered by the client before placing on them.", false);
    public ModeSetting rotate = new ModeSetting("Rotate", "The rotation mode when placing blocks.", "Grim", new String[]{"None", "Normal", "Grim"});
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
    public BooleanSetting airPlace = new BooleanSetting("AirPlace", "Places blocks directly in the air with no real neighbor needed, skipping the support-scaffold bootstrap entirely instead of only using it as a last resort.", false);
    public BooleanSetting attack = new BooleanSetting("Attack", "Attacks any crystal that interferes with your trap.", false);
    public BooleanSetting attackRotate = new BooleanSetting("AttackRotate", "Rotate", "Rotates toward the crystal before attacking it.", new BooleanSetting.Visibility(this.attack, true), true);
    public NumberSetting attackRange = new NumberSetting("AttackRange", "Range", "The maximum range at which crystals will be attacked.", new BooleanSetting.Visibility(this.attack, true), Float.valueOf(3.0f), Float.valueOf(1.0f), Float.valueOf(6.0f));
    public NumberSetting attackAge = new NumberSetting("AttackAge", "Age", "The minimum age (in ticks) a crystal must reach before being attacked.", new BooleanSetting.Visibility(this.attack, true), 5, 0, 20);
    public BooleanSetting attackMultiTask = new BooleanSetting("AttackMultiTask", "Multitask", "Allows attacking while an item is already in use.", new BooleanSetting.Visibility(this.attack, true), true);
    public BooleanSetting attackSwing = new BooleanSetting("AttackSwing", "Swing", "Sends a swing packet whenever attacking a crystal.", new BooleanSetting.Visibility(this.attack, true), true);
    public BooleanSetting holeCheck = new BooleanSetting("HoleCheck", "Checks if the target is in a hole or not before placing.", true);
    public BooleanSetting antiStep = new BooleanSetting("AntiStep", "Adds additional blocks that prevent the player from stepping out of the hole.", false);
    public BooleanSetting whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
    public BooleanSetting ignoreNaked = new BooleanSetting("IgnoreNaked", "Ignore Naked, even that player wears elytra only", false);
    public BooleanSetting selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module once it is finished with placing.", false);
    public BooleanSetting itemDisable = new BooleanSetting("ItemDisable", "Toggles off the module whenever you run out of items to place with.", true);
    public BooleanSetting render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
    private List<BlockPos> positions = new ArrayList<BlockPos>();
    private int blocksPlaced = 0;
    private boolean feetTrappedOnce = false;

    @Override
    public void onEnable() {
        if (AutoTrapModule.mc.player == null || AutoTrapModule.mc.level == null) {
            this.setToggled(false);
        }
        this.feetTrappedOnce = false;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (!this.whileEating.getValue() && EntityUtils.isEating()) {
            return;
        }
        List players = AutoTrapModule.mc.level.players();
        Runnable runnable = () -> {
            this.blocksPlaced = 0;
            if (this.autoSwitch.getValue().equalsIgnoreCase("None") && !(AutoTrapModule.mc.player.getMainHandItem().getItem() instanceof BlockItem)) {
                if (this.itemDisable.getValue()) {
                    Night.CHAT_MANAGER.tagged("You are currently not holding any blocks.", this.getName());
                    this.setToggled(false);
                }
                this.positions = new ArrayList<BlockPos>();
                return;
            }
            int slot = InventoryUtils.findHardestBlock(0, this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
            int previousSlot = AutoTrapModule.mc.player.getInventory().getSelectedSlot();
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
            if (this.attack.getValue()) {
                this.attackTrapCrystals();
            }
            int feetY = target.player().getBlockY();
            Night.ROTATION_MANAGER.beginBatchRotation();
            try {
                InventoryUtils.switchSlot(this.autoSwitch.getValue(), slot, previousSlot);
                AutoCrystalModule autoCrystal = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoCrystalModule.class) : null;
                int headY = Mth.floor((double)target.player().getY()) + 2;
                ArrayList<BlockPos> placedPositions = new ArrayList<BlockPos>();
                for (BlockPos position : this.positions) {
                    Direction direction;
                    if (this.blocksPlaced >= this.bpt.getValue().intValue()) break;
                    if (position.getY() == headY && autoCrystal != null && autoCrystal.isToggled() && autoCrystal.hasLiveCrystalNear(position.above())) continue;
                    if (this.attack.getValue()) {
                        for (EndCrystal crystal : AutoTrapModule.mc.level.getEntitiesOfClass(EndCrystal.class, new AABB(position).inflate(0.5))) {
                            if (!crystal.isAlive()) continue;
                            mc.getConnection().send((Packet)new ServerboundAttackPacket(crystal.getId()));
                            if (!this.attackSwing.getValue()) continue;
                            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                        }
                    }
                    if ((direction = WorldUtils.getDirection(position, placedPositions, this.strictDirection.getValue())) == null) {
                        Direction supportDir;
                        if (this.airPlace.getValue()) {
                            WorldUtils.airPlaceBlock(position, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.render.getValue());
                            placedPositions.add(position);
                            ++this.blocksPlaced;
                            if (position.getY() != feetY) continue;
                            this.feetTrappedOnce = true;
                            continue;
                        }
                        BlockPos support = WorldUtils.findSupportBlock(position, placedPositions, this.strictDirection.getValue(), 2);
                        if (support == null || (supportDir = WorldUtils.getDirection(support, placedPositions, this.strictDirection.getValue())) == null || !WorldUtils.placeBlock(support, supportDir, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue())) continue;
                        placedPositions.add(support);
                        ++this.blocksPlaced;
                        continue;
                    }
                    WorldUtils.placeBlock(position, direction, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue());
                    placedPositions.add(position);
                    ++this.blocksPlaced;
                    if (position.getY() != feetY) continue;
                    this.feetTrappedOnce = true;
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

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        ClientboundAddEntityPacket addPacket;
        Packet<?> packet;
        if (AutoTrapModule.mc.player == null || AutoTrapModule.mc.level == null) {
            return;
        }
        if (this.attack.getValue() && (packet = event.getPacket()) instanceof ClientboundAddEntityPacket && (addPacket = (ClientboundAddEntityPacket)packet).getType() == EntityTypes.END_CRYSTAL) {
            Vec3 crystalPos = new Vec3(addPacket.getX(), addPacket.getY(), addPacket.getZ());
            AABB crystalBox = new AABB(crystalPos.x - 1.0, crystalPos.y, crystalPos.z - 1.0, crystalPos.x + 1.0, crystalPos.y + 2.0, crystalPos.z + 1.0);
            boolean nearTrap = false;
            for (BlockPos pos : this.positions) {
                if (!crystalBox.intersects(new AABB(pos)) && !(crystalPos.distanceToSqr(Vec3.atCenterOf((Vec3i)pos)) <= 4.0)) continue;
                nearTrap = true;
                break;
            }
            if (nearTrap && AutoTrapModule.mc.player.getEyePosition().distanceToSqr(crystalPos) <= Mth.square((double)this.attackRange.getValue().doubleValue()) && (this.attackMultiTask.getValue() || !AutoTrapModule.mc.player.isUsingItem())) {
                mc.getConnection().send((Packet)new ServerboundAttackPacket(addPacket.getId()));
                if (this.attackSwing.getValue()) {
                    mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                }
            }
        }
    }

    private void attackTrapCrystals() {
        Night.ROTATION_MANAGER.beginBatchRotation();
        for (EndCrystal crystal : AutoTrapModule.mc.level.getEntitiesOfClass(EndCrystal.class, new AABB(AutoTrapModule.mc.player.blockPosition()).inflate(this.attackRange.getValue().doubleValue() + 3.0))) {
            if (!crystal.isAlive() || crystal.isRemoved() || crystal.tickCount < this.attackAge.getValue().intValue()) continue;
            AABB crystalBox = crystal.getBoundingBox();
            boolean threatens = false;
            for (BlockPos pos : this.positions) {
                if (!AutoTrapModule.mc.level.getBlockState(pos).canBeReplaced() || !new AABB(pos).intersects(crystalBox)) continue;
                threatens = true;
                break;
            }
            if (!threatens || crystalBox.distanceToSqr(AutoTrapModule.mc.player.getEyePosition()) > this.attackRange.getValue().doubleValue() * this.attackRange.getValue().doubleValue() || !this.attackMultiTask.getValue() && AutoTrapModule.mc.player.isUsingItem()) continue;
            if (this.attackRotate.getValue()) {
                float[] rotations = RotationUtils.getRotations(crystal.getBoundingBox().getCenter());
                Night.ROTATION_MANAGER.silentRotate(rotations[0], rotations[1]);
                boolean insideBox = crystalBox.contains(AutoTrapModule.mc.player.getEyePosition());
                if (!insideBox && !WorldUtils.canSee(crystal.getBoundingBox().getCenter())) continue;
            }
            mc.getConnection().send((Packet)new ServerboundAttackPacket(crystal.getId()));
            if (!this.attackSwing.getValue()) continue;
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
        Night.ROTATION_MANAGER.endBatchRotation();
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
        Target optimalTarget = null;
        LogoutSpotModule logoutSpot = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(LogoutSpotModule.class) : null;
        PopChamsModule popChams = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(PopChamsModule.class) : null;
        ArrayList<AbstractClientPlayer> allCandidates = new ArrayList<AbstractClientPlayer>(players);
        if (logoutSpot != null && logoutSpot.isToggled()) {
            for (Player player : logoutSpot.getGhosts()) {
                if (player == null || allCandidates.contains(player)) continue;
                allCandidates.add((AbstractClientPlayer)player);
            }
        }
        for (Player player : allCandidates) {
            boolean feet;
            LogoutSpotModule.Spot spot;
            boolean ghost;
            if (player == AutoTrapModule.mc.player || popChams != null && popChams.isGhost((Entity)player) || !(ghost = EntityUtils.isGhost((Entity)player)) && (!player.isAlive() || player.getHealth() <= 0.0f) || AutoTrapModule.mc.player.distanceToSqr((Entity)player) > Mth.square((double)this.enemyRange.getValue().doubleValue()) || this.ignoreNaked.getValue() && EntityUtils.isNaked(player) || (logoutSpot == null || !logoutSpot.isGhost((Entity)player) ? Night.FRIEND_MANAGER.contains(player.getName().getString()) : (spot = logoutSpot.getSpot((RemotePlayer)player)) != null && Night.FRIEND_MANAGER.contains(spot.data.name))) continue;
            if (this.holeCheck.getValue() && !HoleUtils.isPlayerInHole(player)) continue;
            boolean bl = feet = !this.noFeet.getValue() && (!this.disableAfterFeet.getValue() || !this.feetTrappedOnce);
            List<BlockPos> positions = HoleUtils.getTrapPositions(player, this.mode.getValue().equalsIgnoreCase("Partial"), this.head.getValue(), this.antiStep.getValue(), false, this.strictDirection.getValue(), feet, this.airPlace.getValue()).stream().filter(position -> AutoTrapModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)position)) <= Mth.square((double)this.range.getValue().doubleValue())).filter(WorldUtils::isPlaceable).toList();
            if (positions.isEmpty()) continue;
            if (optimalTarget == null) {
                optimalTarget = new Target(player, positions);
                continue;
            }
            boolean bestIsGhost = EntityUtils.isGhost((Entity)optimalTarget.player());
            if (bestIsGhost != ghost) {
                if (!bestIsGhost) continue;
                optimalTarget = new Target(player, positions);
                continue;
            }
            if (!(AutoTrapModule.mc.player.distanceToSqr((Entity)player) < AutoTrapModule.mc.player.distanceToSqr((Entity)optimalTarget.player()))) continue;
            optimalTarget = new Target(player, positions);
        }
        return optimalTarget;
    }

    private record Target(Player player, List<BlockPos> positions) {
    }
}

