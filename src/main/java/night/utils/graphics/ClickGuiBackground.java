/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.buffers.Std140Builder
 *  com.mojang.blaze3d.buffers.Std140SizeCalculator
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.mojang.blaze3d.resource.GraphicsResourceAllocator
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.GpuTexture
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.renderer.LevelTargetBundle
 *  net.minecraft.client.renderer.PostChain
 *  net.minecraft.client.renderer.PostPass
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.resources.Identifier
 *  org.lwjgl.system.MemoryStack
 */
package night.utils.graphics;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import night.mixins.accessors.PostEffectProcessorAccessor;
import night.mixins.accessors.PostPassAccessor;
import org.lwjgl.system.MemoryStack;

public class ClickGuiBackground {
    private static final Identifier CHAIN_ID = Identifier.fromNamespaceAndPath((String)"night", (String)"clickgui_bg");
    private static final Identifier TARGET_ID = Identifier.withDefaultNamespace((String)"clickgui_bg_target");
    private static final Identifier TEXTURE_ID = Identifier.fromNamespaceAndPath((String)"night", (String)"clickgui_bg_texture");
    private static final String UNIFORM_NAME = "ClickGuiBgConfig";
    private static final int UBO_SIZE = new Std140SizeCalculator().putVec4().putVec4().align(16).get();
    private static PostChain chain;
    private static GpuBuffer uboBuffer;
    private static long lastProcessedAtMs;
    private static BgTexture texture;
    private static float phaseSeconds;
    private static long lastPhaseUpdateMs;

    private static void ensureProcessed(int modeIndex, float speed, float opacity, Color themeColor, boolean isRainbow) {
        long now = System.currentTimeMillis();
        if (now - lastProcessedAtMs < 8L) {
            return;
        }
        lastProcessedAtMs = now;
        PostChain freshChain = Minecraft.getInstance().getShaderManager().getPostChain(CHAIN_ID, LevelTargetBundle.MAIN_TARGETS);
        if (freshChain != chain) {
            chain = freshChain;
            uboBuffer = null;
        }
        if (chain == null) {
            return;
        }
        ClickGuiBackground.writeUniforms(modeIndex, speed, opacity, themeColor, isRainbow);
        chain.process(Minecraft.getInstance().gameRenderer.mainRenderTarget(), GraphicsResourceAllocator.UNPOOLED);
        RenderTarget target = ((PostEffectProcessorAccessor)chain).getPersistentTargets().get(TARGET_ID);
        if (target == null || target.getColorTextureView() == null) {
            return;
        }
        if (texture == null || !texture.wraps(target.getColorTextureView())) {
            if (texture != null) {
                Minecraft.getInstance().getTextureManager().release(TEXTURE_ID);
            }
            texture = new BgTexture(target.getColorTexture(), target.getColorTextureView());
            Minecraft.getInstance().getTextureManager().register(TEXTURE_ID, (AbstractTexture)texture);
        }
    }

    private static void writeUniforms(int modeIndex, float speed, float opacity, Color themeColor, boolean isRainbow) {
        for (PostPass pass : ((PostEffectProcessorAccessor)chain).getPasses()) {
            GpuBuffer displaced;
            Map<String, GpuBuffer> uniforms = ((PostPassAccessor)pass).night$getCustomUniforms();
            if (!uniforms.containsKey(UNIFORM_NAME)) continue;
            if ((uboBuffer == null || uniforms.get(UNIFORM_NAME) != uboBuffer) && (displaced = uniforms.put(UNIFORM_NAME, uboBuffer = RenderSystem.getDevice().createBuffer(() -> "Cheats MC ClickGUI BG UBO", 136, (long)UBO_SIZE))) != null) {
                displaced.close();
            }
            long now = System.currentTimeMillis();
            if (lastPhaseUpdateMs >= 0L) {
                phaseSeconds += (float)(now - lastPhaseUpdateMs) / 1000.0f * speed;
            }
            lastPhaseUpdateMs = now;
            float r = (float)themeColor.getRed() / 255.0f;
            float g = (float)themeColor.getGreen() / 255.0f;
            float b = (float)themeColor.getBlue() / 255.0f;
            float rainbowFlag = isRainbow ? 1.0f : 0.0f;
            MemoryStack stack = MemoryStack.stackPush();
            try {
                ByteBuffer data = Std140Builder.onStack((MemoryStack)stack, (int)UBO_SIZE).putVec4(phaseSeconds, (float)modeIndex, opacity, speed).putVec4(r, g, b, rainbowFlag).get();
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(uboBuffer.slice(), data);
            }
            finally {
                if (stack == null) continue;
                stack.close();
            }
        }
    }

    public static void renderBackground(GuiGraphicsExtractor context, int screenW, int screenH, int modeIndex, float speed, float opacity, Color themeColor, boolean isRainbow) {
        if (opacity <= 0.001f || modeIndex < 0) {
            return;
        }
        ClickGuiBackground.ensureProcessed(modeIndex, speed, opacity, themeColor, isRainbow);
        if (texture == null) {
            return;
        }
        int alpha = Math.min(255, Math.max(0, Math.round(opacity * 255.0f)));
        int colorMask = alpha << 24 | 0xFFFFFF;
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_ID, 0, 0, 0.0f, 0.0f, screenW, screenH, screenW, screenH, screenW, screenH, colorMask);
    }

    static {
        lastProcessedAtMs = -1L;
        phaseSeconds = 0.0f;
        lastPhaseUpdateMs = -1L;
    }

    private static class BgTexture
    extends AbstractTexture {
        BgTexture(GpuTexture texture, GpuTextureView view) {
            this.texture = texture;
            this.textureView = view;
        }

        boolean wraps(GpuTextureView view) {
            return this.textureView == view;
        }

        public void close() {
        }
    }
}

