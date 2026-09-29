/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.ByteBufferBuilder
 *  com.mojang.blaze3d.vertex.MeshData
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  lombok.Generated
 *  net.minecraft.client.gui.Font$DisplayMode
 *  net.minecraft.client.gui.Font$GlyphVisitor
 *  net.minecraft.client.gui.Font$PreparedText
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.font.TextRenderable
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.network.chat.TextColor
 *  net.minecraft.util.FormattedCharSequence
 *  org.joml.Matrix4fc
 */
package night.managers;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Generated;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import night.Night;
import night.modules.impl.core.FontModule;
import night.modules.impl.core.HUDModule;
import night.utils.IMinecraft;
import night.utils.color.ColorUtils;
import night.utils.font.FontRenderer;
import night.utils.graphics.Renderer3D;
import org.joml.Matrix4fc;

public class FontManager
implements IMinecraft {
    private FontRenderer fontRenderer;
    private static final ThreadLocal<Boolean> HUD_RENDERING = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Integer> CLIENT_TEXT_DEPTH = ThreadLocal.withInitial(() -> 0);

    public static void pushClientTextRendering() {
        CLIENT_TEXT_DEPTH.set(CLIENT_TEXT_DEPTH.get() + 1);
    }

    public static void popClientTextRendering() {
        int depth = CLIENT_TEXT_DEPTH.get() - 1;
        CLIENT_TEXT_DEPTH.set(Math.max(0, depth));
    }

    public static boolean isClientTextRendering() {
        return CLIENT_TEXT_DEPTH.get() > 0;
    }

    public static void setHudRendering(boolean rendering) {
        HUD_RENDERING.set(rendering);
    }

    public static boolean isHudRendering() {
        return HUD_RENDERING.get();
    }

    public boolean isHudGlowEnabled() {
        FontModule fontModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FontModule.class) : null;
        HUDModule hudModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(HUDModule.class) : null;
        boolean fontGlow = fontModule != null && fontModule.isToggled() && fontModule.glow.getValue();
        boolean hudGlow = hudModule != null && hudModule.isToggled() && hudModule.textGlow.getValue();
        return fontGlow || hudGlow;
    }

    public boolean isCustomFontActive() {
        return this.useCustomFont(false);
    }

    private boolean useCustomFont() {
        return this.useCustomFont(false);
    }

    private boolean useCustomFont(boolean glow) {
        boolean custom;
        FontModule module;
        FontModule fontModule = module = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FontModule.class) : null;
        if (module == null || !module.isToggled()) {
            return false;
        }
        boolean bl = custom = module.customFont.getValue() || glow || FontManager.isHudRendering() && this.isHudGlowEnabled();
        if (this.fontRenderer == null && custom) {
            module.updateFontRenderer();
        }
        return this.fontRenderer != null && custom;
    }

    private boolean shadowEnabled() {
        FontModule module = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FontModule.class) : null;
        return module != null && !module.shadowMode.getValue().equalsIgnoreCase("None");
    }

    public void drawText(GuiGraphicsExtractor context, String text, int x, int y, Color color) {
        this.drawText(context, text, x, y, color, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void drawText(GuiGraphicsExtractor context, String text, int x, int y, Color color, boolean glow) {
        FontManager.pushClientTextRendering();
        try {
            boolean effectiveGlow;
            boolean bl = effectiveGlow = glow || FontManager.isHudRendering() && this.isHudGlowEnabled();
            if (this.useCustomFont(effectiveGlow)) {
                if (effectiveGlow) {
                    this.fontRenderer.drawString(context, text, (float)x, (float)y, color.getRGB(), false, true, false);
                }
                this.fontRenderer.drawString(context, text, (float)x, (float)y, color.getRGB(), false, false, true);
            } else {
                context.text(FontManager.mc.font, text, x, y, color.getRGB(), false);
            }
        }
        finally {
            FontManager.popClientTextRendering();
        }
    }

    public void drawTextWithShadow(GuiGraphicsExtractor context, String text, int x, int y, Color color) {
        this.drawTextWithShadow(context, text, x, y, color, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void drawTextWithShadow(GuiGraphicsExtractor context, String text, int x, int y, Color color, boolean glow) {
        FontManager.pushClientTextRendering();
        try {
            boolean effectiveGlow;
            boolean bl = effectiveGlow = glow || FontManager.isHudRendering() && this.isHudGlowEnabled();
            if (this.useCustomFont(effectiveGlow)) {
                if (effectiveGlow) {
                    this.fontRenderer.drawString(context, text, (float)x, (float)y, color.getRGB(), false, true, false);
                }
                if (this.shadowEnabled()) {
                    this.fontRenderer.drawString(context, text, (float)x + this.getShadowOffset(), (float)y + this.getShadowOffset(), color.getRGB(), true, false, true);
                }
                this.fontRenderer.drawString(context, text, (float)x, (float)y, color.getRGB(), false, false, true);
            } else {
                context.text(FontManager.mc.font, text, x, y, color.getRGB(), this.shadowEnabled());
            }
        }
        finally {
            FontManager.popClientTextRendering();
        }
    }

    public void drawText(GuiGraphicsExtractor context, FormattedCharSequence text, int x, int y, Color color) {
        this.drawText(context, text, x, y, color, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void drawText(GuiGraphicsExtractor context, FormattedCharSequence text, int x, int y, Color color, boolean glow) {
        FontManager.pushClientTextRendering();
        try {
            boolean effectiveGlow;
            boolean bl = effectiveGlow = glow || FontManager.isHudRendering() && this.isHudGlowEnabled();
            if (this.useCustomFont(effectiveGlow)) {
                if (effectiveGlow) {
                    this.fontRenderer.drawText(context, text, (float)x, (float)y, color.getRGB(), false, true, false);
                }
                this.fontRenderer.drawText(context, text, (float)x, (float)y, color.getRGB(), false, false, true);
            } else {
                context.text(FontManager.mc.font, text, x, y, color.getRGB(), false);
            }
        }
        finally {
            FontManager.popClientTextRendering();
        }
    }

    public void drawTextWithShadow(GuiGraphicsExtractor context, FormattedCharSequence text, int x, int y, Color color) {
        this.drawTextWithShadow(context, text, x, y, color, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void drawTextWithShadow(GuiGraphicsExtractor context, FormattedCharSequence text, int x, int y, Color color, boolean glow) {
        FontManager.pushClientTextRendering();
        try {
            boolean effectiveGlow;
            boolean bl = effectiveGlow = glow || FontManager.isHudRendering() && this.isHudGlowEnabled();
            if (this.useCustomFont(effectiveGlow)) {
                if (effectiveGlow) {
                    this.fontRenderer.drawText(context, text, (float)x, (float)y, color.getRGB(), false, true, false);
                }
                if (this.shadowEnabled()) {
                    this.fontRenderer.drawText(context, text, (float)x + this.getShadowOffset(), (float)y + this.getShadowOffset(), color.getRGB(), true, false, true);
                }
                this.fontRenderer.drawText(context, text, (float)x, (float)y, color.getRGB(), false, false, true);
            } else {
                context.text(FontManager.mc.font, text, x, y, color.getRGB(), this.shadowEnabled());
            }
        }
        finally {
            FontManager.popClientTextRendering();
        }
    }

    public void drawTextWithShadow(PoseStack matrices, String text, int x, int y, Color color) {
        this.drawTextWithShadow(matrices, text, (float)x, (float)y, color, false);
    }

    public void drawTextWithShadow(PoseStack matrices, String text, int x, int y, Color color, boolean glow) {
        this.drawTextWithShadow(matrices, text, (float)x, (float)y, color, glow);
    }

    public void drawTextWithShadow(PoseStack matrices, String text, float x, float y, Color color) {
        this.drawTextWithShadow(matrices, text, x, y, color, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void drawTextWithShadow(final PoseStack matrices, String text, float x, float y, Color color, boolean glow) {
        block11: {
            FontManager.pushClientTextRendering();
            try {
                boolean effectiveGlow;
                boolean bl = effectiveGlow = glow || FontManager.isHudRendering() && this.isHudGlowEnabled();
                if (this.useCustomFont(effectiveGlow)) {
                    if (effectiveGlow) {
                        this.fontRenderer.drawString(matrices, text, x, y, color.getRGB(), false, true, false);
                    }
                    if (this.shadowEnabled() && !glow) {
                        this.fontRenderer.drawString(matrices, text, x + this.getShadowOffset(), y + this.getShadowOffset(), color.getRGB(), true, false, true);
                    }
                    this.fontRenderer.drawString(matrices, text, x, y, color.getRGB(), false, false, true);
                    break block11;
                }
                Font.PreparedText prepared = FontManager.mc.font.prepareText(text, x, y, color.getRGB(), this.shadowEnabled() && !glow, 0);
                final Map<RenderType, ByteBufferBuilder> owners = new HashMap<>();
                final Map<RenderType, BufferBuilder> buffers = new HashMap<>();
                try {
                    prepared.visit(new Font.GlyphVisitor(){
                        

                        public void acceptRenderable(TextRenderable renderable) {
                            RenderType renderType = renderable.renderType(Font.DisplayMode.SEE_THROUGH);
                            BufferBuilder buffer = buffers.computeIfAbsent(renderType, rt -> {
                                ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(256);
                                owners.put(rt, byteBufferBuilder);
                                return new BufferBuilder(byteBufferBuilder, rt.primitiveTopology(), rt.format());
                            });
                            renderable.render((Matrix4fc)matrices.last().pose(), (VertexConsumer)buffer, 0xF000F0, false);
                        }
                    });
                    for (Map.Entry<RenderType, BufferBuilder> entry : buffers.entrySet()) {
                        MeshData mesh = ((BufferBuilder)entry.getValue()).build();
                        Renderer3D.draw((RenderType)entry.getKey(), mesh);
                    }
                }
                finally {
                    for (ByteBufferBuilder byteBufferBuilder : owners.values()) {
                        byteBufferBuilder.close();
                    }
                }
            }
            finally {
                FontManager.popClientTextRendering();
            }
        }
    }

    public void drawText(PoseStack matrices, String text, int x, int y, Color color) {
        this.drawText(matrices, text, (float)x, (float)y, color, false);
    }

    public void drawText(PoseStack matrices, String text, int x, int y, Color color, boolean glow) {
        this.drawText(matrices, text, (float)x, (float)y, color, glow);
    }

    public void drawText(PoseStack matrices, String text, float x, float y, Color color) {
        this.drawText(matrices, text, x, y, color, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void drawText(final PoseStack matrices, String text, float x, float y, Color color, boolean glow) {
        block10: {
            FontManager.pushClientTextRendering();
            try {
                boolean effectiveGlow;
                boolean bl = effectiveGlow = glow || FontManager.isHudRendering() && this.isHudGlowEnabled();
                if (this.useCustomFont(effectiveGlow)) {
                    if (effectiveGlow) {
                        this.fontRenderer.drawString(matrices, text, x, y, color.getRGB(), false, true, false);
                    }
                    this.fontRenderer.drawString(matrices, text, x, y, color.getRGB(), false, false, true);
                    break block10;
                }
                Font.PreparedText prepared = FontManager.mc.font.prepareText(text, x, y, color.getRGB(), false, 0);
                final Map<RenderType, ByteBufferBuilder> owners = new HashMap<>();
                final Map<RenderType, BufferBuilder> buffers = new HashMap<>();
                try {
                    prepared.visit(new Font.GlyphVisitor(){
                        

                        public void acceptRenderable(TextRenderable renderable) {
                            RenderType renderType = renderable.renderType(Font.DisplayMode.SEE_THROUGH);
                            BufferBuilder buffer = buffers.computeIfAbsent(renderType, rt -> {
                                ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(256);
                                owners.put(rt, byteBufferBuilder);
                                return new BufferBuilder(byteBufferBuilder, rt.primitiveTopology(), rt.format());
                            });
                            renderable.render((Matrix4fc)matrices.last().pose(), (VertexConsumer)buffer, 0xF000F0, false);
                        }
                    });
                    for (Map.Entry<RenderType, BufferBuilder> entry : buffers.entrySet()) {
                        MeshData mesh = ((BufferBuilder)entry.getValue()).build();
                        Renderer3D.draw((RenderType)entry.getKey(), mesh);
                    }
                }
                finally {
                    for (ByteBufferBuilder byteBufferBuilder : owners.values()) {
                        byteBufferBuilder.close();
                    }
                }
            }
            finally {
                FontManager.popClientTextRendering();
            }
        }
    }

    public void drawTextWithOutline(GuiGraphicsExtractor context, String text, int x, int y, Color color, Color outlineColor) {
        if (this.useCustomFont()) {
            this.fontRenderer.drawString(context, FontRenderer.stripControlCodes(text), (float)x + 0.5f, (float)y - 0.5f, outlineColor.getRGB(), false);
            this.fontRenderer.drawString(context, FontRenderer.stripControlCodes(text), (float)x - 0.5f, (float)y + 0.5f, outlineColor.getRGB(), false);
            this.fontRenderer.drawString(context, FontRenderer.stripControlCodes(text), (float)x + 0.5f, (float)y + 0.5f, outlineColor.getRGB(), false);
            this.fontRenderer.drawString(context, FontRenderer.stripControlCodes(text), (float)x - 0.5f, (float)y - 0.5f, outlineColor.getRGB(), false);
            this.fontRenderer.drawString(context, text, (float)x, (float)y, color.getRGB(), false);
        } else {
            for (int xo = -1; xo <= 1; ++xo) {
                for (int yo = -1; yo <= 1; ++yo) {
                    if (xo == 0 && yo == 0) continue;
                    context.text(FontManager.mc.font, text, x + xo, y + yo, outlineColor.getRGB(), false);
                }
            }
            context.text(FontManager.mc.font, text, x, y, color.getRGB(), false);
        }
    }

    public void drawRainbowString(GuiGraphicsExtractor context, String string, int x, int y, long offset) {
        MutableComponent builder = Component.empty();
        int[] i = new int[]{0};
        Component.literal((String)string).getVisualOrderText().accept((index, style, codePoint) -> {
            MutableComponent text = Component.empty();
            if (style.getColor() == null) {
                long index1 = (long)i[0] * offset;
                Color color = ColorUtils.getOffsetRainbow(index1);
                text.append((Component)Component.literal((String)String.valueOf(Character.toChars(codePoint))).setStyle(Style.EMPTY.withColor(TextColor.fromRgb((int)color.getRGB()))));
            } else {
                text.append((Component)Component.literal((String)String.valueOf(Character.toChars(codePoint))).setStyle(style));
            }
            builder.append((Component)text);
            i[0] = i[0] + 1;
            return true;
        });
        Night.FONT_MANAGER.drawTextWithShadow(context, builder.getVisualOrderText(), x, y, Color.WHITE);
    }

    public int getWidth(String text) {
        if (this.useCustomFont()) {
            return (int)this.fontRenderer.getTextWidth(text);
        }
        return FontManager.mc.font.width(text);
    }

    public int getHeight() {
        if (this.useCustomFont()) {
            FontModule module = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FontModule.class) : null;
            int offset = module != null ? module.heightOffset.getValue().intValue() : 0;
            return (int)this.fontRenderer.getHeight() + offset;
        }
        Objects.requireNonNull(FontManager.mc.font);
        return 9;
    }

    public float getShadowOffset() {
        FontModule module;
        FontModule fontModule = module = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FontModule.class) : null;
        if (module == null || module.shadowMode.getValue().equalsIgnoreCase("None")) {
            return 0.0f;
        }
        if (module.shadowMode.getValue().equalsIgnoreCase("Custom")) {
            return module.shadowOffset.getValue().floatValue();
        }
        return 1.0f;
    }

    public boolean hasFont(String name) {
        for (String fontName : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            if (!fontName.equalsIgnoreCase(name)) continue;
            return true;
        }
        return false;
    }

    @Generated
    public FontRenderer getFontRenderer() {
        return this.fontRenderer;
    }

    @Generated
    public void setFontRenderer(FontRenderer fontRenderer) {
        this.fontRenderer = fontRenderer;
    }
}

