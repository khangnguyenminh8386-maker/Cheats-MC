/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.mojang.blaze3d.PrimitiveTopology
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.textures.GpuSampler
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.ByteBufferBuilder
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.MeshData
 *  com.mojang.blaze3d.vertex.PoseStack
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectList
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.navigation.ScreenRectangle
 *  net.minecraft.client.gui.render.TextureSetup
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.rendertype.RenderSetup
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.client.renderer.state.gui.GuiElementRenderState
 *  net.minecraft.client.renderer.state.gui.GuiRenderState
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 *  net.minecraft.util.FormattedCharSequence
 *  org.joml.Matrix3x2f
 *  org.joml.Matrix3x2fc
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package night.utils.font;
import java.util.Map.Entry;


import com.google.common.base.Preconditions;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.awt.Font;
import java.io.Closeable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import night.Night;
import night.mixins.accessors.GuiGraphicsExtractorAccessor;
import night.modules.impl.core.FontModule;
import night.utils.IMinecraft;
import night.utils.font.Glyph;
import night.utils.font.GlyphMap;
import night.utils.graphics.GuiQuadRenderState;
import night.utils.graphics.Renderer2D;
import night.utils.graphics.Renderer3D;
import night.utils.graphics.TextGlowShader;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public class FontRenderer
implements Closeable,
IMinecraft {
    private final ObjectList<GlyphMap> maps = new ObjectArrayList();
    private static final Map<Identifier, RenderType> WORLD_TYPES = new HashMap<Identifier, RenderType>();
    private final Font[] originalFonts;
    private Font[] fonts;
    private final float size;
    private final int charsPerPage;
    private final int padding;
    private int multiplier = 0;
    private int previousGameScale = -1;
    private boolean initialized;
    private final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();

    public FontRenderer(Font[] fonts, float size) {
        this(fonts, size, 256, 48);
    }

    public FontRenderer(Font[] fonts, float size, int charactersPerPage, int paddingBetweenCharacters) {
        Preconditions.checkArgument((size > 0.0f ? 1 : 0) != 0, (Object)"size <= 0");
        Preconditions.checkArgument((fonts.length > 0 ? 1 : 0) != 0, (Object)"fonts.length <= 0");
        Preconditions.checkArgument((charactersPerPage > 4 ? 1 : 0) != 0, (Object)"Unreasonable charactersPerPage count");
        Preconditions.checkArgument((paddingBetweenCharacters > 0 ? 1 : 0) != 0, (Object)"paddingBetweenCharacters <= 0");
        this.originalFonts = (Font[])fonts.clone();
        this.size = size;
        this.charsPerPage = charactersPerPage;
        this.padding = paddingBetweenCharacters;
        this.init(this.originalFonts, size);
    }

    public void drawString(GuiGraphicsExtractor context, String text, float x, float y, int color, boolean dropShadow) {
        this.drawString(context, text, x, y, color, dropShadow, false, true);
    }

    public void drawString(GuiGraphicsExtractor context, String text, float x, float y, int color, boolean dropShadow, boolean glow) {
        this.drawString(context, text, x, y, color, dropShadow, glow, true);
    }

    public void drawString(GuiGraphicsExtractor context, String text, float x, float y, int color, boolean dropShadow, boolean glow, boolean renderPrimary) {
        this.drawText(context, Component.literal((String)text).getVisualOrderText(), x, y, color, dropShadow, glow, renderPrimary);
    }

    public void drawText(GuiGraphicsExtractor context, FormattedCharSequence text, float x, float y, int color, boolean dropShadow) {
        this.drawText(context, text, x, y, color, dropShadow, false, true);
    }

    public void drawText(GuiGraphicsExtractor context, FormattedCharSequence text, float x, float y, int color, boolean dropShadow, boolean glow) {
        this.drawText(context, text, x, y, color, dropShadow, glow, true);
    }

    public void drawText(GuiGraphicsExtractor context, FormattedCharSequence text, float x, float y, int color, boolean dropShadow, boolean glow, boolean renderPrimary) {
        GuiRenderState renderState = ((GuiGraphicsExtractorAccessor)context).night$getGuiRenderState();
        this.submitGlyphs(renderState, (Matrix3x2fc)new Matrix3x2f((Matrix3x2fc)context.pose()), Renderer2D.peekScissor(context), text, x, y, color, dropShadow, glow, renderPrimary);
    }

    public void submitGlyphs(GuiRenderState guiRenderState, Matrix3x2fc pose, ScreenRectangle scissor, FormattedCharSequence text, float x, float y, int color, boolean dropShadow) {
        this.submitGlyphs(guiRenderState, pose, scissor, text, x, y, color, dropShadow, false, true);
    }

   public void submitGlyphs(
      GuiRenderState guiRenderState,
      Matrix3x2fc pose,
      ScreenRectangle scissor,
      FormattedCharSequence text,
      float x,
      float y,
      int color,
      boolean dropShadow,
      boolean glow,
      boolean renderPrimary
   ) {
      this.ensureScale();
      if (Night.MODULE_MANAGER != null) {
         FontModule module = Night.MODULE_MANAGER.getModule(FontModule.class);
         if (module != null && module.isToggled()) {
            x += module.xOffset.getValue().floatValue();
            y += module.yOffset.getValue().floatValue();
         }
      }

      Map<GlyphMap, ObjectList<FontRenderer.DrawEntry>> pages = this.layoutGlyphs(text, color, dropShadow, new float[1]);
      if (!pages.isEmpty()) {
         float inv = 1.0F / this.multiplier;

         for (Entry<GlyphMap, ObjectList<FontRenderer.DrawEntry>> entry : pages.entrySet()) {
            GlyphMap map = entry.getKey();
            List<FontRenderer.DrawEntry> entries = entry.getValue();
            int n = entries.size();
            TextureSetup ts = TextureSetup.singleTexture(map.getTexture().getTextureView(), map.getTexture().getSampler());
            if (glow) {
               float atlasPad = 17.0F;
               float du = atlasPad / map.getWidth();
               float dv = atlasPad / map.getHeight();
               float screenPad = atlasPad * inv;
               float[] g_xs = new float[n * 4];
               float[] g_ys = new float[n * 4];
               float[] g_us = new float[n * 4];
               float[] g_vs = new float[n * 4];
               int[] g_cols = new int[n * 4];
               int g_qi = 0;

               for (FontRenderer.DrawEntry d : entries) {
                  Glyph glyph = d.toDraw();
                  float u1 = (float)glyph.u() / map.getWidth();
                  float v1 = (float)glyph.v() / map.getHeight();
                  float u2 = (float)(glyph.u() + glyph.width()) / map.getWidth();
                  float v2 = (float)(glyph.v() + glyph.height()) / map.getHeight();
                  float gx0 = x + d.atX() * inv - screenPad;
                  float gy0 = y + d.atY() * inv - screenPad;
                  float gx1 = x + (d.atX() + glyph.width()) * inv + screenPad;
                  float gy1 = y + (d.atY() + glyph.height()) * inv + screenPad;
                  int b = g_qi * 4;
                  g_xs[b] = gx0;
                  g_ys[b] = gy0;
                  g_us[b] = u1 - du;
                  g_vs[b] = v1 - dv;
                  g_xs[b + 1] = gx0;
                  g_ys[b + 1] = gy1;
                  g_us[b + 1] = u1 - du;
                  g_vs[b + 1] = v2 + dv;
                  g_xs[b + 2] = gx1;
                  g_ys[b + 2] = gy1;
                  g_us[b + 2] = u2 + du;
                  g_vs[b + 2] = v2 + dv;
                  g_xs[b + 3] = gx1;
                  g_ys[b + 3] = gy0;
                  g_us[b + 3] = u2 + du;
                  g_vs[b + 3] = v1 - dv;
                  g_cols[b] = g_cols[b + 1] = g_cols[b + 2] = g_cols[b + 3] = d.argb();
                  g_qi++;
               }

               guiRenderState.addGuiElement(
                  new GuiQuadRenderState(TextGlowShader.TEXT_GLOW_PIPELINE, ts, new Matrix3x2f(pose), g_xs, g_ys, g_cols, g_us, g_vs, scissor)
               );
            }

            if (renderPrimary) {
               float[] xs = new float[n * 4];
               float[] ys = new float[n * 4];
               float[] us = new float[n * 4];
               float[] vs = new float[n * 4];
               int[] cols = new int[n * 4];
               int qi = 0;

               for (FontRenderer.DrawEntry d : entries) {
                  Glyph glyph = d.toDraw();
                  float u1 = (float)glyph.u() / map.getWidth();
                  float v1 = (float)glyph.v() / map.getHeight();
                  float u2 = (float)(glyph.u() + glyph.width()) / map.getWidth();
                  float v2 = (float)(glyph.v() + glyph.height()) / map.getHeight();
                  float gx0 = x + d.atX() * inv;
                  float gy0 = y + d.atY() * inv;
                  float gx1 = x + (d.atX() + glyph.width()) * inv;
                  float gy1 = y + (d.atY() + glyph.height()) * inv;
                  int b = qi * 4;
                  xs[b] = gx0;
                  ys[b] = gy0;
                  us[b] = u1;
                  vs[b] = v1;
                  xs[b + 1] = gx0;
                  ys[b + 1] = gy1;
                  us[b + 1] = u1;
                  vs[b + 1] = v2;
                  xs[b + 2] = gx1;
                  ys[b + 2] = gy1;
                  us[b + 2] = u2;
                  vs[b + 2] = v2;
                  xs[b + 3] = gx1;
                  ys[b + 3] = gy0;
                  us[b + 3] = u2;
                  vs[b + 3] = v1;
                  cols[b] = cols[b + 1] = cols[b + 2] = cols[b + 3] = d.argb();
                  qi++;
               }

               guiRenderState.addGuiElement(new GuiQuadRenderState(ts, new Matrix3x2f(pose), xs, ys, cols, us, vs, scissor));
            }
         }
      }
   }

    public void drawString(PoseStack matrices, String text, float x, float y, int color, boolean dropShadow) {
        this.drawString(matrices, text, x, y, color, dropShadow, false, true);
    }

    public void drawString(PoseStack matrices, String text, float x, float y, int color, boolean dropShadow, boolean glow) {
        this.drawString(matrices, text, x, y, color, dropShadow, glow, true);
    }

    public void drawString(PoseStack matrices, String text, float x, float y, int color, boolean dropShadow, boolean glow, boolean renderPrimary) {
        this.drawText(matrices, Component.literal((String)text).getVisualOrderText(), x, y, color, dropShadow, glow, renderPrimary);
    }

    public void drawText(PoseStack matrices, FormattedCharSequence text, float x, float y, int color, boolean dropShadow) {
        this.drawText(matrices, text, x, y, color, dropShadow, false, true);
    }

    public void drawText(PoseStack matrices, FormattedCharSequence text, float x, float y, int color, boolean dropShadow, boolean glow) {
        this.drawText(matrices, text, x, y, color, dropShadow, glow, true);
    }

    public void drawText(PoseStack matrices, FormattedCharSequence text, float x, float y, int color, boolean dropShadow, boolean glow, boolean renderPrimary) {
        Map<GlyphMap, ObjectList<DrawEntry>> pages;
        FontModule module;
        this.ensureScale();
        if (Night.MODULE_MANAGER != null && (module = Night.MODULE_MANAGER.getModule(FontModule.class)) != null && module.isToggled()) {
            x += module.xOffset.getValue().floatValue();
            y += module.yOffset.getValue().floatValue();
        }
        if ((pages = this.layoutGlyphs(text, color, dropShadow, new float[1])).isEmpty()) {
            return;
        }
        matrices.pushPose();
        matrices.translate(x, y, 0.0f);
        matrices.scale(1.0f / (float)this.multiplier, 1.0f / (float)this.multiplier, 1.0f);
        Matrix4f matrix = matrices.last().pose();
        for (Map.Entry<GlyphMap, ObjectList<DrawEntry>> entry : pages.entrySet()) {
            GlyphMap map = entry.getKey();
            if (glow) {
                try {
                    float atlasPad = 17.0f;
                    float du = atlasPad / (float)map.getWidth();
                    float dv = atlasPad / (float)map.getHeight();
                    try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(entry.getValue().size() * 4 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize()));){
                        BufferBuilder glowBuffer = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                        for (DrawEntry d : entry.getValue()) {
                            Glyph glyph = d.toDraw();
                            float u1 = (float)glyph.u() / (float)map.getWidth();
                            float v1 = (float)glyph.v() / (float)map.getHeight();
                            float u2 = (float)(glyph.u() + glyph.width()) / (float)map.getWidth();
                            float v2 = (float)(glyph.v() + glyph.height()) / (float)map.getHeight();
                            glowBuffer.addVertex((Matrix4fc)matrix, d.atX() - atlasPad, d.atY() + (float)glyph.height() + atlasPad, 0.0f).setUv(u1 - du, v2 + dv).setColor(d.argb());
                            glowBuffer.addVertex((Matrix4fc)matrix, d.atX() + (float)glyph.width() + atlasPad, d.atY() + (float)glyph.height() + atlasPad, 0.0f).setUv(u2 + du, v2 + dv).setColor(d.argb());
                            glowBuffer.addVertex((Matrix4fc)matrix, d.atX() + (float)glyph.width() + atlasPad, d.atY() - atlasPad, 0.0f).setUv(u2 + du, v1 - dv).setColor(d.argb());
                            glowBuffer.addVertex((Matrix4fc)matrix, d.atX() - atlasPad, d.atY() - atlasPad, 0.0f).setUv(u1 - du, v1 - dv).setColor(d.argb());
                        }
                        MeshData glowMesh = glowBuffer.build();
                        if (glowMesh != null) {
                            Renderer3D.draw(TextGlowShader.getWorldGlowType(map.getTextureId()), glowMesh);
                        }
                    }
                }
                catch (Throwable atlasPad) {
                    // empty catch block
                }
            }
            if (!renderPrimary) continue;
            ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(entry.getValue().size() * 4 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize()));
            try {
                BufferBuilder buffer = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                for (DrawEntry d : entry.getValue()) {
                    Glyph glyph = d.toDraw();
                    float u1 = (float)glyph.u() / (float)map.getWidth();
                    float v1 = (float)glyph.v() / (float)map.getHeight();
                    float u2 = (float)(glyph.u() + glyph.width()) / (float)map.getWidth();
                    float v2 = (float)(glyph.v() + glyph.height()) / (float)map.getHeight();
                    buffer.addVertex((Matrix4fc)matrix, d.atX(), d.atY() + (float)glyph.height(), 0.0f).setUv(u1, v2).setColor(d.argb());
                    buffer.addVertex((Matrix4fc)matrix, d.atX() + (float)glyph.width(), d.atY() + (float)glyph.height(), 0.0f).setUv(u2, v2).setColor(d.argb());
                    buffer.addVertex((Matrix4fc)matrix, d.atX() + (float)glyph.width(), d.atY(), 0.0f).setUv(u2, v1).setColor(d.argb());
                    buffer.addVertex((Matrix4fc)matrix, d.atX(), d.atY(), 0.0f).setUv(u1, v1).setColor(d.argb());
                }
                MeshData mesh = buffer.build();
                Renderer3D.draw(FontRenderer.worldTexturedType(map.getTextureId()), mesh);
            }
            finally {
                if (byteBufferBuilder == null) continue;
                byteBufferBuilder.close();
            }
        }
        matrices.popPose();
    }

    private Map<GlyphMap, ObjectList<DrawEntry>> layoutGlyphs(FormattedCharSequence text, int color, boolean dropShadow, float[] outWidth) {
        if ((color & 0xFC000000) == 0) {
            color |= 0xFF000000;
        }
        if (dropShadow) {
            color = (color & 0xFCFCFC) >> 2 | color & 0xFF000000;
        }
        int baseAlpha = color >> 24 & 0xFF;
        int baseRed = color >> 16 & 0xFF;
        int baseGreen = color >> 8 & 0xFF;
        int baseBlue = color & 0xFF;
        LinkedHashMap<GlyphMap, ObjectList<DrawEntry>> pages = new LinkedHashMap<GlyphMap, ObjectList<DrawEntry>>();
        float[] currentX = new float[]{0.0f};
        text.accept((index, style, codePoint) -> {
            char[] chars = Character.toChars(codePoint);
            int r = baseRed;
            int g = baseGreen;
            int b = baseBlue;
            if (style.getColor() != null) {
                int rgb = style.getColor().getValue();
                if ((rgb & 0xFC000000) == 0) {
                    rgb |= 0xFF000000;
                }
                if (dropShadow) {
                    rgb = (rgb & 0xFCFCFC) >> 2 | rgb & 0xFF000000;
                }
                r = rgb >> 16 & 0xFF;
                g = rgb >> 8 & 0xFF;
                b = rgb & 0xFF;
            }
            int argb = baseAlpha << 24 | r << 16 | g << 8 | b;
            for (char character : chars) {
                Glyph glyph = this.locateGlyph(character);
                if (glyph == null) continue;
                if (glyph.value() != ' ') {
                    pages.computeIfAbsent(glyph.parent(), k -> new ObjectArrayList()).add((Object)new DrawEntry(currentX[0], 0.0f, argb, glyph));
                }
                currentX[0] = currentX[0] + (float)glyph.width();
            }
            return true;
        });
        outWidth[0] = currentX[0];
        return pages;
    }

    private static RenderType worldTexturedType(Identifier id) {
        return WORLD_TYPES.computeIfAbsent(id, i -> RenderType.create((String)("night_font_" + String.valueOf(i)), (RenderSetup)RenderSetup.builder((RenderPipeline)RenderPipelines.GUI_TEXTURED).withTexture("Sampler0", i).createRenderSetup()));
    }

    private void ensureScale() {
        if (mc.getWindow().getGuiScale() != this.previousGameScale) {
            this.close();
            this.init(this.originalFonts, this.size);
        }
    }

    public float getTextWidth(String text) {
        char[] characters = FontRenderer.stripControlCodes(text).toCharArray();
        float width = 0.0f;
        for (char ch : characters) {
            Glyph glyph = this.locateGlyph(ch);
            if (glyph == null) continue;
            width += (float)glyph.width() / (float)this.multiplier;
        }
        return Math.max(width, 0.0f);
    }

    public float getTextWidth(FormattedCharSequence text) {
        float[] dimensions = new float[2];
        text.accept((index, style, codePoint) -> {
            char character = (char)codePoint;
            Glyph glyph = this.locateGlyph(character);
            if (glyph != null) {
                dimensions[0] = dimensions[0] + (float)glyph.width() / (float)this.multiplier;
            }
            return true;
        });
        return Math.max(dimensions[0], dimensions[1]);
    }

    public float getHeight() {
        Glyph glyph = this.locateGlyph('A');
        if (glyph != null) {
            return (float)glyph.height() / (float)this.multiplier;
        }
        return 0.0f;
    }

    public static String stripControlCodes(String text) {
        char[] chars = text.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < chars.length; ++i) {
            char character = chars[i];
            if (character == '\u00a7') {
                ++i;
                continue;
            }
            builder.append(character);
        }
        return builder.toString();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void init(Font[] fonts, float sizePx) {
        if (this.initialized) {
            throw new IllegalStateException("Double call to init()");
        }
        this.LOCK.writeLock().lock();
        try {
            int scale = mc != null && mc.getWindow() != null ? mc.getWindow().getGuiScale() : 2;
            this.previousGameScale = scale <= 0 ? 2 : scale;
            this.multiplier = this.previousGameScale * 2;
            this.fonts = new Font[fonts.length];
            for (int i = 0; i < fonts.length; ++i) {
                this.fonts[i] = fonts[i].deriveFont(sizePx * (float)this.multiplier);
            }
            this.initialized = true;
        }
        finally {
            this.LOCK.writeLock().unlock();
        }
        this.locateGlyph('A');
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
   private Glyph locateGlyph(char glyph) {
      this.LOCK.readLock().lock();

      try {
         for (GlyphMap map : this.maps) {
            if (map.contains(glyph)) {
               return map.getGlyph(glyph);
            }
         }
      } finally {
         this.LOCK.readLock().unlock();
      }

      int var13 = this.charsPerPage * (int)Math.floor((double)glyph / this.charsPerPage);
      GlyphMap var14 = new GlyphMap(this.fonts, (char)var13, (char)(var13 + this.charsPerPage), this.padding);
      this.LOCK.writeLock().lock();

      try {
         var14.generate();
         this.maps.add(var14);
      } finally {
         this.LOCK.writeLock().unlock();
      }

      return var14.getGlyph(glyph);
   }

    @Override
    public void close() {
        this.LOCK.writeLock().lock();
        try {
            for (GlyphMap map : this.maps) {
                map.destroy();
            }
            this.maps.clear();
            this.initialized = false;
        }
        finally {
            this.LOCK.writeLock().unlock();
        }
    }

    public record DrawEntry(float atX, float atY, int argb, Glyph toDraw) {
    }
}

