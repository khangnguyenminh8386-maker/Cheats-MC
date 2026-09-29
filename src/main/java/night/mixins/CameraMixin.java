/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Camera
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.world.level.material.FogType
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package night.mixins;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.level.material.FogType;
import night.Night;
import night.modules.api.IFOVModifierModule;
import night.modules.api.IFreecamModule;
import night.modules.api.IMotionCameraModule;
import night.modules.api.IViewClipModule;
import night.modules.impl.combat.AutoMaceModule;
import night.modules.impl.movement.ElytraFlyModule;
import night.modules.impl.player.RotationLockModule;
import night.modules.impl.visuals.NoRenderModule;
import night.utils.IMinecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={Camera.class})
public abstract class CameraMixin
implements IMinecraft {
    @Shadow
    private boolean detached;

    @Shadow
    protected abstract void move(float var1, float var2, float var3);

    @Shadow
    protected abstract float getMaxZoom(float var1);

    @ModifyArgs(method={"alignWithEntity"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/Camera;getMaxZoom(F)F"))
    private void update(Args args) {
        IViewClipModule viewClip;
        IMotionCameraModule motionCamera;
        IMotionCameraModule iMotionCameraModule = motionCamera = Night.MODULE_MANAGER != null ? (IMotionCameraModule)((Object)Night.MODULE_MANAGER.getModule("MotionCamera")) : null;
        if (motionCamera != null && motionCamera.isToggled() && motionCamera.isSmoothPerspective()) {
            args.set(0, (Object)Float.valueOf((float)motionCamera.getDistance()));
            return;
        }
        IViewClipModule iViewClipModule = viewClip = Night.MODULE_MANAGER != null ? (IViewClipModule)((Object)Night.MODULE_MANAGER.getModule("ViewClip")) : null;
        if (viewClip != null && viewClip.isToggled() && viewClip.isExtend()) {
            args.set(0, (Object)Float.valueOf(viewClip.getDistanceValue()));
        }
    }

    @Inject(method={"getMaxZoom"}, at={@At(value="HEAD")}, cancellable=true)
    private void clipToSpace(float cameraDist, CallbackInfoReturnable<Float> info) {
        IViewClipModule viewClip;
        IViewClipModule iViewClipModule = viewClip = Night.MODULE_MANAGER != null ? (IViewClipModule)((Object)Night.MODULE_MANAGER.getModule("ViewClip")) : null;
        if (viewClip != null && viewClip.isToggled()) {
            info.setReturnValue(Float.valueOf(cameraDist));
        }
    }

    @Inject(method={"getFluidInCamera"}, at={@At(value="HEAD")}, cancellable=true)
    private void getSubmersionType(CallbackInfoReturnable<FogType> info) {
        if (Night.MODULE_MANAGER != null && Night.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() && Night.MODULE_MANAGER.getModule(NoRenderModule.class).liquidOverlay.getValue()) {
            info.setReturnValue(FogType.NONE);
        }
    }

    @Inject(method={"update"}, at={@At(value="TAIL")})
    private void update$TAIL(DeltaTracker deltaTracker, CallbackInfo info) {
        if (Night.MODULE_MANAGER == null) {
            return;
        }
        if (((IFreecamModule)((Object)Night.MODULE_MANAGER.getModule("Freecam"))).isToggled()) {
            this.detached = true;
            return;
        }
        IMotionCameraModule motionCamera = (IMotionCameraModule)((Object)Night.MODULE_MANAGER.getModule("MotionCamera"));
        if (motionCamera != null && motionCamera.isToggled() && motionCamera.isSmoothPerspective() && motionCamera.shouldBeDetached()) {
            float dist;
            this.detached = true;
            if (CameraMixin.mc.options != null && CameraMixin.mc.options.getCameraType().isFirstPerson() && (dist = (float)motionCamera.getDistance()) > 0.001f) {
                this.move(-this.getMaxZoom(dist), 0.0f, 0.0f);
            }
        }
    }

    @ModifyArgs(method={"alignWithEntity"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/Camera;setRotation(FF)V"))
    private void update$setRotation(Args args) {
        if (Night.MODULE_MANAGER != null) {
            IFreecamModule freecam = (IFreecamModule)((Object)Night.MODULE_MANAGER.getModule("Freecam"));
            if (freecam != null && freecam.isToggled()) {
                args.setAll(new Object[]{Float.valueOf(freecam.getFreeYaw()), Float.valueOf(freecam.getFreePitch())});
                return;
            }
            ElytraFlyModule elytraFly = Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
            if (elytraFly != null && elytraFly.isFreeLookActive()) {
                boolean mirrored = CameraMixin.mc.options != null && CameraMixin.mc.options.getCameraType().isMirrored();
                float yaw = elytraFly.getFreeYaw();
                float pitch = elytraFly.getFreePitch();
                if (mirrored) {
                    yaw += 180.0f;
                    pitch = -pitch;
                }
                args.setAll(new Object[]{Float.valueOf(yaw), Float.valueOf(pitch)});
                return;
            }
            AutoMaceModule autoMace = Night.MODULE_MANAGER.getModule(AutoMaceModule.class);
            if (autoMace != null && autoMace.isFreeLookActive()) {
                boolean mirrored = CameraMixin.mc.options != null && CameraMixin.mc.options.getCameraType().isMirrored();
                float yaw = autoMace.getFreeYaw();
                float pitch = autoMace.getFreePitch();
                if (mirrored) {
                    yaw += 180.0f;
                    pitch = -pitch;
                }
                args.setAll(new Object[]{Float.valueOf(yaw), Float.valueOf(pitch)});
                return;
            }
            RotationLockModule lock = Night.MODULE_MANAGER.getModule(RotationLockModule.class);
            if (lock != null && lock.isToggled() && lock.custom.getValue()) {
                float yawOffset = lock.getShakeYawOffset();
                float pitchOffset = lock.getShakePitchOffset();
                if (yawOffset != 0.0f || pitchOffset != 0.0f) {
                    args.setAll(new Object[]{Float.valueOf(((Float)args.get(0)).floatValue() + yawOffset), Float.valueOf(((Float)args.get(1)).floatValue() + pitchOffset)});
                }
            }
        }
    }

    @ModifyArgs(method={"alignWithEntity"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/Camera;setPosition(DDD)V"))
    private void update$setPos(Args args) {
        if (Night.MODULE_MANAGER == null) {
            return;
        }
        IFreecamModule fc = (IFreecamModule)((Object)Night.MODULE_MANAGER.getModule("Freecam"));
        if (fc.isToggled()) {
            args.setAll(new Object[]{fc.getFreeX(), fc.getFreeY(), fc.getFreeZ()});
        } else {
            IMotionCameraModule motionCamera = (IMotionCameraModule)((Object)Night.MODULE_MANAGER.getModule("MotionCamera"));
            if (motionCamera != null && motionCamera.isToggled() && motionCamera.on()) {
                args.setAll(new Object[]{motionCamera.getFakeX(), motionCamera.getFakeY(), motionCamera.getFakeZ()});
            }
        }
    }

    @Inject(method={"calculateFov"}, at={@At(value="TAIL")}, cancellable=true)
    private void getFOV(float partialTicks, CallbackInfoReturnable<Float> info) {
        if (Night.MODULE_MANAGER == null) {
            return;
        }
        IFOVModifierModule module = (IFOVModifierModule)((Object)Night.MODULE_MANAGER.getModule("FOVModifier"));
        if (module.isToggled()) {
            info.setReturnValue(Float.valueOf(module.getFovValue()));
        }
    }
}

