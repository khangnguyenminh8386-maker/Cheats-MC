/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundAddEntityPacket
 *  net.minecraft.network.protocol.game.ServerboundAttackPacket
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.EntityTypes
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.combat;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PacketSendEvent;
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
import night.utils.rotations.RotationUtils;
import night.utils.system.ThreadExecutor;

@RegisterModule(name="SelfTrap", description="Automatically places blocks around you to prevent other people from getting inside your hole.", category=Module.Category.COMBAT, proxyEnhanced=true)
public class SelfTrapModule
extends Module {
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
    public ModeSetting mode = new ModeSetting("Mode", "The offsets that will be used when trapping.", "Partial", new String[]{"Partial", "Full"});
    public BooleanSetting head = new BooleanSetting("Head", "Whether or not to cover the block on the players head.", new ModeSetting.Visibility(this.mode, "Full"), true);
    public BooleanSetting asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
    public NumberSetting bpt = new NumberSetting("BPT", "The maximum number of blocks to place per tick.", 4, 1, 8);
    public BooleanSetting await = new BooleanSetting("Await", "Waits for blocks to be registered by the client before placing on them.", false);
    public ModeSetting rotate = new ModeSetting("Rotate", "The rotation mode when placing blocks.", "Grim", new String[]{"None", "Normal", "Grim"});
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
    public BooleanSetting airPlace = new BooleanSetting("AirPlace", "Places blocks in the air without needing neighboring blocks.", false);
    public BooleanSetting attack = new BooleanSetting("Attack", "Attacks any crystal that interferes with your trap.", false);
    public BooleanSetting attackRotate = new BooleanSetting("AttackRotate", "Rotate", "Rotates toward the crystal before attacking it.", new BooleanSetting.Visibility(this.attack, true), true);
    public NumberSetting attackRange = new NumberSetting("AttackRange", "Range", "The maximum range at which crystals will be attacked.", new BooleanSetting.Visibility(this.attack, true), Float.valueOf(3.0f), Float.valueOf(1.0f), Float.valueOf(6.0f));
    public NumberSetting attackAge = new NumberSetting("AttackAge", "Age", "The minimum age (in ticks) a crystal must reach before being attacked.", new BooleanSetting.Visibility(this.attack, true), 5, 0, 20);
    public BooleanSetting attackMultiTask = new BooleanSetting("AttackMultiTask", "Multitask", "Allows attacking while an item is already in use.", new BooleanSetting.Visibility(this.attack, true), true);
    public BooleanSetting attackSwing = new BooleanSetting("AttackSwing", "Swing", "Sends a swing packet whenever attacking a crystal.", new BooleanSetting.Visibility(this.attack, true), true);
    public BooleanSetting antiStep = new BooleanSetting("AntiStep", "Adds additional blocks that prevent anyone from stepping out of the hole.", false);
    public BooleanSetting antiBomb = new BooleanSetting("AntiBomb", "Places an extra block above your head to prevent you from getting bombed.", false);
    public BooleanSetting holeCheck = new BooleanSetting("HoleCheck", "Only self traps whenever you are in a hole.", false);
    public ModeSetting autoJump = new ModeSetting("AutoJump", "Automatically jumps to place high blocks when on-ground eye height is too low for StrictDirection.", "Grim", new String[]{"None", "NCP", "Grim"});
    public BooleanSetting whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
    public BooleanSetting selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module once it is finished with placing.", false);
    public BooleanSetting render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
    private int packetsSent = 0;
    private boolean isWorking = false;
    private List<BlockPos> targetPositions = new ArrayList<BlockPos>();
    private BlockPos lastGroundPos = null;
    private boolean jumpingForTrap = false;
    private int blocksPlaced = 0;

    @Override
    public String getMetaData() {
        return "Packet: " + this.packetsSent;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (!this.whileEating.getValue() && EntityUtils.isEating()) {
            return;
        }
        Runnable runnable = () -> {
            BlockItem blockItem;
            Item patt0$temp;
            this.blocksPlaced = 0;
            if (!(!this.autoSwitch.getValue().equalsIgnoreCase("None") || (patt0$temp = SelfTrapModule.mc.player.getMainHandItem().getItem()) instanceof BlockItem && this.isBlastProof((blockItem = (BlockItem)patt0$temp).getBlock()))) {
                Night.CHAT_MANAGER.tagged("You are currently not holding any blast-proof blocks.", this.getName());
                this.setToggled(false);
                this.targetPositions = new ArrayList<BlockPos>();
                return;
            }
            if (SelfTrapModule.mc.player.onGround()) {
                this.lastGroundPos = SelfTrapModule.mc.player.blockPosition();
                this.jumpingForTrap = false;
            }
            if (this.isTrapComplete(this.lastGroundPos)) {
                if (this.selfDisable.getValue() && SelfTrapModule.mc.player.onGround() && !this.jumpingForTrap) {
                    this.setToggled(false);
                }
                return;
            }
            if (this.holeCheck.getValue() && !HoleUtils.isPlayerInHole((Player)SelfTrapModule.mc.player) && (this.autoJump.getValue().equalsIgnoreCase("None") || SelfTrapModule.mc.player.onGround())) {
                return;
            }
            int slot = this.findBlastProofBlock(0, this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
            int previousSlot = SelfTrapModule.mc.player.getInventory().getSelectedSlot();
            if (slot == -1) {
                Night.CHAT_MANAGER.tagged("No blast-proof blocks could be found in your hotbar.", this.getName());
                this.setToggled(false);
                this.targetPositions = new ArrayList<BlockPos>();
                return;
            }
            this.targetPositions = HoleUtils.getTrapPositions((Player)SelfTrapModule.mc.player, this.lastGroundPos, this.mode.getValue().equalsIgnoreCase("Partial"), this.head.getValue(), this.antiStep.getValue(), this.antiBomb.getValue(), this.strictDirection.getValue(), this.airPlace.getValue()).stream().filter(pos -> WorldUtils.isPlaceable(pos, true)).toList();
            if (this.attack.getValue()) {
                this.attackTrapCrystals();
            }
            if (this.targetPositions.isEmpty()) {
                if (this.selfDisable.getValue() && SelfTrapModule.mc.player.onGround() && !this.jumpingForTrap && this.isTrapComplete(this.lastGroundPos)) {
                    this.setToggled(false);
                }
                return;
            }
            if (!this.autoJump.getValue().equalsIgnoreCase("None") && this.strictDirection.getValue() && SelfTrapModule.mc.player.onGround() && !this.airPlace.getValue()) {
                boolean needsJump = false;
                for (BlockPos pos2 : this.targetPositions) {
                    Direction dir;
                    if (pos2.getY() < SelfTrapModule.mc.player.getBlockY() + 2 || (dir = WorldUtils.getDirection(pos2, this.strictDirection.getValue())) != null) continue;
                    needsJump = true;
                    break;
                }
                if (needsJump) {
                    this.jumpingForTrap = true;
                    mc.execute(() -> {
                        if (SelfTrapModule.mc.player != null && SelfTrapModule.mc.player.onGround()) {
                            SelfTrapModule.mc.player.jumpFromGround();
                        }
                    });
                }
            }
            this.isWorking = true;
            Night.ROTATION_MANAGER.beginBatchRotation();
            try {
                if (this.blocksPlaced >= this.bpt.getValue().intValue()) {
                    return;
                }
                InventoryUtils.switchSlot(this.autoSwitch.getValue(), slot, previousSlot);
                ArrayList<BlockPos> placedPositions = new ArrayList<BlockPos>();
                for (BlockPos position : this.targetPositions) {
                    Direction direction;
                    if (this.blocksPlaced >= this.bpt.getValue().intValue()) break;
                    if (this.attack.getValue()) {
                        for (EndCrystal crystal : SelfTrapModule.mc.level.getEntitiesOfClass(EndCrystal.class, new AABB(position).inflate(0.5))) {
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
                            continue;
                        }
                        BlockPos support = WorldUtils.findSupportBlock(position, placedPositions, this.strictDirection.getValue(), 2);
                        if (support == null || (supportDir = WorldUtils.getDirection(support, placedPositions, this.strictDirection.getValue())) == null || !WorldUtils.placeBlock(support, supportDir, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue())) continue;
                        placedPositions.add(support);
                        ++this.blocksPlaced;
                        continue;
                    }
                    if (!WorldUtils.placeBlock(position, direction, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue())) continue;
                    placedPositions.add(position);
                    ++this.blocksPlaced;
                }
                if (!SelfTrapModule.mc.player.onGround() && this.autoJump.getValue().equalsIgnoreCase("NCP")) {
                    mc.execute(() -> {
                        if (SelfTrapModule.mc.player != null && !SelfTrapModule.mc.player.onGround()) {
                            SelfTrapModule.mc.player.setDeltaMovement(new Vec3(SelfTrapModule.mc.player.getDeltaMovement().x, -0.42, SelfTrapModule.mc.player.getDeltaMovement().z));
                        }
                    });
                }
                InventoryUtils.switchBack(this.autoSwitch.getValue(), slot, previousSlot);
                if (this.selfDisable.getValue() && SelfTrapModule.mc.player.onGround() && !this.jumpingForTrap && this.isTrapComplete(this.lastGroundPos)) {
                    this.setToggled(false);
                }
            }
            finally {
                Night.ROTATION_MANAGER.endBatchRotation();
                this.isWorking = false;
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
        if (SelfTrapModule.mc.player == null || SelfTrapModule.mc.level == null) {
            return;
        }
        if (this.attack.getValue() && (packet = event.getPacket()) instanceof ClientboundAddEntityPacket && (addPacket = (ClientboundAddEntityPacket)packet).getType() == EntityTypes.END_CRYSTAL) {
            Vec3 crystalPos = new Vec3(addPacket.getX(), addPacket.getY(), addPacket.getZ());
            AABB crystalBox = new AABB(crystalPos.x - 1.0, crystalPos.y, crystalPos.z - 1.0, crystalPos.x + 1.0, crystalPos.y + 2.0, crystalPos.z + 1.0);
            boolean nearTrap = false;
            for (BlockPos pos : this.targetPositions) {
                if (!crystalBox.intersects(new AABB(pos)) && !(crystalPos.distanceToSqr(Vec3.atCenterOf((Vec3i)pos)) <= 4.0)) continue;
                nearTrap = true;
                break;
            }
            if (nearTrap && SelfTrapModule.mc.player.getEyePosition().distanceToSqr(crystalPos) <= Mth.square((double)this.attackRange.getValue().doubleValue()) && (this.attackMultiTask.getValue() || !SelfTrapModule.mc.player.isUsingItem())) {
                mc.getConnection().send((Packet)new ServerboundAttackPacket(addPacket.getId()));
                if (this.attackSwing.getValue()) {
                    mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                }
            }
        }
    }

    private void attackTrapCrystals() {
        Night.ROTATION_MANAGER.beginBatchRotation();
        for (EndCrystal crystal : SelfTrapModule.mc.level.getEntitiesOfClass(EndCrystal.class, new AABB(SelfTrapModule.mc.player.blockPosition()).inflate(this.attackRange.getValue().doubleValue() + 3.0))) {
            if (!crystal.isAlive() || crystal.isRemoved() || crystal.tickCount < this.attackAge.getValue().intValue()) continue;
            AABB crystalBox = crystal.getBoundingBox();
            boolean threatens = false;
            for (BlockPos pos : this.targetPositions) {
                if (!SelfTrapModule.mc.level.getBlockState(pos).canBeReplaced() || !new AABB(pos).intersects(crystalBox)) continue;
                threatens = true;
                break;
            }
            if (!threatens || crystalBox.distanceToSqr(SelfTrapModule.mc.player.getEyePosition()) > this.attackRange.getValue().doubleValue() * this.attackRange.getValue().doubleValue() || !this.attackMultiTask.getValue() && SelfTrapModule.mc.player.isUsingItem()) continue;
            if (this.attackRotate.getValue()) {
                float[] rotations = RotationUtils.getRotations(crystal.getBoundingBox().getCenter());
                Night.ROTATION_MANAGER.silentRotate(rotations[0], rotations[1]);
                boolean insideBox = crystalBox.contains(SelfTrapModule.mc.player.getEyePosition());
                if (!insideBox && !WorldUtils.canSee(crystal.getBoundingBox().getCenter())) continue;
            }
            mc.getConnection().send((Packet)new ServerboundAttackPacket(crystal.getId()));
            if (!this.attackSwing.getValue()) continue;
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
        Night.ROTATION_MANAGER.endBatchRotation();
    }

    @Override
    public void onEnable() {
        if (SelfTrapModule.mc.player == null || SelfTrapModule.mc.level == null) {
            return;
        }
        this.packetsSent = 0;
        this.targetPositions.clear();
        this.lastGroundPos = SelfTrapModule.mc.player.blockPosition();
        this.jumpingForTrap = false;
    }

    @Override
    public void onDisable() {
        this.targetPositions = new ArrayList<BlockPos>();
        this.lastGroundPos = null;
        this.jumpingForTrap = false;
    }

    private boolean isTrapComplete(BlockPos base) {
        if (SelfTrapModule.mc.level == null || base == null) {
            return false;
        }
        if (this.mode.getValue().equalsIgnoreCase("Partial")) {
            BlockPos headPos = base.offset(0, 2, 0);
            return !SelfTrapModule.mc.level.getBlockState(headPos).canBeReplaced();
        }
        List<BlockPos> req = HoleUtils.getTrapPositions((Player)SelfTrapModule.mc.player, base, false, this.head.getValue(), this.antiStep.getValue(), this.antiBomb.getValue(), this.strictDirection.getValue(), this.airPlace.getValue());
        for (BlockPos pos : req) {
            if (!SelfTrapModule.mc.level.getBlockState(pos).canBeReplaced()) continue;
            return false;
        }
        return true;
    }

    private boolean isBlastProof(Block block) {
        return block == Blocks.OBSIDIAN || block == Blocks.ENDER_CHEST || block == Blocks.CRYING_OBSIDIAN || block == Blocks.NETHERITE_BLOCK || block == Blocks.RESPAWN_ANCHOR || block == Blocks.ANCIENT_DEBRIS || block == Blocks.ENCHANTING_TABLE || block == Blocks.ANVIL || block == Blocks.CHIPPED_ANVIL || block == Blocks.DAMAGED_ANVIL;
    }

    private int findBlastProofBlock(int start, int end) {
        float bestHardness = -1.0f;
        int bestSlot = -1;
        for (int i = start; i <= end; ++i) {
            BlockItem item;
            Block block;
            Item item2 = SelfTrapModule.mc.player.getInventory().getItem(i).getItem();
            if (!(item2 instanceof BlockItem) || !this.isBlastProof(block = (item = (BlockItem)item2).getBlock())) continue;
            float hardness = block.defaultDestroyTime();
            if (hardness == -1.0f) {
                return i;
            }
            if (!(hardness > bestHardness)) continue;
            bestHardness = hardness;
            bestSlot = i;
        }
        return bestSlot;
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent.Post event) {
        if (this.isWorking) {
            ++this.packetsSent;
        }
    }
}

