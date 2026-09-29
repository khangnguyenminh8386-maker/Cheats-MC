/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.gui.navigation.ScreenRectangle
 *  net.minecraft.client.gui.render.TextureSetup
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.state.gui.GuiElementRenderState
 *  org.joml.Matrix3x2fc
 *  org.jspecify.annotations.Nullable
 */
package night.utils.graphics;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

public record GuiQuadRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2fc pose, float[] xs, float[] ys, int[] cols, float @Nullable [] us, float @Nullable [] vs, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds) implements GuiElementRenderState
{
    public GuiQuadRenderState(Matrix3x2fc pose, float[] xs, float[] ys, int[] cols, @Nullable ScreenRectangle scissorArea) {
        this(RenderPipelines.GUI, TextureSetup.noTexture(), pose, xs, ys, cols, null, null, scissorArea, GuiQuadRenderState.computeBounds(xs, ys, pose, scissorArea));
    }

    public GuiQuadRenderState(TextureSetup textureSetup, Matrix3x2fc pose, float[] xs, float[] ys, int[] cols, float[] us, float[] vs, @Nullable ScreenRectangle scissorArea) {
        this(RenderPipelines.GUI_TEXTURED, textureSetup, pose, xs, ys, cols, us, vs, scissorArea, GuiQuadRenderState.computeBounds(xs, ys, pose, scissorArea));
    }

    public GuiQuadRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2fc pose, float[] xs, float[] ys, int[] cols, float @Nullable [] us, float @Nullable [] vs, @Nullable ScreenRectangle scissorArea) {
        this(pipeline, textureSetup, pose, xs, ys, cols, us, vs, scissorArea, GuiQuadRenderState.computeBounds(xs, ys, pose, scissorArea));
    }

    public void buildVertices(VertexConsumer consumer) {
        if (this.us != null && this.vs != null) {
            for (int i = 0; i < this.xs.length; ++i) {
                consumer.addVertexWith2DPose(this.pose, this.xs[i], this.ys[i]).setUv(this.us[i], this.vs[i]).setColor(this.cols[i]);
            }
        } else {
            for (int i = 0; i < this.xs.length; ++i) {
                consumer.addVertexWith2DPose(this.pose, this.xs[i], this.ys[i]).setColor(this.cols[i]);
            }
        }
    }

    private static @Nullable ScreenRectangle computeBounds(float[] xs, float[] ys, Matrix3x2fc pose, @Nullable ScreenRectangle scissorArea) {
        if (xs.length == 0) {
            return scissorArea;
        }
        float minX = xs[0];
        float maxX = xs[0];
        float minY = ys[0];
        float maxY = ys[0];
        for (int i = 1; i < xs.length; ++i) {
            if (xs[i] < minX) {
                minX = xs[i];
            }
            if (xs[i] > maxX) {
                maxX = xs[i];
            }
            if (ys[i] < minY) {
                minY = ys[i];
            }
            if (!(ys[i] > maxY)) continue;
            maxY = ys[i];
        }
        int x0 = (int)Math.floor(minX);
        int y0 = (int)Math.floor(minY);
        int x1 = (int)Math.ceil(maxX);
        int y1 = (int)Math.ceil(maxY);
        ScreenRectangle bounds = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(bounds) : bounds;
    }
}

