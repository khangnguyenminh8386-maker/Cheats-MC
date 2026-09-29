/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.LoadingOverlay
 *  net.minecraft.client.gui.screens.Overlay
 *  net.minecraft.server.packs.resources.ReloadInstance
 *  net.minecraft.util.Mth
 *  net.minecraft.util.Util
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import java.awt.Color;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import night.gui.ClickGuiScreen;
import night.utils.IMinecraft;
import night.utils.graphics.MenuShader;
import night.utils.graphics.Renderer2D;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LoadingOverlay.class})
public abstract class LoadingOverlayMixin
extends Overlay
implements IMinecraft {
    @Shadow
    @Final
    private ReloadInstance reload;
    @Shadow
    @Final
    private Consumer<Optional<Throwable>> onFinish;
    @Shadow
    @Final
    private boolean fadeIn;
    @Shadow
    private float currentProgress;
    @Shadow
    private long fadeOutStart;
    @Shadow
    private long fadeInStart;

    @Inject(method={"extractRenderState"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$renderLoadingScreen(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ci.cancel();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        long now = Util.getMillis();
        if (this.fadeIn && this.fadeInStart == -1L) {
            this.fadeInStart = now;
        }
        float fadeOut = this.fadeOutStart > -1L ? (float)(now - this.fadeOutStart) / 1000.0f : -1.0f;
        float fadeIn = this.fadeInStart > -1L ? (float)(now - this.fadeInStart) / 500.0f : -1.0f;
        float alpha = 1.0f;
        if (fadeOut >= 1.0f) {
            if (LoadingOverlayMixin.mc.gui.screen() != null) {
                LoadingOverlayMixin.mc.gui.screen().init(mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
            }
            try {
                this.reload.checkExceptions();
                this.onFinish.accept(Optional.empty());
            }
            catch (Throwable t) {
                this.onFinish.accept(Optional.of(t));
            }
            LoadingOverlayMixin.mc.gui.setOverlay(null);
            return;
        }
        if (fadeOut >= 0.0f) {
            alpha = 1.0f - Mth.clamp((float)fadeOut, (float)0.0f, (float)1.0f);
        } else if (this.fadeIn && fadeIn >= 0.0f) {
            alpha = Mth.clamp((float)fadeIn, (float)0.0f, (float)1.0f);
        }
        MenuShader.render(context, width, height, alpha, 0.0f, this.reload.isDone());
        float reloadProgress = this.reload.getActualProgress();
        this.currentProgress = Mth.clamp((float)(this.currentProgress * 0.95f + reloadProgress * 0.05f), (float)0.0f, (float)1.0f);
        int barWidth = Math.min(width - 40, 260);
        int barHeight = 4;
        int barX = width / 2 - barWidth / 2;
        int barY = height / 2 + 30;
        Renderer2D.renderQuad(context, barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, new Color(20, 20, 25, (int)(200.0f * alpha)));
        int fillWidth = (int)((float)barWidth * this.currentProgress);
        if (fillWidth > 0) {
            Color barColor = ClickGuiScreen.getButtonColor(0, (int)(240.0f * alpha));
            Renderer2D.renderQuad(context, barX, barY, barX + fillWidth, barY + barHeight, barColor);
        }
        if (this.reload.isDone() && this.fadeOutStart == -1L) {
            this.fadeOutStart = now;
        }
    }
}

