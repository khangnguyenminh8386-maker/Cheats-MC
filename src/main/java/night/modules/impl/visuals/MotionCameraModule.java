/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl
 */
package night.modules.impl.visuals;

import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.EntitySpawnEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.TickEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.api.IMotionCameraModule;
import night.modules.impl.visuals.ViewClipModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.NumberSetting;
import night.utils.animations.AnimateUtil;
import night.utils.system.MathUtils;
import night.utils.system.Timer;

@RegisterModule(name="MotionCamera", description="Smooths camera movement and adds smooth perspective transitions.", category=Module.Category.VISUALS)
public class MotionCameraModule
extends Module
implements IMotionCameraModule {
    public BooleanSetting noFirstPerson = new BooleanSetting("NoFirstPerson", "Disables motion camera in first person.", true);
    public BooleanSetting pearlException = new BooleanSetting("PearlException", "Still smooths in 1st person for a moment after throwing an ender pearl (ignores NoFirstPerson).", new BooleanSetting.Visibility(this.noFirstPerson, true), true);
    public BooleanSetting rubberbandException = new BooleanSetting("RubberbandException", "Still smooths in 1st person for a moment after a rubberband/teleport correction (ignores NoFirstPerson).", new BooleanSetting.Visibility(this.noFirstPerson, true), true);
    public NumberSetting exceptionDuration = new NumberSetting("ExceptionDuration", "How long the pearl/rubberband exception window lasts, in ms.", new BooleanSetting.Visibility(this.noFirstPerson, true), (Number)500, (Number)100, (Number)2000);
    public NumberSetting firstPersonSpeed = new NumberSetting("FirstPersonSpeed", "Movement smoothness speed in first person.", 0.6, 0.01, 1.0);
    public NumberSetting speed = new NumberSetting("Speed", "Movement smoothness speed in third person.", 0.3, 0.01, 1.0);
    public BooleanSetting smoothPerspective = new BooleanSetting("SmoothPerspective", "Smoothly transitions distance when switching between 1st and 3rd person.", true);
    public NumberSetting perspectiveSpeed = new NumberSetting("PerspectiveSpeed", "Speed of perspective switch transition.", 0.5, 0.05, 1.0);
    private double fakeX;
    private double fakeY;
    private double fakeZ;
    private double prevFakeX;
    private double prevFakeY;
    private double prevFakeZ;
    private double distance = 0.0;
    private double prevDistance = 0.0;
    private final Timer pearlTimer = new Timer();
    private final Timer rubberbandTimer = new Timer();

    @SubscribeEvent
    public void onEntitySpawn(EntitySpawnEvent event) {
        Player owner;
        ThrownEnderpearl pearl;
        if (!this.pearlException.getValue() || MotionCameraModule.mc.player == null) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof ThrownEnderpearl && (entity = (pearl = (ThrownEnderpearl)entity).getOwner()) instanceof Player && (owner = (Player)entity) == MotionCameraModule.mc.player) {
            this.pearlTimer.reset();
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!this.rubberbandException.getValue()) {
            return;
        }
        if (event.getPacket() instanceof ClientboundPlayerPositionPacket) {
            this.rubberbandTimer.reset();
        }
    }

    private boolean inExceptionWindow() {
        long window = this.exceptionDuration.getValue().longValue();
        return this.pearlException.getValue() && !this.pearlTimer.hasTimeElapsed(window) || this.rubberbandException.getValue() && !this.rubberbandTimer.hasTimeElapsed(window);
    }

    private boolean isRidingOrFlying() {
        return MotionCameraModule.mc.player != null && (MotionCameraModule.mc.player.isPassenger() || MotionCameraModule.mc.player.isFallFlying());
    }

    @Override
    public boolean isSmoothPerspective() {
        return this.smoothPerspective.getValue();
    }

    @Override
    public boolean on() {
        if (MotionCameraModule.mc.options == null) {
            return false;
        }
        return this.isToggled() && !this.isRidingOrFlying() && (!this.noFirstPerson.getValue() || !MotionCameraModule.mc.options.getCameraType().isFirstPerson() || this.inExceptionWindow());
    }

    @Override
    public boolean shouldBeDetached() {
        if (MotionCameraModule.mc.options == null) {
            return false;
        }
        return this.isToggled() && this.smoothPerspective.getValue() && (this.distance > 0.01 || !MotionCameraModule.mc.options.getCameraType().isFirstPerson());
    }

    @Override
    public void onEnable() {
        if (MotionCameraModule.mc.player != null) {
            double targetDist;
            this.fakeX = MotionCameraModule.mc.player.getX();
            this.fakeY = MotionCameraModule.mc.player.getY() + (double)MotionCameraModule.mc.player.getEyeHeight(MotionCameraModule.mc.player.getPose());
            this.fakeZ = MotionCameraModule.mc.player.getZ();
            this.prevFakeX = this.fakeX;
            this.prevFakeY = this.fakeY;
            this.prevFakeZ = this.fakeZ;
            this.distance = targetDist = MotionCameraModule.mc.options.getCameraType().isFirstPerson() ? 0.0 : this.getTargetPerspectiveDistance();
            this.prevDistance = targetDist;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (MotionCameraModule.mc.player == null || MotionCameraModule.mc.options == null) {
            return;
        }
        this.prevFakeX = this.fakeX;
        this.prevFakeY = this.fakeY;
        this.prevFakeZ = this.fakeZ;
        double targetX = MotionCameraModule.mc.player.getX();
        double targetY = MotionCameraModule.mc.player.getY() + (double)MotionCameraModule.mc.player.getEyeHeight(MotionCameraModule.mc.player.getPose());
        double targetZ = MotionCameraModule.mc.player.getZ();
        if (this.isRidingOrFlying()) {
            this.fakeX = targetX;
            this.fakeY = targetY;
            this.fakeZ = targetZ;
        } else {
            double currentSpeed = MotionCameraModule.mc.options.getCameraType().isFirstPerson() ? this.firstPersonSpeed.getValue().doubleValue() : this.speed.getValue().doubleValue();
            this.fakeX = AnimateUtil.animate(this.fakeX, targetX, currentSpeed);
            this.fakeY = AnimateUtil.animate(this.fakeY, targetY, currentSpeed);
            this.fakeZ = AnimateUtil.animate(this.fakeZ, targetZ, currentSpeed);
        }
        this.prevDistance = this.distance;
        boolean firstPerson = MotionCameraModule.mc.options.getCameraType().isFirstPerson();
        double targetDistance = firstPerson ? 0.0 : this.getTargetPerspectiveDistance();
        this.distance = this.smoothPerspective.getValue() && !firstPerson ? AnimateUtil.animate(this.distance, targetDistance, this.perspectiveSpeed.getValue().doubleValue()) : targetDistance;
        if (firstPerson) {
            this.prevDistance = 0.0;
        }
    }

    private double getTargetPerspectiveDistance() {
        ViewClipModule viewClip = Night.MODULE_MANAGER.getModule(ViewClipModule.class);
        if (viewClip != null && viewClip.isToggled() && viewClip.extend.getValue()) {
            return viewClip.distance.getValue().doubleValue();
        }
        return 4.0;
    }

    @Override
    public double getFakeX() {
        float delta = mc.getDeltaTracker() != null ? mc.getDeltaTracker().getGameTimeDeltaPartialTick(true) : 1.0f;
        return MathUtils.interpolate(this.prevFakeX, this.fakeX, delta);
    }

    @Override
    public double getFakeY() {
        float delta = mc.getDeltaTracker() != null ? mc.getDeltaTracker().getGameTimeDeltaPartialTick(true) : 1.0f;
        return MathUtils.interpolate(this.prevFakeY, this.fakeY, delta);
    }

    @Override
    public double getFakeZ() {
        float delta = mc.getDeltaTracker() != null ? mc.getDeltaTracker().getGameTimeDeltaPartialTick(true) : 1.0f;
        return MathUtils.interpolate(this.prevFakeZ, this.fakeZ, delta);
    }

    @Override
    public double getDistance() {
        float delta = mc.getDeltaTracker() != null ? mc.getDeltaTracker().getGameTimeDeltaPartialTick(true) : 1.0f;
        return MathUtils.interpolate(this.prevDistance, this.distance, delta);
    }
}

