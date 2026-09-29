/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundAttackPacket
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.combat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PlayerUpdateEvent;
import night.events.impl.RenderWorldEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.movement.ElytraFlyModule;
import night.settings.Setting;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.color.ColorUtils;
import night.utils.graphics.Renderer3D;
import night.utils.minecraft.EntityUtils;
import night.utils.minecraft.InventoryUtils;
import night.utils.rotations.RotationUtils;

@RegisterModule(name="AutoMace", description="Elytra-boosts above the target, free-falls in, and mace-smashes on repeat.", category=Module.Category.COMBAT)
public class AutoMaceModule
extends Module {
    public NumberSetting range = new NumberSetting("Range", "Search radius to find a target.", 20.0, 5.0, 50.0);
    public NumberSetting minHeight = new NumberSetting("MinHeight", "Vertical height above the target to reach before diving.", 15.0, 3.0, 30.0);
    public NumberSetting attackRange = new NumberSetting("AttackRange", "Distance to the target that triggers the mace attack.", 4.0, 1.0, 6.0);
    public NumberSetting angle = new NumberSetting("Angle", "Angle", "Flight angle relative to the target: 0 = horizontal, 90 = straight vertical.", new Setting.Visibility(), 60.0, 0.0, 90.0, 1);
    public BooleanSetting smartTerrain = new BooleanSetting("SmartTerrain", "Predicts terrain ahead during ascend/dive and steepens the boost to fly over/around it instead of Angle's fixed value.", true);
    public NumberSetting terrainLookAhead = new NumberSetting("TerrainLookAhead", "SmartTerrain: how many steps ahead along the boost line are checked for obstacles.", 6.0, 2.0, 15.0);
    public NumberSetting terrainClimbRate = new NumberSetting("TerrainClimbRate", "SmartTerrain: pitch (degrees) added per tick while the line ahead is blocked.", 4.0, 1.0, 15.0);
    public NumberSetting terrainMaxClimb = new NumberSetting("TerrainMaxClimb", "SmartTerrain: max pitch lift before falling back to the escape sweep.", 40.0, 10.0, 85.0);
    public BooleanSetting fakeFly = new BooleanSetting("FakeFly", "Keeps real armor on and cycles the elytra in/out (GrimV3 trick) instead of wearing it the whole combo. Opt-in, reliability unverified.", false);
    public BooleanSetting swing = new BooleanSetting("Swing", "Whether to swing your hand when attacking.", true);
    public ModeSetting swap = new ModeSetting("Swap", "Weapon switch mode.", "Silent", new String[]{"None", "Normal", "Silent"});
    public BooleanSetting render = new BooleanSetting("Render", "Renders an indicator around the current target.", true);
    public ModeSetting renderMode = new ModeSetting("RenderMode", "Target render mode.", new BooleanSetting.Visibility(this.render, true), "Circle", new String[]{"Box", "Circle", "Both"});
    public CategorySetting entitiesCategory = new CategorySetting("Entities", "Target entity filter settings.");
    public BooleanSetting players = new BooleanSetting("Players", "Target player entities.", new CategorySetting.Visibility(this.entitiesCategory), true);
    public BooleanSetting friends = new BooleanSetting("Friends", "Target friends.", new CategorySetting.Visibility(this.entitiesCategory), false);
    public BooleanSetting hostiles = new BooleanSetting("Hostiles", "Target hostile mobs.", new CategorySetting.Visibility(this.entitiesCategory), false);
    public BooleanSetting neutrals = new BooleanSetting("Neutrals", "Target neutral mobs.", new CategorySetting.Visibility(this.entitiesCategory), false);
    public BooleanSetting animals = new BooleanSetting("Animals", "Target passive animal mobs.", new CategorySetting.Visibility(this.entitiesCategory), false);
    public ColorSetting boxColor = new ColorSetting("TargetColor", "Color of the target render box.", new BooleanSetting.Visibility(this.render, true), ColorUtils.getDefaultFillColor());
    private static final long FADE_TIME_MS = 300L;
    private static final float SMASH_FALL_DISTANCE = 1.5f;
    private static final int MIN_FREEFALL_TICKS = 3;
    private static final float STEEP_PITCH = 85.0f;
    private static final double LEAD_TICKS = 4.0;
    private static final int STUCK_SWEEP_TICKS = 20;
    private static final double SCAN_LOOKAHEAD = 8.0;
    private static final double SCAN_CLEAR_DIST = 6.0;
    private static final double SCAN_MIN_DIST = 2.5;
    private static final float[] SCAN_YAW_OFFSETS = new float[]{0.0f, -25.0f, 25.0f, -50.0f, 50.0f, -75.0f, 75.0f};
    public Entity target = null;
    private Entity lastTarget = null;
    private long lastTargetTime = 0L;
    private int originalSlot = -1;
    private boolean ascending = true;
    private boolean elytraUnequipped = false;
    private int elytraParkedSlot = -1;
    private int freefallTicks = 0;
    private double freefallLastDistance = 0.0;
    private double freefallLastTargetDist = -1.0;
    private boolean launchKicked = false;
    private int armorParkedSlot = -1;
    private int escapeStuckTicks = 0;
    private double terrainLift = 0.0;
    private int escapeSteepTicks = 0;
    private int fakeFlyTicks = 0;
    private static final int ESCAPE_STEEP_TICKS = 10;
    private static final int FAKE_FLY_DELAY_TICKS = 2;
    private boolean freeLookInitialized = false;
    private float freeYaw;
    private float freePitch;

    public Entity getTarget() {
        return this.target;
    }

    public boolean isFreeLookActive() {
        return this.isToggled() && this.target != null;
    }

    public float getFreeYaw() {
        return this.freeYaw;
    }

    public float getFreePitch() {
        return this.freePitch;
    }

    public void onMouseTurn(double cursorDeltaYaw, double cursorDeltaPitch) {
        if (!this.freeLookInitialized && AutoMaceModule.mc.player != null) {
            this.freeYaw = AutoMaceModule.mc.player.getYRot();
            this.freePitch = AutoMaceModule.mc.player.getXRot();
            this.freeLookInitialized = true;
        }
        this.freeYaw += (float)(cursorDeltaYaw * 0.15);
        this.freePitch = Mth.clamp((float)(this.freePitch + (float)(cursorDeltaPitch * 0.15)), (float)-90.0f, (float)90.0f);
    }

    @Override
    public void onEnable() {
        this.target = null;
        this.lastTarget = null;
        this.ascending = true;
        this.elytraUnequipped = false;
        this.elytraParkedSlot = -1;
        this.freefallTicks = 0;
        this.freefallLastDistance = 0.0;
        this.freefallLastTargetDist = -1.0;
        this.originalSlot = -1;
        this.freeLookInitialized = false;
        this.launchKicked = false;
        this.armorParkedSlot = -1;
        this.escapeStuckTicks = 0;
        this.escapeSteepTicks = 0;
        this.terrainLift = 0.0;
        this.fakeFlyTicks = 0;
        if (AutoMaceModule.mc.player != null && InventoryUtils.find(Items.MACE) == -1) {
            this.setToggled(false);
        }
    }

    @Override
    public void onDisable() {
        if (AutoMaceModule.mc.player == null) {
            return;
        }
        this.reequipElytra();
        this.restoreArmorIfParked();
        if (this.originalSlot != -1) {
            InventoryUtils.switchSlot("Normal", this.originalSlot, AutoMaceModule.mc.player.getInventory().getSelectedSlot());
            this.originalSlot = -1;
        }
        this.target = null;
        this.lastTarget = null;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        ElytraFlyModule elytraFly;
        if (AutoMaceModule.mc.player == null || AutoMaceModule.mc.level == null || AutoMaceModule.mc.player.isDeadOrDying()) {
            return;
        }
        if (this.target == null || !this.target.isAlive()) {
            this.target = this.findTarget();
        }
        if (this.target == null) {
            this.reequipElytra();
            this.restoreArmorIfParked();
            this.ascending = true;
            this.freeLookInitialized = false;
            return;
        }
        if (!this.freeLookInitialized) {
            this.freeYaw = AutoMaceModule.mc.player.getYRot();
            this.freePitch = AutoMaceModule.mc.player.getXRot();
            this.freeLookInitialized = true;
        }
        if ((elytraFly = Night.MODULE_MANAGER.getModule(ElytraFlyModule.class)) == null) {
            return;
        }
        if (this.ascending) {
            this.tickAscend(elytraFly);
        } else {
            this.tickDive(elytraFly);
        }
    }

    private void tickAscend(ElytraFlyModule elytraFly) {
        boolean forward;
        float[] steer;
        this.reequipElytra();
        double heightAboveTarget = AutoMaceModule.mc.player.getY() - this.target.getY();
        if (heightAboveTarget >= this.minHeight.getValue().doubleValue()) {
            double ascendHorizDist = Math.hypot(AutoMaceModule.mc.player.getX() - this.target.getX(), AutoMaceModule.mc.player.getZ() - this.target.getZ());
            Vec3 ascendVel = AutoMaceModule.mc.player.getDeltaMovement();
            Night.LOGGER.info("[MaceDbg] ASCEND->DIVE height={} horizDist={} vel=({},{},{})", new Object[]{heightAboveTarget, ascendHorizDist, ascendVel.x, ascendVel.y, ascendVel.z});
            this.ascending = false;
            this.freefallTicks = 0;
            this.escapeStuckTicks = 0;
            return;
        }
        if (!this.ensureFlying(elytraFly)) {
            return;
        }
        Vec3 targetCenter = this.target.getBoundingBox().getCenter();
        float yawToTarget = (float)RotationUtils.getYRotToVec((Entity)AutoMaceModule.mc.player, targetCenter);
        float yaw = yawToTarget + 180.0f;
        float pitch = -this.angle.getValue().floatValue();
        if (this.escapeSteepTicks > 0) {
            --this.escapeSteepTicks;
            pitch = -85.0f;
        }
        boolean smart = this.smartTerrain.getValue();
        boolean stalled = AutoMaceModule.mc.player.getDeltaMovement().horizontalDistanceSqr() < 0.0025;
        float[] fArray = steer = smart && !stalled ? this.steerClear(yaw, pitch) : null;
        if (steer != null) {
            this.escapeStuckTicks = 0;
            this.snapRotation(steer[0], steer[1]);
            elytraFly.tryUseGrimFirework(steer[0], steer[1]);
            return;
        }
        boolean ceiling = smart && this.ceilingBlocked();
        boolean bl = forward = smart && (this.predictsCollision() || stalled);
        this.escapeStuckTicks = ceiling || forward ? ++this.escapeStuckTicks : 0;
        if (ceiling || this.escapeStuckTicks > 20) {
            pitch = -15.0f;
            yaw += 90.0f * (float)(this.escapeStuckTicks / 20 % 4);
        } else if (forward) {
            pitch = -85.0f;
        }
        this.snapRotation(yaw, pitch);
        elytraFly.tryUseGrimFirework(yaw, pitch);
    }

    private void tickDive(ElytraFlyModule elytraFly) {
        Vec3 targetCenter = this.target.getBoundingBox().getCenter();
        Vec3 aimPoint = targetCenter.add(this.target.getDeltaMovement().scale(4.0));
        float yawAim = (float)RotationUtils.getYRotToVec((Entity)AutoMaceModule.mc.player, aimPoint);
        float pitchAim = (float)RotationUtils.getXRotToVec((Entity)AutoMaceModule.mc.player, aimPoint);
        if (!this.elytraUnequipped) {
            double heightAboveTarget = AutoMaceModule.mc.player.getY() - this.target.getY();
            double cutHeight = this.attackRange.getValue().doubleValue() + 3.0;
            double horizDist = Math.sqrt(Math.pow(AutoMaceModule.mc.player.getX() - targetCenter.x, 2.0) + Math.pow(AutoMaceModule.mc.player.getZ() - targetCenter.z, 2.0));
            double cutHorizDist = this.attackRange.getValue().doubleValue() * 1.5;
            Vec3 diveVelDbg = AutoMaceModule.mc.player.getDeltaMovement();
            Night.LOGGER.info("[MaceDbg] DIVE-HOMING height={} horizDist={} cutHeight={} cutHorizDist={} vel=({},{},{})", new Object[]{heightAboveTarget, horizDist, cutHeight, cutHorizDist, diveVelDbg.x, diveVelDbg.y, diveVelDbg.z});
            if (heightAboveTarget < 1.0 && horizDist > cutHorizDist) {
                Night.LOGGER.info("[MaceDbg] DIVE ABORT-TO-ASCEND (ran out of height, still far) height={} horizDist={}", (Object)heightAboveTarget, (Object)horizDist);
                this.ascending = true;
                return;
            }
            if (heightAboveTarget > cutHeight || horizDist > cutHorizDist) {
                boolean diveStuck;
                float[] steer;
                if (!this.ensureFlying(elytraFly)) {
                    return;
                }
                float pitch = pitchAim;
                boolean diveStalled = AutoMaceModule.mc.player.getDeltaMovement().horizontalDistanceSqr() < 0.0025;
                float[] fArray = steer = this.smartTerrain.getValue() && !diveStalled ? this.steerClear(yawAim, pitchAim) : null;
                if (steer != null) {
                    this.escapeStuckTicks = 0;
                    this.snapRotation(steer[0], steer[1]);
                    elytraFly.tryUseGrimFirework(steer[0], steer[1]);
                    return;
                }
                boolean bl = diveStuck = this.smartTerrain.getValue() && (this.predictsCollision() || diveStalled);
                if (diveStuck) {
                    ++this.escapeStuckTicks;
                    if (this.escapeStuckTicks > 20) {
                        this.ascending = true;
                        return;
                    }
                    pitch = pitchAim < 0.0f ? -85.0f : 85.0f;
                } else {
                    this.escapeStuckTicks = 0;
                }
                this.snapRotation(yawAim, pitch);
                elytraFly.tryUseGrimFirework(yawAim, pitch);
                return;
            }
            Vec3 cutVelDbg = AutoMaceModule.mc.player.getDeltaMovement();
            Night.LOGGER.info("[MaceDbg] CUT-ELYTRA (commit to freefall) height={} horizDist={} vel=({},{},{})", new Object[]{heightAboveTarget, horizDist, cutVelDbg.x, cutVelDbg.y, cutVelDbg.z});
            this.unequipElytra();
            this.freefallTicks = 0;
            this.freefallLastDistance = AutoMaceModule.mc.player.fallDistance;
            this.freefallLastTargetDist = -1.0;
            return;
        }
        Vec3 eye = AutoMaceModule.mc.player.getEyePosition(1.0f);
        Vec3 hitPoint = RotationUtils.getClampClosestPoint(eye, this.target.getBoundingBox());
        Night.ROTATION_MANAGER.silentRotate(RotationUtils.getYRotToVec(eye, hitPoint), RotationUtils.getXRotToVec(eye, hitPoint));
        ++this.freefallTicks;
        double dist = eye.distanceTo(hitPoint);
        Night.LOGGER.info("[MaceDbg] FREEFALL tick={} dist={} fallDistance={} y={} targetY={} onGround={}", new Object[]{this.freefallTicks, dist, AutoMaceModule.mc.player.fallDistance, AutoMaceModule.mc.player.getY(), this.target.getY(), AutoMaceModule.mc.player.onGround()});
        if (this.freefallTicks >= 3 && dist <= this.attackRange.getValue().doubleValue() && AutoMaceModule.mc.player.fallDistance >= 1.5) {
            Night.LOGGER.info("[MaceDbg] ATTACK dist={} attackRange={} fallDistance={} onGround={}", new Object[]{dist, this.attackRange.getValue().doubleValue(), AutoMaceModule.mc.player.fallDistance, AutoMaceModule.mc.player.onGround()});
            this.attack(this.target);
            elytraFly.resetFireworkCooldown();
            this.escapeSteepTicks = 10;
            this.reequipElytra();
            elytraFly.sendGlideStart();
            this.ascending = true;
            this.launchKicked = false;
            return;
        }
        if (AutoMaceModule.mc.player.onGround()) {
            Night.LOGGER.info("[MaceDbg] MISS-ONGROUND dist={} attackRange={} freefallTicks={}", new Object[]{dist, this.attackRange.getValue().doubleValue(), this.freefallTicks});
            elytraFly.resetFireworkCooldown();
            this.reequipElytra();
            elytraFly.sendGlideStart();
            this.ascending = true;
            return;
        }
        if (AutoMaceModule.mc.player.fallDistance + 0.01 < this.freefallLastDistance) {
            this.reequipElytra();
            this.ascending = true;
            return;
        }
        this.freefallLastDistance = AutoMaceModule.mc.player.fallDistance;
        if (this.freefallLastTargetDist >= 0.0 && dist > this.freefallLastTargetDist + 0.05) {
            Night.LOGGER.info("[MaceDbg] MISS-OVERSHOOT-DETECTOR dist={} prevDist={}", (Object)dist, (Object)this.freefallLastTargetDist);
            this.reequipElytra();
            this.freefallLastTargetDist = -1.0;
            return;
        }
        this.freefallLastTargetDist = dist;
    }

    private void snapRotation(float yaw, float pitch) {
        AutoMaceModule.mc.player.setYRot(yaw);
        AutoMaceModule.mc.player.setXRot(pitch);
        AutoMaceModule.mc.player.setYBodyRot(yaw);
        AutoMaceModule.mc.player.setYHeadRot(yaw);
    }

    private boolean predictsCollision() {
        if (AutoMaceModule.mc.level == null) {
            return false;
        }
        Vec3 vel = AutoMaceModule.mc.player.getDeltaMovement();
        if (vel.lengthSqr() < 1.0E-4) {
            return false;
        }
        Vec3 from = AutoMaceModule.mc.player.position().add(0.0, (double)AutoMaceModule.mc.player.getBbHeight() * 0.5, 0.0);
        double lookAhead = Math.max(3.0, vel.length() * 6.0);
        Vec3 to = from.add(vel.normalize().scale(lookAhead));
        BlockHitResult hit = AutoMaceModule.mc.level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)AutoMaceModule.mc.player));
        return hit.getType() == HitResult.Type.BLOCK;
    }

    private boolean ceilingBlocked() {
        Vec3 to;
        if (AutoMaceModule.mc.level == null) {
            return false;
        }
        Vec3 from = AutoMaceModule.mc.player.position().add(0.0, (double)AutoMaceModule.mc.player.getBbHeight(), 0.0);
        BlockHitResult hit = AutoMaceModule.mc.level.clip(new ClipContext(from, to = from.add(0.0, 5.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)AutoMaceModule.mc.player));
        return hit.getType() == HitResult.Type.BLOCK;
    }

    private double rayClearDistance(float yaw, float pitch) {
        return this.rayClearDistance(yaw, pitch, 8.0);
    }

    private double rayClearDistance(float yaw, float pitch, double lookAhead) {
        if (AutoMaceModule.mc.level == null) {
            return 0.0;
        }
        double y = Math.toRadians(yaw);
        double p = Math.toRadians(pitch);
        double cp = Math.cos(p);
        Vec3 dir = new Vec3(-Math.sin(y) * cp, -Math.sin(p), Math.cos(y) * cp);
        Vec3 from = AutoMaceModule.mc.player.position().add(0.0, (double)AutoMaceModule.mc.player.getBbHeight() * 0.5, 0.0);
        BlockHitResult hit = AutoMaceModule.mc.level.clip(new ClipContext(from, from.add(dir.scale(lookAhead)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)AutoMaceModule.mc.player));
        return hit.getType() == HitResult.Type.BLOCK ? hit.getLocation().distanceTo(from) : lookAhead;
    }

    private float[] steerClear(float yaw, float pitch) {
        if (AutoMaceModule.mc.level == null) {
            return null;
        }
        int steps = this.terrainLookAhead.getValue().intValue();
        double stepDist = 8.0 / (double)steps;
        boolean blocked = false;
        for (int n = 1; n <= steps; ++n) {
            float liftedPitch = pitch - (float)(this.terrainLift * (double)n / (double)steps);
            double dist = stepDist * (double)n;
            if (!(this.rayClearDistance(yaw, liftedPitch, dist) < dist - 0.001)) continue;
            blocked = true;
            break;
        }
        double climbRate = this.terrainClimbRate.getValue().doubleValue();
        double maxClimb = this.terrainMaxClimb.getValue().doubleValue();
        this.terrainLift = blocked ? Math.min(this.terrainLift + climbRate, maxClimb) : Math.max(0.0, this.terrainLift - climbRate * 0.5);
        if (!blocked || this.terrainLift < maxClimb) {
            return new float[]{yaw, pitch - (float)this.terrainLift};
        }
        return null;
    }

    private boolean ensureFlying(ElytraFlyModule elytraFly) {
        if (AutoMaceModule.mc.player.onGround()) {
            Vec3 v = AutoMaceModule.mc.player.getDeltaMovement();
            if (v.y < 0.42) {
                AutoMaceModule.mc.player.setDeltaMovement(v.x, 0.45, v.z);
            }
            this.launchKicked = true;
            return false;
        }
        if (this.launchKicked) {
            this.launchKicked = false;
            this.ensureElytraWorn();
            this.fakeFlyTicks = 0;
            elytraFly.sendGlideStart();
            return false;
        }
        this.fakeFlyCycle(elytraFly);
        if (AutoMaceModule.mc.player.isFallFlying()) {
            return true;
        }
        this.ensureElytraWorn();
        elytraFly.sendGlideStart();
        return false;
    }

    private void fakeFlyCycle(ElytraFlyModule elytraFly) {
        if (!this.fakeFly.getValue() || this.elytraUnequipped || AutoMaceModule.mc.player.onGround()) {
            return;
        }
        if (this.fakeFlyTicks++ < 2) {
            return;
        }
        this.fakeFlyTicks = 0;
        elytraFly.sendGlideStart();
    }

    public boolean shouldPinFallFlying() {
        return this.isToggled() && this.fakeFly.getValue() && this.target != null && !this.elytraUnequipped && AutoMaceModule.mc.player != null && !AutoMaceModule.mc.player.onGround();
    }

    private void ensureElytraWorn() {
        int scratch;
        if (this.fakeFly.getValue()) {
            return;
        }
        if (AutoMaceModule.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
            return;
        }
        int elytraSlot = InventoryUtils.find(Items.ELYTRA);
        if (elytraSlot == -1) {
            return;
        }
        if (this.armorParkedSlot == -1 && !AutoMaceModule.mc.player.getItemBySlot(EquipmentSlot.CHEST).isEmpty() && (scratch = this.findEmptySlot()) != -1) {
            InventoryUtils.swapEquipment(scratch, 6);
            this.armorParkedSlot = scratch;
        }
        InventoryUtils.swapEquipment(elytraSlot, 6);
    }

    private void restoreArmorIfParked() {
        if (this.armorParkedSlot == -1) {
            return;
        }
        InventoryUtils.swapEquipment(this.armorParkedSlot, 6);
        this.armorParkedSlot = -1;
    }

    private void unequipElytra() {
        if (this.elytraUnequipped) {
            return;
        }
        if (this.fakeFly.getValue()) {
            this.elytraUnequipped = true;
            return;
        }
        if (AutoMaceModule.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
            return;
        }
        int emptySlot = this.findEmptySlot();
        if (emptySlot == -1) {
            return;
        }
        InventoryUtils.swapEquipment(emptySlot, 6);
        this.elytraParkedSlot = emptySlot;
        this.elytraUnequipped = true;
    }

    private void reequipElytra() {
        if (!this.elytraUnequipped) {
            return;
        }
        if (this.elytraParkedSlot != -1) {
            InventoryUtils.swapEquipment(this.elytraParkedSlot, 6);
        }
        this.elytraUnequipped = false;
        this.elytraParkedSlot = -1;
    }

    private int findEmptySlot() {
        for (int i = 0; i <= 35; ++i) {
            if (!AutoMaceModule.mc.player.getInventory().getItem(i).isEmpty()) continue;
            return i;
        }
        return -1;
    }

    private void attack(Entity target) {
        int prevSlot = AutoMaceModule.mc.player.getInventory().getSelectedSlot();
        int maceSlot = InventoryUtils.find(Items.MACE);
        boolean switchedSilent = false;
        if (maceSlot != -1 && maceSlot != prevSlot) {
            if (this.swap.getValue().equalsIgnoreCase("Normal")) {
                if (this.originalSlot == -1) {
                    this.originalSlot = prevSlot;
                }
                InventoryUtils.switchSlot("Normal", maceSlot, prevSlot);
            } else if (this.swap.getValue().equalsIgnoreCase("Silent")) {
                InventoryUtils.switchSlot("Silent", maceSlot, prevSlot);
                switchedSilent = true;
            }
        }
        mc.getConnection().send((Packet)new ServerboundAttackPacket(target.getId()));
        AutoMaceModule.mc.player.resetAttackStrengthTicker();
        if (this.swing.getValue()) {
            AutoMaceModule.mc.player.swing(InteractionHand.MAIN_HAND);
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
        if (switchedSilent) {
            InventoryUtils.switchBack("Silent", maceSlot, prevSlot);
        }
    }

    private Entity findTarget() {
        if (AutoMaceModule.mc.player == null || AutoMaceModule.mc.level == null) {
            return null;
        }
        ArrayList<Entity> candidates = new ArrayList<Entity>();
        Vec3 eyePos = AutoMaceModule.mc.player.getEyePosition(1.0f);
        double maxRange = this.range.getValue().doubleValue();
        for (Entity e2 : AutoMaceModule.mc.level.entitiesForRendering()) {
            double dist;
            if (e2 == AutoMaceModule.mc.player || !e2.isAlive() || EntityUtils.isGhost(e2) || (dist = eyePos.distanceTo(RotationUtils.getClampClosestPoint(eyePos, e2.getBoundingBox()))) > maxRange) continue;
            if (e2 instanceof Player) {
                Player p = (Player)e2;
                if (!this.players.getValue() || !this.friends.getValue() && Night.FRIEND_MANAGER.contains(p.getName().getString())) continue;
                candidates.add(e2);
                continue;
            }
            if (EntityUtils.isNeutral(e2)) {
                if (!this.neutrals.getValue() && !EntityUtils.isAngry(e2)) continue;
                candidates.add(e2);
                continue;
            }
            if (this.hostiles.getValue() && EntityUtils.isHostile(e2)) {
                candidates.add(e2);
                continue;
            }
            if (!this.animals.getValue() || !EntityUtils.isAnimal(e2)) continue;
            candidates.add(e2);
        }
        return candidates.stream().min(Comparator.comparingDouble(e -> eyePos.distanceTo(RotationUtils.getClampClosestPoint(eyePos, e.getBoundingBox())))).orElse(null);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (!this.render.getValue() || AutoMaceModule.mc.player == null || AutoMaceModule.mc.player.isDeadOrDying()) {
            return;
        }
        Entity renderEntity = this.target;
        float alphaMult = 1.0f;
        if (renderEntity != null) {
            this.lastTarget = renderEntity;
            this.lastTargetTime = System.currentTimeMillis();
        } else if (this.lastTarget != null) {
            long elapsed = System.currentTimeMillis() - this.lastTargetTime;
            if (elapsed < 300L) {
                renderEntity = this.lastTarget;
                alphaMult = 1.0f - (float)elapsed / 300.0f;
            } else {
                this.lastTarget = null;
            }
        }
        if (renderEntity == null) {
            return;
        }
        Color renderColor = ColorUtils.getColor(this.boxColor.getColor(), (int)((float)this.boxColor.getColor().getAlpha() * alphaMult));
        if (this.renderMode.getValue().equalsIgnoreCase("Box") || this.renderMode.getValue().equalsIgnoreCase("Both")) {
            Vec3 vec3d = EntityUtils.getRenderPos(renderEntity, event.getTickDelta());
            AABB box = renderEntity.getBoundingBox().move(vec3d.x - renderEntity.getX(), vec3d.y - renderEntity.getY(), vec3d.z - renderEntity.getZ());
            Renderer3D.renderBox(event.getMatrices(), box, renderColor);
            Renderer3D.renderBoxOutline(event.getMatrices(), box, renderColor);
        }
        if (this.renderMode.getValue().equalsIgnoreCase("Circle") || this.renderMode.getValue().equalsIgnoreCase("Both")) {
            Renderer3D.renderTargetCircle(event.getMatrices(), renderEntity, event.getTickDelta(), renderColor);
        }
    }
}

