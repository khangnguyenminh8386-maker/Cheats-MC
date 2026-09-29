/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.ScreenEffectRenderer
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import night.Night;
import night.modules.impl.visuals.NoRenderModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ScreenEffectRenderer.class})
public class ScreenEffectRendererMixin {
    @Inject(method={"submitFire"}, at={@At(value="HEAD")}, cancellable=true)
    private static void night$renderFire(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, TextureAtlasSprite sprite, CallbackInfo ci) {
        NoRenderModule module = Night.MODULE_MANAGER.getModule(NoRenderModule.class);
        if (module.isToggled() && module.fireOverlay.getValue()) {
            ci.cancel();
        }
    }

    @Inject(method={"submitWater"}, at={@At(value="HEAD")}, cancellable=true)
    private static void night$renderWater(Minecraft minecraft, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CallbackInfo ci) {
        NoRenderModule module = Night.MODULE_MANAGER.getModule(NoRenderModule.class);
        if (module.isToggled() && module.liquidOverlay.getValue()) {
            ci.cancel();
        }
    }

    @Inject(method={"submitBlockSprite"}, at={@At(value="HEAD")}, cancellable=true)
    private static void night$renderTex(TextureAtlasSprite sprite, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int color, CallbackInfo ci) {
        NoRenderModule module = Night.MODULE_MANAGER.getModule(NoRenderModule.class);
        if (module.isToggled() && module.blockOverlay.getValue()) {
            ci.cancel();
        }
    }
}

