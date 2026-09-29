/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.core.Holder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.SwingAnimation
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Constant
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyConstant
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package night.mixins;
import org.spongepowered.asm.mixin.injection.At.Shift;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.impl.ConsumeItemEvent;
import night.events.impl.PlayerJumpEvent;
import night.managers.RotationManager;
import night.mixins.accessors.EntityAccessor;
import night.modules.api.IFastClimbModule;
import night.modules.api.IHoleSnapModule;
import night.modules.api.INoJumpDelayModule;
import night.modules.api.ISprintModule;
import night.modules.impl.combat.AutoMaceModule;
import night.modules.impl.movement.ElytraFlyModule;
import night.modules.impl.movement.StepModule;
import night.modules.impl.player.SwingModule;
import night.utils.IMinecraft;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LivingEntity.class})
public abstract class LivingEntityMixin
extends Entity
implements IMinecraft {
    @Shadow
    @Final
    private static AttributeModifier SPEED_MODIFIER_SPRINTING;
    @Shadow
    private int noJumpDelay;
    @Shadow
    protected ItemStack useItem;
    @Unique
    private float night$grimYaw0;
    @Unique
    private boolean night$grimSwapped;

    @Shadow
    @Nullable
    public abstract AttributeInstance getAttribute(Holder<Attribute> var1);

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @WrapMethod(method={"maxUpStep"})
    private float getStepHeight(Operation<Float> original) {
        IHoleSnapModule holeSnap;
        IHoleSnapModule iHoleSnapModule = holeSnap = Night.MODULE_MANAGER != null ? (IHoleSnapModule)((Object)Night.MODULE_MANAGER.getModule("HoleSnap")) : null;
        if ((Object)(Object)this == LivingEntityMixin.mc.player && Night.MODULE_MANAGER != null && (Night.MODULE_MANAGER.getModule(StepModule.class).isToggled() && LivingEntityMixin.mc.player.onGround() || holeSnap != null && holeSnap.isToggled() && holeSnap.isStepEnabled())) {
            return Night.MODULE_MANAGER.getModule(StepModule.class).height.getValue().floatValue();
        }
        return ((Float)original.call(new Object[0])).floatValue();
    }

    @Inject(method={"aiStep"}, at={@At(value="INVOKE", target="Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", ordinal=2, shift=At.Shift.BEFORE)})
    private void doItemUse(CallbackInfo info) {
        INoJumpDelayModule noJumpDelayModule;
        INoJumpDelayModule iNoJumpDelayModule = noJumpDelayModule = Night.MODULE_MANAGER != null ? (INoJumpDelayModule)((Object)Night.MODULE_MANAGER.getModule("NoJumpDelay")) : null;
        if (noJumpDelayModule != null && noJumpDelayModule.isToggled() && this.noJumpDelay == 10) {
            this.noJumpDelay = noJumpDelayModule.getTicksValue();
        }
    }

    @Inject(method={"aiStep"}, at={@At(value="RETURN")})
    private void night$bounceNoJumpDelay(CallbackInfo ci) {
        if ((Object)(Object)this != LivingEntityMixin.mc.player || Night.MODULE_MANAGER == null) {
            return;
        }
        ElytraFlyModule ef = Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
        if (ef != null && ef.isBounceActive()) {
            this.noJumpDelay = 0;
        }
    }

    @WrapOperation(method={"getCurrentSwingDuration"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/item/component/SwingAnimation;duration()I")})
    private int getHandSwingDuration(SwingAnimation instance, Operation<Integer> original) {
        int constant = (Integer)original.call(new Object[]{instance});
        if ((Object)(Object)this != LivingEntityMixin.mc.player) {
            return constant;
        }
        return Night.MODULE_MANAGER.getModule(SwingModule.class).isToggled() && Night.MODULE_MANAGER.getModule(SwingModule.class).modifySpeed.getValue() && LivingEntityMixin.mc.options.getCameraType().isFirstPerson() ? 21 - Night.MODULE_MANAGER.getModule(SwingModule.class).speed.getValue().intValue() : constant;
    }

    @Inject(method={"completeUsingItem"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;", shift=At.Shift.AFTER)})
    private void consumeItem(CallbackInfo ci) {
        if ((Object)(Object)this == LivingEntityMixin.mc.player) {
            Night.EVENT_HANDLER.post(new ConsumeItemEvent(this.useItem));
        }
    }

    @Inject(method={"setSprinting"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;setSprinting(Z)V", shift=At.Shift.AFTER)}, cancellable=true)
    private void setSprinting$setSprinting(boolean sprinting, CallbackInfo info) {
        if ((Object)(Object)this == LivingEntityMixin.mc.player) {
            ISprintModule sprint;
            ElytraFlyModule ef;
            ElytraFlyModule elytraFlyModule = ef = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ElytraFlyModule.class) : null;
            if (ef != null && ef.isToggled() && ef.mode.getValue().equalsIgnoreCase("Bounce")) {
                return;
            }
            ISprintModule iSprintModule = sprint = Night.MODULE_MANAGER == null ? null : (ISprintModule)((Object)Night.MODULE_MANAGER.getModule("Sprint"));
            if (sprint != null && sprint.isToggled()) {
                AttributeInstance entityAttributeInstance = this.getAttribute((Holder<Attribute>)Attributes.MOVEMENT_SPEED);
                if (entityAttributeInstance != null) {
                    entityAttributeInstance.removeModifier(SPEED_MODIFIER_SPRINTING.id());
                    if (sprint.shouldSprint()) {
                        this.setSharedFlag(3, true);
                        entityAttributeInstance.addTransientModifier(SPEED_MODIFIER_SPRINTING);
                    } else {
                        this.setSharedFlag(3, false);
                    }
                }
                info.cancel();
            }
        }
    }

    @Inject(method={"jumpFromGround"}, at={@At(value="HEAD")})
    private void jump$HEAD(CallbackInfo info) {
        if ((Object)(Object)this != LivingEntityMixin.mc.player) {
            return;
        }
        Night.EVENT_HANDLER.post(new PlayerJumpEvent());
    }

    @Inject(method={"jumpFromGround"}, at={@At(value="RETURN")})
    private void jump$RETURN(CallbackInfo info) {
        if ((Object)(Object)this != LivingEntityMixin.mc.player) {
            return;
        }
        Night.EVENT_HANDLER.post(new PlayerJumpEvent.Post());
    }

    @Inject(method={"onClimbable"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$fastClimbIgnore(CallbackInfoReturnable<Boolean> cir) {
        if ((Object)(Object)this != LivingEntityMixin.mc.player) {
            return;
        }
        IFastClimbModule fastClimb = (IFastClimbModule)((Object)Night.MODULE_MANAGER.getModule("FastClimb"));
        if (fastClimb.isToggled() && fastClimb.getSpeedValue() == 0.0f) {
            cir.setReturnValue(false);
        }
    }

    @ModifyConstant(method={"handleOnClimbable"}, constant={@Constant(floatValue=0.15f)}, require=0)
    private float night$fastClimbScale(float constant) {
        return this.night$fastClimbFactor(constant);
    }

    @Unique
    private float night$fastClimbFactor(float constant) {
        if ((Object)(Object)this != LivingEntityMixin.mc.player) {
            return constant;
        }
        IFastClimbModule fastClimb = (IFastClimbModule)((Object)Night.MODULE_MANAGER.getModule("FastClimb"));
        if (!fastClimb.isToggled()) {
            return constant;
        }
        float factor = fastClimb.getSpeedValue();
        if (factor <= 0.0f || factor >= 1.0f) {
            return constant;
        }
        return constant * factor;
    }

    @Inject(method={"getItemBySlot"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$hideElytraSwap(EquipmentSlot slot, CallbackInfoReturnable<ItemStack> cir) {
        if (slot != EquipmentSlot.CHEST || (Object)(Object)this != LivingEntityMixin.mc.player || Night.MODULE_MANAGER == null) {
            return;
        }
        ItemStack parked = Night.MODULE_MANAGER.getModule(ElytraFlyModule.class).getGrimHiddenElytra();
        if (parked != null) {
            cir.setReturnValue(parked);
        }
    }

    @Inject(method={"isFallFlying"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$pinBounceAndControlRocketGlide(CallbackInfoReturnable<Boolean> cir) {
        AutoMaceModule am;
        if ((Object)(Object)this != LivingEntityMixin.mc.player || Night.MODULE_MANAGER == null) {
            return;
        }
        ElytraFlyModule ef = Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
        if (ef != null && ef.isToggled()) {
            if (ef.isBounceActive() && ef.isGliding()) {
                cir.setReturnValue(true);
                return;
            }
            if (ef.shouldPinFallFlying()) {
                cir.setReturnValue(true);
                return;
            }
        }
        if ((am = Night.MODULE_MANAGER.getModule(AutoMaceModule.class)) != null && am.shouldPinFallFlying()) {
            cir.setReturnValue(true);
        }
    }

    @WrapOperation(method={"updateFallFlyingMovement"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;getXRot()F")})
    private float wrapFallFlyingPitch(LivingEntity instance, Operation<Float> original) {
        ElytraFlyModule ef;
        if ((Object)(Object)this == LivingEntityMixin.mc.player && Night.MODULE_MANAGER != null && (ef = Night.MODULE_MANAGER.getModule(ElytraFlyModule.class)) != null && ef.isBounceAutoPitch()) {
            return ef.getBouncePitch();
        }
        return ((Float)original.call(new Object[]{instance})).floatValue();
    }

    @WrapOperation(method={"updateFallFlyingMovement"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;getLookAngle()Lnet/minecraft/world/phys/Vec3;")})
    private Vec3 wrapFallFlyingLookAngle(LivingEntity instance, Operation<Vec3> original) {
        ElytraFlyModule ef;
        if ((Object)(Object)this == LivingEntityMixin.mc.player && Night.MODULE_MANAGER != null && (ef = Night.MODULE_MANAGER.getModule(ElytraFlyModule.class)) != null && ef.isBounceAutoPitch()) {
            return Vec3.directionFromRotation((float)ef.getBouncePitch(), (float)instance.getYRot());
        }
        return (Vec3)original.call(new Object[]{instance});
    }

    @Inject(method={"travel"}, at={@At(value="HEAD")})
   private void travel$grimHead(Vec3 movementInput, CallbackInfo info) {
      this.night$grimSwapped = false;
      if ((Object)this == mc.player && this.night$shouldSpoofRotation()) {
         this.night$grimYaw0 = ((EntityAccessor)this).getRawYRot();
         ((EntityAccessor)this).setRawYRot(this.night$spoofYaw());
         RotationManager.swapActive = true;
         this.night$grimSwapped = true;
      }
   }

    @Inject(method={"travel"}, at={@At(value="RETURN")})
    private void travel$grimReturn(Vec3 movementInput, CallbackInfo info) {
        if (!this.night$grimSwapped) {
            return;
        }
        this.night$grimSwapped = false;
        RotationManager.swapActive = false;
        ((EntityAccessor)((Object)this)).setRawYRot(this.night$grimYaw0);
    }

    @ModifyExpressionValue(method={"jumpFromGround"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;getYRot()F")})
   private float jumpFromGround$grimYaw(float original) {
      return (Object)this == mc.player && this.night$shouldSpoofRotation() ? this.night$spoofYaw() : original;
   }

    @Unique
    private boolean night$grimCompensating() {
        if (Night.MODULE_MANAGER == null) {
            return false;
        }
        ISprintModule sprint = (ISprintModule)((Object)Night.MODULE_MANAGER.getModule("Sprint"));
        return sprint != null && sprint.isGrimCompensating();
    }

    @Unique
    private boolean night$shouldSpoofRotation() {
        return this.night$grimCompensating();
    }

    @Unique
    private float night$spoofYaw() {
        return ((ISprintModule)((Object)Night.MODULE_MANAGER.getModule("Sprint"))).getGrimYaw();
    }
}

