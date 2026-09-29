/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.PrimitiveTopology
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.ByteBufferBuilder
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.MeshData
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.navigation.ScreenRectangle
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.rendertype.RenderSetup
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.client.renderer.rendertype.RenderTypes
 *  net.minecraft.client.renderer.state.gui.GuiElementRenderState
 *  net.minecraft.client.renderer.state.gui.GuiRenderState
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix3x2f
 *  org.joml.Matrix3x2fc
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector4f
 */
package night.utils.graphics;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.mixins.accessors.GuiGraphicsExtractorAccessor;
import night.utils.IMinecraft;
import night.utils.graphics.GuiQuadRenderState;
import night.utils.graphics.Renderer3D;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Renderer2D
implements IMinecraft {
    public static final Matrix4f LAST_PROJECTION_MATRIX = new Matrix4f();
    public static final Matrix4f LAST_MODEL_MATRIX = new Matrix4f();
    public static final Matrix4f LAST_WORLD_MATRIX = new Matrix4f();
    private static final Map<Identifier, RenderType> TEXTURE_TYPES = new HashMap<Identifier, RenderType>();
    private static MethodHandle SCISSOR_GET;
    private static MethodHandle SCISSOR_PEEK;
    private static boolean scissorHandleInitialized;

    public static void renderQuad(GuiGraphicsExtractor context, float left, float top, float right, float bottom, Color color) {
        int c = color.getRGB();
        Renderer2D.submit(context, new float[]{left, left, right, right}, new float[]{top, bottom, bottom, top}, new int[]{c, c, c, c});
    }

    public static void renderGradient(GuiGraphicsExtractor context, float left, float top, float right, float bottom, Color startColor, Color endColor) {
        int s = startColor.getRGB();
        int e = endColor.getRGB();
        Renderer2D.submit(context, new float[]{left, left, right, right}, new float[]{top, bottom, bottom, top}, new int[]{s, e, e, s});
    }

    public static void renderSidewaysGradient(GuiGraphicsExtractor context, float left, float top, float right, float bottom, Color startColor, Color endColor) {
        int s = startColor.getRGB();
        int e = endColor.getRGB();
        Renderer2D.submit(context, new float[]{left, left, right, right}, new float[]{top, bottom, bottom, top}, new int[]{s, s, e, e});
    }

    public static void renderCheckerboard(GuiGraphicsExtractor context, float left, float top, float right, float bottom, float tileSize, Color c1, Color c2) {
        int rows;
        int color1 = c1.getRGB();
        int color2 = c2.getRGB();
        int cols = (int)Math.ceil((right - left) / tileSize);
        int quadCount = cols * (rows = (int)Math.ceil((bottom - top) / tileSize));
        if (quadCount <= 0) {
            return;
        }
        float[] xs = new float[quadCount * 4];
        float[] ys = new float[quadCount * 4];
        int[] colorArr = new int[quadCount * 4];
        int qi = 0;
        for (int r = 0; r < rows; ++r) {
            float y0 = top + (float)r * tileSize;
            float y1 = Math.min(top + (float)(r + 1) * tileSize, bottom);
            for (int c = 0; c < cols; ++c) {
                float x0 = left + (float)c * tileSize;
                float x1 = Math.min(left + (float)(c + 1) * tileSize, right);
                int clr = (r + c) % 2 == 0 ? color1 : color2;
                Renderer2D.quad(xs, ys, colorArr, qi++, x0, y0, x1, y1, clr);
            }
        }
        Renderer2D.submit(context, xs, ys, colorArr);
    }

    public static void renderOutline(GuiGraphicsExtractor context, float left, float top, float right, float bottom, Color color) {
        int c = color.getRGB();
        float[] xs = new float[16];
        float[] ys = new float[16];
        int[] cols = new int[16];
        Renderer2D.quad(xs, ys, cols, 0, left, top, left + 0.5f, bottom, c);
        Renderer2D.quad(xs, ys, cols, 1, right - 0.5f, top, right, bottom, c);
        Renderer2D.quad(xs, ys, cols, 2, left, bottom - 0.5f, right, bottom, c);
        Renderer2D.quad(xs, ys, cols, 3, left, top, right, top + 0.5f, c);
        Renderer2D.submit(context, xs, ys, cols);
    }

   public static ScreenRectangle peekScissor(GuiGraphicsExtractor context) {
      if (!scissorHandleInitialized) {
         scissorHandleInitialized = true;

         try {
            Field f = GuiGraphicsExtractor.class.getDeclaredField("scissorStack");
            f.setAccessible(true);
            SCISSOR_GET = MethodHandles.lookup().unreflectGetter(f);
            Method m = f.getType().getDeclaredMethod("peek");
            m.setAccessible(true);
            SCISSOR_PEEK = MethodHandles.lookup().unreflect(m);
         } catch (Throwable t) {
            Night.LOGGER.debug("Could not bind scissorStack handle: {}", t.getMessage());
         }
      }

      if (SCISSOR_GET != null && SCISSOR_PEEK != null) {
         try {
            Object stack = (Object)SCISSOR_GET.invoke((GuiGraphicsExtractor)context);
            if (stack != null) {
               return (ScreenRectangle)SCISSOR_PEEK.invoke((Object)stack);
            }
         } catch (Throwable var3) {
         }
      }

      return null;
   }

    private static void submit(GuiGraphicsExtractor context, float[] xs, float[] ys, int[] cols) {
        GuiRenderState renderState = ((GuiGraphicsExtractorAccessor)context).night$getGuiRenderState();
        renderState.addGuiElement((GuiElementRenderState)new GuiQuadRenderState((Matrix3x2fc)new Matrix3x2f((Matrix3x2fc)context.pose()), xs, ys, cols, Renderer2D.peekScissor(context)));
    }

    private static void quad(float[] xs, float[] ys, int[] cols, int qi, float x0, float y0, float x1, float y1, int c) {
        int b = qi * 4;
        xs[b] = x0;
        ys[b] = y0;
        xs[b + 1] = x0;
        ys[b + 1] = y1;
        xs[b + 2] = x1;
        ys[b + 2] = y1;
        xs[b + 3] = x1;
        ys[b + 3] = y0;
        int n = c;
        cols[b + 3] = n;
        cols[b + 2] = n;
        cols[b + 1] = n;
        cols[b] = n;
    }

    public static void renderImmediateQuad(PoseStack matrices, float left, float top, float right, float bottom, Color color) {
        Matrix4f matrix = matrices.last().pose();
        int c = color.getRGB();
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(4 * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));){
            BufferBuilder buffer = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
            buffer.addVertex((Matrix4fc)matrix, left, top, 0.0f).setColor(c);
            buffer.addVertex((Matrix4fc)matrix, left, bottom, 0.0f).setColor(c);
            buffer.addVertex((Matrix4fc)matrix, right, bottom, 0.0f).setColor(c);
            buffer.addVertex((Matrix4fc)matrix, right, top, 0.0f).setColor(c);
            MeshData mesh = buffer.build();
            Renderer3D.draw(RenderTypes.debugQuads(), mesh);
        }
    }

    public static void renderCircle(PoseStack matrices, float x, float y, float radius, Color color) {
        Matrix4f matrix = matrices.last().pose();
        int c = color.getRGB();
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(361 * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));){
            BufferBuilder buffer = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
            for (int i = 0; i <= 360; ++i) {
                buffer.addVertex((Matrix4fc)matrix, (float)((double)x + Math.sin((double)i * Math.PI / 180.0) * (double)radius), (float)((double)y + Math.cos((double)i * Math.PI / 180.0) * (double)radius), 0.0f).setColor(c);
            }
            MeshData mesh = buffer.build();
            Renderer3D.draw(RenderTypes.debugTriangleFan(), mesh);
        }
    }

    public static void renderTexture(PoseStack matrices, float left, float top, float right, float bottom, Identifier identifier, Color color) {
        Matrix4f matrix = matrices.last().pose();
        int c = color.getRGB();
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(4 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize()));){
            BufferBuilder buffer = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.addVertex((Matrix4fc)matrix, left, top, 0.0f).setUv(0.0f, 0.0f).setColor(c);
            buffer.addVertex((Matrix4fc)matrix, left, bottom, 0.0f).setUv(0.0f, 1.0f).setColor(c);
            buffer.addVertex((Matrix4fc)matrix, right, bottom, 0.0f).setUv(1.0f, 1.0f).setColor(c);
            buffer.addVertex((Matrix4fc)matrix, right, top, 0.0f).setUv(1.0f, 0.0f).setColor(c);
            MeshData mesh = buffer.build();
            Renderer3D.draw(Renderer2D.texturedType(identifier), mesh);
        }
    }

    public static void renderArrow(GuiGraphicsExtractor context, float x, float y, float width, float height, Color color) {
        int c = color.getRGB();
        float notchY = y + height * 0.75f;
        float[] xs = new float[]{x, x - width, x, x, x, x, x + width, x};
        float[] ys = new float[]{y, y + height, notchY, y, y, notchY, y + height, y};
        int[] cols = new int[]{c, c, c, c, c, c, c, c};
        Renderer2D.submit(context, xs, ys, cols);
    }

    public static void renderArrowOutline(GuiGraphicsExtractor context, float x, float y, float width, float height, Color color) {
        int c = color.getRGB();
        float notchY = y + height * 0.75f;
        float[] xs = new float[16];
        float[] ys = new float[16];
        int[] cols = new int[16];
        Renderer2D.quadLine(xs, ys, cols, 0, x, y, x - width, y + height, 0.5f, c);
        Renderer2D.quadLine(xs, ys, cols, 1, x - width, y + height, x, notchY, 0.5f, c);
        Renderer2D.quadLine(xs, ys, cols, 2, x, notchY, x + width, y + height, 0.5f, c);
        Renderer2D.quadLine(xs, ys, cols, 3, x + width, y + height, x, y, 0.5f, c);
        Renderer2D.submit(context, xs, ys, cols);
    }

    private static void quadLine(float[] xs, float[] ys, int[] cols, int qi, float x1, float y1, float x2, float y2, float halfWidth, int c) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float)Math.sqrt(dx * dx + dy * dy);
        if (len < 1.0E-4f) {
            return;
        }
        float nx = -dy / len * halfWidth;
        float ny = dx / len * halfWidth;
        int b = qi * 4;
        xs[b] = x1 + nx;
        ys[b] = y1 + ny;
        xs[b + 1] = x1 - nx;
        ys[b + 1] = y1 - ny;
        xs[b + 2] = x2 - nx;
        ys[b + 2] = y2 - ny;
        xs[b + 3] = x2 + nx;
        ys[b + 3] = y2 + ny;
        int n = c;
        cols[b + 3] = n;
        cols[b + 2] = n;
        cols[b + 1] = n;
        cols[b] = n;
    }

    public static void renderArrow(PoseStack matrices, float x, float y, float width, float height, Color color) {
        Matrix4f matrix = matrices.last().pose();
        int c = color.getRGB();
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(5 * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));){
            BufferBuilder buffer = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
            buffer.addVertex((Matrix4fc)matrix, x, y, 0.0f).setColor(c);
            buffer.addVertex((Matrix4fc)matrix, x - width, y + height, 0.0f).setColor(c);
            buffer.addVertex((Matrix4fc)matrix, x, y + height, 0.0f).setColor(c);
            buffer.addVertex((Matrix4fc)matrix, x + width, y + height, 0.0f).setColor(c);
            buffer.addVertex((Matrix4fc)matrix, x, y, 0.0f).setColor(c);
            MeshData mesh = buffer.build();
            Renderer3D.draw(RenderTypes.debugTriangleFan(), mesh);
        }
    }

    public static void renderArrowOutline(PoseStack matrices, float x, float y, float width, float height, Color color) {
        Matrix4f matrix = matrices.last().pose();
        int c = color.getRGB();
        float[] px = new float[]{x, x - width, x, x + width, x};
        float[] py = new float[]{y, y + height, y + height, y + height, y};
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)((px.length - 1) * 4 * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));){
            BufferBuilder buffer = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
            for (int i = 0; i < px.length - 1; ++i) {
                Renderer2D.segment(buffer, matrix, px[i], py[i], px[i + 1], py[i + 1], 0.5f, c);
            }
            MeshData mesh = buffer.build();
            Renderer3D.draw(RenderTypes.debugQuads(), mesh);
        }
    }

    private static void segment(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float x2, float y2, float w, int c) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float)Math.sqrt(dx * dx + dy * dy);
        if (len == 0.0f) {
            return;
        }
        float nx = -dy / len * w;
        float ny = dx / len * w;
        buffer.addVertex((Matrix4fc)matrix, x1 + nx, y1 + ny, 0.0f).setColor(c);
        buffer.addVertex((Matrix4fc)matrix, x1 - nx, y1 - ny, 0.0f).setColor(c);
        buffer.addVertex((Matrix4fc)matrix, x2 - nx, y2 - ny, 0.0f).setColor(c);
        buffer.addVertex((Matrix4fc)matrix, x2 + nx, y2 + ny, 0.0f).setColor(c);
    }

    private static RenderType texturedType(Identifier identifier) {
        return TEXTURE_TYPES.computeIfAbsent(identifier, id -> RenderType.create((String)("night_textured_" + String.valueOf(id)), (RenderSetup)RenderSetup.builder((RenderPipeline)RenderPipelines.GUI_TEXTURED).withTexture("Sampler0", id).createRenderSetup()));
    }

    public static Vec3 project(Vec3 vec3d) {
        Vec3 camera = Renderer2D.mc.gameRenderer.mainCamera().position();
        int[] viewport = new int[]{0, 0, mc.getWindow().getWidth(), mc.getWindow().getHeight()};
        Vector3f target = new Vector3f();
        Vector4f transform = new Vector4f((float)(vec3d.x - camera.x), (float)(vec3d.y - camera.y), (float)(vec3d.z - camera.z), 1.0f).mul((Matrix4fc)LAST_WORLD_MATRIX);
        Matrix4f matrixProj = new Matrix4f((Matrix4fc)LAST_PROJECTION_MATRIX);
        Matrix4f matrixModel = new Matrix4f((Matrix4fc)LAST_MODEL_MATRIX);
        matrixProj.mul((Matrix4fc)matrixModel).project(transform.x(), transform.y(), transform.z(), viewport, target);
        return new Vec3((double)(target.x / (float)mc.getWindow().getGuiScale()), (double)(((float)mc.getWindow().getHeight() - target.y) / (float)mc.getWindow().getGuiScale()), (double)target.z);
    }

    static {
        scissorHandleInitialized = false;
    }
}

