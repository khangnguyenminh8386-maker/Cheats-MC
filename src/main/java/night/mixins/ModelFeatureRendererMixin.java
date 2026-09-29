/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.model.Model
 *  net.minecraft.client.renderer.feature.ModelFeatureRenderer
 *  net.minecraft.client.renderer.feature.ModelFeatureRenderer$Submit
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package night.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import night.utils.graphics.ChamsVertexConsumer;
import night.utils.mixins.IChamsCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={ModelFeatureRenderer.class})
public class ModelFeatureRendererMixin {
    @Redirect(method={"prepareModel"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"))
    private <S> void night$captureChams(Model<S> model, PoseStack pose, VertexConsumer consumer, int light, int overlay, int tint, ModelFeatureRenderer.Submit<S> submit) {
        IChamsCapture capture;
        Object object = submit.state();
        if (object instanceof IChamsCapture && ((capture = (IChamsCapture)object).night$chamsFill() || capture.night$chamsOutline() || capture.night$chamsGlint() || capture.night$chamsRealAlpha() < 1.0f)) {
            int scaledTint = tint;
            float realAlpha = capture.night$chamsRealAlpha();
            if (realAlpha < 1.0f) {
                int tintAlpha = tint >>> 24 & 0xFF;
                int scaledAlpha = Math.round((float)tintAlpha * realAlpha);
                scaledTint = tint & 0xFFFFFF | scaledAlpha << 24;
            }
            model.renderToBuffer(pose, (VertexConsumer)new ChamsVertexConsumer(consumer, capture.night$chamsFill(), capture.night$chamsFillColor(), capture.night$chamsOutline(), capture.night$chamsOutlineColor(), capture.night$chamsShine(), capture.night$chamsGlint(), capture.night$chamsGlintColor(), capture.night$chamsGlintParams(), capture.night$chamsSuppressReal(), realAlpha), light, overlay, scaledTint);
        } else {
            model.renderToBuffer(pose, consumer, light, overlay, tint);
        }
    }
}

