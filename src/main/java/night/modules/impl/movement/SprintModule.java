/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.util.Mth
 *  net.minecraft.world.phys.Vec2
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.movement;

import lombok.Generated;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.ClientRotationEvent;
import night.events.impl.PlayerMoveEvent;
import night.events.impl.PlayerUpdateEvent;
import night.mixins.accessors.ClientPlayerEntityAccessor;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.api.IFreecamModule;
import night.modules.api.ISprintModule;
import night.modules.impl.movement.ElytraFlyModule;
import night.modules.impl.movement.HoleSnapModule;
import night.modules.impl.movement.NoSlowModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.minecraft.MovementUtils;

@RegisterModule(name="Sprint", description="Makes it so that you are always sprinting when possible.", category=Module.Category.MOVEMENT)
public class SprintModule
extends Module
implements ISprintModule {
    public ModeSetting mode = new ModeSetting("Mode", "The limits to when you can be sprinting.", "Rage", new String[]{"Legit", "Rage", "Instant", "Grim"});
    public NumberSetting instantSpeed = new NumberSetting("InstantSpeed", "Per-tick horizontal speed to move at (blocks/tick) when Mode is Instant.", new ModeSetting.Visibility(this.mode, "Instant"), (Number)Float.valueOf((float)MovementUtils.DEFAULT_SPEED), (Number)Float.valueOf(0.05f), (Number)Float.valueOf(0.6f));
    public BooleanSetting instantWater = new BooleanSetting("Water", "Keeps applying the Instant speed override while in water.", new ModeSetting.Visibility(this.mode, "Instant"), true);
    public BooleanSetting instantLava = new BooleanSetting("Lava", "Keeps applying the Instant speed override while in lava.", new ModeSetting.Visibility(this.mode, "Instant"), false);
    public BooleanSetting pauseOnSetback = new BooleanSetting("PauseOnSetback", "Briefly pauses sprinting when receiving a setback from the server to allow GrimAC buffer to decay.", true);
    private float cachedYaw;
    private Vec2 cachedMove;
    private boolean grimQueued;
    private Float pendingYaw;
    private float pendingPitch;
    private int grimStrafe;

    @Override
    public int getGrimStrafe() {
        return this.grimStrafe;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (SprintModule.mc.player == null) {
            return;
        }
        this.cachedYaw = SprintModule.mc.player.getYRot();
        this.cachedMove = SprintModule.mc.player.input.getMoveVector();
        if (this.isGrim()) {
            this.grimUpdate();
        } else {
            this.pendingYaw = null;
        }
        boolean sprint = this.shouldSprint();
        if (sprint) {
            SprintModule.mc.player.setSprinting(true);
        } else if (SprintModule.mc.player.isSprinting()) {
            SprintModule.mc.player.setSprinting(false);
        }
    }

    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent event) {
        boolean instantAllowed;
        if (SprintModule.mc.player == null) {
            return;
        }
        if (!this.mode.getValue().equalsIgnoreCase("Instant")) {
            return;
        }
        ElytraFlyModule elytra = Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
        if (elytra.isToggled() && elytra.mode.getValue().equalsIgnoreCase("Control") && SprintModule.mc.player.isFallFlying()) {
            return;
        }
        HoleSnapModule holeSnap = Night.MODULE_MANAGER.getModule(HoleSnapModule.class);
        if (holeSnap.isToggled() && holeSnap.hole != null) {
            return;
        }
        boolean bl = instantAllowed = !(SprintModule.mc.player.getFoodData().getFoodLevel() <= 6 || !this.instantLava.getValue() && SprintModule.mc.player.isInLava() || !this.instantWater.getValue() && SprintModule.mc.player.isInWater() || this.pauseOnSetback.getValue() && Night.SERVER_MANAGER != null && !Night.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(150L));
        if (!instantAllowed) {
            return;
        }
        Vec2 move = this.cachedMove;
        if (move.x != 0.0f || move.y != 0.0f) {
            double moveLength = Math.sqrt(move.x * move.x + move.y * move.y);
            double normX = (double)move.x / moveLength;
            double normY = (double)move.y / moveLength;
            double yawRad = Math.toRadians(this.cachedYaw);
            double sin = Math.sin(yawRad);
            double cos = Math.cos(yawRad);
            double vx = normX * cos - normY * sin;
            double vz = normX * sin + normY * cos;
            double speed = this.instantSpeed.getValue().doubleValue();
            event.setMovement(new Vec3(vx * speed, event.getMovement().y, vz * speed));
        } else {
            event.setMovement(new Vec3(0.0, event.getMovement().y, 0.0));
        }
        event.setCancelled(true);
    }

    @SubscribeEvent
    public void onClientRotation(ClientRotationEvent event) {
        if (this.pendingYaw == null || event.isCancelled()) {
            return;
        }
        event.setYaw(this.pendingYaw.floatValue());
        event.setPitch(this.pendingPitch);
        event.setOwner(this);
    }

    private boolean isGrim() {
        return this.mode.getValue().equalsIgnoreCase("Grim");
    }

    private void grimUpdate() {
        float targetYaw;
        this.grimQueued = false;
        this.pendingYaw = null;
        if (SprintModule.mc.player.isFallFlying() || SprintModule.mc.player.isPassenger()) {
            return;
        }
        if (this.pauseOnSetback.getValue() && Night.SERVER_MANAGER != null && !Night.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(150L)) {
            return;
        }
        if (((IFreecamModule)((Object)Night.MODULE_MANAGER.getModule("Freecam"))).isToggled()) {
            return;
        }
        int inputX = (SprintModule.mc.options.keyRight.isDown() ? 1 : 0) - (SprintModule.mc.options.keyLeft.isDown() ? 1 : 0);
        int inputZ = (SprintModule.mc.options.keyUp.isDown() ? 1 : 0) - (SprintModule.mc.options.keyDown.isDown() ? 1 : 0);
        if (inputX == 0 && inputZ == 0) {
            return;
        }
        float moveAngle = (float)Math.toDegrees(Math.atan2(inputX, inputZ));
        int strafe = 0;
        if (inputX != 0 && inputZ != 0) {
            strafe = inputX < 0 ? 1 : -1;
            targetYaw = Mth.wrapDegrees((float)(SprintModule.mc.player.getYRot() + moveAngle + (strafe > 0 ? 45.0f : -45.0f)));
        } else {
            targetYaw = Mth.wrapDegrees((float)(SprintModule.mc.player.getYRot() + moveAngle));
        }
        this.pendingYaw = Float.valueOf(targetYaw);
        this.pendingPitch = SprintModule.mc.player.getXRot();
        this.grimStrafe = strafe;
        this.grimQueued = true;
    }

    @Override
    public boolean isGrimCompensating() {
        if (!(SprintModule.mc.player != null && this.isToggled() && this.isGrim() && this.grimQueued)) {
            return false;
        }
        if (this.pauseOnSetback.getValue() && Night.SERVER_MANAGER != null && !Night.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(150L)) {
            return false;
        }
        return Night.ROTATION_MANAGER.getRotation() != null && Night.ROTATION_MANAGER.getRotationOwner() == this;
    }

    @Override
    public float getGrimYaw() {
        return Night.ROTATION_MANAGER.getRotation().getYaw();
    }

    @Override
    public boolean isInstantMode() {
        return this.mode.getValue().equalsIgnoreCase("Instant");
    }

    @Override
    public void onEnable() {
        if (SprintModule.mc.player == null) {
            return;
        }
        SprintModule.mc.player.setSprinting(this.shouldSprint());
    }

    @Override
    public void onDisable() {
        this.grimQueued = false;
        if (SprintModule.mc.player == null) {
            return;
        }
        SprintModule.mc.player.setSprinting(false);
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue();
    }

    @Override
    public boolean shouldSprint() {
        if (this.pauseOnSetback.getValue() && Night.SERVER_MANAGER != null && !Night.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(150L)) {
            return false;
        }
        if (!((ClientPlayerEntityAccessor)SprintModule.mc.player).invokeCanSprint(true)) {
            return false;
        }
        if (SprintModule.mc.player.isInWater() && !SprintModule.mc.player.isUnderWater()) {
            return false;
        }
        if (SprintModule.mc.player.isSwimming() && !SprintModule.mc.player.onGround() && !SprintModule.mc.player.input.keyPresses.shift() && !SprintModule.mc.player.isInWater()) {
            return false;
        }
        if (SprintModule.mc.player.isInLava()) {
            return false;
        }
        if (SprintModule.mc.player.onClimbable()) {
            return false;
        }
        if (SprintModule.mc.player.isFallFlying()) {
            ElytraFlyModule ef;
            ElytraFlyModule elytraFlyModule = ef = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ElytraFlyModule.class) : null;
            if (ef == null || !ef.isToggled() || !ef.mode.getValue().equalsIgnoreCase("Bounce")) {
                return false;
            }
        }
        if (SprintModule.mc.player.getFoodData().getFoodLevel() <= 6) {
            return false;
        }
        if (!(!SprintModule.mc.player.isUsingItem() || Night.MODULE_MANAGER.getModule(NoSlowModule.class).isToggled() && Night.MODULE_MANAGER.getModule(NoSlowModule.class).items.getValue())) {
            return false;
        }
        if (this.mode.getValue().equalsIgnoreCase("Rage") || this.mode.getValue().equalsIgnoreCase("Instant") || this.isGrim()) {
            if (SprintModule.mc.player.horizontalCollision && !SprintModule.mc.player.minorHorizontalCollision) {
                return false;
            }
            if (this.isGrim() && !this.isGrimCompensating()) {
                return SprintModule.mc.player.input.keyPresses.forward();
            }
            return SprintModule.mc.player.input.keyPresses.forward() || SprintModule.mc.player.input.keyPresses.backward() || SprintModule.mc.player.input.keyPresses.left() || SprintModule.mc.player.input.keyPresses.right();
        }
        if (!((ClientPlayerEntityAccessor)SprintModule.mc.player).invokeIsWalking()) {
            return false;
        }
        if (SprintModule.mc.player.horizontalCollision && !SprintModule.mc.player.minorHorizontalCollision) {
            return false;
        }
        return SprintModule.mc.player.input.hasForwardImpulse();
    }

    @Generated
    public Float getPendingYaw() {
        return this.pendingYaw;
    }
}

