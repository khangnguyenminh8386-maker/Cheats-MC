/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemPacket
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.movement;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.api.ISprintModule;
import night.modules.impl.player.MultiTaskModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.utils.minecraft.InventoryUtils;
import night.utils.minecraft.NetworkUtils;

@RegisterModule(name="Phase", description="Throws an ender pearl to phase into blocks. KingMC Crawl mode requires crawling away from the target block before throwing.", category=Module.Category.MOVEMENT)
public class PhaseModule
extends Module {
    public ModeSetting logic = new ModeSetting("Logic", "Phase bypass targeting logic: KingMC (bedrock = Grim behavior, crawl requires crawling away from block before throwing), Grim, or NCP.", "KingMC", new String[]{"KingMC", "Grim", "NCP"});
    public ModeSetting swap = new ModeSetting("Swap", "The mode that will be used for switching to the pearl before throwing it.", "Silent", InventoryUtils.SWITCH_MODES);
    public BooleanSetting crawl = new BooleanSetting("Crawl", "Crawl phase mode. In KingMC mode, crawl away from the target block before throwing.", false);
    public BooleanSetting antiVoid = new BooleanSetting("AntiVoid", "Prevents phasing into or through blocks that lead directly into the void.", true);
    public BooleanSetting debug = new BooleanSetting("Debug", "Chat-prints pos/yaw/pitch/target for every throw, so a failed (popped-up) attempt can be matched back to its exact numbers.", false);
    public ModeSetting ncpMode = new ModeSetting("NCP-Mode", "Item used to bypass pearl distance checks on NCP.", new ModeSetting.Visibility(this.logic, "NCP"), "FireCharge", new String[]{"FireCharge", "FlintAndSteel", "Cobweb"});
    public ModeSetting bypassSwitch = new ModeSetting("Switch", "The mode that will be used for switching to the bypass item.", new ModeSetting.Visibility(this.logic, "NCP"), "Silent", new String[]{"Normal", "Silent", "AltPickup", "AltSwap"});
    public BooleanSetting autoRemove = new BooleanSetting("AutoRemove", "Automatically removes the bypass block/fire once you have phased.", new ModeSetting.Visibility(this.logic, "NCP"), true);
    private static final double CORNER_THRESHOLD = 0.5;
    private static final double CORNER_OFFSET = 0.5;
    private int attempt = 0;

    private boolean isInWeb(Entity entity) {
        if (entity.getInBlockState().is(Blocks.COBWEB)) {
            return true;
        }
        for (BlockPos pos : BlockPos.betweenClosed((BlockPos)BlockPos.containing((double)entity.getBoundingBox().minX, (double)entity.getBoundingBox().minY, (double)entity.getBoundingBox().minZ), (BlockPos)BlockPos.containing((double)entity.getBoundingBox().maxX, (double)entity.getBoundingBox().maxY, (double)entity.getBoundingBox().maxZ))) {
            if (!entity.level().getBlockState(pos).is(Blocks.COBWEB)) continue;
            return true;
        }
        return false;
    }

    private boolean isInLadder(Entity entity) {
        if (entity.getInBlockState().is(Blocks.LADDER)) {
            return true;
        }
        for (BlockPos pos : BlockPos.betweenClosed((BlockPos)BlockPos.containing((double)entity.getBoundingBox().minX, (double)entity.getBoundingBox().minY, (double)entity.getBoundingBox().minZ), (BlockPos)BlockPos.containing((double)entity.getBoundingBox().maxX, (double)entity.getBoundingBox().maxY, (double)entity.getBoundingBox().maxZ))) {
            if (!entity.level().getBlockState(pos).is(Blocks.LADDER)) continue;
            return true;
        }
        return false;
    }

    private boolean isPlayerCrawling() {
        if (PhaseModule.mc.player == null) {
            return false;
        }
        return PhaseModule.mc.player.getPose() == Pose.SWIMMING || PhaseModule.mc.player.isVisuallyCrawling() || PhaseModule.mc.player.getEyeHeight() <= 1.0f;
    }

    @Override
    public void onEnable() {
        Vec3 target;
        if (PhaseModule.mc.player == null || PhaseModule.mc.level == null) {
            this.setToggled(false);
            return;
        }
        for (BlockPos scaffoldPosition : new BlockPos[]{PhaseModule.mc.player.blockPosition(), PhaseModule.mc.player.blockPosition().below()}) {
            if (!PhaseModule.mc.level.getBlockState(scaffoldPosition).is(Blocks.SCAFFOLDING)) continue;
            NetworkUtils.sendSequencedPacket(sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, scaffoldPosition, Direction.UP, sequence));
            NetworkUtils.sendSequencedPacket(sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, scaffoldPosition, Direction.UP, sequence));
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            PhaseModule.mc.level.removeBlock(scaffoldPosition, false);
        }
        boolean isKingMC = this.logic.getValue().equalsIgnoreCase("KingMC");
        boolean swimPose = this.isPlayerCrawling();
        if (!this.crawl.getValue() && swimPose && !isKingMC) {
            this.setToggled(false);
            return;
        }
        boolean isCrawling = this.crawl.getValue() && swimPose;
        boolean crawlSwimStuck = isCrawling && swimPose;
        boolean inCobweb = this.isInWeb((Entity)PhaseModule.mc.player);
        boolean inLadder = this.isInLadder((Entity)PhaseModule.mc.player);
        if (!(crawlSwimStuck || inCobweb || inLadder || PhaseModule.mc.level.getBlockState(PhaseModule.mc.player.blockPosition()).canBeReplaced())) {
            this.setToggled(false);
            return;
        }
        if (PhaseModule.mc.player.isCrouching()) {
            this.setToggled(false);
            return;
        }
        if (PhaseModule.mc.player.getCooldowns().isOnCooldown(new ItemStack((ItemLike)Items.ENDER_PEARL))) {
            this.setToggled(false);
            return;
        }
        MultiTaskModule multiTask = Night.MODULE_MANAGER.getModule(MultiTaskModule.class);
        boolean keepEating = multiTask != null && multiTask.isToggled() && multiTask.pearl.getValue() && PhaseModule.mc.player.isUsingItem();
        String pearlMode = keepEating ? "AltSwap" : this.swap.getValue();
        int slot = InventoryUtils.find(Items.ENDER_PEARL, 0, pearlMode.equalsIgnoreCase("AltSwap") || pearlMode.equalsIgnoreCase("AltPickup") ? 35 : 8);
        int previousSlot = PhaseModule.mc.player.getInventory().getSelectedSlot();
        boolean didPlaceBypass = false;
        if (slot == -1) {
            Night.CHAT_MANAGER.tagged("No pearls could be found in your hotbar.", this.getName());
            this.setToggled(false);
            return;
        }
        float prevYaw = PhaseModule.mc.player.getYRot();
        float prevPitch = PhaseModule.mc.player.getXRot();
        BlockPos downPosition = PhaseModule.mc.player.blockPosition().below();
        float rawYaw = Mth.wrapDegrees((float)this.getPhaseYaw());
        float mod90 = (rawYaw % 90.0f + 90.0f) % 90.0f;
        boolean nearCardinal = Math.min(mod90, 90.0f - mod90) < 22.5f;
        boolean onBedrock = this.isOnBedrock();
        boolean isGrim = this.logic.getValue().equalsIgnoreCase("Grim");
        if (isKingMC && !onBedrock) {
            target = isCrawling ? this.calculateKingMCCrawlTarget(rawYaw) : this.calculateKingMCStandingTarget(rawYaw);
        } else if (isGrim) {
            target = this.calculateGrimSectorTarget();
        } else {
            Vec3 vec3 = target = nearCardinal ? this.boundaryTarget(rawYaw) : this.calculateTargetPos();
        }
        if (this.antiVoid.getValue() && this.isVoidHazard(target, rawYaw)) {
            Night.CHAT_MANAGER.tagged("Phase cancelled to prevent falling into the void.", this.getName());
            if (mc.getSoundManager() != null) {
                mc.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((Holder)SoundEvents.NOTE_BLOCK_PLING, (float)1.5f));
            }
            this.setToggled(false);
            return;
        }
        float yaw = this.calcYaw(target);
        float pitch = this.solvePitch(target);
        if (this.debug.getValue()) {
            ++this.attempt;
            Night.CHAT_MANAGER.tagged(String.format("#%d [%s%s] pos=(%.4f, %.4f, %.4f) yaw=%.2f pitch=%.2f target=(%.4f, %.4f, %.4f) sector=%s", this.attempt, this.logic.getValue(), isKingMC && onBedrock ? "/Bedrock" : (isKingMC && isCrawling ? "/Crawl" : (isKingMC ? "/Standing" : "")), PhaseModule.mc.player.getX(), PhaseModule.mc.player.getY(), PhaseModule.mc.player.getZ(), Float.valueOf(yaw), Float.valueOf(pitch), target.x, target.y, target.z, nearCardinal ? "straight" : "corner"), this.getName());
        }
        if (this.logic.getValue().equalsIgnoreCase("NCP") && (PhaseModule.mc.level.getBlockState(PhaseModule.mc.player.blockPosition()).isAir() || inCobweb || inLadder) && !PhaseModule.mc.level.getBlockState(downPosition).canBeReplaced()) {
            Item targetItem = switch (this.ncpMode.getValue()) {
                case "FlintAndSteel" -> Items.FLINT_AND_STEEL;
                case "Cobweb" -> Items.COBWEB;
                default -> Items.FIRE_CHARGE;
            };
            int bypassSlot = InventoryUtils.find(targetItem, InventoryUtils.HOTBAR_START, this.bypassSwitch.getValue().equalsIgnoreCase("AltSwap") || this.bypassSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
            if (bypassSlot != -1) {
                Night.ROTATION_MANAGER.packetRotate(yaw, 90.0f, true);
                InventoryUtils.switchSlot(this.bypassSwitch.getValue(), bypassSlot, previousSlot);
                NetworkUtils.sendSequencedPacket(sequence -> new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf((Vec3i)downPosition).add(0.0, 1.0, 0.0), Direction.UP, downPosition, false), sequence));
                mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                InventoryUtils.switchBack(this.bypassSwitch.getValue(), bypassSlot, previousSlot);
                didPlaceBypass = true;
            }
        }
        Night.ROTATION_MANAGER.packetRotate(yaw, pitch, true);
        if (InventoryUtils.switchSlot(pearlMode, slot, previousSlot)) {
            NetworkUtils.sendSequencedPacket(sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, yaw, pitch));
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            InventoryUtils.switchBack(pearlMode, slot, previousSlot);
        }
        if (didPlaceBypass && this.autoRemove.getValue()) {
            NetworkUtils.sendSequencedPacket(sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, PhaseModule.mc.player.blockPosition(), Direction.UP, sequence));
            NetworkUtils.sendSequencedPacket(sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, PhaseModule.mc.player.blockPosition(), Direction.UP, sequence));
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
        this.setToggled(false);
    }

    private float getPhaseYaw() {
        ISprintModule sprint;
        if (PhaseModule.mc.player == null) {
            return 0.0f;
        }
        float realYaw = PhaseModule.mc.player.getYRot();
        if (Night.MODULE_MANAGER != null && (sprint = (ISprintModule)((Object)Night.MODULE_MANAGER.getModule("Sprint"))) != null && sprint.isGrimCompensating()) {
            return sprint.getGrimYaw();
        }
        int inputX = (PhaseModule.mc.options.keyRight.isDown() ? 1 : 0) - (PhaseModule.mc.options.keyLeft.isDown() ? 1 : 0);
        int inputZ = (PhaseModule.mc.options.keyUp.isDown() ? 1 : 0) - (PhaseModule.mc.options.keyDown.isDown() ? 1 : 0);
        if (inputX != 0 || inputZ != 0) {
            float moveAngle = (float)Math.toDegrees(Math.atan2(inputX, inputZ));
            return Mth.wrapDegrees((float)(realYaw + moveAngle));
        }
        return realYaw;
    }

    private boolean isOnBedrock() {
        if (PhaseModule.mc.player == null || PhaseModule.mc.level == null) {
            return false;
        }
        double minY = PhaseModule.mc.player.getBoundingBox().minY;
        BlockPos below = BlockPos.containing((double)PhaseModule.mc.player.getX(), (double)(minY - 0.1), (double)PhaseModule.mc.player.getZ());
        if (PhaseModule.mc.level.getBlockState(below).is(Blocks.BEDROCK)) {
            return true;
        }
        if (PhaseModule.mc.level.getBlockState(PhaseModule.mc.player.blockPosition().below()).is(Blocks.BEDROCK)) {
            return true;
        }
        return PhaseModule.mc.level.getBlockState(PhaseModule.mc.player.blockPosition()).is(Blocks.BEDROCK);
    }

    private Vec3 calculateKingMCStandingTarget(float rawYaw) {
        double px = PhaseModule.mc.player.getX();
        double py = PhaseModule.mc.player.getY();
        double pz = PhaseModule.mc.player.getZ();
        double yawRad = Math.toRadians(rawYaw);
        Vec3 lookDir = new Vec3(-Math.sin(yawRad), 0.0, Math.cos(yawRad)).normalize();
        float mod90 = (rawYaw % 90.0f + 90.0f) % 90.0f;
        boolean nearCardinal = Math.min(mod90, 90.0f - mod90) < 22.5f;
        double SAFE_OFFSET = 0.165;
        if (nearCardinal) {
            boolean zAxis;
            boolean bl = zAxis = Math.abs(lookDir.z) > Math.abs(lookDir.x);
            if (zAxis) {
                double boundaryZ = lookDir.z > 0.0 ? Math.floor(pz) + 1.0 : Math.floor(pz);
                double targetZ = lookDir.z > 0.0 ? boundaryZ - 0.165 : boundaryZ + 0.165;
                return new Vec3(px, py, targetZ);
            }
            double boundaryX = lookDir.x > 0.0 ? Math.floor(px) + 1.0 : Math.floor(px);
            double targetX = lookDir.x > 0.0 ? boundaryX - 0.165 : boundaryX + 0.165;
            return new Vec3(targetX, py, pz);
        }
        double boundaryX = lookDir.x > 0.0 ? Math.floor(px) + 1.0 : Math.floor(px);
        double boundaryZ = lookDir.z > 0.0 ? Math.floor(pz) + 1.0 : Math.floor(pz);
        double targetX = lookDir.x > 0.0 ? boundaryX - 0.165 : boundaryX + 0.165;
        double targetZ = lookDir.z > 0.0 ? boundaryZ - 0.165 : boundaryZ + 0.165;
        return new Vec3(targetX, py, targetZ);
    }

    private Vec3 calculateKingMCCrawlTarget(float rawYaw) {
        double seamZ;
        double seamX;
        boolean nearCardinal;
        Vec3 lookDir;
        float cameraYaw;
        double camYawRad;
        Vec3 camLookDir;
        Vec3 eyePos = this.getEffectiveEyePos();
        BlockHitResult hit = PhaseModule.mc.level.clip(new ClipContext(eyePos, eyePos.add((camLookDir = new Vec3(-Math.sin(camYawRad = Math.toRadians(cameraYaw = PhaseModule.mc.player.getYRot())), 0.0, Math.cos(camYawRad)).normalize()).scale(4.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)PhaseModule.mc.player));
        if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
            rawYaw = cameraYaw;
            lookDir = camLookDir;
        } else {
            double yawRad = Math.toRadians(rawYaw);
            lookDir = new Vec3(-Math.sin(yawRad), 0.0, Math.cos(yawRad)).normalize();
            hit = PhaseModule.mc.level.clip(new ClipContext(eyePos, eyePos.add(lookDir.scale(4.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)PhaseModule.mc.player));
        }
        BlockPos targetBlock = null;
        Direction hitSide = null;
        Vec3 hitPos = null;
        if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult bHit = hit;
            targetBlock = bHit.getBlockPos();
            hitSide = bHit.getDirection();
            hitPos = bHit.getLocation();
        } else {
            Direction facing = Direction.fromYRot((double)rawYaw);
            targetBlock = PhaseModule.mc.player.blockPosition().relative(facing, 1);
            hitSide = facing.getOpposite();
        }
        double targetY = (double)targetBlock.getY() + 0.52;
        float mod90 = (rawYaw % 90.0f + 90.0f) % 90.0f;
        boolean bl = nearCardinal = Math.min(mod90, 90.0f - mod90) < 22.5f;
        if (nearCardinal && hitSide != null && hitSide.getAxis().isHorizontal()) {
            double targetX;
            double targetZ;
            if (hitSide.getAxis() == Direction.Axis.Z) {
                double d = targetZ = hitSide == Direction.NORTH ? (double)targetBlock.getZ() : (double)targetBlock.getZ() + 1.0;
                targetX = hitPos != null && Math.abs(hitPos.x - (double)Math.round(hitPos.x)) < 0.25 ? (double)Math.round(hitPos.x) : (double)targetBlock.getX() + 0.5;
            } else {
                targetX = hitSide == Direction.WEST ? (double)targetBlock.getX() : (double)targetBlock.getX() + 1.0;
                targetZ = hitPos != null && Math.abs(hitPos.z - (double)Math.round(hitPos.z)) < 0.25 ? (double)Math.round(hitPos.z) : (double)targetBlock.getZ() + 0.5;
            }
            return new Vec3(targetX, targetY, targetZ);
        }
        if (hitPos != null) {
            seamX = Math.round(hitPos.x);
            seamZ = Math.round(hitPos.z);
        } else {
            seamX = Math.round(PhaseModule.mc.player.getX() + lookDir.x);
            seamZ = Math.round(PhaseModule.mc.player.getZ() + lookDir.z);
        }
        return new Vec3(seamX, targetY, seamZ);
    }

    private Vec3 calculateTargetPos() {
        double playerX = PhaseModule.mc.player.getX();
        double playerZ = PhaseModule.mc.player.getZ();
        double y = this.crawl.getValue() && this.isPlayerCrawling() ? (double)PhaseModule.mc.player.blockPosition().below().getY() : PhaseModule.mc.player.getY() - 0.5;
        double nearestIntX = Math.round(playerX);
        double nearestIntZ = Math.round(playerZ);
        double dxCorner = nearestIntX - playerX;
        double dzCorner = nearestIntZ - playerZ;
        if (Math.abs(dxCorner) <= 0.5 && Math.abs(dzCorner) <= 0.5) {
            return new Vec3(playerX + Mth.clamp((double)dxCorner, (double)-0.5, (double)0.5), y, playerZ + Mth.clamp((double)dzCorner, (double)-0.5, (double)0.5));
        }
        double A = 0.241660973353061;
        double B = 0.7853981633974483;
        double x = playerX + Mth.clamp((double)(this.toClosest(playerX, Math.floor(playerX) + 0.241660973353061, Math.floor(playerX) + 0.7853981633974483) - playerX), (double)-0.2, (double)0.2);
        double z = playerZ + Mth.clamp((double)(this.toClosest(playerZ, Math.floor(playerZ) + 0.241660973353061, Math.floor(playerZ) + 0.7853981633974483) - playerZ), (double)-0.2, (double)0.2);
        return new Vec3(x, y, z);
    }

    private double toClosest(double num, double min, double max) {
        return num - min > max - num ? max : min;
    }

    private Vec3 calculateGrimSectorTarget() {
        double centerX;
        double px = PhaseModule.mc.player.getX();
        double pz = PhaseModule.mc.player.getZ();
        double y = this.crawl.getValue() && this.isPlayerCrawling() ? (double)PhaseModule.mc.player.blockPosition().below().getY() : PhaseModule.mc.player.getY() - 0.5;
        double floorX = Math.floor(px);
        double floorZ = Math.floor(pz);
        double centerZ = floorZ + 0.5;
        double angle = Math.toDegrees(Math.atan2(pz - centerZ, px - (centerX = floorX + 0.5)));
        if (angle < 0.0) {
            angle += 360.0;
        }
        int sector = (int)Math.floor((angle + 22.5) / 45.0) & 7;
        double x1 = floorX + 1.0;
        double z1 = floorZ + 1.0;
        return switch (sector) {
            case 0 -> new Vec3(x1, y, centerZ);
            case 1 -> new Vec3(x1, y, z1);
            case 2 -> new Vec3(centerX, y, z1);
            case 3 -> new Vec3(floorX, y, z1);
            case 4 -> new Vec3(floorX, y, centerZ);
            case 5 -> new Vec3(floorX, y, floorZ);
            case 6 -> new Vec3(centerX, y, floorZ);
            default -> new Vec3(x1, y, floorZ);
        };
    }

    private Vec3 boundaryTarget(float yawDeg) {
        boolean zAxis;
        double px = PhaseModule.mc.player.getX();
        double pz = PhaseModule.mc.player.getZ();
        double y = this.crawl.getValue() && this.isPlayerCrawling() ? (double)PhaseModule.mc.player.blockPosition().below().getY() : PhaseModule.mc.player.getY() - 0.5;
        double rad = Math.toRadians(yawDeg);
        Vec3 lookDir = new Vec3(-Math.sin(rad), 0.0, Math.cos(rad)).normalize();
        boolean bl = zAxis = Math.abs(lookDir.z) > Math.abs(lookDir.x);
        if (zAxis) {
            double boundaryZ = lookDir.z > 0.0 ? Math.floor(pz) + 1.0 : Math.floor(pz);
            return new Vec3(px, y, boundaryZ);
        }
        double boundaryX = lookDir.x > 0.0 ? Math.floor(px) + 1.0 : Math.floor(px);
        return new Vec3(boundaryX, y, pz);
    }

    private Vec3 getEffectiveEyePos() {
        if (PhaseModule.mc.player == null) {
            return Vec3.ZERO;
        }
        double eyeY = PhaseModule.mc.player.getEyeY() - 0.1;
        return new Vec3(PhaseModule.mc.player.getX(), eyeY, PhaseModule.mc.player.getZ());
    }

    private float calcYaw(Vec3 target) {
        Vec3 eye = this.getEffectiveEyePos();
        Vec3 diff = target.subtract(eye);
        return (float)Math.toDegrees(Math.atan2(-diff.x, diff.z));
    }

    private float solvePitch(Vec3 target) {
        Vec3 eye = this.getEffectiveEyePos();
        double d = Math.hypot(target.x - eye.x, target.z - eye.z);
        double dy = target.y - eye.y;
        if (d < 1.0E-4) {
            return dy < 0.0 ? 85.0f : -85.0f;
        }
        double lo = -85.0;
        double hi = 89.9;
        for (int i = 0; i < 30; ++i) {
            double mid = (lo + hi) * 0.5;
            double simY = PhaseModule.simulatePearlYAtDist(mid, d);
            if (simY < dy) {
                hi = mid;
                continue;
            }
            lo = mid;
        }
        return (float)((lo + hi) * 0.5);
    }

    private static double simulatePearlYAtDist(double pitchDeg, double targetDist) {
        double p = Math.toRadians(pitchDeg);
        double vh = 1.5 * Math.cos(p);
        double vy = -1.5 * Math.sin(p);
        double x = 0.0;
        double y = 0.0;
        for (int i = 0; i < 100; ++i) {
            double prevX = x;
            double prevY = y;
            x += vh;
            y += vy;
            if (x >= targetDist) {
                double f = (targetDist - prevX) / (x - prevX);
                return prevY + (y - prevY) * f;
            }
            vh *= 0.99;
            vy = vy * 0.99 - 0.03;
        }
        return y;
    }

    private boolean isFloorVoid(double x, double y, double z) {
        if (PhaseModule.mc.level == null) {
            return false;
        }
        int minY = PhaseModule.mc.level.getMinY();
        int startY = (int)Math.floor(y);
        if (startY < minY) {
            return true;
        }
        for (int checkY = startY; checkY >= minY; --checkY) {
            BlockPos pos = BlockPos.containing((double)x, (double)checkY, (double)z);
            BlockState state = PhaseModule.mc.level.getBlockState(pos);
            if (state.isAir() || state.getCollisionShape((BlockGetter)PhaseModule.mc.level, pos).isEmpty()) continue;
            return false;
        }
        return true;
    }

    private boolean isVoidHazard(Vec3 target, float rawYaw) {
        if (PhaseModule.mc.player == null || PhaseModule.mc.level == null) {
            return false;
        }
        double px = PhaseModule.mc.player.getX();
        double py = PhaseModule.mc.player.getY();
        double pz = PhaseModule.mc.player.getZ();
        int minY = PhaseModule.mc.level.getMinY();
        if (py < (double)minY) {
            return true;
        }
        double rad = Math.toRadians(rawYaw);
        Vec3 lookDir = new Vec3(-Math.sin(rad), 0.0, Math.cos(rad)).normalize();
        BlockPos targetPos = BlockPos.containing((double)target.x, (double)target.y, (double)target.z);
        BlockPos destPos = BlockPos.containing((double)(px + lookDir.x * 0.5), (double)py, (double)(pz + lookDir.z * 0.5));
        boolean targetHasFloor = !this.isFloorVoid((double)targetPos.getX() + 0.5, target.y, (double)targetPos.getZ() + 0.5);
        boolean destHasFloor = !this.isFloorVoid((double)destPos.getX() + 0.5, py, (double)destPos.getZ() + 0.5);
        return !targetHasFloor && !destHasFloor;
    }
}

