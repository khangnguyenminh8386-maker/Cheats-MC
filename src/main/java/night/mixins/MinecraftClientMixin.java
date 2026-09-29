/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.Options
 *  net.minecraft.client.main.GameConfig
 *  net.minecraft.client.multiplayer.MultiPlayerGameMode
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import night.Night;
import night.events.impl.GameLoopEvent;
import night.events.impl.TickEvent;
import night.modules.api.IFreecamModule;
import night.modules.impl.combat.MainhandModule;
import night.modules.impl.core.NoMiddleClickModule;
import night.modules.impl.miscellaneous.AutoEscapeModule;
import night.modules.impl.player.FastPlaceModule;
import night.modules.impl.player.MultiTaskModule;
import night.utils.IMinecraft;
import night.utils.minecraft.WorldUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Minecraft.class})
public abstract class MinecraftClientMixin
implements IMinecraft {
    @Shadow
    @Nullable
    public LocalPlayer player;
    @Shadow
    private int rightClickDelay;
    @Shadow
    @Final
    public Options options;
    @Shadow
    @Nullable
    public HitResult hitResult;
    @Shadow
    @Nullable
    public Entity crosshairPickEntity;

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void init(GameConfig args, CallbackInfo info) {
        Night.onPostInitialize();
    }

    @Inject(method={"runTick"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/Minecraft;runAllTasks()V", shift=At.Shift.AFTER)})
    private void runTickHook(boolean tick, CallbackInfo info) {
        Night.EVENT_HANDLER.post(new GameLoopEvent());
    }

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    private void tick(CallbackInfo info) {
        Night.EVENT_HANDLER.post(new TickEvent());
    }

    @Inject(method={"tick"}, at={@At(value="TAIL")})
    private void tick$post(CallbackInfo info) {
        Night.EVENT_HANDLER.post(new TickEvent.Post());
    }

    @Inject(method={"startUseItem"}, at={@At(value="FIELD", target="Lnet/minecraft/client/Minecraft;rightClickDelay:I", shift=At.Shift.AFTER)})
    private void doItemUse(CallbackInfo info) {
        if (Night.MODULE_MANAGER != null && Night.MODULE_MANAGER.getModule(FastPlaceModule.class).isToggled() && Night.MODULE_MANAGER.getModule(FastPlaceModule.class).isValidItem(this.player.getMainHandItem().getItem())) {
            this.rightClickDelay = Night.MODULE_MANAGER.getModule(FastPlaceModule.class).ticks.getValue().intValue();
        }
    }

    @ModifyExpressionValue(method={"continueAttack"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z")})
    private boolean handleBlockBreaking(boolean original) {
        if (Night.MODULE_MANAGER != null && Night.MODULE_MANAGER.getModule(MultiTaskModule.class).isToggled()) {
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(method={"startUseItem"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;isDestroying()Z")})
    private boolean handleInputEvents(boolean original) {
        if (Night.MODULE_MANAGER != null && Night.MODULE_MANAGER.getModule(MultiTaskModule.class).isToggled()) {
            return false;
        }
        return original;
    }

    @Redirect(method={"handleKeybinds"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V"))
    private void night$dontReleaseFakedUse(MultiPlayerGameMode instance, Player releasedPlayer) {
        if (Night.MODULE_MANAGER.getModule(AutoEscapeModule.class).isEating()) {
            return;
        }
        instance.releaseUsingItem(releasedPlayer);
    }

    @Redirect(method={"handleKeybinds"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V"))
    private void night$redirectSetSelectedSlot(Inventory inventory, int slot) {
        MainhandModule mainhand;
        if (Night.MODULE_MANAGER != null && (mainhand = Night.MODULE_MANAGER.getModule(MainhandModule.class)) != null && mainhand.shouldLockSlot()) {
            return;
        }
        inventory.setSelectedSlot(slot);
    }

    @Inject(method={"pick"}, at={@At(value="HEAD")}, cancellable=true)
    private void pick(float partialTicks, CallbackInfo info) {
        if (MinecraftClientMixin.mc.level == null || this.player == null) {
            return;
        }
        IFreecamModule module = (IFreecamModule)((Object)Night.MODULE_MANAGER.getModule("Freecam"));
        if (module.isToggled()) {
            Entity entity;
            this.hitResult = WorldUtils.getRaytraceTarget(module.getFreeYaw(), module.getFreePitch(), module.getFreeX(), module.getFreeY(), module.getFreeZ());
            HitResult hitResult = this.hitResult;
            if (hitResult instanceof EntityHitResult) {
                EntityHitResult entityHitResult = (EntityHitResult)hitResult;
                entity = entityHitResult.getEntity();
            } else {
                entity = null;
            }
            this.crosshairPickEntity = entity;
            info.cancel();
        }
    }

    @Inject(method={"pickBlockOrEntity"}, at={@At(value="HEAD")}, cancellable=true, require=1)
    private void night$onPickBlock(CallbackInfo info) {
        NoMiddleClickModule module;
        if (Night.MODULE_MANAGER != null && (module = Night.MODULE_MANAGER.getModule(NoMiddleClickModule.class)) != null && module.isToggled()) {
            info.cancel();
        }
    }
}

