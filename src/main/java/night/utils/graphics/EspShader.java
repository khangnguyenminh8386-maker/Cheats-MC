/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.IndexType
 *  com.mojang.blaze3d.PrimitiveTopology
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.buffers.Std140Builder
 *  com.mojang.blaze3d.buffers.Std140SizeCalculator
 *  com.mojang.blaze3d.pipeline.BindGroupLayout
 *  com.mojang.blaze3d.pipeline.DepthStencilState
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.mojang.blaze3d.platform.CompareOp
 *  com.mojang.blaze3d.shaders.UniformType
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.systems.RenderSystem$AutoStorageIndexBuffer
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.ByteBufferBuilder
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.MeshData
 *  net.minecraft.client.renderer.PostChain
 *  net.minecraft.client.renderer.PostPass
 *  net.minecraft.client.renderer.rendertype.OutputTarget
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Vector2f
 *  org.joml.Vector3f
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.lwjgl.system.MemoryStack
 */
package night.utils.graphics;

import com.mojang.blaze3d.IndexType;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.mixins.accessors.PostEffectProcessorAccessor;
import night.mixins.accessors.PostPassAccessor;
import night.mixins.accessors.RenderPipelinesAccessor;
import night.modules.impl.core.ColorModule;
import night.modules.impl.visuals.ShadersModule;
import night.utils.IMinecraft;
import night.utils.graphics.Renderer3D;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;

public class EspShader
implements IMinecraft {
    public static final String[] MODES = new String[]{"None", "Gradient", "Rainbow", "Garmadon", "Nebula", "Blue", "Purple", "Glowing", "Pink", "Neekeri", "Image"};
    private static final Identifier FRAGMENT = Identifier.fromNamespaceAndPath((String)"night", (String)"core/esp");
    private static final BindGroupLayout ESP_SETTINGS_LAYOUT = BindGroupLayout.builder().withUniform("EspSettings", UniformType.UNIFORM_BUFFER).build();
    private static final RenderPipeline QUADS_PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getDebugFilledSnippet()}).withLocation("night/esp_shader_quads").withFragmentShader(FRAGMENT).withBindGroupLayout(ESP_SETTINGS_LAYOUT).withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).build();
    private static final RenderPipeline LINES_PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getLinesSnippet()}).withLocation("night/esp_shader_lines").withFragmentShader(FRAGMENT).withBindGroupLayout(ESP_SETTINGS_LAYOUT).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).build();
    private static final int UBO_SIZE = new Std140SizeCalculator().putVec4().putVec4().putVec4().putVec4().putVec2().putFloat().putFloat().putFloat().putFloat().putInt().putVec2().align(16).get();
    private static final String OUTLINE_UNIFORM = "EspSettings";
    private static final int OUTLINE_UBO_SIZE = new Std140SizeCalculator().putVec4().putVec4().putVec4().putVec4().putVec4().putVec4().get();
    private static final String STAR_GLOW_UNIFORM = "StarGlowConfig";
    private static final String STAR_GLOW_UNIFORM_V = "StarGlowConfigV";
    private static final int STAR_GLOW_UBO_SIZE = new Std140SizeCalculator().putVec4().get();
    private static GpuBuffer starGlowUboBuffer;
    private static GpuBuffer starGlowUboBufferV;
    private static final String OUTLINE_GLOW_UNIFORM = "OutlineGlowConfig";
    private static final String OUTLINE_GLOW_UNIFORM_V = "OutlineGlowConfigV";
    private static final int OUTLINE_GLOW_UBO_SIZE;
    private static GpuBuffer outlineGlowUboBuffer;
    private static GpuBuffer outlineGlowUboBufferV;
    private static final String BLOOM_UNIFORM = "BloomConfig";
    private static final int BLOOM_UBO_SIZE;
    private static GpuBuffer bloomUboBuffer;
    private static GpuBuffer uboBuffer;
    private static GpuBuffer outlineUboBuffer;
    private static Settings settings;
    public static final String[] SHADER_ACTIVE_MODES;

    public static void writeStarGlowSettings(PostChain chain, float intensity) {
        float radius = Math.min(4.0f + intensity * 0.05f, 14.0f);
        for (PostPass pass : ((PostEffectProcessorAccessor)chain).getPasses()) {
            ByteBuffer data;
            MemoryStack stack;
            GpuBuffer displaced;
            Map<String, GpuBuffer> uniforms = ((PostPassAccessor)pass).night$getCustomUniforms();
            if (uniforms.containsKey(STAR_GLOW_UNIFORM)) {
                if ((starGlowUboBuffer == null || uniforms.get(STAR_GLOW_UNIFORM) != starGlowUboBuffer) && (displaced = uniforms.put(STAR_GLOW_UNIFORM, starGlowUboBuffer = RenderSystem.getDevice().createBuffer(() -> "Cheats MC star glow H shader UBO", 136, (long)STAR_GLOW_UBO_SIZE))) != null) {
                    displaced.close();
                }
                stack = MemoryStack.stackPush();
                try {
                    data = Std140Builder.onStack((MemoryStack)stack, (int)STAR_GLOW_UBO_SIZE).putVec4(intensity, radius, 1.0f, 0.0f).get();
                    RenderSystem.getDevice().createCommandEncoder().writeToBuffer(starGlowUboBuffer.slice(), data);
                }
                finally {
                    if (stack != null) {
                        stack.close();
                    }
                }
            }
            if (!uniforms.containsKey(STAR_GLOW_UNIFORM_V)) continue;
            if ((starGlowUboBufferV == null || uniforms.get(STAR_GLOW_UNIFORM_V) != starGlowUboBufferV) && (displaced = uniforms.put(STAR_GLOW_UNIFORM_V, starGlowUboBufferV = RenderSystem.getDevice().createBuffer(() -> "Cheats MC star glow V shader UBO", 136, (long)STAR_GLOW_UBO_SIZE))) != null) {
                displaced.close();
            }
            stack = MemoryStack.stackPush();
            try {
                data = Std140Builder.onStack((MemoryStack)stack, (int)STAR_GLOW_UBO_SIZE).putVec4(intensity, radius, 0.0f, 1.0f).get();
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(starGlowUboBufferV.slice(), data);
            }
            finally {
                if (stack == null) continue;
                stack.close();
            }
        }
    }

    public static int modeIndex(String mode) {
        for (int i = 0; i < MODES.length; ++i) {
            if (!MODES[i].equalsIgnoreCase(mode)) continue;
            return i;
        }
        return 0;
    }

    public static void setSettings(Settings value) {
        settings = value;
    }

    public static void writeOutlineSettings(PostChain chain, Settings settings, float glow) {
        float glowRadius = Math.min(6.0f + glow * 18.0f, 24.0f);
        for (PostPass pass : ((PostEffectProcessorAccessor)chain).getPasses()) {
            GpuBuffer displaced;
            ByteBuffer data;
            ColorModule cm;
            ShadersModule shaders;
            GpuBuffer displaced2;
            Map<String, GpuBuffer> uniforms = ((PostPassAccessor)pass).night$getCustomUniforms();
            if (uniforms.containsKey(BLOOM_UNIFORM)) {
                if ((bloomUboBuffer == null || uniforms.get(BLOOM_UNIFORM) != bloomUboBuffer) && (displaced2 = uniforms.put(BLOOM_UNIFORM, bloomUboBuffer = RenderSystem.getDevice().createBuffer(() -> "Cheats MC outline bloom UBO", 136, (long)BLOOM_UBO_SIZE))) != null) {
                    displaced2.close();
                }
                shaders = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ShadersModule.class) : null;
                float width = shaders != null ? shaders.bloomWidth.getValue().floatValue() : 3.0f;
                float glowInside = shaders != null && shaders.bloomGlowInside.getValue() ? 1.0f : 0.0f;
                float quality = shaders != null ? shaders.bloomGlowQuality.getValue().floatValue() : 1.0f;
                float multiplier = shaders != null ? shaders.bloomGlowMultiplier.getValue().floatValue() : 1.0f;
                float fillAlpha = shaders != null ? shaders.bloomFillAlpha.getValue().floatValue() / 100.0f : 1.0f;
                float outlineAlpha = shaders != null ? shaders.bloomOutlineAlpha.getValue().floatValue() / 100.0f : 1.0f;
                float fillMode = shaders != null ? (float)ShadersModule.getBloomFillModeIndex(shaders.bloomFillMode.getValue()) : 0.0f;
                float gradFactor = shaders != null ? shaders.bloomGradientFactor.getValue().floatValue() : 200.0f;
                Color gradColor = shaders != null ? shaders.bloomGradientColor.getColor() : Color.RED;
                float time = System.currentTimeMillis() % 2000000L;
                try (MemoryStack stack = MemoryStack.stackPush();){
                    ByteBuffer data2 = Std140Builder.onStack((MemoryStack)stack, (int)BLOOM_UBO_SIZE).putVec4(width, glowInside, quality, multiplier).putVec4(fillAlpha, outlineAlpha, fillMode, gradFactor).putVec4((float)gradColor.getRed() / 255.0f, (float)gradColor.getGreen() / 255.0f, (float)gradColor.getBlue() / 255.0f, (float)gradColor.getAlpha() / 255.0f).putVec4(time, 0.0f, (float)mc.getWindow().getWidth(), (float)mc.getWindow().getHeight()).get();
                    RenderSystem.getDevice().createCommandEncoder().writeToBuffer(bloomUboBuffer.slice(), data2);
                }
            }
            if (uniforms.containsKey(OUTLINE_GLOW_UNIFORM)) {
                if ((outlineGlowUboBuffer == null || uniforms.get(OUTLINE_GLOW_UNIFORM) != outlineGlowUboBuffer) && (displaced2 = uniforms.put(OUTLINE_GLOW_UNIFORM, outlineGlowUboBuffer = RenderSystem.getDevice().createBuffer(() -> "Cheats MC outline glow H UBO", 136, (long)OUTLINE_GLOW_UBO_SIZE))) != null) {
                    displaced2.close();
                }
                shaders = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ShadersModule.class) : null;
                Color gColor = shaders != null && shaders.glowColor != null ? shaders.glowColor.getColor() : Color.MAGENTA;
                try (MemoryStack stack = MemoryStack.stackPush();){
                    ByteBuffer data3 = Std140Builder.onStack((MemoryStack)stack, (int)OUTLINE_GLOW_UBO_SIZE).putVec4(glow, glowRadius, 1.0f, 0.0f).putVec4((float)gColor.getRed() / 255.0f, (float)gColor.getGreen() / 255.0f, (float)gColor.getBlue() / 255.0f, (float)gColor.getAlpha() / 255.0f).get();
                    RenderSystem.getDevice().createCommandEncoder().writeToBuffer(outlineGlowUboBuffer.slice(), data3);
                }
            }
            shaders = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ShadersModule.class) : null;
            ColorModule colorModule = cm = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ColorModule.class) : null;
            if (uniforms.containsKey(OUTLINE_GLOW_UNIFORM_V)) {
                GpuBuffer displaced3;
                if ((outlineGlowUboBufferV == null || uniforms.get(OUTLINE_GLOW_UNIFORM_V) != outlineGlowUboBufferV) && (displaced3 = uniforms.put(OUTLINE_GLOW_UNIFORM_V, outlineGlowUboBufferV = RenderSystem.getDevice().createBuffer(() -> "Cheats MC outline glow V UBO", 136, (long)OUTLINE_GLOW_UBO_SIZE))) != null) {
                    displaced3.close();
                }
                Color gColor = shaders != null && shaders.glowColor != null ? shaders.glowColor.getColor() : Color.MAGENTA;
                boolean isGlowRainbow = shaders != null && shaders.glowColor != null && (shaders.glowColor.isRainbow() || shaders.glowColor.isSync() && cm != null && cm.color.isRainbow());
                try (MemoryStack stack = MemoryStack.stackPush();){
                    data = Std140Builder.onStack((MemoryStack)stack, (int)OUTLINE_GLOW_UBO_SIZE).putVec4(glow, glowRadius, isGlowRainbow ? 1.0f : 0.0f, 1.0f).putVec4((float)gColor.getRed() / 255.0f, (float)gColor.getGreen() / 255.0f, (float)gColor.getBlue() / 255.0f, (float)gColor.getAlpha() / 255.0f).get();
                    RenderSystem.getDevice().createCommandEncoder().writeToBuffer(outlineGlowUboBufferV.slice(), data);
                }
            }
            if (!uniforms.containsKey(OUTLINE_UNIFORM)) continue;
            if ((outlineUboBuffer == null || uniforms.get(OUTLINE_UNIFORM) != outlineUboBuffer) && (displaced = uniforms.put(OUTLINE_UNIFORM, outlineUboBuffer = RenderSystem.getDevice().createBuffer(() -> "Cheats MC outline shader UBO", 136, (long)OUTLINE_UBO_SIZE))) != null) {
                displaced.close();
            }
            boolean isColorRainbow = shaders != null && shaders.color != null && (shaders.color.isRainbow() || shaders.color.isSync() && cm != null && cm.color.isRainbow());
            float rainbowSat = cm != null ? cm.rainbowSaturation.getValue().floatValue() / 100.0f : 1.0f;
            MemoryStack stack = MemoryStack.stackPush();
            try {
                data = Std140Builder.onStack((MemoryStack)stack, (int)OUTLINE_UBO_SIZE).putVec4((Vector4fc)settings.color0).putVec4((Vector4fc)settings.color1).putVec4((Vector4fc)settings.color2).putVec4((Vector4fc)settings.color3).putVec4(settings.time, settings.step, settings.distance, (float)settings.effect).putVec4(settings.alpha, glow, isColorRainbow ? 1.0f : 0.0f, rainbowSat).get();
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(outlineUboBuffer.slice(), data);
            }
            finally {
                if (stack == null) continue;
                stack.close();
            }
        }
    }

    public static Settings createSettings(String mode, float opacity, float speed, float step, Color c1, Color c2, Color c3, Color c4, Color glow, Vec3 pos) {
        return EspShader.createSettings(mode, opacity, speed, step, c1, c2, c3, c4, glow, pos, new Vector2f(0.0f, 0.0f));
    }

    public static Settings createSettings(String mode, float opacity, float speed, float step, Color c1, Color c2, Color c3, Color c4, Color glow, Vec3 pos, Vector2f offset) {
        int effect = EspShader.modeIndex(mode);
        if (effect == 0) {
            return null;
        }
        float time = (float)(System.currentTimeMillis() % 2000000L) / 1000.0f * speed * (effect == 1 ? step : 1.0f);
        float distance = pos != null && EspShader.mc.player != null ? (float)Math.sqrt(EspShader.mc.player.distanceToSqr(pos)) : 1.0f;
        return new Settings(effect, time, step, opacity / 100.0f, distance, effect == 7 ? glow : c1, c2, c3, c4, offset);
    }

    public static void draw(List<Renderer3D.VertexCollection> quads, List<Renderer3D.VertexCollection> debugLines) {
        EspShader.draw(quads, debugLines, settings);
    }

    public static void draw(List<Renderer3D.VertexCollection> quads, List<Renderer3D.VertexCollection> debugLines, Settings settings) {
        BufferBuilder buffer;
        ByteBufferBuilder byteBufferBuilder;
        if (settings == null || quads.isEmpty() && debugLines.isEmpty()) {
            quads.clear();
            debugLines.clear();
            return;
        }
        GpuBuffer ubo = RenderSystem.getDevice().createBuffer(() -> "Cheats MC ESP shader UBO", 136, (long)UBO_SIZE);
        try (MemoryStack stack = MemoryStack.stackPush();){
            ByteBuffer data = Std140Builder.onStack((MemoryStack)stack, (int)UBO_SIZE).putVec4((Vector4fc)settings.color0).putVec4((Vector4fc)settings.color1).putVec4((Vector4fc)settings.color2).putVec4((Vector4fc)settings.color3).putVec2((float)mc.getWindow().getWidth(), (float)mc.getWindow().getHeight()).putFloat(settings.time).putFloat(settings.step).putFloat(settings.alpha).putFloat(settings.distance).putInt(settings.effect).putVec2(settings.offset.x, settings.offset.y).get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(ubo.slice(), data);
        }
        if (!quads.isEmpty()) {
            int vertexCount = Renderer3D.vertexCount(quads);
            byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(vertexCount * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));
            try {
                buffer = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
                for (Renderer3D.VertexCollection collection : quads) {
                    collection.quad(buffer);
                }
                EspShader.submit(QUADS_PIPELINE, buffer.build(), ubo);
            }
            finally {
                if (byteBufferBuilder != null) {
                    byteBufferBuilder.close();
                }
            }
        }
        if (!debugLines.isEmpty()) {
            int vertexCount = Renderer3D.vertexCount(debugLines) * 2;
            byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(vertexCount * DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH.getVertexSize()));
            try {
                buffer = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);
                Renderer3D.buildLines(buffer, debugLines);
                EspShader.submit(LINES_PIPELINE, buffer.build(), ubo);
            }
            finally {
                if (byteBufferBuilder != null) {
                    byteBufferBuilder.close();
                }
            }
        }
        ubo.close();
        quads.clear();
        debugLines.clear();
    }

    private static void submit(RenderPipeline pipeline, MeshData mesh, GpuBuffer ubo) {
        if (mesh == null) {
            return;
        }
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrixCopy(), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector3f(), new Matrix4f());
        try (MeshData meshData = mesh;){
            GpuTextureView colorTexture;
            IndexType indexType;
            GpuBuffer indices;
            GpuBuffer vertices = RenderSystem.getDevice().createBuffer(() -> "Cheats MC ESP shader vertices", 32, mesh.vertexBuffer());
            GpuBuffer ownedIndices = null;
            if (mesh.indexBuffer() == null) {
                RenderSystem.AutoStorageIndexBuffer autoIndices = RenderSystem.getSequentialBuffer((PrimitiveTopology)mesh.drawState().primitiveTopology());
                indices = autoIndices.getBuffer(mesh.drawState().indexCount());
                indexType = autoIndices.type();
            } else {
                indices = ownedIndices = RenderSystem.getDevice().createBuffer(() -> "Cheats MC ESP shader indices", 64, mesh.indexBuffer());
                indexType = mesh.drawState().indexType();
            }
            RenderTarget renderTarget = OutputTarget.MAIN_TARGET.getRenderTarget();
            GpuTextureView gpuTextureView = colorTexture = RenderSystem.outputColorTextureOverride != null ? RenderSystem.outputColorTextureOverride : renderTarget.getColorTextureView();
            GpuTextureView depthTexture = renderTarget.useDepth ? (RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : renderTarget.getDepthTextureView()) : null;
            try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Cheats MC ESP shader draw", colorTexture, Optional.empty(), depthTexture, OptionalDouble.empty());){
                renderPass.setPipeline(pipeline);
                RenderSystem.bindDefaultUniforms((RenderPass)renderPass);
                renderPass.setUniform("DynamicTransforms", dynamicTransforms);
                renderPass.setUniform(OUTLINE_UNIFORM, ubo);
                renderPass.setVertexBuffer(0, vertices.slice());
                renderPass.setIndexBuffer(indices, indexType);
                renderPass.drawIndexed(mesh.drawState().indexCount(), 1, 0, 0, 0);
            }
            vertices.close();
            if (ownedIndices != null) {
                ownedIndices.close();
            }
        }
    }

    static {
        OUTLINE_GLOW_UBO_SIZE = new Std140SizeCalculator().putVec4().putVec4().get();
        BLOOM_UBO_SIZE = new Std140SizeCalculator().putVec4().putVec4().putVec4().putVec4().get();
        SHADER_ACTIVE_MODES = Arrays.copyOfRange(MODES, 1, MODES.length);
    }

    public static class Settings {
        public final int effect;
        public final float time;
        public final float step;
        public final float alpha;
        public final float distance;
        public final Vector4f color0;
        public final Vector4f color1;
        public final Vector4f color2;
        public final Vector4f color3;
        public final Vector2f offset;

        public Settings(int effect, float time, float step, float alpha, float distance, Color c0, Color c1, Color c2, Color c3) {
            this(effect, time, step, alpha, distance, c0, c1, c2, c3, new Vector2f(0.0f, 0.0f));
        }

        public Settings(int effect, float time, float step, float alpha, float distance, Color c0, Color c1, Color c2, Color c3, Vector2f offset) {
            this.effect = effect;
            this.time = time;
            this.step = step;
            this.alpha = alpha;
            this.distance = distance;
            this.color0 = Settings.toVector(c0);
            this.color1 = Settings.toVector(c1);
            this.color2 = Settings.toVector(c2);
            this.color3 = Settings.toVector(c3);
            this.offset = offset != null ? offset : new Vector2f(0.0f, 0.0f);
        }

        private static Vector4f toVector(Color color) {
            return new Vector4f((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
        }
    }
}

