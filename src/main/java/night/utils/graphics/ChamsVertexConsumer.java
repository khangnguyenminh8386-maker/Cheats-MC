/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  lombok.Generated
 *  org.joml.Matrix4f
 */
package night.utils.graphics;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import lombok.Generated;
import night.utils.graphics.Glint;
import night.utils.graphics.Renderer3D;
import org.joml.Matrix4f;

public class ChamsVertexConsumer
implements VertexConsumer {
    private static final Matrix4f IDENTITY = new Matrix4f();
    private final VertexConsumer real;
    private final boolean fill;
    private final int fillColor;
    private final boolean outline;
    private final int outlineColor;
    private final boolean shine;
    private final boolean glint;
    private final int glintColor;
    private final Glint.GlintParams glintParams;
    private final boolean suppressReal;
    private final float realAlphaMultiplier;
    private final float[] xs = new float[4];
    private final float[] ys = new float[4];
    private final float[] zs = new float[4];
    private int i = 0;

    public ChamsVertexConsumer(VertexConsumer real, boolean fill, int fillColor, boolean outline, int outlineColor, boolean shine) {
        this(real, fill, fillColor, outline, outlineColor, shine, false, -1, null, false, 1.0f);
    }

    public ChamsVertexConsumer(VertexConsumer real, boolean fill, int fillColor, boolean outline, int outlineColor, boolean shine, boolean suppressReal) {
        this(real, fill, fillColor, outline, outlineColor, shine, false, -1, null, suppressReal, 1.0f);
    }

    public ChamsVertexConsumer(VertexConsumer real, boolean fill, int fillColor, boolean outline, int outlineColor, boolean shine, boolean suppressReal, float realAlphaMultiplier) {
        this(real, fill, fillColor, outline, outlineColor, shine, false, -1, null, suppressReal, realAlphaMultiplier);
    }

    public ChamsVertexConsumer(VertexConsumer real, boolean fill, int fillColor, boolean outline, int outlineColor, boolean shine, boolean glint, int glintColor, boolean suppressReal, float realAlphaMultiplier) {
        this(real, fill, fillColor, outline, outlineColor, shine, glint, glintColor, null, suppressReal, realAlphaMultiplier);
    }

    public ChamsVertexConsumer(VertexConsumer real, boolean fill, int fillColor, boolean outline, int outlineColor, boolean shine, boolean glint, int glintColor, Glint.GlintParams glintParams, boolean suppressReal, float realAlphaMultiplier) {
        this.real = real;
        this.fill = fill;
        this.fillColor = fillColor;
        this.outline = outline;
        this.outlineColor = outlineColor;
        this.shine = shine;
        this.glint = glint;
        this.glintColor = glintColor;
        this.glintParams = glintParams;
        this.suppressReal = suppressReal;
        this.realAlphaMultiplier = realAlphaMultiplier;
    }

    public VertexConsumer addVertex(float x, float y, float z) {
        if (!this.suppressReal && this.real != null) {
            this.real.addVertex(x, y, z);
        }
        this.xs[this.i] = x;
        this.ys[this.i] = y;
        this.zs[this.i] = z;
        ++this.i;
        if (this.i == 4) {
            this.flushQuad();
            this.i = 0;
        }
        return this;
    }

   private void flushQuad() {
      List<Renderer3D.VertexCollection> quads = this.shine ? Renderer3D.SHINE_QUADS : Renderer3D.QUADS;
      List<Renderer3D.VertexCollection> lines = this.shine ? Renderer3D.SHINE_DEBUG_LINES : Renderer3D.DEBUG_LINES;
      if (this.fill) {
         quads.add(
            new Renderer3D.VertexCollection(
               new Renderer3D.Vertex(IDENTITY, this.xs[0], this.ys[0], this.zs[0], this.fillColor),
               new Renderer3D.Vertex(IDENTITY, this.xs[1], this.ys[1], this.zs[1], this.fillColor),
               new Renderer3D.Vertex(IDENTITY, this.xs[2], this.ys[2], this.zs[2], this.fillColor),
               new Renderer3D.Vertex(IDENTITY, this.xs[3], this.ys[3], this.zs[3], this.fillColor)
            )
         );
      }

      if (this.outline) {
         lines.add(
            new Renderer3D.VertexCollection(
               new Renderer3D.Vertex(IDENTITY, this.xs[0], this.ys[0], this.zs[0], this.outlineColor),
               new Renderer3D.Vertex(IDENTITY, this.xs[1], this.ys[1], this.zs[1], this.outlineColor)
            )
         );
         lines.add(
            new Renderer3D.VertexCollection(
               new Renderer3D.Vertex(IDENTITY, this.xs[1], this.ys[1], this.zs[1], this.outlineColor),
               new Renderer3D.Vertex(IDENTITY, this.xs[2], this.ys[2], this.zs[2], this.outlineColor)
            )
         );
         lines.add(
            new Renderer3D.VertexCollection(
               new Renderer3D.Vertex(IDENTITY, this.xs[2], this.ys[2], this.zs[2], this.outlineColor),
               new Renderer3D.Vertex(IDENTITY, this.xs[3], this.ys[3], this.zs[3], this.outlineColor)
            )
         );
         lines.add(
            new Renderer3D.VertexCollection(
               new Renderer3D.Vertex(IDENTITY, this.xs[3], this.ys[3], this.zs[3], this.outlineColor),
               new Renderer3D.Vertex(IDENTITY, this.xs[0], this.ys[0], this.zs[0], this.outlineColor)
            )
         );
      }

      if (this.glint) {
         Glint.GlintParams params = this.glintParams != null ? this.glintParams : Glint.ENTITY;
         float[] uArr = new float[4];
         float[] vArr = new float[4];

         for (int vi = 0; vi < 4; vi++) {
            float[] uv = params.getGlintUV(this.xs[vi], this.ys[vi], this.zs[vi]);
            uArr[vi] = uv[0];
            vArr[vi] = uv[1];
         }

         Glint.QUADS
            .add(
               new Glint.TexturedVertexCollection(
                  new Glint.TexturedVertex(IDENTITY, this.xs[0], this.ys[0], this.zs[0], uArr[0], vArr[0], this.glintColor),
                  new Glint.TexturedVertex(IDENTITY, this.xs[1], this.ys[1], this.zs[1], uArr[1], vArr[1], this.glintColor),
                  new Glint.TexturedVertex(IDENTITY, this.xs[2], this.ys[2], this.zs[2], uArr[2], vArr[2], this.glintColor),
                  new Glint.TexturedVertex(IDENTITY, this.xs[3], this.ys[3], this.zs[3], uArr[3], vArr[3], this.glintColor)
               )
            );
      }
   }

    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        if (!this.suppressReal && this.real != null) {
            int scaledAlpha = this.realAlphaMultiplier >= 1.0f ? alpha : Math.round((float)alpha * this.realAlphaMultiplier);
            this.real.setColor(red, green, blue, scaledAlpha);
        }
        return this;
    }

    public VertexConsumer setColor(int argb) {
        if (!this.suppressReal && this.real != null) {
            if (this.realAlphaMultiplier >= 1.0f) {
                this.real.setColor(argb);
            } else {
                int alpha = argb >>> 24 & 0xFF;
                int scaledAlpha = Math.round((float)alpha * this.realAlphaMultiplier);
                this.real.setColor(argb & 0xFFFFFF | scaledAlpha << 24);
            }
        }
        return this;
    }

    public VertexConsumer setUv(float u, float v) {
        if (!this.suppressReal && this.real != null) {
            this.real.setUv(u, v);
        }
        return this;
    }

    public VertexConsumer setUv1(int u, int v) {
        if (!this.suppressReal && this.real != null) {
            this.real.setUv1(u, v);
        }
        return this;
    }

    public VertexConsumer setUv2(int u, int v) {
        if (!this.suppressReal && this.real != null) {
            this.real.setUv2(u, v);
        }
        return this;
    }

    public VertexConsumer setNormal(float x, float y, float z) {
        if (!this.suppressReal && this.real != null) {
            this.real.setNormal(x, y, z);
        }
        return this;
    }

    public VertexConsumer setLineWidth(float width) {
        if (!this.suppressReal && this.real != null) {
            this.real.setLineWidth(width);
        }
        return this;
    }

    @Generated
    public VertexConsumer getReal() {
        return this.real;
    }
}

