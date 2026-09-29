/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.PrimitiveTopology
 *  com.mojang.blaze3d.buffers.GpuBuffer
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
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  net.minecraft.client.Camera
 *  net.minecraft.client.renderer.rendertype.OutputTarget
 *  net.minecraft.resources.Identifier
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.lwjgl.system.MemoryStack
 */
package night.utils.graphics;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
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
import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalDouble;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.resources.Identifier;
import night.Night;
import night.mixins.accessors.RenderPipelinesAccessor;
import night.modules.impl.visuals.AtmosphereModule;
import night.utils.IMinecraft;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;

public class SkyShader
implements IMinecraft {
    private static final Identifier FRAGMENT = Identifier.fromNamespaceAndPath((String)"night", (String)"core/sky");
    private static final BindGroupLayout SKY_SETTINGS_LAYOUT = BindGroupLayout.builder().withUniform("SkySettings", UniformType.UNIFORM_BUFFER).build();
    private static final RenderPipeline SKY_PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getGlobalsSnippet()}).withLocation("night/sky_shader").withVertexShader("core/screenquad").withFragmentShader(FRAGMENT).withBindGroupLayout(SKY_SETTINGS_LAYOUT).withPrimitiveTopology(PrimitiveTopology.TRIANGLES).withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).build();
    private static final int UBO_SIZE = new Std140SizeCalculator().putVec4().putVec4().putVec4().putVec4().putVec2().putFloat().putFloat().putFloat().putInt().align(16).get();
    private static float time = 0.0f;

    public static void renderSky() {
        GpuTextureView colorTexture;
        AtmosphereModule atmosphere;
        if (SkyShader.mc.level == null || SkyShader.mc.player == null) {
            return;
        }
        AtmosphereModule atmosphereModule = atmosphere = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AtmosphereModule.class) : null;
        if (atmosphere == null || !atmosphere.isToggled() || !atmosphere.modifySky.getValue()) {
            return;
        }
        if (!atmosphere.skyMode.getValue().equalsIgnoreCase("Custom")) {
            return;
        }
        int width = mc.getWindow().getWidth();
        int height = mc.getWindow().getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        float tickDelta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        time += tickDelta * 0.05f * atmosphere.speed.getValue().floatValue();
        int mode = atmosphere.customSkyMode.getValue().equalsIgnoreCase("Smoke") ? 1 : 0;
        Color tint = atmosphere.skyTint.getColor();
        float r = (float)tint.getRed() / 255.0f;
        float g = (float)tint.getGreen() / 255.0f;
        float b = (float)tint.getBlue() / 255.0f;
        float a = (float)tint.getAlpha() / 255.0f;
        Vector4f tintVec = new Vector4f(r, g, b, a);
        Vector4f color1 = new Vector4f(r * 0.1f, g * 0.1f, b * 0.1f, 1.0f);
        Vector4f color2 = new Vector4f(r * 0.8f, g * 0.8f, b * 0.8f, 1.0f);
        float yaw = 0.0f;
        float pitch = 0.0f;
        float fov = (float)Math.toRadians(70.0);
        if (SkyShader.mc.gameRenderer != null && SkyShader.mc.gameRenderer.mainCamera() != null) {
            Camera camera = SkyShader.mc.gameRenderer.mainCamera();
            yaw = (float)Math.toRadians(camera.yRot());
            pitch = (float)Math.toRadians(camera.xRot());
        }
        if (SkyShader.mc.options != null) {
            fov = (float)Math.toRadians(((Integer)SkyShader.mc.options.fov().get()).intValue());
        }
        Vector4f cameraAngles = new Vector4f(yaw, pitch, fov, 0.0f);
        GpuBuffer ubo = RenderSystem.getDevice().createBuffer(() -> "Cheats MC Sky shader UBO", 136, (long)UBO_SIZE);
        try (MemoryStack stack = MemoryStack.stackPush();){
            ByteBuffer data = Std140Builder.onStack((MemoryStack)stack, (int)UBO_SIZE).putVec4((Vector4fc)tintVec).putVec4((Vector4fc)color1).putVec4((Vector4fc)color2).putVec4((Vector4fc)cameraAngles).putVec2((float)width, (float)height).putFloat(time).putFloat(atmosphere.intensity.getValue().floatValue()).putFloat(atmosphere.speed.getValue().floatValue()).putInt(mode).get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(ubo.slice(), data);
        }
        RenderTarget renderTarget = OutputTarget.MAIN_TARGET.getRenderTarget();
        GpuTextureView gpuTextureView = colorTexture = RenderSystem.outputColorTextureOverride != null ? RenderSystem.outputColorTextureOverride : renderTarget.getColorTextureView();
        GpuTextureView depthTexture = renderTarget.useDepth ? (RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : renderTarget.getDepthTextureView()) : null;
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Cheats MC Sky shader draw", colorTexture, Optional.empty(), depthTexture, OptionalDouble.empty());){
            renderPass.setPipeline(SKY_PIPELINE);
            renderPass.setUniform("SkySettings", ubo);
            renderPass.draw(3, 1, 0, 0);
        }
        ubo.close();
    }
}

