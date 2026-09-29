/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.MultiPlayerGameMode
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket$Action
 *  net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Input
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.phys.BlockHitResult
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package night.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.phys.BlockHitResult;
import night.Night;
import night.events.impl.AttackBlockEvent;
import night.events.impl.AttackEntityEvent;
import night.events.impl.BreakBlockEvent;
import night.mixins.accessors.ClientPlayerEntityAccessor;
import night.modules.impl.core.NoMiddleClickModule;
import night.modules.impl.movement.InventoryControlModule;
import night.modules.impl.player.NoBreakDelayModule;
import night.modules.impl.player.NoInteractModule;
import night.pingbypass.PingBypassFlags;
import night.utils.minecraft.WorldUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={MultiPlayerGameMode.class})
public class ClientPlayerInteractionManagerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private int destroyDelay;
    @Unique
    private boolean night$stoppedSprintForClick = false;

    @Inject(method={"handleContainerInput"}, at={@At(value="HEAD")})
    private void beforeContainerClick(int containerId, int slotNum, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo info) {
        this.night$stoppedSprintForClick = false;
        InventoryControlModule module = Night.MODULE_MANAGER.getModule(InventoryControlModule.class);
        if (!module.isToggled() || !module.grimV2.getValue()) {
            return;
        }
        if (this.minecraft.player == null || this.minecraft.getConnection() == null) {
            return;
        }
        Input real = this.minecraft.player.input.keyPresses;
        if (real.forward() || real.backward() || real.left() || real.right() || real.jump()) {
            Input fake = new Input(false, false, false, false, false, real.shift(), real.sprint());
            this.minecraft.getConnection().send((Packet)new ServerboundPlayerInputPacket(fake));
            ((ClientPlayerEntityAccessor)this.minecraft.player).setLastSentInput(fake);
        }
        if (this.minecraft.player.isSprinting()) {
            this.minecraft.getConnection().send((Packet)new ServerboundPlayerCommandPacket((Entity)(Object)this.minecraft.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
            this.night$stoppedSprintForClick = true;
        }
    }

    @Inject(method={"handleContainerInput"}, at={@At(value="TAIL")})
    private void afterContainerClick(int containerId, int slotNum, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo info) {
        if (!this.night$stoppedSprintForClick) {
            return;
        }
        this.night$stoppedSprintForClick = false;
        if (this.minecraft.player == null || this.minecraft.getConnection() == null) {
            return;
        }
        this.minecraft.getConnection().send((Packet)new ServerboundPlayerCommandPacket((Entity)(Object)this.minecraft.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
    }

    @Inject(method={"ensureHasSentCarriedItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void syncSelectedSlot(CallbackInfo info) {
        if (PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer()) {
            info.cancel();
        }
    }

    @Inject(method={"releaseUsingItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void stopUsingItem(CallbackInfo info) {
        if (PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer()) {
            info.cancel();
        }
    }

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    private void onTick(CallbackInfo ci) {
        NoBreakDelayModule module;
        if (Night.MODULE_MANAGER != null && (module = Night.MODULE_MANAGER.getModule(NoBreakDelayModule.class)) != null && module.isToggled() && this.destroyDelay > module.delay.getValue().intValue()) {
            this.destroyDelay = module.delay.getValue().intValue();
        }
    }

    @Inject(method={"continueDestroyBlock"}, at={@At(value="HEAD")})
    private void onContinueDestroyBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        NoBreakDelayModule module;
        if (Night.MODULE_MANAGER != null && (module = Night.MODULE_MANAGER.getModule(NoBreakDelayModule.class)) != null && module.isToggled() && this.destroyDelay > module.delay.getValue().intValue()) {
            this.destroyDelay = module.delay.getValue().intValue();
        }
    }

    @Inject(method={"startDestroyBlock"}, at={@At(value="HEAD")}, cancellable=true)
    private void attackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> info) {
        NoBreakDelayModule module;
        if (Night.MODULE_MANAGER != null && (module = Night.MODULE_MANAGER.getModule(NoBreakDelayModule.class)) != null && module.isToggled() && this.destroyDelay > module.delay.getValue().intValue()) {
            this.destroyDelay = module.delay.getValue().intValue();
        }
        AttackBlockEvent event = new AttackBlockEvent(pos, direction);
        Night.EVENT_HANDLER.post(event);
        if (event.isCancelled()) {
            info.setReturnValue(false);
        }
    }

    @Inject(method={"useItemOn"}, at={@At(value="HEAD")}, cancellable=true)
    private void interactBlock(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> info) {
        NoInteractModule noInteractModule = Night.MODULE_MANAGER.getModule(NoInteractModule.class);
        if (noInteractModule.isToggled() && noInteractModule.shouldNoInteract() && noInteractModule.mode.getValue().equalsIgnoreCase("Disable") && WorldUtils.RIGHT_CLICKABLE_BLOCKS.contains(this.minecraft.level.getBlockState(hitResult.getBlockPos()).getBlock())) {
            info.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method={"useItemOn"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V")})
    private void interactBlock$BEFORE(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        NoInteractModule noInteractModule = Night.MODULE_MANAGER.getModule(NoInteractModule.class);
        if (!this.minecraft.player.isShiftKeyDown() && noInteractModule.isToggled() && noInteractModule.shouldNoInteract() && noInteractModule.mode.getValue().equalsIgnoreCase("Sneak") && WorldUtils.RIGHT_CLICKABLE_BLOCKS.contains(this.minecraft.level.getBlockState(hitResult.getBlockPos()).getBlock())) {
            this.minecraft.player.connection.send((Packet)new ServerboundPlayerInputPacket(new Input(false, false, false, false, false, true, false)));
        }
    }

    @Inject(method={"useItemOn"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V", shift=At.Shift.AFTER)})
    private void interactBlock$AFTER(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> info) {
        NoInteractModule noInteractModule = Night.MODULE_MANAGER.getModule(NoInteractModule.class);
        if (!this.minecraft.player.isShiftKeyDown() && noInteractModule.isToggled() && noInteractModule.shouldNoInteract() && noInteractModule.mode.getValue().equalsIgnoreCase("Sneak") && WorldUtils.RIGHT_CLICKABLE_BLOCKS.contains(this.minecraft.level.getBlockState(hitResult.getBlockPos()).getBlock())) {
            this.minecraft.player.connection.send((Packet)new ServerboundPlayerInputPacket(new Input(false, false, false, false, false, false, false)));
        }
    }

    @Inject(method={"attack"}, at={@At(value="HEAD")})
    private void attackEntity(Player player, Entity target, CallbackInfo ci) {
        Night.EVENT_HANDLER.post(new AttackEntityEvent(player, target));
    }

    @Inject(method={"destroyBlock"}, at={@At(value="HEAD")})
    private void breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Night.EVENT_HANDLER.post(new BreakBlockEvent(pos));
    }

    @Inject(method={"destroyBlock"}, at={@At(value="TAIL")})
    private void afterBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        NoBreakDelayModule module;
        if (Night.MODULE_MANAGER != null && (module = Night.MODULE_MANAGER.getModule(NoBreakDelayModule.class)) != null && module.isToggled() && this.destroyDelay > module.delay.getValue().intValue()) {
            this.destroyDelay = module.delay.getValue().intValue();
        }
    }

    @Inject(method={"handlePickItemFromBlock"}, at={@At(value="HEAD")}, cancellable=true)
    private void onPickBlock(BlockPos pos, boolean includeData, CallbackInfo info) {
        NoMiddleClickModule module;
        if (Night.MODULE_MANAGER != null && (module = Night.MODULE_MANAGER.getModule(NoMiddleClickModule.class)) != null && module.isToggled()) {
            info.cancel();
        }
    }

    @Inject(method={"handlePickItemFromEntity"}, at={@At(value="HEAD")}, cancellable=true)
    private void onPickEntity(Entity entity, boolean includeData, CallbackInfo info) {
        NoMiddleClickModule module;
        if (Night.MODULE_MANAGER != null && (module = Night.MODULE_MANAGER.getModule(NoMiddleClickModule.class)) != null && module.isToggled()) {
            info.cancel();
        }
    }
}

