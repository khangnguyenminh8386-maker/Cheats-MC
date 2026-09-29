/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.MoverType
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package night.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.impl.PlayerTravelEvent;
import night.modules.api.IPatchModule;
import night.modules.api.IReachModule;
import night.modules.api.ISpeedModule;
import night.modules.impl.combat.AutoMaceModule;
import night.modules.impl.movement.ElytraFlyModule;
import night.modules.impl.movement.VelocityModule;
import night.utils.IMinecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Player.class})
public abstract class PlayerEntityMixin
extends LivingEntity
implements IMinecraft {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @ModifyReturnValue(method={"isPushedByFluid"}, at={@At(value="RETURN")})
    private boolean isPushedByFluids(boolean original) {
        if ((Object)(Object)this == PlayerEntityMixin.mc.player && Night.MODULE_MANAGER.getModule(VelocityModule.class).isToggled() && Night.MODULE_MANAGER.getModule(VelocityModule.class).antiLiquidPush.getValue()) {
            return false;
        }
        return original;
    }

    @Redirect(method={"causeExtraKnockback"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    private void grimAttackVelocity$setDeltaMovement(Player instance, Vec3 deltaMovement) {
        if ((Object)(Object)this == PlayerEntityMixin.mc.player && ((IPatchModule)((Object)Night.MODULE_MANAGER.getModule("Patch"))).isGrimAttackVelocity()) {
            return;
        }
        instance.setDeltaMovement(deltaMovement);
    }

    @Redirect(method={"causeExtraKnockback"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;setSprinting(Z)V"))
    private void grimAttackVelocity$setSprinting(Player instance, boolean sprinting) {
        if ((Object)(Object)this == PlayerEntityMixin.mc.player && ((IPatchModule)((Object)Night.MODULE_MANAGER.getModule("Patch"))).isGrimAttackVelocity()) {
            return;
        }
        instance.setSprinting(sprinting);
    }

    @Inject(method={"travel"}, at={@At(value="HEAD")}, cancellable=true)
    private void travel(Vec3 movementInput, CallbackInfo info) {
        PlayerTravelEvent event = new PlayerTravelEvent(movementInput);
        Night.EVENT_HANDLER.post(event);
        if (event.isCancelled()) {
            this.move(MoverType.SELF, this.getDeltaMovement());
            info.cancel();
        }
    }

    @Inject(method={"blockInteractionRange"}, at={@At(value="HEAD")}, cancellable=true)
    private void getBlockInteractionRange(CallbackInfoReturnable<Double> info) {
        IReachModule reach = (IReachModule)((Object)Night.MODULE_MANAGER.getModule("Reach"));
        if (reach.isToggled()) {
            info.setReturnValue(reach.getAmountValue());
        }
    }

    @Inject(method={"entityInteractionRange"}, at={@At(value="HEAD")}, cancellable=true)
    private void getEntityInteractionRange(CallbackInfoReturnable<Double> info) {
        IReachModule reach = (IReachModule)((Object)Night.MODULE_MANAGER.getModule("Reach"));
        if (reach.isToggled()) {
            info.setReturnValue(reach.getAmountValue());
        }
    }

    @ModifyReturnValue(method={"getDesiredPose"}, at={@At(value="RETURN")})
    private Pose night$controlRocketStandingHitbox(Pose original) {
        if (original != Pose.FALL_FLYING || (Object)(Object)this != PlayerEntityMixin.mc.player || Night.MODULE_MANAGER == null) {
            return original;
        }
        ElytraFlyModule ef = Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
        if (ef != null && ef.shouldPinFallFlying()) {
            return Pose.STANDING;
        }
        AutoMaceModule am = Night.MODULE_MANAGER.getModule(AutoMaceModule.class);
        return am != null && am.shouldPinFallFlying() ? Pose.STANDING : original;
    }

    @Inject(method={"getSpeed"}, at={@At(value="HEAD")}, cancellable=true)
    private void getMovementSpeed(CallbackInfoReturnable<Float> info) {
        ISpeedModule speed = (ISpeedModule)((Object)Night.MODULE_MANAGER.getModule("Speed"));
        if (speed.isToggled() && speed.getModeValue().equalsIgnoreCase("Vanilla")) {
            info.setReturnValue(Float.valueOf(speed.getVanillaSpeedValue()));
        }
    }
}

