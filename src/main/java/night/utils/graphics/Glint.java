/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.PrimitiveTopology
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.ColorTargetState
 *  com.mojang.blaze3d.pipeline.DepthStencilState
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.BlendFactor
 *  com.mojang.blaze3d.platform.CompareOp
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.ByteBufferBuilder
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.MeshData
 *  net.minecraft.client.renderer.rendertype.RenderSetup
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.resources.Identifier
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package night.utils.graphics;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import night.mixins.accessors.RenderPipelinesAccessor;
import night.utils.graphics.Renderer3D;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public class Glint {
    private static final Identifier GLINT_TEXTURE = Identifier.fromNamespaceAndPath((String)"night", (String)"textures/glint.png");
    private static final RenderType NO_DEPTH_GLINT_QUADS = RenderType.create((String)"night_no_depth_glint", (RenderSetup)RenderSetup.builder((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getGuiTexturedSnippet()}).withLocation("night/no_depth_glint").withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE))).build()).withTexture("Sampler0", GLINT_TEXTURE).sortOnUpload().createRenderSetup());
    public static List<TexturedVertexCollection> QUADS = new ArrayList<TexturedVertexCollection>();
    public static final GlintParams ENTITY = new GlintParams();
    public static final GlintParams CRYSTAL = new GlintParams();
    private static long lastMs = 0L;

    public static void tick() {
        long nowMs = System.currentTimeMillis();
        if (lastMs == 0L) {
            lastMs = nowMs;
        }
        float delta = (float)(nowMs - lastMs) / 1000.0f;
        lastMs = nowMs;
        ENTITY.tick(delta);
        CRYSTAL.tick(delta);
    }

    public static void draw() {
        if (QUADS.isEmpty()) {
            return;
        }
        int vertexCount = 0;
        for (TexturedVertexCollection collection : QUADS) {
            vertexCount += collection.vertices().length;
        }
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(vertexCount * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize()));){
            BufferBuilder buffer = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            for (TexturedVertexCollection collection : QUADS) {
                collection.vertex(buffer);
            }
            MeshData mesh = buffer.build();
            Renderer3D.draw(NO_DEPTH_GLINT_QUADS, mesh);
        }
        QUADS.clear();
    }

    public static class GlintParams {
        public float speedU = 0.25f;
        public float speedV = 0.1f;
        public float scale = 1.0f;
        public String direction = "Diagonal";
        public float scrollU = 0.0f;
        public float scrollV = 0.0f;

        public void tick(float delta) {
            this.scrollU = (this.scrollU + this.speedU * delta) % 1.0f;
            this.scrollV = (this.scrollV + this.speedV * delta) % 1.0f;
            if (this.scrollU < 0.0f) {
                this.scrollU += 1.0f;
            }
            if (this.scrollV < 0.0f) {
                this.scrollV += 1.0f;
            }
        }

      public float[] getGlintUV(float x, float y, float z) {
         float s = this.scale * 0.06F;
         float u;
         float v;
         switch (this.direction) {
            case "Horizontal":
               u = (x + z * 0.5F) * s;
               v = y * s;
               break;
            case "Vertical":
               u = y * s;
               v = (x + z * 0.5F) * s;
               break;
            default:
               u = x * s + z * s * 0.5F;
               v = y * s + z * s * 0.5F;
         }

         u += this.scrollU;
         v += this.scrollV;
         return new float[]{u, v};
      }
    }

    public record TexturedVertexCollection(TexturedVertex... vertices) {
        public void vertex(BufferBuilder buffer) {
            for (TexturedVertex vertex : this.vertices) {
                buffer.addVertex((Matrix4fc)vertex.matrix, vertex.x, vertex.y, vertex.z).setUv(vertex.u, vertex.v).setColor(vertex.color);
            }
        }
    }

    public record TexturedVertex(Matrix4f matrix, float x, float y, float z, float u, float v, int color) {
    }
}

