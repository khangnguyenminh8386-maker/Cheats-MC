/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderPass$RenderArea
 *  com.mojang.blaze3d.systems.RenderPassBackend
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderPassBackend;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={RenderPass.class})
public abstract class RenderPassMixin {
    @Shadow
    @Final
    private RenderPass.RenderArea renderArea;
    @Shadow
    @Final
    private RenderPassBackend backend;

    @Shadow
    public abstract void disableScissor();

    @Inject(method={"enableScissor"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$clampScissorToBounds(int x, int y, int width, int height, CallbackInfo ci) {
        if (this.renderArea == null) {
            return;
        }
        int areaX = this.renderArea.x();
        int areaY = this.renderArea.y();
        int areaWidth = this.renderArea.width();
        int areaHeight = this.renderArea.height();
        int x1 = x;
        int y1 = y;
        int x2 = x + width;
        int y2 = y + height;
        if (x1 < areaX) {
            x1 = areaX;
        }
        if (y1 < areaY) {
            y1 = areaY;
        }
        if (x2 > areaX + areaWidth) {
            x2 = areaX + areaWidth;
        }
        if (y2 > areaY + areaHeight) {
            y2 = areaY + areaHeight;
        }
        int clampedWidth = x2 - x1;
        int clampedHeight = y2 - y1;
        if (clampedWidth <= 0 || clampedHeight <= 0) {
            this.disableScissor();
            ci.cancel();
            return;
        }
        if (x1 != x || y1 != y || clampedWidth != width || clampedHeight != height) {
            this.backend.enableScissor(x1, y1, clampedWidth, clampedHeight);
            ci.cancel();
        }
    }
}

