/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.client.renderer.SubmitNodeStorage
 *  net.minecraft.client.renderer.feature.FeatureRenderDispatcher
 *  net.minecraft.client.renderer.feature.FeatureRenderDispatcher$PreparedFrame
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={FeatureRenderDispatcher.class})
public class FeatureRenderDispatcherMixin {
    @Inject(method={"renderAllFeatures"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;executeTranslucentAfterTerrain()V")})
    private void night$executeOutline(SubmitNodeStorage storage, CallbackInfo ci, @Local FeatureRenderDispatcher.PreparedFrame frame) {
        frame.executeOutline();
    }
}

