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
import night.utils.IMinecraft;
import night.utils.graphics.Renderer2D;
import org.lwjgl.system.MemoryStack;

public class MenuShader
implements IMinecraft {
    private static final Identifier CHAIN_ID = Identifier.fromNamespaceAndPath((String)"night", (String)"menu_bg");
    private static final Identifier TARGET_ID = Identifier.withDefaultNamespace((String)"menu_bg_target");
    private static final Identifier TEXTURE_ID = Identifier.fromNamespaceAndPath((String)"night", (String)"menu_bg_texture");
    private static final String UNIFORM_NAME = "MenuBgConfig";
    private static final int UBO_SIZE = new Std140SizeCalculator().putVec4().align(16).get();
    private static PostChain chain;
    private static GpuBuffer uboBuffer;
    private static long lastProcessedAtMs;
    private static MenuTexture texture;
    private static final long START_TIME;
    private static final Identifier STATIC_BG;

    private static void ensureProcessed(float alpha, float transitionProgress, boolean resourcesReady) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getShaderManager() == null || mc.gameRenderer == null || mc.gameRenderer.mainRenderTarget() == null) {
            return;
        }
        if (!resourcesReady) {
            return;
        }
        try {
            PostChain freshChain = mc.getShaderManager().getPostChain(CHAIN_ID, LevelTargetBundle.MAIN_TARGETS);
            if (freshChain != chain) {
                chain = freshChain;
                uboBuffer = null;
            }
            if (chain == null) {
                return;
            }
            MenuShader.writeUniforms(alpha, transitionProgress);
            chain.process(mc.gameRenderer.mainRenderTarget(), GraphicsResourceAllocator.UNPOOLED);
            RenderTarget target = ((PostEffectProcessorAccessor)chain).getPersistentTargets().get(TARGET_ID);
            if (target == null || target.getColorTextureView() == null) {
                return;
            }
            if (texture == null || !texture.wraps(target.getColorTextureView())) {
                if (texture != null) {
                    mc.getTextureManager().release(TEXTURE_ID);
                }
                texture = new MenuTexture(target.getColorTexture(), target.getColorTextureView());
                mc.getTextureManager().register(TEXTURE_ID, (AbstractTexture)texture);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void writeUniforms(float alpha, float transitionProgress) {
        if (chain == null) {
            return;
        }
        for (PostPass pass : ((PostEffectProcessorAccessor)chain).getPasses()) {
            GpuBuffer displaced;
            Map<String, GpuBuffer> uniforms = ((PostPassAccessor)pass).night$getCustomUniforms();
            if (!uniforms.containsKey(UNIFORM_NAME)) continue;
            if ((uboBuffer == null || uniforms.get(UNIFORM_NAME) != uboBuffer) && (displaced = uniforms.put(UNIFORM_NAME, uboBuffer = RenderSystem.getDevice().createBuffer(() -> "Cheats MC Menu BG UBO", 136, (long)UBO_SIZE))) != null) {
                displaced.close();
            }
            float time = (float)(System.currentTimeMillis() - START_TIME) / 1000.0f;
            MemoryStack stack = MemoryStack.stackPush();
            try {
                ByteBuffer data = Std140Builder.onStack((MemoryStack)stack, (int)UBO_SIZE).putVec4(time, alpha, transitionProgress, 0.0f).get();
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(uboBuffer.slice(), data);
            }
            finally {
                if (stack == null) continue;
                stack.close();
            }
        }
    }

    public static void render(GuiGraphicsExtractor context, int screenW, int screenH, float alpha, float transitionProgress) {
        MenuShader.render(context, screenW, screenH, alpha, transitionProgress, true);
    }

    public static void render(GuiGraphicsExtractor context, int screenW, int screenH, float alpha, float transitionProgress, boolean resourcesReady) {
        try {
            MenuShader.ensureProcessed(alpha, transitionProgress, resourcesReady);
            int a = Math.min(255, Math.max(0, Math.round(alpha * 255.0f)));
            int colorMask = a << 24 | 0xFFFFFF;
            if (texture != null) {
                context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_ID, 0, 0, 0.0f, 0.0f, screenW, screenH, screenW, screenH, screenW, screenH, colorMask);
            } else {
                context.blit(RenderPipelines.GUI_TEXTURED, STATIC_BG, 0, 0, 0.0f, 0.0f, screenW, screenH, screenW, screenH, screenW, screenH, colorMask);
            }
        }
        catch (Throwable t) {
            Renderer2D.renderQuad(context, 0.0f, 0.0f, screenW, screenH, new Color(8, 8, 12, (int)(255.0f * alpha)));
        }
    }

    public static void render(GuiGraphicsExtractor context, int screenW, int screenH, float alpha) {
        MenuShader.render(context, screenW, screenH, alpha, 1.0f);
    }

    static {
        lastProcessedAtMs = -1L;
        START_TIME = System.currentTimeMillis();
        STATIC_BG = Identifier.fromNamespaceAndPath((String)"night", (String)"textures/gui/moonlight_bg.png");
    }

    private static class MenuTexture
    extends AbstractTexture {
        MenuTexture(GpuTexture texture, GpuTextureView view) {
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

