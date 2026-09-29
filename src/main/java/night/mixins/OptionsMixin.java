/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.CameraType
 *  net.minecraft.client.Options
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import net.minecraft.client.CameraType;
import net.minecraft.client.Options;
import night.Night;
import night.modules.api.IFreecamModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Options.class})
public abstract class OptionsMixin {
    @Inject(method={"setCameraType"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$blockThirdPersonOnFreecam(CameraType cameraType, CallbackInfo ci) {
        IFreecamModule freecam;
        if (Night.MODULE_MANAGER != null && (freecam = (IFreecamModule)((Object)Night.MODULE_MANAGER.getModule("Freecam"))) != null && freecam.isToggled() && !cameraType.isFirstPerson()) {
            ci.cancel();
        }
    }
}

