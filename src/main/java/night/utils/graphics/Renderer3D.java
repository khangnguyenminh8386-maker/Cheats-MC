/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.IndexType
 *  com.mojang.blaze3d.PrimitiveTopology
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.ColorTargetState
 *  com.mojang.blaze3d.pipeline.DepthStencilState
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Builder
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.BlendFactor
 *  com.mojang.blaze3d.platform.CompareOp
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.systems.RenderSystem$AutoStorageIndexBuffer
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.ByteBufferBuilder
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.MeshData
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.QuadInstance
 *  it.unimi.dsi.fastutil.ints.IntList
 *  net.minecraft.client.renderer.fog.FogRenderer
 *  net.minecraft.client.renderer.fog.FogRenderer$FogMode
 *  net.minecraft.client.renderer.item.ItemStackRenderState
 *  net.minecraft.client.renderer.item.ItemStackRenderState$FoilType
 *  net.minecraft.client.renderer.rendertype.OutputTarget
 *  net.minecraft.client.renderer.rendertype.RenderSetup
 *  net.minecraft.client.renderer.rendertype.RenderSetup$OutlineProperty
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.client.renderer.rendertype.RenderTypes
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.resources.model.geometry.BakedQuad
 *  net.minecraft.client.resources.model.geometry.BakedQuad$MaterialInfo
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.ItemOwner
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 */
package night.utils.graphics;
import com.mojang.blaze3d.pipeline.RenderPipeline.Builder;
import com.mojang.blaze3d.systems.RenderSystem.AutoStorageIndexBuffer;
import java.util.Map.Entry;
import net.minecraft.client.renderer.fog.FogRenderer.FogMode;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.rendertype.RenderSetup.OutlineProperty;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;


import com.mojang.blaze3d.IndexType;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import it.unimi.dsi.fastutil.ints.IntList;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.mixins.accessors.GameRendererAccessor;
import night.mixins.accessors.ItemStackRenderStateAccessor;
import night.mixins.accessors.LayerRenderStateAccessor;
import night.mixins.accessors.RenderPipelinesAccessor;
import night.utils.IMinecraft;
import night.utils.color.ColorUtils;
import night.utils.graphics.Glint;
import night.utils.minecraft.EntityUtils;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;

public class Renderer3D
implements IMinecraft {
    public static boolean RENDERING = false;
    private static final RenderType NO_DEPTH_QUADS = RenderType.create((String)"night_no_depth_quads", (RenderSetup)RenderSetup.builder((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getDebugFilledSnippet()}).withLocation("night/no_depth_quads").withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).build()).sortOnUpload().createRenderSetup());
    private static final RenderType NO_DEPTH_LINES = RenderType.create((String)"night_no_depth_lines", (RenderSetup)RenderSetup.builder((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getLinesSnippet()}).withLocation("night/no_depth_lines").withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA))).build()).createRenderSetup());
    private static final BlendFunction SHINE_BLEND = new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_CONSTANT_ALPHA);
    private static final RenderType NO_DEPTH_QUADS_SHINE = RenderType.create((String)"night_no_depth_quads_shine", (RenderSetup)RenderSetup.builder((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getDebugFilledSnippet()}).withLocation("night/no_depth_quads_shine").withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(new ColorTargetState(SHINE_BLEND)).build()).sortOnUpload().createRenderSetup());
    private static final RenderType NO_DEPTH_LINES_SHINE = RenderType.create((String)"night_no_depth_lines_shine", (RenderSetup)RenderSetup.builder((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getLinesSnippet()}).withLocation("night/no_depth_lines_shine").withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(new ColorTargetState(SHINE_BLEND)).build()).createRenderSetup());
    public static List<VertexCollection> QUADS = new ArrayList<VertexCollection>();
    public static List<VertexCollection> DEBUG_LINES = new ArrayList<VertexCollection>();
    public static List<VertexCollection> SHINE_QUADS = new ArrayList<VertexCollection>();
    public static List<VertexCollection> SHINE_DEBUG_LINES = new ArrayList<VertexCollection>();
    public static List<VertexCollection> SHADER_QUADS = new ArrayList<VertexCollection>();
    public static List<VertexCollection> SHADER_DEBUG_LINES = new ArrayList<VertexCollection>();
    public static List<VertexCollection> TRACER_DEBUG_LINES = new ArrayList<VertexCollection>();
    private static final QuadInstance QUAD_INSTANCE = new QuadInstance();
    private static final ItemStackRenderState ITEM_RENDER_STATE = new ItemStackRenderState();
    private static final Map<Identifier, RenderType> NO_DEPTH_ITEM_CUTOUT = new HashMap<Identifier, RenderType>();
    private static final Map<Identifier, RenderType> NO_DEPTH_ITEM_TRANSLUCENT = new HashMap<Identifier, RenderType>();
    private static final Map<Identifier, RenderType> NO_DEPTH_TEXTURE_TYPES = new HashMap<Identifier, RenderType>();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
   public static void draw(RenderType renderType, MeshData mesh) {
      if (mesh != null) {
         if (mesh.drawState().indexCount() == 0) {
            mesh.close();
         } else {
            MeshData var2 = mesh;

            try {
               GpuBuffer vertices = RenderSystem.getDevice().createBuffer(() -> "Cheats MC immediate vertices", 32, mesh.vertexBuffer());
               GpuBuffer ownedIndices = null;
               GpuBuffer indices;
               IndexType indexType;
               if (mesh.indexBuffer() == null) {
                  RenderSystem.AutoStorageIndexBuffer autoIndices = RenderSystem.getSequentialBuffer(mesh.drawState().primitiveTopology());
                  indices = autoIndices.getBuffer(mesh.drawState().indexCount());
                  indexType = autoIndices.type();
               } else {
                  indices = ownedIndices = RenderSystem.getDevice().createBuffer(() -> "Cheats MC immediate indices", 64, mesh.indexBuffer());
                  indexType = mesh.drawState().indexType();
               }

               try {
                  renderType.prepare().drawFromBuffer(vertices, indices, indexType, 0, 0, mesh.drawState().indexCount());
               } catch (Exception var14) {
               } finally {
                  vertices.close();
                  if (ownedIndices != null) {
                     ownedIndices.close();
                  }
               }
            } catch (Throwable var16) {
               if (mesh != null) {
                  try {
                     var2.close();
                  } catch (Throwable var13) {
                     var16.addSuppressed(var13);
                  }
               }

               throw var16;
            }

            if (mesh != null) {
               mesh.close();
            }
         }
      }
   }

    public static void renderBox(PoseStack matrices, AABB box, Color color) {
        Renderer3D.renderGradientBox(matrices, box, color, color);
    }

    public static void renderGradientBox(PoseStack matrices, AABB box, Color startColor, Color endColor) {
        Renderer3D.renderGradientBox(QUADS, matrices, box, startColor, endColor);
    }

    public static void renderGradientBox(List<VertexCollection> QUADS, PoseStack matrices, AABB box, Color startColor, Color endColor) {
        if (!RENDERING) {
            return;
        }
        if (!Renderer3D.isFrustumVisible(box)) {
            return;
        }
        Matrix4f matrix = matrices.last().pose();
        box = Renderer3D.cameraTransform(box);
        QUADS.add(new VertexCollection(new Vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ, startColor.getRGB()), new Vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ, startColor.getRGB()), new Vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ, startColor.getRGB()), new Vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ, startColor.getRGB())));
        QUADS.add(new VertexCollection(new Vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ, endColor.getRGB()), new Vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ, endColor.getRGB()), new Vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ, startColor.getRGB()), new Vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ, startColor.getRGB())));
        QUADS.add(new VertexCollection(new Vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ, startColor.getRGB()), new Vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ, endColor.getRGB()), new Vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ, endColor.getRGB()), new Vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ, startColor.getRGB())));
        QUADS.add(new VertexCollection(new Vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ, startColor.getRGB()), new Vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ, endColor.getRGB()), new Vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ, endColor.getRGB()), new Vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ, startColor.getRGB())));
        QUADS.add(new VertexCollection(new Vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ, startColor.getRGB()), new Vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ, endColor.getRGB()), new Vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ, endColor.getRGB()), new Vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ, startColor.getRGB())));
        QUADS.add(new VertexCollection(new Vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ, endColor.getRGB()), new Vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ, endColor.getRGB()), new Vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ, endColor.getRGB()), new Vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ, endColor.getRGB())));
    }

    public static void renderBoxOutline(PoseStack matrices, AABB box, Color color) {
        Renderer3D.renderGradientBoxOutline(matrices, box, color, color);
    }

    public static void renderGradientBoxOutline(PoseStack matrices, AABB box, Color startColor, Color endColor) {
        Renderer3D.renderGradientBoxOutline(DEBUG_LINES, matrices, box, startColor, endColor);
    }

    public static void renderGradientBoxOutline(List<VertexCollection> DEBUG_LINES, PoseStack matrices, AABB box, Color startColor, Color endColor) {
        if (!RENDERING) {
            return;
        }
        if (!Renderer3D.isFrustumVisible(box)) {
            return;
        }
        Matrix4f matrix = matrices.last().pose();
        box = Renderer3D.cameraTransform(box);
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ, endColor.getRGB()), new Vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ, endColor.getRGB()), new Vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ, endColor.getRGB()), new Vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ, endColor.getRGB())));
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ, endColor.getRGB()), new Vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ, endColor.getRGB()), new Vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ, endColor.getRGB()), new Vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ, endColor.getRGB())));
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ, startColor.getRGB()), new Vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ, startColor.getRGB()), new Vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ, startColor.getRGB()), new Vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ, startColor.getRGB())));
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ, startColor.getRGB()), new Vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ, startColor.getRGB()), new Vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ, startColor.getRGB()), new Vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ, startColor.getRGB())));
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ, endColor.getRGB()), new Vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ, startColor.getRGB()), new Vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ, endColor.getRGB()), new Vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ, startColor.getRGB())));
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ, endColor.getRGB()), new Vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ, startColor.getRGB()), new Vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ, endColor.getRGB()), new Vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ, startColor.getRGB())));
    }

    public static void renderTargetCircle(PoseStack matrices, Entity target, float tickDelta, Color color) {
        Renderer3D.renderTargetCircle(SHINE_QUADS, SHINE_DEBUG_LINES, matrices, target, tickDelta, color);
    }

    public static void renderTargetCircle(List<VertexCollection> quadList, List<VertexCollection> lineList, PoseStack matrices, Entity target, float tickDelta, Color color) {
        if (!RENDERING || target == null || Renderer3D.mc.player == null) {
            return;
        }
        Vec3 pos = EntityUtils.getRenderPos(target, tickDelta);
        double height = Math.max(0.5, target.getBoundingBox().getYsize());
        double radius = Math.max(0.4, (double)target.getBbWidth() / 2.0 + 0.25);
        AABB boundBox = new AABB(pos.x - radius, pos.y - 0.5, pos.z - radius, pos.x + radius, pos.y + height + 0.5, pos.z + radius);
        if (!Renderer3D.isFrustumVisible(boundBox)) {
            return;
        }
        Matrix4f matrix = matrices.last().pose();
        double time = (double)System.currentTimeMillis() / 420.0;
        double sinVal = Math.sin(time);
        double cosVal = Math.cos(time);
        double progress = (sinVal + 1.0) / 2.0;
        double ringY = pos.y + progress * height;
        double trailOffset = -cosVal * 0.225;
        double trailY = ringY + trailOffset;
        float alphaFactor = (float)color.getAlpha() / 255.0f;
        if (alphaFactor <= 0.001f) {
            return;
        }
        int ringAlpha = (int)(180.0f * alphaFactor);
        Color ringColor = ColorUtils.getColor(color, ringAlpha);
        Color trailColor = ColorUtils.getColor(color, 0);
        Color lineRingColor = ColorUtils.getColor(color, (int)(255.0f * alphaFactor));
        int segments = 48;
        for (int i = 0; i < segments; ++i) {
            double angle1 = (double)i * 2.0 * Math.PI / (double)segments;
            double angle2 = (double)(i + 1) * 2.0 * Math.PI / (double)segments;
            double x1 = pos.x + Math.cos(angle1) * radius;
            double z1 = pos.z + Math.sin(angle1) * radius;
            double x2 = pos.x + Math.cos(angle2) * radius;
            double z2 = pos.z + Math.sin(angle2) * radius;
            Vec3 p1_ring = Renderer3D.cameraTransform(new Vec3(x1, ringY, z1));
            Vec3 p2_ring = Renderer3D.cameraTransform(new Vec3(x2, ringY, z2));
            Vec3 p2_trail = Renderer3D.cameraTransform(new Vec3(x2, trailY, z2));
            Vec3 p1_trail = Renderer3D.cameraTransform(new Vec3(x1, trailY, z1));
            quadList.add(new VertexCollection(new Vertex(matrix, (float)p1_ring.x, (float)p1_ring.y, (float)p1_ring.z, ringColor.getRGB()), new Vertex(matrix, (float)p2_ring.x, (float)p2_ring.y, (float)p2_ring.z, ringColor.getRGB()), new Vertex(matrix, (float)p2_trail.x, (float)p2_trail.y, (float)p2_trail.z, trailColor.getRGB()), new Vertex(matrix, (float)p1_trail.x, (float)p1_trail.y, (float)p1_trail.z, trailColor.getRGB())));
            quadList.add(new VertexCollection(new Vertex(matrix, (float)p1_trail.x, (float)p1_trail.y, (float)p1_trail.z, trailColor.getRGB()), new Vertex(matrix, (float)p2_trail.x, (float)p2_trail.y, (float)p2_trail.z, trailColor.getRGB()), new Vertex(matrix, (float)p2_ring.x, (float)p2_ring.y, (float)p2_ring.z, ringColor.getRGB()), new Vertex(matrix, (float)p1_ring.x, (float)p1_ring.y, (float)p1_ring.z, ringColor.getRGB())));
            lineList.add(new VertexCollection(new Vertex(matrix, (float)p1_ring.x, (float)p1_ring.y, (float)p1_ring.z, lineRingColor.getRGB(), 2.0f), new Vertex(matrix, (float)p2_ring.x, (float)p2_ring.y, (float)p2_ring.z, lineRingColor.getRGB(), 2.0f)));
        }
    }

    public static void renderBoxWireframe(PoseStack matrices, AABB box, Color color) {
        Renderer3D.renderBoxWireframe(DEBUG_LINES, matrices, box, color, color);
    }

    public static void renderBoxWireframe(PoseStack matrices, AABB box, Color startColor, Color endColor) {
        Renderer3D.renderBoxWireframe(DEBUG_LINES, matrices, box, startColor, endColor);
    }

    public static void renderBoxWireframe(List<VertexCollection> DEBUG_LINES, PoseStack matrices, AABB box, Color startColor, Color endColor) {
        if (!RENDERING) {
            return;
        }
        if (!Renderer3D.isFrustumVisible(box)) {
            return;
        }
        Matrix4f matrix = matrices.last().pose();
        box = Renderer3D.cameraTransform(box);
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ, endColor.getRGB()), new Vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ, endColor.getRGB())));
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ, endColor.getRGB()), new Vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ, endColor.getRGB())));
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ, startColor.getRGB()), new Vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ, startColor.getRGB())));
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ, startColor.getRGB()), new Vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ, startColor.getRGB())));
    }

    public static void renderQuad(PoseStack matrices, float left, float top, float right, float bottom, Color color) {
        if (!RENDERING) {
            return;
        }
        Matrix4f matrix = matrices.last().pose();
        int rgb = color.getRGB();
        QUADS.add(new VertexCollection(new Vertex(matrix, left, top, 0.0f, rgb), new Vertex(matrix, left, bottom, 0.0f, rgb), new Vertex(matrix, right, bottom, 0.0f, rgb), new Vertex(matrix, right, top, 0.0f, rgb)));
    }

    public static void renderOutline(PoseStack matrices, float left, float top, float right, float bottom, Color color) {
        if (!RENDERING) {
            return;
        }
        Matrix4f matrix = matrices.last().pose();
        int rgb = color.getRGB();
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix, left, top, 0.0f, rgb), new Vertex(matrix, right, top, 0.0f, rgb)));
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix, right, top, 0.0f, rgb), new Vertex(matrix, right, bottom, 0.0f, rgb)));
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix, right, bottom, 0.0f, rgb), new Vertex(matrix, left, bottom, 0.0f, rgb)));
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix, left, bottom, 0.0f, rgb), new Vertex(matrix, left, top, 0.0f, rgb)));
    }

    public static void renderLine(PoseStack matrices, Vec3 from, Vec3 to, Color color) {
        Renderer3D.renderLine(DEBUG_LINES, matrices, from, to, color);
    }

    public static void renderLine(List<VertexCollection> DEBUG_LINES, PoseStack matrices, Vec3 from, Vec3 to, Color color) {
        Renderer3D.renderLine(DEBUG_LINES, matrices, from, to, color, color);
    }

    public static void renderLine(PoseStack matrices, Vec3 from, Vec3 to, Color color1, Color color2) {
        Renderer3D.renderLine(DEBUG_LINES, matrices, from, to, color1, color2);
    }

   public static void renderLine(List<Renderer3D.VertexCollection> DEBUG_LINES, PoseStack matrices, Vec3 from, Vec3 to, Color color1, Color color2) {
      if (RENDERING) {
         if (mc.gameRenderer != null && mc.gameRenderer.mainCamera() != null) {
            Vec3 camPos = mc.gameRenderer.mainCamera().position();
            Vec3 forward = Vec3.directionFromRotation(mc.gameRenderer.mainCamera().xRot(), mc.gameRenderer.mainCamera().yRot());
            double dA = (from.x - camPos.x) * forward.x + (from.y - camPos.y) * forward.y + (from.z - camPos.z) * forward.z;
            double dB = (to.x - camPos.x) * forward.x + (to.y - camPos.y) * forward.y + (to.z - camPos.z) * forward.z;
            double nearPlane = 0.2;
            if (!(dA < nearPlane) || !(dB < nearPlane)) {
               if (dA >= nearPlane && dB < nearPlane) {
                  double t = (dA - nearPlane) / (dA - dB);
                  to = new Vec3(from.x + (to.x - from.x) * t, from.y + (to.y - from.y) * t, from.z + (to.z - from.z) * t);
                  if (!color1.equals(color2)) {
                     color2 = ColorUtils.getColor(color2, (int)(color2.getAlpha() * (1.0 - t) + color1.getAlpha() * t));
                  }
               } else if (dA < nearPlane && dB >= nearPlane) {
                  double t = (nearPlane - dA) / (dB - dA);
                  from = new Vec3(from.x + (to.x - from.x) * t, from.y + (to.y - from.y) * t, from.z + (to.z - from.z) * t);
                  if (!color1.equals(color2)) {
                     color1 = ColorUtils.getColor(color1, (int)(color1.getAlpha() * (1.0 - t) + color2.getAlpha() * t));
                  }
               }

               Matrix4f matrix = matrices.last().pose();
               from = cameraTransform(from);
               to = cameraTransform(to);
               DEBUG_LINES.add(
                  new Renderer3D.VertexCollection(
                     new Renderer3D.Vertex(matrix, (float)from.x, (float)from.y, (float)from.z, color1.getRGB()),
                     new Renderer3D.Vertex(matrix, (float)to.x, (float)to.y, (float)to.z, color2.getRGB())
                  )
               );
            }
         }
      }
   }

    private static RenderType noDepthItemRenderType(BakedQuad.MaterialInfo material) {
        boolean translucent = material.layer().translucent();
        Identifier texture = material.sprite().atlasLocation();
        Map<Identifier, RenderType> cache = translucent ? NO_DEPTH_ITEM_TRANSLUCENT : NO_DEPTH_ITEM_CUTOUT;
        return cache.computeIfAbsent(texture, t -> {
            RenderPipeline.Builder pipelineBuilder = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getItemSnippet()}).withLocation(translucent ? "night/no_depth_item_translucent" : "night/no_depth_item_cutout").withShaderDefine("ALPHA_CUTOUT", 0.1f).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false));
            if (translucent) {
                pipelineBuilder.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT));
            }
            return RenderType.create((String)(translucent ? "night_no_depth_item_translucent" : "night_no_depth_item_cutout"), (RenderSetup)RenderSetup.builder((RenderPipeline)pipelineBuilder.build()).withTexture("Sampler0", t).useOverlay().useLightmap().affectsCrumbling().setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).createRenderSetup());
        });
    }

    private static BufferBuilder itemBuffer(Map<RenderType, ByteBufferBuilder> owners, Map<RenderType, BufferBuilder> buffers, RenderType renderType) {
        return buffers.computeIfAbsent(renderType, rt -> {
            ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(256);
            owners.put((RenderType)rt, byteBufferBuilder);
            return new BufferBuilder(byteBufferBuilder, rt.primitiveTopology(), rt.format());
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void renderItem(PoseStack matrices, ItemStack stack, ItemOwner owner) {
        if (!RENDERING || stack.isEmpty()) {
            return;
        }
        ITEM_RENDER_STATE.clear();
        mc.getItemModelResolver().updateForTopItem(ITEM_RENDER_STATE, stack, ItemDisplayContext.GUI, (Level)Renderer3D.mc.level, owner, 0);
        ItemStackRenderStateAccessor stateAccessor = (ItemStackRenderStateAccessor)ITEM_RENDER_STATE;
        QUAD_INSTANCE.setLightCoords(0xF000F0);
        QUAD_INSTANCE.setOverlayCoords(OverlayTexture.NO_OVERLAY);
        FogRenderer fogRenderer = ((GameRendererAccessor)Renderer3D.mc.gameRenderer).getFogRenderer();
        RenderSystem.setShaderFog((GpuBufferSlice)fogRenderer.getBuffer(FogRenderer.FogMode.NONE));
        HashMap<RenderType, ByteBufferBuilder> owners = new HashMap<RenderType, ByteBufferBuilder>();
        HashMap<RenderType, BufferBuilder> buffers = new HashMap<RenderType, BufferBuilder>();
        try {
            for (int i = 0; i < stateAccessor.night$getActiveLayerCount(); ++i) {
                LayerRenderStateAccessor layer = (LayerRenderStateAccessor)stateAccessor.night$getLayers()[i];
                matrices.pushPose();
                layer.night$applyTransform(matrices.last());
                IntList tints = layer.night$getTintLayers();
                int[] tintLayers = tints != null ? tints.toArray(new int[0]) : new int[]{};
                boolean hasFoil = layer.night$getFoilType() != ItemStackRenderState.FoilType.NONE;
                for (BakedQuad quad : layer.night$getQuads()) {
                    BakedQuad.MaterialInfo material = quad.materialInfo();
                    RenderType renderType = Renderer3D.noDepthItemRenderType(material);
                    int tintIndex = material.isTinted() ? material.tintIndex() : -1;
                    QUAD_INSTANCE.setColor(tintIndex >= 0 && tintIndex < tintLayers.length ? tintLayers[tintIndex] : -1);
                    if (hasFoil) {
                        boolean useTransparentGlint = Renderer3D.mc.gameRenderer.gameRenderState().useShaderTransparency() && renderType.outputTarget() == OutputTarget.ITEM_ENTITY_TARGET;
                        RenderType foilRenderType = useTransparentGlint ? RenderTypes.glintTranslucent() : RenderTypes.glint();
                        Renderer3D.itemBuffer(owners, buffers, foilRenderType).putBakedQuad(matrices.last(), quad, QUAD_INSTANCE);
                    }
                    Renderer3D.itemBuffer(owners, buffers, renderType).putBakedQuad(matrices.last(), quad, QUAD_INSTANCE);
                }
                matrices.popPose();
            }
            for (Map.Entry entry : buffers.entrySet()) {
                MeshData mesh = ((BufferBuilder)entry.getValue()).build();
                Renderer3D.draw((RenderType)entry.getKey(), mesh);
            }
        }
        finally {
            for (ByteBufferBuilder byteBufferBuilder : owners.values()) {
                byteBufferBuilder.close();
            }
            RenderSystem.setShaderFog((GpuBufferSlice)fogRenderer.getBuffer(FogRenderer.FogMode.WORLD));
        }
    }

    public static void renderScaledText(PoseStack matrices, String text, double x, double y, double z, int scale, boolean background, Color color) {
        Renderer3D.renderScaledText(matrices, text, x, y, z, scale, background, false, color, false);
    }

    public static void renderScaledText(PoseStack matrices, String text, double x, double y, double z, int scale, boolean background, Color color, boolean glow) {
        Renderer3D.renderScaledText(matrices, text, x, y, z, scale, background, false, color, glow);
    }

    public static void renderCenteredScaledText(PoseStack matrices, String text, double x, double y, double z, int scale, boolean background, Color color) {
        Renderer3D.renderScaledText(matrices, text, x, y, z, scale, background, true, color, false);
    }

    public static void renderCenteredScaledText(PoseStack matrices, String text, double x, double y, double z, int scale, boolean background, Color color, boolean glow) {
        Renderer3D.renderScaledText(matrices, text, x, y, z, scale, background, true, color, glow);
    }

    public static void renderScaledText(PoseStack matrices, String text, double x, double y, double z, int scale, boolean background, boolean centered, Color color) {
        Renderer3D.renderScaledText(matrices, text, x, y, z, scale, background, centered, color, false);
    }

    public static void renderScaledText(PoseStack matrices, String text, double x, double y, double z, int scale, boolean background, boolean centered, Color color, boolean glow) {
        Vec3 cam = Renderer3D.mc.gameRenderer.mainCamera().position();
        float distance = (float)Math.sqrt(cam.distanceToSqr(x, y, z));
        float scaling = 0.0018f + (float)scale / 10000.0f * Math.max(distance, 8.0f);
        Renderer3D.renderText(matrices, text, x, y, z, scaling, background, centered, color, glow);
    }

    public static void renderText(PoseStack matrices, String text, double x, double y, double z, float scaling, boolean background, Color color) {
        Renderer3D.renderText(matrices, text, x, y, z, scaling, background, false, color, false);
    }

    public static void renderText(PoseStack matrices, String text, double x, double y, double z, float scaling, boolean background, Color color, boolean glow) {
        Renderer3D.renderText(matrices, text, x, y, z, scaling, background, false, color, glow);
    }

    public static void renderText(PoseStack matrices, String text, double x, double y, double z, float scaling, boolean background, boolean centered, Color color) {
        Renderer3D.renderText(matrices, text, x, y, z, scaling, background, centered, color, false);
    }

    public static void renderText(PoseStack matrices, String text, double x, double y, double z, float scaling, boolean background, boolean centered, Color color, boolean glow) {
        float curY;
        Vec3 cam = Renderer3D.mc.gameRenderer.mainCamera().position();
        Vec3 vec3d = new Vec3(x - cam.x, y - cam.y, z - cam.z);
        matrices.pushPose();
        matrices.translate(vec3d.x, vec3d.y, vec3d.z);
        matrices.mulPose((Quaternionfc)Renderer3D.mc.gameRenderer.mainCamera().rotation());
        matrices.scale(scaling, -scaling, scaling);
        String[] lines = text.split("\n");
        float fontHeight = Night.FONT_MANAGER.getHeight();
        float lineSpacing = fontHeight + 2.0f;
        float visualHeight = (float)(lines.length - 1) * lineSpacing + fontHeight;
        float f = curY = centered ? -visualHeight / 2.0f : (float)(-lines.length) * lineSpacing + 1.0f;
        if (background) {
            float maxWidth = 0.0f;
            for (String line : lines) {
                float w = Night.FONT_MANAGER.getWidth(line);
                if (!(w > maxWidth)) continue;
                maxWidth = w;
            }
            float x1 = -maxWidth / 2.0f - 2.0f;
            float y1 = curY - 2.0f;
            float x2 = maxWidth / 2.0f + 2.0f;
            float y2 = curY + visualHeight + 2.0f;
            int rgb = new Color(0, 0, 0, 100).getRGB();
            Matrix4f matrix = matrices.last().pose();
            try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(4 * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));){
                BufferBuilder buffer = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
                buffer.addVertex((Matrix4fc)matrix, x1, y1, 0.0f).setColor(rgb);
                buffer.addVertex((Matrix4fc)matrix, x1, y2, 0.0f).setColor(rgb);
                buffer.addVertex((Matrix4fc)matrix, x2, y2, 0.0f).setColor(rgb);
                buffer.addVertex((Matrix4fc)matrix, x2, y1, 0.0f).setColor(rgb);
                MeshData mesh = buffer.build();
                Renderer3D.draw(NO_DEPTH_QUADS, mesh);
            }
        }
        for (String line : lines) {
            if (glow) {
                Night.FONT_MANAGER.drawText(matrices, line, (float)(-Night.FONT_MANAGER.getWidth(line)) / 2.0f, curY, color, true);
            } else {
                Night.FONT_MANAGER.drawTextWithShadow(matrices, line, (float)(-Night.FONT_MANAGER.getWidth(line)) / 2.0f, curY, color, false);
            }
            curY += lineSpacing;
        }
        matrices.popPose();
    }

    public static RenderType getNoDepthTexturedType(Identifier texture) {
        return NO_DEPTH_TEXTURE_TYPES.computeIfAbsent(texture, tex -> RenderType.create((String)("night_no_depth_tex_" + tex.getNamespace() + "_" + tex.getPath().replace('/', '_')), (RenderSetup)RenderSetup.builder((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getGuiTexturedSnippet()}).withLocation("night/no_depth_tex").withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA))).build()).withTexture("Sampler0", tex).sortOnUpload().createRenderSetup()));
    }

    public static void renderTexture(PoseStack matrices, Identifier texture, float x1, float y1, float x2, float y2, int color) {
        Matrix4f matrix = matrices.last().pose();
        RenderType renderType = Renderer3D.getNoDepthTexturedType(texture);
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(4 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize()));){
            BufferBuilder buffer = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.addVertex((Matrix4fc)matrix, x1, y2, 0.0f).setUv(0.0f, 1.0f).setColor(color);
            buffer.addVertex((Matrix4fc)matrix, x2, y2, 0.0f).setUv(1.0f, 1.0f).setColor(color);
            buffer.addVertex((Matrix4fc)matrix, x2, y1, 0.0f).setUv(1.0f, 0.0f).setColor(color);
            buffer.addVertex((Matrix4fc)matrix, x1, y1, 0.0f).setUv(0.0f, 0.0f).setColor(color);
            MeshData mesh = buffer.build();
            Renderer3D.draw(renderType, mesh);
        }
    }

    public static void prepare() {
        QUADS.clear();
        DEBUG_LINES.clear();
        SHINE_QUADS.clear();
        SHINE_DEBUG_LINES.clear();
        SHADER_QUADS.clear();
        SHADER_DEBUG_LINES.clear();
        TRACER_DEBUG_LINES.clear();
        Glint.QUADS.clear();
        RENDERING = true;
    }

    public static void draw(List<VertexCollection> quads, List<VertexCollection> debugLines, boolean shine) {
        MeshData mesh;
        BufferBuilder buffer;
        ByteBufferBuilder byteBufferBuilder;
        int vertexCount;
        if (!quads.isEmpty()) {
            vertexCount = Renderer3D.vertexCount(quads);
            byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(vertexCount * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));
            try {
                buffer = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
                for (VertexCollection collection : quads) {
                    collection.quad(buffer);
                }
                mesh = buffer.build();
                Renderer3D.draw(shine ? NO_DEPTH_QUADS_SHINE : NO_DEPTH_QUADS, mesh);
            }
            finally {
                if (byteBufferBuilder != null) {
                    byteBufferBuilder.close();
                }
            }
        }
        if (!debugLines.isEmpty()) {
            vertexCount = Renderer3D.vertexCount(debugLines) * 2;
            byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(vertexCount * DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH.getVertexSize()));
            try {
                buffer = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);
                Renderer3D.buildLines(buffer, debugLines);
                mesh = buffer.build();
                Renderer3D.draw(shine ? NO_DEPTH_LINES_SHINE : NO_DEPTH_LINES, mesh);
            }
            finally {
                if (byteBufferBuilder != null) {
                    byteBufferBuilder.close();
                }
            }
        }
        RenderSystem.getDevice();
        quads.clear();
        debugLines.clear();
    }

    static int vertexCount(List<VertexCollection> collections) {
        int count = 0;
        for (VertexCollection collection : collections) {
            count += collection.vertices().length;
        }
        return count;
    }

    public static void buildLines(BufferBuilder buffer, List<VertexCollection> debugLines) {
        for (VertexCollection collection : debugLines) {
            Vertex[] verts = collection.vertices();
            int i = 0;
            while (i + 1 < verts.length) {
                Vertex a = verts[i];
                Vertex b = verts[i + 1];
                float nx = b.x - a.x;
                float ny = b.y - a.y;
                float nz = b.z - a.z;
                float lenSq = nx * nx + ny * ny + nz * nz;
                float step = 0.05f;
                if (lenSq > 1.0E-6f) {
                    float len = (float)Math.sqrt(lenSq);
                    float invLen = 1.0f / len;
                    nx *= invLen;
                    ny *= invLen;
                    nz *= invLen;
                    step = Math.min(0.05f, len * 0.45f);
                } else {
                    nx = 0.0f;
                    ny = 1.0f;
                    nz = 0.0f;
                }
                buffer.addVertex((Matrix4fc)a.matrix, a.x, a.y, a.z).setColor(a.color).setNormal(nx * step, ny * step, nz * step).setLineWidth(a.lineWidth);
                buffer.addVertex((Matrix4fc)b.matrix, b.x, b.y, b.z).setColor(b.color).setNormal(-nx * step, -ny * step, -nz * step).setLineWidth(b.lineWidth);
                i += 2;
            }
        }
    }

    public static boolean isFrustumVisible(AABB box) {
        return Renderer3D.mc.gameRenderer.mainCamera().getCullFrustum().isVisible(box);
    }

    private static Vec3 cameraTransform(Vec3 vec3d) {
        Vec3 camera = Renderer3D.mc.gameRenderer.mainCamera().position();
        return new Vec3(vec3d.x - camera.x, vec3d.y - camera.y, vec3d.z - camera.z);
    }

    private static AABB cameraTransform(AABB box) {
        Vec3 camera = Renderer3D.mc.gameRenderer.mainCamera().position();
        return new AABB(box.minX - camera.x, box.minY - camera.y, box.minZ - camera.z, box.maxX - camera.x, box.maxY - camera.y, box.maxZ - camera.z);
    }

    public record VertexCollection(Vertex... vertices) {
        public void quad(BufferBuilder buffer) {
            for (Vertex vertex : this.vertices) {
                buffer.addVertex((Matrix4fc)vertex.matrix, vertex.x, vertex.y, vertex.z).setColor(vertex.color);
            }
        }
    }

    public record Vertex(Matrix4f matrix, float x, float y, float z, int color, float lineWidth) {
        public Vertex(Matrix4f matrix, float x, float y, float z, int color) {
            this(matrix, x, y, z, color, 1.75f);
        }
    }
}

