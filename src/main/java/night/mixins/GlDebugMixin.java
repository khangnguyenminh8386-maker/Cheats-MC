/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.opengl.GlDebug
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import com.mojang.blaze3d.opengl.GlDebug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={GlDebug.class})
public class GlDebugMixin {
    @Inject(method={"printDebugLog"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$silenceFramebufferSpam(int source, int type, int id, int severity, int length, long message, long userParam, CallbackInfo ci) {
        if (id == 1286) {
            ci.cancel();
        }
    }
}

