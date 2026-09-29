/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Vector2d
 */
package night.modules.impl.movement;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PlayerMoveEvent;
import night.events.impl.TickEvent;
import night.managers.BoostManager;
import night.mixins.accessors.Vec3dAccessor;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.api.ISpeedModule;
import night.modules.api.ISprintModule;
import night.modules.impl.miscellaneous.FakePlayerModule;
import night.modules.impl.movement.ElytraFlyModule;
import night.modules.impl.movement.HoleSnapModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.minecraft.EntityUtils;
import night.utils.minecraft.MovementUtils;
import org.joml.Vector2d;

@RegisterModule(name="Speed", description="Makes it so that you move faster than normal.", category=Module.Category.MOVEMENT)
public class SpeedModule
extends Module
implements ISpeedModule {
    public ModeSetting mode = new ModeSetting("Mode", "The method that will be used to increase your speed.", "Strafe", new String[]{"Vanilla", "Strafe", "StrafeStrict", "Grim"});
    public NumberSetting vanillaSpeed = new NumberSetting("VanillaSpeed", "Speed", "The speed that will be applied to your movement.", new ModeSetting.Visibility(this.mode, "Vanilla"), 10.0, 0.0, 20.0);
    public BooleanSetting vanillaOnGround = new BooleanSetting("VanillaOnGround", "OnGround", "Only applies the speed when you are on ground.", new ModeSetting.Visibility(this.mode, "Vanilla"), false);
    public BooleanSetting useTimer = new BooleanSetting("UseTimer", "Adds a timer multiplier when strafing.", new ModeSetting.Visibility(this.mode, "Strafe", "StrafeStrict"), false);
    public BooleanSetting timerBypass = new BooleanSetting("Bypass", "Allows you to use timer on certain servers.", new BooleanSetting.Visibility(this.useTimer, true), true);
    public NumberSetting bypassThreshold = new NumberSetting("Threshold", "The threshold value for the timer bypass.", new BooleanSetting.Visibility(this.timerBypass, true), (Number)25, (Number)15, (Number)30);
    public NumberSetting timerMultiplier = new NumberSetting("TimerMultiplier", "Multiplier", "The timer multiplier that will be applied to the timer.", new BooleanSetting.Visibility(this.useTimer, true), Float.valueOf(1.08f), Float.valueOf(1.0f), Float.valueOf(1.2f));
    public BooleanSetting speedInLiquid = new BooleanSetting("SpeedInLiquid", "Increases your speed while in water or lava.", new ModeSetting.Visibility(this.mode, "Strafe", "StrafeStrict"), false);
    public BooleanSetting boost = new BooleanSetting("Boost", "Uses explosion knockback to boost your speed.", new ModeSetting.Visibility(this.mode, "Strafe", "StrafeStrict"), false);
    public NumberSetting boostMultiplier = new NumberSetting("BoostMultiplier", "Multiplier", "Scales the explosion knockback boost.", new BooleanSetting.Visibility(this.boost, true), 1.0, 0.01, 5.0);
    public NumberSetting boostCap = new NumberSetting("BoostCap", "Cap", "Maximum boost value that can be applied.", new BooleanSetting.Visibility(this.boost, true), 5.0, 0.0, 10.0);
    public BooleanSetting autoJump = new BooleanSetting("AutoJump", "Automatically jumps for you when on ground.", new ModeSetting.Visibility(this.mode, "Grim"), false);
    private double distance;
    private double speed;
    private double forward;
    private int stage;
    private int ticks;
    private boolean pressed = false;

    @Override
    public void onEnable() {
        this.stage = 1;
        this.ticks = 0;
        this.pressed = false;
    }

    @Override
    public void onDisable() {
        Night.WORLD_MANAGER.setTimerMultiplier(1.0f);
        if (this.pressed) {
            SpeedModule.mc.options.keyJump.setDown(false);
            this.pressed = false;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (SpeedModule.mc.player == null || SpeedModule.mc.level == null) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("Strafe") || this.mode.getValue().equalsIgnoreCase("StrafeStrict")) {
            this.distance = Math.sqrt(Mth.square((double)(SpeedModule.mc.player.getX() - SpeedModule.mc.player.xo)) + Mth.square((double)(SpeedModule.mc.player.getZ() - SpeedModule.mc.player.zo)));
            Night.WORLD_MANAGER.setTimerMultiplier(this.isDrivingTimer() ? this.timerMultiplier.getValue().floatValue() : 1.0f);
        }
        if (this.mode.getValue().equalsIgnoreCase("Grim")) {
            boolean isCrawling = SpeedModule.mc.player.isVisuallyCrawling() || SpeedModule.mc.player.getPose() == Pose.SWIMMING || SpeedModule.mc.player.getEyeHeight() < 1.0f || this.hasCeiling(SpeedModule.mc.player.getX(), SpeedModule.mc.player.getY(), SpeedModule.mc.player.getZ());
            boolean canJump = true;
            if (isCrawling) {
                boolean currentCeiling = this.hasCeiling(SpeedModule.mc.player.getX(), SpeedModule.mc.player.getY(), SpeedModule.mc.player.getZ());
                boolean aheadCeiling = this.hasCeilingAhead();
                if (!currentCeiling || !aheadCeiling) {
                    canJump = false;
                }
            }
            if (!(canJump && this.autoJump.getValue() && MovementUtils.isMoving() || !this.pressed)) {
                SpeedModule.mc.options.keyJump.setDown(false);
                this.pressed = false;
            }
            if (this.autoJump.getValue() && MovementUtils.isMoving() && SpeedModule.mc.player.onGround() && canJump) {
                boolean grimAlreadyCorrect;
                SpeedModule.mc.options.keyJump.setDown(true);
                this.pressed = true;
                ISprintModule sprint = (ISprintModule)((Object)Night.MODULE_MANAGER.getModule("Sprint"));
                boolean bl = grimAlreadyCorrect = sprint != null && sprint.isGrimCompensating();
                if (SpeedModule.mc.player.isSprinting() && !grimAlreadyCorrect) {
                    float yRotRad = SpeedModule.mc.player.getYRot() * ((float)Math.PI / 180);
                    double vanillaX = -Math.sin(yRotRad) * 0.2;
                    double vanillaZ = Math.cos(yRotRad) * 0.2;
                    Vector2d boost = MovementUtils.forward(0.2);
                    SpeedModule.mc.player.setDeltaMovement(SpeedModule.mc.player.getDeltaMovement().x - vanillaX + boost.x, SpeedModule.mc.player.getDeltaMovement().y, SpeedModule.mc.player.getDeltaMovement().z - vanillaZ + boost.y);
                }
            } else if (this.pressed) {
                SpeedModule.mc.options.keyJump.setDown(false);
                this.pressed = false;
            }
            int collisions = 0;
            for (Entity entity : SpeedModule.mc.level.entitiesForRendering()) {
                if (entity == null || entity == SpeedModule.mc.player || !(entity instanceof LivingEntity) || EntityUtils.isGhost(entity) || Night.MODULE_MANAGER.getModule(FakePlayerModule.class).isToggled() && Night.MODULE_MANAGER.getModule(FakePlayerModule.class).getPlayer() == entity || entity instanceof ArmorStand || !((double)Mth.sqrt((float)((float)SpeedModule.mc.player.distanceToSqr(entity))) <= 1.5)) continue;
                ++collisions;
            }
            if (collisions > 0) {
                Vector2d vector2d = MovementUtils.forward(0.08 * (double)collisions);
                SpeedModule.mc.player.setDeltaMovement(SpeedModule.mc.player.getDeltaMovement().x + vector2d.x, SpeedModule.mc.player.getDeltaMovement().y, SpeedModule.mc.player.getDeltaMovement().z + vector2d.y);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent event) {
        if (this.mode.getValue().equalsIgnoreCase("Strafe") || this.mode.getValue().equalsIgnoreCase("StrafeStrict")) {
            if (Night.MODULE_MANAGER.getModule(HoleSnapModule.class).isToggled() && Night.MODULE_MANAGER.getModule(HoleSnapModule.class).hole != null) {
                return;
            }
            ElytraFlyModule elytra = Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
            if (elytra.isToggled() && elytra.mode.getValue().equalsIgnoreCase("Control") && SpeedModule.mc.player.isFallFlying()) {
                return;
            }
            if (SpeedModule.mc.player.fallDistance >= 5.0 || SpeedModule.mc.player.isShiftKeyDown() || SpeedModule.mc.player.onClimbable() || SpeedModule.mc.level.getBlockState(SpeedModule.mc.player.blockPosition()).getBlock() == Blocks.COBWEB || SpeedModule.mc.player.getAbilities().flying || (SpeedModule.mc.player.isInWater() || SpeedModule.mc.player.isInLava()) && !this.speedInLiquid.getValue()) {
                return;
            }
            this.speed = MovementUtils.getPotionSpeed(MovementUtils.DEFAULT_SPEED) * (SpeedModule.mc.player.input.getMoveVector().y <= 0.0f && this.forward > 0.0 ? 0.66 : 1.0);
            if (this.stage == 1 && MovementUtils.isMoving() && SpeedModule.mc.player.verticalCollision) {
                ((Vec3dAccessor)SpeedModule.mc.player.getDeltaMovement()).setY(MovementUtils.getPotionJump(0.3999999463558197));
                event.setMovement(new Vec3(event.getMovement().x, SpeedModule.mc.player.getDeltaMovement().y, event.getMovement().z));
                this.speed *= 2.149;
                if (this.boost.getValue()) {
                    this.speed += this.getAppliedBoost();
                }
                this.stage = 2;
            } else if (this.stage == 2) {
                this.speed = this.distance - 0.66 * (this.distance - MovementUtils.getPotionSpeed(MovementUtils.DEFAULT_SPEED));
                if (this.boost.getValue()) {
                    this.speed += this.getAppliedBoost();
                }
                this.stage = 3;
            } else {
                if (!SpeedModule.mc.level.getEntityCollisions((Entity)SpeedModule.mc.player, SpeedModule.mc.player.getBoundingBox().move(0.0, SpeedModule.mc.player.getDeltaMovement().y, 0.0)).isEmpty() || SpeedModule.mc.player.verticalCollision) {
                    this.stage = 1;
                }
                this.speed = this.distance - this.distance / 159.0;
            }
            this.speed = Math.max(this.speed, MovementUtils.getPotionSpeed(MovementUtils.DEFAULT_SPEED));
            boolean pureForward = SpeedModule.mc.player.input.keyPresses.forward() && !SpeedModule.mc.player.input.keyPresses.backward() && !SpeedModule.mc.player.input.keyPresses.left() && !SpeedModule.mc.player.input.keyPresses.right();
            double ncp = MovementUtils.getPotionSpeed(this.mode.getValue().equalsIgnoreCase("StrafeStrict") || !pureForward ? 0.465 : 0.576);
            double bypass = MovementUtils.getPotionSpeed(this.mode.getValue().equalsIgnoreCase("StrafeStrict") || !pureForward ? 0.44 : 0.57);
            this.speed = Math.min(this.speed, this.ticks > 25 ? ncp : bypass);
            if (this.ticks++ > 50) {
                this.ticks = 0;
            }
            Vector2d velocity = MovementUtils.forward(this.speed);
            event.setMovement(new Vec3(velocity.x, event.getMovement().y, event.getMovement().z));
            event.setMovement(new Vec3(event.getMovement().x, event.getMovement().y, velocity.y));
            this.forward = SpeedModule.mc.player.input.getMoveVector().y;
            event.setCancelled(true);
        }
    }

    private double getAppliedBoost() {
        if (BoostManager.INSTANCE == null) {
            return 0.0;
        }
        double raw = BoostManager.INSTANCE.getBoostSpeed(false);
        if (raw == 0.0) {
            return 0.0;
        }
        double scaled = raw * this.boostMultiplier.getValue().doubleValue();
        return Math.min(scaled, this.boostCap.getValue().doubleValue());
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue();
    }

    @Override
    public String getModeValue() {
        return this.mode.getValue();
    }

    @Override
    public float getVanillaSpeedValue() {
        return this.vanillaSpeed.getValue().floatValue();
    }

    public boolean isDrivingTimer() {
        if (SpeedModule.mc.player == null) {
            return false;
        }
        if (!this.mode.getValue().equalsIgnoreCase("Strafe") && !this.mode.getValue().equalsIgnoreCase("StrafeStrict")) {
            return false;
        }
        if (!this.useTimer.getValue()) {
            return false;
        }
        boolean flag = MovementUtils.isMoving() && !SpeedModule.mc.player.isShiftKeyDown() && !SpeedModule.mc.player.isInWater() && SpeedModule.mc.player.fallDistance < 5.0;
        return flag && (this.ticks > this.bypassThreshold.getValue().intValue() || !this.timerBypass.getValue());
    }

    private boolean hasCeiling(double x, double y, double z) {
        if (SpeedModule.mc.level == null) {
            return false;
        }
        BlockPos headBlock1 = BlockPos.containing((double)x, (double)(y + 1.0), (double)z);
        BlockState state1 = SpeedModule.mc.level.getBlockState(headBlock1);
        if (!state1.isAir() && (state1.isSolid() || state1.blocksMotion() || !state1.canBeReplaced())) {
            return true;
        }
        BlockPos headBlock2 = BlockPos.containing((double)x, (double)(y + 1.2), (double)z);
        if (!headBlock2.equals((Object)headBlock1)) {
            BlockState state2 = SpeedModule.mc.level.getBlockState(headBlock2);
            return !state2.isAir() && (state2.isSolid() || state2.blocksMotion() || !state2.canBeReplaced());
        }
        return false;
    }

    private boolean hasCeilingAhead() {
        if (SpeedModule.mc.player == null || SpeedModule.mc.level == null) {
            return false;
        }
        double vx = SpeedModule.mc.player.getDeltaMovement().x;
        double vz = SpeedModule.mc.player.getDeltaMovement().z;
        double speed = Math.hypot(vx, vz);
        if (speed > 0.01) {
            double step = Math.min(speed, 0.35);
            double checkX = SpeedModule.mc.player.getX() + vx / speed * step;
            double checkZ = SpeedModule.mc.player.getZ() + vz / speed * step;
            if (!this.hasCeiling(checkX, SpeedModule.mc.player.getY(), checkZ)) {
                return false;
            }
        }
        if (MovementUtils.isMoving()) {
            Vector2d fwd = MovementUtils.forward(0.3);
            double checkX = SpeedModule.mc.player.getX() + fwd.x;
            double checkZ = SpeedModule.mc.player.getZ() + fwd.y;
            if (!this.hasCeiling(checkX, SpeedModule.mc.player.getY(), checkZ)) {
                return false;
            }
        }
        return true;
    }
}

