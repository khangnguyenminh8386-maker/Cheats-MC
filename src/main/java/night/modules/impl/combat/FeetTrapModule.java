/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket
 *  net.minecraft.network.protocol.game.ServerboundAttackPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.arrow.Arrow
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PlayerJumpEvent;
import night.events.impl.PlayerUpdateEvent;
import night.modules.Module;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.IMinecraft;
import night.utils.minecraft.HoleUtils;
import night.utils.minecraft.InventoryUtils;
import night.utils.minecraft.NetworkUtils;
import night.utils.minecraft.WorldUtils;
import night.utils.miscellaneous.RenderPosition;
import night.utils.rotations.RotationUtils;

public class FeetTrapModule
extends Module {
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
    public NumberSetting limit = new NumberSetting("Limit", "The number of blocks that can be placed per tick.", 4, 1, 20);
    public NumberSetting delay = new NumberSetting("Delay", "The amount of ticks waited between each group of placements.", 0, 0, 20);
    public NumberSetting range = new NumberSetting("Range", "The maximum range at which the blocks will be placed at.", 5.0, 0.0, 12.0);
    public BooleanSetting extension = new BooleanSetting("Extension", "Extends the trap if there are entities obstructing block placement.", false);
    public BooleanSetting floor = new BooleanSetting("Floor", "Places blocks under your feet as well.", true);
    public BooleanSetting corners = new BooleanSetting("Corners", "Also covers the four diagonals at feet level.", false);
    public BooleanSetting jumpDisable = new BooleanSetting("JumpDisable", "Toggles off the module whenever you leave the ground.", false);
    public ModeSetting rotate = new ModeSetting("Rotate", "The rotation mode when placing blocks.", "Grim", new String[]{"None", "Normal", "Grim"});
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", true);
    public BooleanSetting airPlace = new BooleanSetting("AirPlace", "Places blocks in the air using the block below as support, facing UP, when no direct neighbor is available.", false);
    public BooleanSetting grim = new BooleanSetting("Grim", "AirPlace only: places from the offhand via a swap-and-swapback trick instead of the mainhand.", new BooleanSetting.Visibility(this.airPlace, true), false);
    public BooleanSetting multiTask = new BooleanSetting("MultiTask", "Allows placing while an item is already in use (eating, blocking, drawing a bow).", false);
    public BooleanSetting swing = new BooleanSetting("Swing", "Sends a swing packet whenever placing a block.", true);
    public BooleanSetting antiBreak = new BooleanSetting("AntiBreak", "Pre-places extra blocks around a spot an enemy is actively digging through toward your trap.", false);
    public BooleanSetting selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module once it is finished with placing.", false);
    public BooleanSetting render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
    public BooleanSetting attack = new BooleanSetting("Attack", "Attacks any crystal that lands on one of your trap positions.", false);
    public BooleanSetting attackRotate = new BooleanSetting("AttackRotate", "Rotate", "Rotates toward the crystal before attacking it.", new BooleanSetting.Visibility(this.attack, true), true);
    public NumberSetting attackRange = new NumberSetting("AttackRange", "Range", "The maximum range at which crystals will be attacked.", new BooleanSetting.Visibility(this.attack, true), Float.valueOf(3.0f), Float.valueOf(1.0f), Float.valueOf(6.0f));
    public NumberSetting attackAge = new NumberSetting("AttackAge", "Age", "The minimum age (in ticks) a crystal must reach before being attacked.", new BooleanSetting.Visibility(this.attack, true), 5, 0, 20);
    public BooleanSetting attackMultiTask = new BooleanSetting("AttackMultiTask", "Multitask", "Allows attacking while an item is already in use.", new BooleanSetting.Visibility(this.attack, true), true);
    public BooleanSetting attackSwing = new BooleanSetting("AttackSwing", "Swing", "Sends a swing packet whenever attacking a crystal.", new BooleanSetting.Visibility(this.attack, true), true);
    private int ticks = 0;
    private int targetCount = 0;
    private Set<BlockPos> targetPositions = new LinkedHashSet<BlockPos>();
    private final Map<Integer, BreakState> breakStates = new HashMap<Integer, BreakState>();

    private float getMiningSpeed(BlockState state, Player player) {
        ItemStack stack = new ItemStack((ItemLike)Items.NETHERITE_PICKAXE);
        float speed = stack.getDestroySpeed(state);
        if (speed > 1.0f) {
            int level = 5;
            speed += (float)(level * level + 1);
        }
        if (!player.onGround()) {
            speed /= 5.0f;
        }
        return speed;
    }

    @Override
    public String getMetaData() {
        return String.valueOf(this.targetCount);
    }

    @Override
    public void onDisable() {
        this.ticks = 0;
        this.targetCount = 0;
        this.targetPositions.clear();
        this.breakStates.clear();
    }

    @SubscribeEvent
    public void onPlayerJump(PlayerJumpEvent event) {
        if (this.jumpDisable.getValue()) {
            this.setToggled(false);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!this.antiBreak.getValue() || FeetTrapModule.mc.level == null) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (!(packet instanceof ClientboundBlockDestructionPacket)) {
            return;
        }
        ClientboundBlockDestructionPacket packet2 = (ClientboundBlockDestructionPacket)packet;
        if (!(FeetTrapModule.mc.level.getEntity(packet2.getId()) instanceof Player)) {
            return;
        }
        this.breakStates.computeIfAbsent(packet2.getId(), x$0 -> new BreakState(this, (int)x$0)).startBreak(packet2.getPos(), 0.7f);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        BlockItem blockItem;
        Item item;
        if (FeetTrapModule.mc.player == null || FeetTrapModule.mc.level == null) {
            return;
        }
        if (this.jumpDisable.getValue() && !FeetTrapModule.mc.player.onGround()) {
            this.setToggled(false);
            return;
        }
        if (this.ticks < this.delay.getValue().intValue()) {
            ++this.ticks;
            return;
        }
        if (!(!this.autoSwitch.getValue().equalsIgnoreCase("None") || (item = FeetTrapModule.mc.player.getMainHandItem().getItem()) instanceof BlockItem && this.isBlastProof((blockItem = (BlockItem)item).getBlock()))) {
            Night.CHAT_MANAGER.tagged("You are currently not holding any blast-proof blocks.", this.getName());
            this.setToggled(false);
            return;
        }
        int slot = this.findBlastProofBlock();
        int previousSlot = FeetTrapModule.mc.player.getInventory().getSelectedSlot();
        if (slot == -1) {
            Night.CHAT_MANAGER.tagged("No blast-proof blocks could be found in your hotbar.", this.getName());
            this.setToggled(false);
            return;
        }
        this.targetPositions = this.getTargets();
        if (this.antiBreak.getValue()) {
            this.applyAntiBreak();
        }
        List<BlockPos> positions = this.targetPositions.stream().filter(position -> FeetTrapModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)position)) <= Mth.square((double)this.range.getValue().doubleValue())).filter(WorldUtils::isPlaceable).toList();
        this.targetCount = positions.size();
        if (this.attack.getValue()) {
            this.attackTrapCrystals();
        }
        if (positions.isEmpty()) {
            if (this.selfDisable.getValue()) {
                this.setToggled(false);
            }
            return;
        }
        if (!this.multiTask.getValue() && FeetTrapModule.mc.player.isUsingItem()) {
            return;
        }
        Night.ROTATION_MANAGER.beginBatchRotation();
        try {
            InventoryUtils.switchSlot(this.autoSwitch.getValue(), slot, previousSlot);
            int placed = 0;
            ArrayList<BlockPos> placedPositions = new ArrayList<BlockPos>();
            for (BlockPos position2 : positions) {
                if (placed >= this.limit.getValue().intValue()) break;
                Direction direction = WorldUtils.getDirection(position2, placedPositions, this.strictDirection.getValue());
                if (direction == null) {
                    if (!this.airPlace.getValue()) continue;
                    this.placeAir(position2);
                    placedPositions.add(position2);
                    ++placed;
                    continue;
                }
                if (!WorldUtils.placeBlock(position2, direction, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue())) continue;
                placedPositions.add(position2);
                ++placed;
            }
            InventoryUtils.switchBack(this.autoSwitch.getValue(), slot, previousSlot);
        }
        finally {
            Night.ROTATION_MANAGER.endBatchRotation();
        }
        this.ticks = 0;
    }

    private void placeAir(BlockPos position) {
        RenderPosition renderPosition;
        if (!this.grim.getValue()) {
            WorldUtils.airPlaceBlock(position, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.render.getValue());
            return;
        }
        Vec3 hitVec = Vec3.atCenterOf((Vec3i)position).add(0.0, 0.5, 0.0);
        if (!this.rotate.getValue().equalsIgnoreCase("None")) {
            float[] rotations = RotationUtils.getRotations(hitVec);
            if (Night.ROTATION_MANAGER.isBatchingRotations()) {
                Night.ROTATION_MANAGER.batchRotate(rotations[0], rotations[1]);
            } else {
                Night.ROTATION_MANAGER.silentRotate(rotations[0], rotations[1]);
            }
        }
        if (this.render.getValue() && !Night.RENDER_MANAGER.renderPositions.contains(renderPosition = new RenderPosition(position))) {
            Night.RENDER_MANAGER.renderPositions.add(renderPosition);
        }
        BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, position, false);
        mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
        NetworkUtils.sendSequencedPacket(sequence -> new ServerboundUseItemOnPacket(InteractionHand.OFF_HAND, hitResult, sequence));
        if (this.swing.getValue()) {
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.OFF_HAND));
        }
        mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
    }

    private void attackTrapCrystals() {
        Night.ROTATION_MANAGER.beginBatchRotation();
        for (EndCrystal crystal : FeetTrapModule.mc.level.getEntitiesOfClass(EndCrystal.class, new AABB(FeetTrapModule.mc.player.blockPosition()).inflate(this.range.getValue().doubleValue() + 3.0))) {
            if (!crystal.isAlive() || crystal.isRemoved() || crystal.tickCount < this.attackAge.getValue().intValue()) continue;
            AABB crystalBox = crystal.getBoundingBox();
            boolean threatens = false;
            for (BlockPos pos : this.targetPositions) {
                if (!FeetTrapModule.mc.level.getBlockState(pos).canBeReplaced() || !new AABB(pos).intersects(crystalBox)) continue;
                threatens = true;
                break;
            }
            if (!threatens || crystalBox.distanceToSqr(FeetTrapModule.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue()) || !this.attackMultiTask.getValue() && FeetTrapModule.mc.player.isUsingItem()) continue;
            if (this.attackRotate.getValue()) {
                float[] rotations = RotationUtils.getRotations(crystal.getBoundingBox().getCenter());
                Night.ROTATION_MANAGER.silentRotate(rotations[0], rotations[1]);
                boolean insideBox = crystalBox.contains(FeetTrapModule.mc.player.getEyePosition());
                if (!insideBox && !WorldUtils.canSee(crystal.getBoundingBox().getCenter())) continue;
            }
            mc.getConnection().send((Packet)new ServerboundAttackPacket(crystal.getId()));
            if (!this.attackSwing.getValue()) continue;
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
        Night.ROTATION_MANAGER.endBatchRotation();
    }

    private void applyAntiBreak() {
        for (BreakState state : this.breakStates.values()) {
            state.onTick();
        }
        if (!this.antiBreak.getValue() || this.targetPositions.isEmpty()) {
            return;
        }
        ArrayList<BlockPos> extraTargets = new ArrayList<BlockPos>();
        for (BlockPos pos : this.targetPositions) {
            boolean breaking = false;
            for (BreakState state : this.breakStates.values()) {
                if (!state.isBreaking(pos)) continue;
                breaking = true;
                break;
            }
            if (!breaking) continue;
            for (Direction dir : Direction.values()) {
                BlockPos around;
                if (dir == Direction.DOWN || this.targetPositions.contains(around = pos.relative(dir)) || extraTargets.contains(around) || !FeetTrapModule.mc.level.getBlockState(around).canBeReplaced() || this.isEntityBlocking(around)) continue;
                extraTargets.add(around);
            }
        }
        this.targetPositions.addAll(extraTargets);
    }

    private boolean isEntityBlocking(BlockPos pos) {
        AABB blockBox = new AABB(pos);
        for (Entity entity : FeetTrapModule.mc.level.entitiesForRendering()) {
            if (entity.distanceToSqr((Entity)FeetTrapModule.mc.player) > 10.0 || entity instanceof EndCrystal || entity instanceof ItemEntity || entity instanceof Arrow || !entity.getBoundingBox().intersects(blockBox)) continue;
            return true;
        }
        return false;
    }

    private Set<BlockPos> getTargets() {
        LinkedHashSet<BlockPos> positions = new LinkedHashSet<BlockPos>(HoleUtils.getFeetPositions((Player)FeetTrapModule.mc.player, this.extension.getValue(), this.floor.getValue(), false));
        if (!this.corners.getValue()) {
            return positions;
        }
        AABB box = FeetTrapModule.mc.player.getBoundingBox();
        int y = Mth.floor((double)FeetTrapModule.mc.player.getY());
        int x = Mth.floor((double)box.minX);
        while ((double)x < Math.ceil(box.maxX)) {
            int z = Mth.floor((double)box.minZ);
            while ((double)z < Math.ceil(box.maxZ)) {
                BlockPos base = new BlockPos(x, y, z);
                for (BlockPos corner : new BlockPos[]{base.north().east(), base.north().west(), base.south().east(), base.south().west()}) {
                    if (this.isEntityBlocking(corner)) continue;
                    positions.add(corner);
                }
                ++z;
            }
            ++x;
        }
        return positions;
    }

    private int findBlastProofBlock() {
        float bestHardness = -1.0f;
        int bestSlot = -1;
        for (int i = 0; i <= 8; ++i) {
            BlockItem item;
            Block block;
            Item item2 = FeetTrapModule.mc.player.getInventory().getItem(i).getItem();
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

    private boolean isBlastProof(Block block) {
        return block == Blocks.OBSIDIAN || block == Blocks.ENDER_CHEST || block == Blocks.CRYING_OBSIDIAN || block == Blocks.NETHERITE_BLOCK || block == Blocks.RESPAWN_ANCHOR || block == Blocks.ANCIENT_DEBRIS || block == Blocks.ENCHANTING_TABLE || block == Blocks.ANVIL || block == Blocks.CHIPPED_ANVIL || block == Blocks.DAMAGED_ANVIL;
    }

    private class BreakState {
        final int entityId;
        final BreakTask current;
        final BreakTask doubleMine;
        final /* synthetic */ FeetTrapModule this$0;

        BreakState(FeetTrapModule feetTrapModule, int entityId) {
            FeetTrapModule feetTrapModule2 = feetTrapModule;
            Objects.requireNonNull(feetTrapModule2);
            this.this$0 = feetTrapModule2;
            this.current = new BreakTask();
            this.doubleMine = new BreakTask();
            this.entityId = entityId;
        }

        void startBreak(BlockPos pos, float speed) {
            if (this.current.active && !this.current.pos.equals((Object)pos)) {
                if (!this.doubleMine.active) {
                    this.doubleMine.copyFrom(this.current);
                    this.doubleMine.targetSpeed = 1.0f;
                }
                this.current.reset();
            }
            if (!this.current.active || !this.current.pos.equals((Object)pos)) {
                this.current.start(pos, speed);
            }
        }

        void onTick() {
            this.tickTask(this.current);
            this.tickTask(this.doubleMine);
        }

        private void tickTask(BreakTask task) {
            if (!task.active) {
                return;
            }
            Entity entity = IMinecraft.mc.level.getEntity(this.entityId);
            if (!(entity instanceof Player)) {
                task.reset();
                return;
            }
            Player player = (Player)entity;
            BlockState state = IMinecraft.mc.level.getBlockState(task.pos);
            if (state.isAir()) {
                task.active = false;
                return;
            }
            float hardness = state.getDestroySpeed((BlockGetter)IMinecraft.mc.level, task.pos);
            if (hardness == -1.0f) {
                return;
            }
            float speed = this.this$0.getMiningSpeed(state, player);
            task.progress += speed / hardness / 30.0f;
            if (task.progress >= task.targetSpeed) {
                task.active = false;
            }
        }

        boolean isBreaking(BlockPos pos) {
            if (this.current.pos != null && this.current.pos.equals((Object)pos) && this.current.active) {
                return true;
            }
            return this.doubleMine.pos != null && this.doubleMine.pos.equals((Object)pos) && this.doubleMine.active;
        }
    }

    private static class BreakTask {
        BlockPos pos;
        float targetSpeed;
        float progress;
        boolean active;

        private BreakTask() {
        }

        void start(BlockPos pos, float speed) {
            this.pos = pos;
            this.targetSpeed = speed;
            this.progress = 0.0f;
            this.active = true;
        }

        void copyFrom(BreakTask other) {
            this.pos = other.pos;
            this.targetSpeed = other.targetSpeed;
            this.progress = other.progress;
            this.active = other.active;
        }

        void reset() {
            this.active = false;
            this.progress = 0.0f;
        }
    }
}

