/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.player.ClientInput
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.MoverType
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.Vec3
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package night.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.impl.ChangeHandEvent;
import night.events.impl.PlayerMoveEvent;
import night.events.impl.PlayerUpdateEvent;
import night.events.impl.SendMovementEvent;
import night.events.impl.UpdateMovementEvent;
import night.modules.api.ISprintModule;
import night.modules.impl.movement.ElytraFlyModule;
import night.modules.impl.movement.InventoryControlModule;
import night.modules.impl.movement.NoSlowModule;
import night.modules.impl.movement.VelocityModule;
import night.modules.impl.player.NoEntityTraceModule;
import night.modules.impl.player.SwingModule;
import night.pingbypass.PingBypassFlags;
import night.utils.EarlyTickHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LocalPlayer.class}, priority=500)
public abstract class ClientPlayerEntityMixin
extends AbstractClientPlayer {
    @Shadow
    @Final
    public ClientPacketListener connection;
    @Shadow
    private float xRotLast;
    @Shadow
    public float xBob;
    @Shadow
    public ClientInput input;

    public ClientPlayerEntityMixin(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow
    protected abstract void updateAutoJump(float var1, float var2);

    @Inject(method={"<init>"}, at={@At(value="HEAD")})
    private static void night$clearTrackedBlockEntities(CallbackInfo ci) {
        night.utils.BlockEntityInstanceTracker.localPlayerConstructed();
        night.modules.impl.movement.AirStuckModule.localPlayerConstructed();
    }

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    private void earlyTick$dispatch(CallbackInfo ci) {
        if (Minecraft.getInstance().player != (Object)this) {
            return;
        }
        EarlyTickHooks.dispatch();
    }

    @Inject(method={"tick"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/AbstractClientPlayer;tick()V", shift=At.Shift.BEFORE)})
    private void tick$BEFORE(CallbackInfo info) {
        Night.EVENT_HANDLER.post(new PlayerUpdateEvent());
    }

    @Inject(method={"tick"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/AbstractClientPlayer;tick()V", shift=At.Shift.AFTER)})
    private void tick$AFTER(CallbackInfo info) {
        Night.EVENT_HANDLER.post(new UpdateMovementEvent());
    }

    @Inject(method={"tick"}, at={@At(value="TAIL")})
    private void tick$TAIL(CallbackInfo ci) {
        Night.EVENT_HANDLER.post(new UpdateMovementEvent.Post());
    }

    @ModifyExpressionValue(method={"sendPosition"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;getXRot()F")})
    private float modifySendPositionPitch(float original) {
        ElytraFlyModule ef;
        if (Night.MODULE_MANAGER != null && (ef = Night.MODULE_MANAGER.getModule(ElytraFlyModule.class)) != null && ef.isBounceAutoPitch()) {
            return ef.getBouncePitch();
        }
        return original;
    }

    @WrapOperation(method={"sendPosition"}, at={@At(value="FIELD", target="Lnet/minecraft/client/player/LocalPlayer;yRotLast:F", opcode=180, ordinal=0)})
    private float sendPosition$yRotLast(LocalPlayer instance, Operation<Float> original) {
        if (Night.ROTATION_MANAGER.getRotation() != null) {
            return Night.ROTATION_MANAGER.getServerYaw();
        }
        return ((Float)original.call(new Object[]{instance})).floatValue();
    }

    @WrapOperation(method={"sendPosition"}, at={@At(value="FIELD", target="Lnet/minecraft/client/player/LocalPlayer;xRotLast:F", opcode=180, ordinal=0)})
    private float sendPosition$xRotLast(LocalPlayer instance, Operation<Float> original) {
        if (Night.ROTATION_MANAGER.getRotation() != null) {
            return Night.ROTATION_MANAGER.getServerPitch();
        }
        return ((Float)original.call(new Object[]{instance})).floatValue();
    }

    @ModifyExpressionValue(method={"modifyInput"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z")})
    private boolean tickMovement$isUsingItem(boolean original) {
        if (Night.MODULE_MANAGER.getModule(NoSlowModule.class).isToggled() && Night.MODULE_MANAGER.getModule(NoSlowModule.class).items.getValue() && !Night.MODULE_MANAGER.getModule(NoSlowModule.class).shouldSlow()) {
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(method={"modifyInput"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;isMovingSlowly()Z")})
    private boolean tickMovement$isMovingSlowly(boolean original) {
        if (!original) {
            return original;
        }
        NoSlowModule module = Night.MODULE_MANAGER.getModule(NoSlowModule.class);
        if (module == null || !module.isToggled()) {
            return original;
        }
        if (this.isVisuallyCrawling() && module.crawl.getValue()) {
            return false;
        }
        if (this.isCrouching() && module.sneak.getValue()) {
            return false;
        }
        return original;
    }

    @Inject(method={"move"}, at={@At(value="HEAD")}, cancellable=true)
    private void move(MoverType movementType, Vec3 movement, CallbackInfo info) {
        if (PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer()) {
            info.cancel();
            return;
        }
        PlayerMoveEvent event = new PlayerMoveEvent(movementType, movement);
        Night.EVENT_HANDLER.post(event);
        if (event.isCancelled()) {
            info.cancel();
            double prevX = this.getX();
            double prevZ = this.getZ();
            super.move(movementType, event.getMovement());
            this.updateAutoJump((float)(this.getX() - prevX), (float)(this.getZ() - prevZ));
        }
    }

    @Inject(method={"sendPosition"}, at={@At(value="HEAD")}, cancellable=true)
    private void sendMovementPackets(CallbackInfo info) {
        SendMovementEvent event = new SendMovementEvent();
        Night.EVENT_HANDLER.post(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @ModifyExpressionValue(method={"pick"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;")})
    @Nullable
    private static EntityHitResult pick$getEntityHitResult(@Nullable EntityHitResult original) {
        NoEntityTraceModule module = Night.MODULE_MANAGER.getModule(NoEntityTraceModule.class);
        if (module.isToggled() && module.shouldIgnore(original != null ? original.getEntity() : null)) {
            return null;
        }
        return original;
    }

    @Inject(method={"itemUseSpeedMultiplier"}, at={@At(value="HEAD")}, cancellable=true)
    private void onItemUseSpeedMultiplier(CallbackInfoReturnable<Float> cir) {
        NoSlowModule module;
        NoSlowModule noSlowModule = module = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(NoSlowModule.class) : null;
        if (module != null && module.isToggled() && module.items.getValue() && !module.shouldSlow()) {
            cir.setReturnValue(Float.valueOf(1.0f));
        }
    }

    @Inject(method={"shouldStopRunSprinting"}, at={@At(value="HEAD")}, cancellable=true)
    private void shouldStopRunSprinting$HEAD(CallbackInfoReturnable<Boolean> cir) {
        NoSlowModule module;
        NoSlowModule noSlowModule = module = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(NoSlowModule.class) : null;
        if (module != null && module.isToggled() && module.items.getValue() && !module.shouldSlow() && this.isUsingItem() && !this.isFallFlying() && !this.isMobilityRestricted() && !this.isPassenger()) {
            boolean moving;
            ISprintModule sprintModule;
            boolean enoughFood;
            if (this.horizontalCollision && !this.minorHorizontalCollision) {
                cir.setReturnValue(true);
                return;
            }
            boolean bl = enoughFood = (float)this.getFoodData().getFoodLevel() > 6.0f || this.getAbilities().mayfly;
            if (!enoughFood) {
                cir.setReturnValue(true);
                return;
            }
            ISprintModule iSprintModule = sprintModule = Night.MODULE_MANAGER == null ? null : (ISprintModule)((Object)Night.MODULE_MANAGER.getModule("Sprint"));
            moving = sprintModule != null && sprintModule.isToggled() ? sprintModule.shouldSprint() : (this.input != null && this.input.hasForwardImpulse());
            if (!moving) {
                cir.setReturnValue(true);
                return;
            }
            if (this.isInWater() && !this.isUnderWater()) {
                cir.setReturnValue(true);
                return;
            }
            cir.setReturnValue(false);
        }
    }

    @Inject(method={"shouldStopRunSprinting"}, at={@At(value="RETURN")}, cancellable=true)
    private void shouldStopRunSprinting$RETURN(CallbackInfoReturnable<Boolean> cir) {
        if (((Boolean)cir.getReturnValue()).booleanValue()) {
            NoSlowModule module;
            NoSlowModule noSlowModule = module = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(NoSlowModule.class) : null;
            if (!(module == null || !module.isToggled() || !module.items.getValue() || module.shouldSlow() || !this.isUsingItem() || this.isFallFlying() || this.isMobilityRestricted() || this.isPassenger() || this.horizontalCollision && !this.minorHorizontalCollision)) {
                boolean moving;
                ISprintModule sprintModule;
                boolean enoughFood = (float)this.getFoodData().getFoodLevel() > 6.0f || this.getAbilities().mayfly;
                ISprintModule iSprintModule = sprintModule = Night.MODULE_MANAGER == null ? null : (ISprintModule)((Object)Night.MODULE_MANAGER.getModule("Sprint"));
                moving = sprintModule != null && sprintModule.isToggled() ? sprintModule.shouldSprint() : (this.input != null && this.input.hasForwardImpulse());
                if (enoughFood && moving && (!this.isInWater() || this.isUnderWater())) {
                    cir.setReturnValue(false);
                }
            }
        }
    }

    @Inject(method={"canStartSprinting"}, at={@At(value="HEAD")}, cancellable=true)
    private void canStartSprinting$HEAD(CallbackInfoReturnable<Boolean> cir) {
        NoSlowModule module;
        NoSlowModule noSlowModule = module = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(NoSlowModule.class) : null;
        if (module != null && module.isToggled() && module.items.getValue() && !module.shouldSlow() && this.isUsingItem() && !this.isSprinting() && !this.isFallFlying() && !this.isMobilityRestricted() && !this.isPassenger()) {
            boolean enoughFood;
            boolean bl = enoughFood = (float)this.getFoodData().getFoodLevel() > 6.0f || this.getAbilities().mayfly;
            if (enoughFood) {
                boolean moving;
                ISprintModule sprintModule;
                ISprintModule iSprintModule = sprintModule = Night.MODULE_MANAGER == null ? null : (ISprintModule)((Object)Night.MODULE_MANAGER.getModule("Sprint"));
                moving = sprintModule != null && sprintModule.isToggled() ? sprintModule.shouldSprint() : (this.input != null && this.input.hasForwardImpulse());
                if (moving && (!this.isInWater() || this.isUnderWater())) {
                    cir.setReturnValue(true);
                }
            }
        }
    }

    @ModifyExpressionValue(method={"canStartSprinting"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;isSlowDueToUsingItem()Z")})
    private boolean canStartSprinting$isUsingItem(boolean original) {
        NoSlowModule module;
        NoSlowModule noSlowModule = module = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(NoSlowModule.class) : null;
        if (module != null && module.isToggled() && module.items.getValue() && !module.shouldSlow()) {
            return false;
        }
        return original;
    }

    @Inject(method={"canStartSprinting"}, at={@At(value="RETURN")}, cancellable=true)
    private void canStartSprinting$RETURN(CallbackInfoReturnable<Boolean> cir) {
        if (!((Boolean)cir.getReturnValue()).booleanValue()) {
            NoSlowModule module;
            NoSlowModule noSlowModule = module = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(NoSlowModule.class) : null;
            if (module != null && module.isToggled() && module.items.getValue() && !module.shouldSlow() && this.isUsingItem() && !this.isSprinting() && !this.isFallFlying() && !this.isMobilityRestricted() && !this.isPassenger()) {
                boolean enoughFood;
                boolean bl = enoughFood = (float)this.getFoodData().getFoodLevel() > 6.0f || this.getAbilities().mayfly;
                if (enoughFood) {
                    boolean moving;
                    ISprintModule sprintModule;
                    ISprintModule iSprintModule = sprintModule = Night.MODULE_MANAGER == null ? null : (ISprintModule)((Object)Night.MODULE_MANAGER.getModule("Sprint"));
                    moving = sprintModule != null && sprintModule.isToggled() ? sprintModule.shouldSprint() : (this.input != null && this.input.hasForwardImpulse());
                    if (moving && (!this.isInWater() || this.isUnderWater())) {
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }

    @Inject(method={"moveTowardsClosestSpace"}, at={@At(value="HEAD")}, cancellable=true)
    private void pushOutOfBlocks(double x, double z, CallbackInfo info) {
        if (Night.MODULE_MANAGER.getModule(VelocityModule.class).isToggled() && Night.MODULE_MANAGER.getModule(VelocityModule.class).antiBlockPush.getValue()) {
            info.cancel();
        }
    }

    @Inject(method={"swing"}, at={@At(value="HEAD")}, cancellable=true)
    private void swingHand(InteractionHand hand, CallbackInfo info) {
        if (Night.MODULE_MANAGER.getModule(SwingModule.class).isToggled()) {
            if (!Night.MODULE_MANAGER.getModule(SwingModule.class).hand.getValue().equals("None")) {
                switch (Night.MODULE_MANAGER.getModule(SwingModule.class).hand.getValue()) {
                    case "Default": {
                        super.swing(hand);
                        break;
                    }
                    case "Mainhand": {
                        super.swing(InteractionHand.MAIN_HAND);
                        break;
                    }
                    case "Offhand": {
                        super.swing(InteractionHand.OFF_HAND);
                        break;
                    }
                    case "Both": {
                        super.swing(InteractionHand.MAIN_HAND);
                        super.swing(InteractionHand.OFF_HAND);
                    }
                }
                if (Night.MODULE_MANAGER.getModule(SwingModule.class).hand.getValue().equalsIgnoreCase("Packet") || !Night.MODULE_MANAGER.getModule(SwingModule.class).noPacket.getValue()) {
                    this.connection.send((Packet)new ServerboundSwingPacket(hand));
                }
            }
            info.cancel();
        }
    }

    @Inject(method={"handlePortalTransitionEffect"}, at={@At(value="HEAD")}, cancellable=true)
    private void tickNausea(boolean fromPortalEffect, CallbackInfo info) {
        if (Night.MODULE_MANAGER.getModule(InventoryControlModule.class).isToggled() && Night.MODULE_MANAGER.getModule(InventoryControlModule.class).portals.getValue()) {
            info.cancel();
        }
    }

    @Inject(method={"startUsingItem"}, at={@At(value="HEAD")})
    private void setCurrentHand(InteractionHand hand, CallbackInfo info) {
        Night.EVENT_HANDLER.post(new ChangeHandEvent());
    }
}

