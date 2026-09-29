/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 */
package night.modules.impl.player;

import net.minecraft.util.Mth;
import night.events.SubscribeEvent;
import night.events.impl.PlayerUpdateEvent;
import night.mixins.accessors.EntityAccessor;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;

@RegisterModule(name="RotationLock", description="Locks your rotation to a certain yaw and pitch.", category=Module.Category.PLAYER)
public class RotationLockModule
extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "Whether the pitch or yaw will be locked.", "Both", new String[]{"Yaw", "Pitch", "Both"});
    public BooleanSetting custom = new BooleanSetting("Custom", "Holds your locked angle steady (camera free to turn) and only re-locks, snapping straight to the next 45deg, once you've turned far enough past it -- instead of an always-fixed value.", new ModeSetting.Visibility(this.mode, "Yaw", "Pitch", "Both"), false);
    public NumberSetting pitch = new NumberSetting("Pitch", "Pitch", "The fixed degrees pitch locks to when Custom is off.", new ModeSetting.Visibility(this.mode, "Pitch", "Both"), Float.valueOf(0.0f), Float.valueOf(-90.0f), Float.valueOf(90.0f), 1);
    private boolean lockInitialized = false;
    private float lockedYaw = 0.0f;
    private float lockedPitch = 0.0f;
    private static final long SHAKE_DURATION_MS = 180L;
    private static final float SHAKE_AMPLITUDE_DEG = 3.0f;
    private long yawSnapAtMs = -1L;
    private long pitchSnapAtMs = -1L;

    @Override
    public void onEnable() {
        this.lockInitialized = false;
    }

    private float shakeOffset(long snapAtMs) {
        if (snapAtMs < 0L) {
            return 0.0f;
        }
        long elapsed = System.currentTimeMillis() - snapAtMs;
        if (elapsed >= 180L) {
            return 0.0f;
        }
        float progress = (float)elapsed / 180.0f;
        return (float)(Math.sin((double)progress * Math.PI * 5.0) * 3.0 * (double)(1.0f - progress));
    }

    public float getShakeYawOffset() {
        return this.custom.getValue() ? this.shakeOffset(this.yawSnapAtMs) : 0.0f;
    }

    public float getShakePitchOffset() {
        return this.custom.getValue() ? this.shakeOffset(this.pitchSnapAtMs) : 0.0f;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (RotationLockModule.mc.player == null || !this.custom.getValue()) {
            this.lockInitialized = false;
            return;
        }
        float realYaw = ((EntityAccessor)RotationLockModule.mc.player).getRawYRot();
        float realPitch = ((EntityAccessor)RotationLockModule.mc.player).getRawXRot();
        if (!this.lockInitialized) {
            this.lockedYaw = RotationLockModule.nearest45(realYaw);
            this.lockedPitch = RotationLockModule.nearest45(realPitch);
            this.lockInitialized = true;
            return;
        }
        if (Math.abs(Mth.wrapDegrees((float)(realYaw - this.lockedYaw))) >= 45.0f) {
            float newYaw = RotationLockModule.nearest45(realYaw);
            if (newYaw != this.lockedYaw) {
                this.yawSnapAtMs = System.currentTimeMillis();
            }
            this.lockedYaw = newYaw;
        }
        if (Math.abs(Mth.wrapDegrees((float)(realPitch - this.lockedPitch))) >= 45.0f) {
            float newPitch = RotationLockModule.nearest45(realPitch);
            if (newPitch != this.lockedPitch) {
                this.pitchSnapAtMs = System.currentTimeMillis();
            }
            this.lockedPitch = newPitch;
        }
    }

    private static float nearest45(float degrees) {
        return Mth.wrapDegrees((float)((float)Math.round(degrees / 45.0f) * 45.0f));
    }

    public float getYawValue(float realYaw) {
        if (this.custom.getValue()) {
            return this.lockedYaw;
        }
        return RotationLockModule.nearest45(realYaw);
    }

    public float getPitchValue(float realPitch) {
        if (this.custom.getValue()) {
            return this.lockedPitch;
        }
        return this.pitch.getValue().floatValue();
    }

    @Override
    public String getMetaData() {
        String yawText = this.custom.getValue() ? String.valueOf(this.lockedYaw) : "Auto";
        String pitchText = this.custom.getValue() ? String.valueOf(this.lockedPitch) : String.valueOf(this.pitch.getValue().floatValue());
        return yawText + ", " + pitchText;
    }
}

