/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.FramerateLimitTracker
 *  com.mojang.blaze3d.platform.FramerateLimitTracker$FramerateThrottleReason
 *  net.minecraft.client.Minecraft
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package night.mixins;

import com.mojang.blaze3d.platform.FramerateLimitTracker;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={FramerateLimitTracker.class})
public abstract class FramerateLimitTrackerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private int framerateLimit;

    @Inject(method={"getThrottleReason"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$preventThrottleInGui(CallbackInfoReturnable<FramerateLimitTracker.FramerateThrottleReason> cir) {
        if (this.minecraft != null && this.minecraft.getWindow() != null) {
            if (this.minecraft.getWindow().isIconified()) {
                cir.setReturnValue(FramerateLimitTracker.FramerateThrottleReason.WINDOW_ICONIFIED);
            } else {
                cir.setReturnValue(FramerateLimitTracker.FramerateThrottleReason.NONE);
            }
        }
    }
}

