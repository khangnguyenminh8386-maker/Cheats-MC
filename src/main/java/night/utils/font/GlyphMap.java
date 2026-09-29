/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  com.mojang.blaze3d.platform.NativeImage$Format
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.AddressMode
 *  com.mojang.blaze3d.textures.FilterMode
 *  it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap
 *  lombok.Generated
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.resources.Identifier
 *  org.lwjgl.system.MemoryUtil
 */
package night.utils.font;
import com.mojang.blaze3d.platform.NativeImage.Format;
import java.util.List;


import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import night.mixins.accessors.AbstractTextureAccessor;
import night.mixins.accessors.NativeImageAccessor;
import night.utils.font.Glyph;
import org.lwjgl.system.MemoryUtil;

public class GlyphMap {
    private static final AtomicInteger PAGE_SEQ = new AtomicInteger();
    private DynamicTexture texture;
    private Identifier textureId;
    private final Font[] fonts;
    private int width;
    private int height;
    private final char include;
    private final char exclude;
    private final int padding;
    private final Char2ObjectArrayMap<Glyph> glyphs = new Char2ObjectArrayMap();
    boolean generated = false;

    public GlyphMap(Font[] fonts, char include, char exclude, int padding) {
        this.fonts = fonts;
        this.include = include;
        this.exclude = exclude;
        this.padding = padding;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void generate() {
        GlyphMap glyphMap = this;
        synchronized (glyphMap) {
            this.privateGenerate();
        }
    }

   private void privateGenerate() {
      if (!this.generated) {
         int range = this.exclude - this.include - 1;
         int charsVert = (int)(Math.ceil(Math.sqrt(range)) * 1.5);
         int generatedChars = 0;
         int charNX = 0;
         int maxX = 0;
         int maxY = 0;
         int currentX = 0;
         int currentY = 0;
         int currentRowMaxY = 0;
         List<Glyph> glyphs = new ArrayList<>();
         AffineTransform affineTransform = new AffineTransform();
         FontRenderContext context = new FontRenderContext(affineTransform, true, true);

         while (generatedChars <= range) {
            char currentChar = (char)(this.include + generatedChars);
            Font font = this.getFontForGlyph(currentChar);
            Rectangle2D stringBounds = font.getStringBounds(String.valueOf(currentChar), context);
            int width = (int)Math.ceil(stringBounds.getWidth());
            int height = (int)Math.ceil(stringBounds.getHeight());
            generatedChars++;
            maxX = Math.max(maxX, currentX + width);
            maxY = Math.max(maxY, currentY + height);
            if (charNX >= charsVert) {
               currentX = 0;
               currentY += currentRowMaxY + this.padding;
               charNX = 0;
               currentRowMaxY = 0;
            }

            currentRowMaxY = Math.max(currentRowMaxY, height);
            glyphs.add(new Glyph(this, currentX, currentY, width, height, currentChar));
            currentX += width + this.padding;
            charNX++;
         }

         BufferedImage bufferedImage = new BufferedImage(Math.max(maxX + this.padding, 1), Math.max(maxY + this.padding, 1), 2);
         this.width = bufferedImage.getWidth();
         this.height = bufferedImage.getHeight();
         Graphics2D graphics = bufferedImage.createGraphics();
         graphics.setColor(new Color(255, 255, 255, 0));
         graphics.fillRect(0, 0, this.width, this.height);
         graphics.setColor(Color.WHITE);
         graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
         graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

         for (Glyph glyph : glyphs) {
            graphics.setFont(this.getFontForGlyph(glyph.value()));
            FontMetrics fontMetrics = graphics.getFontMetrics();
            graphics.drawString(String.valueOf(glyph.value()), glyph.u(), glyph.v() + fontMetrics.getAscent());
            this.glyphs.put(glyph.value(), glyph);
         }

         NativeImage image = new NativeImage(Format.RGBA, bufferedImage.getWidth(), bufferedImage.getHeight(), false);
         IntBuffer backingBuffer = MemoryUtil.memIntBuffer(((NativeImageAccessor)(Object)image).getPointer(), image.getWidth() * image.getHeight());
         WritableRaster raster = bufferedImage.getRaster();
         ColorModel colorModel = bufferedImage.getColorModel();
         int numBands = raster.getNumBands();
         int dataType = raster.getDataBuffer().getDataType();

         Object object = switch (dataType) {
            case 0 -> new byte[numBands];
            case 1 -> new short[numBands];
            default -> throw new IllegalArgumentException("Unknown data buffer type: " + dataType);
            case 3 -> new int[numBands];
            case 4 -> new float[numBands];
            case 5 -> new double[numBands];
         };

         for (int y = 0; y < bufferedImage.getHeight(); y++) {
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
               raster.getDataElements(x, y, object);
               backingBuffer.put(
                  colorModel.getAlpha(object) << 24 | colorModel.getBlue(object) << 16 | colorModel.getGreen(object) << 8 | colorModel.getRed(object)
               );
            }
         }

         int page = PAGE_SEQ.getAndIncrement();
         DynamicTexture texture = new DynamicTexture(() -> "night/glyph_page_" + page, image);
         this.texture = texture;
         this.textureId = Identifier.fromNamespaceAndPath("night", "glyph_page_" + page);
         Minecraft.getInstance().getTextureManager().register(this.textureId, texture);
         ((AbstractTextureAccessor)texture)
            .setSampler(
               RenderSystem.getDevice()
                  .createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, 1, OptionalDouble.empty())
            );
         this.generated = true;
      }
   }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Glyph getGlyph(char character) {
        GlyphMap glyphMap = this;
        synchronized (glyphMap) {
            if (!this.generated) {
                this.privateGenerate();
            }
            return (Glyph)this.glyphs.get(character);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void destroy() {
        GlyphMap glyphMap = this;
        synchronized (glyphMap) {
            this.generated = false;
            if (this.textureId != null) {
                Minecraft.getInstance().getTextureManager().release(this.textureId);
                this.textureId = null;
            }
            if (this.texture != null) {
                this.texture.close();
            }
            this.glyphs.clear();
            this.width = -1;
            this.height = -1;
        }
    }

    public boolean contains(char c) {
        return c >= this.include && c < this.exclude;
    }

    private Font getFontForGlyph(char c) {
        for (Font font1 : this.fonts) {
            if (!font1.canDisplay(c)) continue;
            return font1;
        }
        return this.fonts[0];
    }

    @Generated
    public DynamicTexture getTexture() {
        return this.texture;
    }

    @Generated
    public Identifier getTextureId() {
        return this.textureId;
    }

    @Generated
    public Font[] getFonts() {
        return this.fonts;
    }

    @Generated
    public int getWidth() {
        return this.width;
    }

    @Generated
    public int getHeight() {
        return this.height;
    }

    @Generated
    public char getInclude() {
        return this.include;
    }

    @Generated
    public char getExclude() {
        return this.exclude;
    }

    @Generated
    public int getPadding() {
        return this.padding;
    }

    @Generated
    public Char2ObjectArrayMap<Glyph> getGlyphs() {
        return this.glyphs;
    }

    @Generated
    public boolean isGenerated() {
        return this.generated;
    }
}

