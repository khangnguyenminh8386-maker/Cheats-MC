/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.client.player.RemotePlayer
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityFluidInteraction
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package night.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.impl.ChangePitchEvent;
import night.events.impl.ChangeYawEvent;
import night.events.impl.UpdateVelocityEvent;
import night.managers.RotationManager;
import night.modules.api.IFreecamModule;
import night.modules.impl.combat.AutoMaceModule;
import night.modules.impl.miscellaneous.FakePlayerModule;
import night.modules.impl.movement.ElytraFlyModule;
import night.modules.impl.movement.NoSlowModule;
import night.modules.impl.movement.VelocityModule;
import night.modules.impl.player.RotationLockModule;
import night.modules.impl.visuals.LogoutSpotModule;
import night.modules.impl.visuals.PopChamsModule;
import night.utils.IMinecraft;
import night.utils.minecraft.EntityUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Entity.class})
public abstract class EntityMixin
implements IMinecraft {
    @Shadow
    private float yRot;
    @Shadow
    private float xRot;

    @Inject(method={"tick"}, at={@At(value="HEAD")}, cancellable=true)
   private void night$cancelGhostTick(CallbackInfo ci) {
      if ((Object)this instanceof RemotePlayer ghost) {
         PopChamsModule popChams = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(PopChamsModule.class) : null;
         if (popChams != null && popChams.isGhost(ghost)) {
            ci.cancel();
            return;
         }

         LogoutSpotModule logoutSpot = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(LogoutSpotModule.class) : null;
         if (logoutSpot != null && logoutSpot.isGhost(ghost)) {
            ci.cancel();
         }
      }
   }

    @Inject(method={"turn"}, at={@At(value="HEAD")}, cancellable=true)
    public void onTurn(double cursorDeltaYaw, double cursorDeltaPitch, CallbackInfo ci) {
        if ((Object)(Object)this == EntityMixin.mc.player && Night.MODULE_MANAGER != null) {
            IFreecamModule freecam = (IFreecamModule)((Object)Night.MODULE_MANAGER.getModule("Freecam"));
            if (freecam != null && freecam.isToggled() && !freecam.isRotate()) {
                freecam.onMouseTurn(cursorDeltaYaw, cursorDeltaPitch);
                ci.cancel();
                return;
            }
            ElytraFlyModule elytraFly = Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
            if (elytraFly != null && elytraFly.isFreeLookActive()) {
                elytraFly.onMouseTurn(cursorDeltaYaw, cursorDeltaPitch);
                ci.cancel();
                return;
            }
            AutoMaceModule autoMace = Night.MODULE_MANAGER.getModule(AutoMaceModule.class);
            if (autoMace != null && autoMace.isFreeLookActive()) {
                autoMace.onMouseTurn(cursorDeltaYaw, cursorDeltaPitch);
                ci.cancel();
            }
        }
    }

    @Inject(method={"push(Lnet/minecraft/world/entity/Entity;)V"}, at={@At(value="HEAD")}, cancellable=true)
   private void pushAwayFrom(Entity entity, CallbackInfo info) {
      if (((Object)this != mc.player || !this.isPhantomEntity(entity)) && (entity != mc.player || !this.isPhantomEntity((Entity)(Object)this))) {
         if ((Object)this == mc.player
            && Night.MODULE_MANAGER.getModule(VelocityModule.class).isToggled()
            && Night.MODULE_MANAGER.getModule(VelocityModule.class).antiPush.getValue()) {
            info.cancel();
         }
      } else {
         info.cancel();
      }
   }

    private boolean isPhantomEntity(Entity entity) {
        if (EntityUtils.isGhost(entity)) {
            return true;
        }
        FakePlayerModule fakePlayer = Night.MODULE_MANAGER.getModule(FakePlayerModule.class);
        return fakePlayer != null && entity == fakePlayer.getPlayer();
    }

    @WrapOperation(method={"updateFluidInteraction"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/EntityFluidInteraction;applyCurrentTo(Lnet/minecraft/tags/TagKey;Lnet/minecraft/world/entity/Entity;D)V")})
    private void updateMovementInFluid(EntityFluidInteraction instance, TagKey<Fluid> fluid, Entity entity, double motionScale, Operation<Void> original) {
        if ((Object)(Object)this == EntityMixin.mc.player && Night.MODULE_MANAGER.getModule(VelocityModule.class).isToggled() && Night.MODULE_MANAGER.getModule(VelocityModule.class).antiLiquidPush.getValue()) {
            return;
        }
        original.call(new Object[]{instance, fluid, entity, motionScale});
    }

    @Inject(method={"moveRelative"}, at={@At(value="HEAD")}, cancellable=true)
    private void updateVelocity(float speed, Vec3 movementInput, CallbackInfo info) {
        if ((Object)(Object)this != EntityMixin.mc.player) {
            return;
        }
        UpdateVelocityEvent event = new UpdateVelocityEvent(movementInput, speed);
        Night.EVENT_HANDLER.post(event);
        if (event.isCancelled()) {
            info.cancel();
            EntityMixin.mc.player.setDeltaMovement(EntityMixin.mc.player.getDeltaMovement().add(event.getVelocity()));
        }
    }

    @Inject(method={"getYRot()F"}, at={@At(value="HEAD")}, cancellable=true)
    private void getYaw(CallbackInfoReturnable<Float> info) {
        if (RotationManager.swapActive && (Object)(Object)this == EntityMixin.mc.player) {
            return;
        }
        RotationLockModule lock = Night.MODULE_MANAGER.getModule(RotationLockModule.class);
        if (lock.isToggled() && (lock.mode.getValue().equals("Yaw") || lock.mode.getValue().equals("Both")) && (Object)(Object)this == EntityMixin.mc.player) {
            info.setReturnValue(Float.valueOf(lock.getYawValue(this.yRot)));
        }
    }

    @Inject(method={"getXRot()F"}, at={@At(value="HEAD")}, cancellable=true)
    private void getPitchRaw(CallbackInfoReturnable<Float> info) {
        if (RotationManager.swapActive && (Object)(Object)this == EntityMixin.mc.player) {
            return;
        }
        RotationLockModule lock = Night.MODULE_MANAGER.getModule(RotationLockModule.class);
        if (lock.isToggled() && (lock.mode.getValue().equals("Pitch") || lock.mode.getValue().equals("Both")) && (Object)(Object)this == EntityMixin.mc.player) {
            info.setReturnValue(Float.valueOf(lock.getPitchValue(this.xRot)));
        }
    }

    @Inject(method={"setYRot"}, at={@At(value="HEAD")}, cancellable=true)
    private void setYaw(float yaw, CallbackInfo info) {
        if ((Object)(Object)this != EntityMixin.mc.player) {
            return;
        }
        Night.EVENT_HANDLER.post(new ChangeYawEvent(yaw));
    }

    @Inject(method={"setXRot"}, at={@At(value="HEAD")}, cancellable=true)
    private void setPitch(float pitch, CallbackInfo info) {
        if ((Object)(Object)this != EntityMixin.mc.player) {
            return;
        }
        Night.EVENT_HANDLER.post(new ChangePitchEvent(pitch));
    }

    @ModifyExpressionValue(method={"getBlockSpeedFactor"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;")})
    private Block getVelocityMultiplier(Block original) {
        if (Night.MODULE_MANAGER.getModule(NoSlowModule.class).isToggled() && (original == Blocks.SOUL_SAND && Night.MODULE_MANAGER.getModule(NoSlowModule.class).soulSand.getValue() || original == Blocks.HONEY_BLOCK && Night.MODULE_MANAGER.getModule(NoSlowModule.class).honeyBlocks.getValue())) {
            return Blocks.STONE;
        }
        return original;
    }

    @ModifyReturnValue(method={"isSprinting()Z"}, at={@At(value="RETURN")})
    private boolean injectIsSprinting(boolean original) {
        if ((Object)(Object)this != EntityMixin.mc.player || Night.MODULE_MANAGER == null) {
            return original;
        }
        ElytraFlyModule ef = Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
        if (ef != null && ef.isBounceActive() && ef.isGliding()) {
            return true;
        }
        return original;
    }

    @ModifyReturnValue(method={"getPose"}, at={@At(value="RETURN")})
    private Pose injectGetPose(Pose original) {
        if ((Object)(Object)this != EntityMixin.mc.player || Night.MODULE_MANAGER == null) {
            return original;
        }
        ElytraFlyModule ef = Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
        if (ef != null && ef.isBounceActive() && ef.isGliding()) {
            return Pose.FALL_FLYING;
        }
        return original;
    }
}

