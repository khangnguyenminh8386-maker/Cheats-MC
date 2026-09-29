/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.player.LocalPlayer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package night.mixins;

import net.minecraft.client.player.LocalPlayer;
import night.Night;
import night.mixins.accessors.EntityAccessor;
import night.modules.impl.player.RotationLockModule;
import night.utils.IMinecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LocalPlayer.class})
public abstract class LocalPlayerMixin
implements IMinecraft {
    @Inject(method={"getViewYRot"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$unlockViewYaw(float partialTick, CallbackInfoReturnable<Float> info) {
        if (((LocalPlayer)(Object)this).isPassenger()) {
            return;
        }
        RotationLockModule lock = Night.MODULE_MANAGER.getModule(RotationLockModule.class);
        if (lock.isToggled() && lock.custom.getValue() && (lock.mode.getValue().equals("Yaw") || lock.mode.getValue().equals("Both"))) {
            info.setReturnValue(Float.valueOf(((EntityAccessor)(this)).getRawYRot()));
        }
    }

    @Inject(method={"getViewXRot"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$unlockViewPitch(float partialTick, CallbackInfoReturnable<Float> info) {
        RotationLockModule lock = Night.MODULE_MANAGER.getModule(RotationLockModule.class);
        if (lock.isToggled() && lock.custom.getValue() && (lock.mode.getValue().equals("Pitch") || lock.mode.getValue().equals("Both"))) {
            info.setReturnValue(Float.valueOf(((EntityAccessor)(this)).getRawXRot()));
        }
    }
}

