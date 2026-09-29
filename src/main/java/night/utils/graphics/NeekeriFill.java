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
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import night.Night;
import night.mixins.accessors.PostEffectProcessorAccessor;
import night.mixins.accessors.PostPassAccessor;
import night.modules.impl.core.ClickGuiModule;
import night.utils.graphics.EspShader;
import org.lwjgl.system.MemoryStack;

public class NeekeriFill {
    private static final Identifier CHAIN_ID = Identifier.fromNamespaceAndPath((String)"night", (String)"neekeri_ui");
    private static final Identifier PATTERN_TARGET_ID = Identifier.withDefaultNamespace((String)"pattern");
    private static final Identifier TEXTURE_ID = Identifier.fromNamespaceAndPath((String)"night", (String)"neekeri_ui_pattern");
    private static final String UNIFORM_NAME = "NeekeriConfig";
    private static final int UBO_SIZE = new Std140SizeCalculator().putVec4().get();
    private static PostChain chain;
    private static GpuBuffer uboBuffer;
    private static long lastProcessedAtMs;
    private static PatternTexture texture;
    private static float phaseSeconds;
    private static long lastPhaseUpdateMs;

    private static void ensureProcessed() {
        long now = System.currentTimeMillis();
        if (now - lastProcessedAtMs < 10L) {
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
        try {
            NeekeriFill.writeTime();
            chain.process(Minecraft.getInstance().gameRenderer.mainRenderTarget(), GraphicsResourceAllocator.UNPOOLED);
        }
        catch (IllegalStateException e) {
            return;
        }
        RenderTarget pattern = ((PostEffectProcessorAccessor)chain).getPersistentTargets().get(PATTERN_TARGET_ID);
        if (pattern == null || pattern.getColorTextureView() == null) {
            return;
        }
        if (texture == null || !texture.wraps(pattern.getColorTextureView())) {
            if (texture != null) {
                Minecraft.getInstance().getTextureManager().release(TEXTURE_ID);
            }
            texture = new PatternTexture(pattern.getColorTexture(), pattern.getColorTextureView());
            Minecraft.getInstance().getTextureManager().register(TEXTURE_ID, (AbstractTexture)texture);
        }
    }

    private static void writeTime() {
        for (PostPass pass : ((PostEffectProcessorAccessor)chain).getPasses()) {
            GpuBuffer displaced;
            Map<String, GpuBuffer> uniforms = ((PostPassAccessor)pass).night$getCustomUniforms();
            if (!uniforms.containsKey(UNIFORM_NAME)) continue;
            if ((uboBuffer == null || uniforms.get(UNIFORM_NAME) != uboBuffer) && (displaced = uniforms.put(UNIFORM_NAME, uboBuffer = RenderSystem.getDevice().createBuffer(() -> "Cheats MC neekeri fill UBO", 136, (long)UBO_SIZE))) != null) {
                displaced.close();
            }
            ClickGuiModule clickGui = Night.MODULE_MANAGER.getModule(ClickGuiModule.class);
            long now = System.currentTimeMillis();
            float speed = clickGui.neekeriSpeed.getValue().floatValue();
            if (lastPhaseUpdateMs >= 0L) {
                phaseSeconds += (float)(now - lastPhaseUpdateMs) / 1000.0f * speed;
            }
            lastPhaseUpdateMs = now;
            int effect = Arrays.asList(EspShader.MODES).indexOf(clickGui.fillMode.getValue());
            if (effect <= 0) {
                effect = EspShader.MODES.length - 1;
            }
            MemoryStack stack = MemoryStack.stackPush();
            try {
                ByteBuffer data = Std140Builder.onStack((MemoryStack)stack, (int)UBO_SIZE).putVec4(phaseSeconds, (float)effect, 0.0f, 0.0f).get();
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(uboBuffer.slice(), data);
            }
            finally {
                if (stack == null) continue;
                stack.close();
            }
        }
    }

    public static void fill(GuiGraphicsExtractor context, int x, int y, int width, int height, int alpha) {
        NeekeriFill.ensureProcessed();
        if (texture == null) {
            return;
        }
        int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_ID, x, y, (float)x, (float)y, width, height, width, height, screenW, screenH, alpha << 24 | 0xFFFFFF);
    }

    static {
        lastProcessedAtMs = -1L;
        phaseSeconds = 0.0f;
        lastPhaseUpdateMs = -1L;
    }

    private static class PatternTexture
    extends AbstractTexture {
        PatternTexture(GpuTexture texture, GpuTextureView view) {
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

