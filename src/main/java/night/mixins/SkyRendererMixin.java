/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.mojang.blaze3d.pipeline.TextureTarget
 *  com.mojang.blaze3d.resource.GraphicsResourceAllocator
 *  com.mojang.blaze3d.systems.CommandEncoder
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.LevelTargetBundle
 *  net.minecraft.client.renderer.PostChain
 *  net.minecraft.client.renderer.SkyRenderer
 *  net.minecraft.client.renderer.state.level.SkyRenderState
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.level.MoonPhase
 *  net.minecraft.world.level.dimension.DimensionType$Skybox
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.dimension.DimensionType;
import night.Night;
import night.modules.impl.visuals.AtmosphereModule;
import night.utils.graphics.EspShader;
import night.utils.graphics.SkyShader;
import night.utils.graphics.StarCapture;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={SkyRenderer.class})
public abstract class SkyRendererMixin {
    @Redirect(method={"renderStars"}, at=@At(value="INVOKE", target="Lcom/mojang/blaze3d/systems/CommandEncoder;createRenderPass(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/Optional;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/OptionalDouble;)Lcom/mojang/blaze3d/systems/RenderPass;"))
    private RenderPass night$redirectStars(CommandEncoder encoder, Supplier<String> label, GpuTextureView color, Optional<Vector4fc> clearColor, GpuTextureView depth, OptionalDouble clearDepth) {
        AtmosphereModule atmosphere = Night.MODULE_MANAGER.getModule(AtmosphereModule.class);
        if (atmosphere.isToggled() && atmosphere.starGlow.getValue()) {
            TextureTarget target = StarCapture.ensure();
            return encoder.createRenderPass(label, target.getColorTextureView(), Optional.of(new Vector4f(0.0f, 0.0f, 0.0f, 0.0f)), depth, clearDepth);
        }
        return encoder.createRenderPass(label, color, clearColor, depth, clearDepth);
    }

    @Inject(method={"renderSunMoonAndStars"}, at={@At(value="TAIL")})
    private void night$resolveStarGlow(PoseStack poseStack, float sunAngle, float moonAngle, float starAngle, MoonPhase moonPhase, float rainBrightness, float starBrightness, CallbackInfo ci) {
        if (starBrightness <= 0.0f) {
            return;
        }
        AtmosphereModule atmosphere = Night.MODULE_MANAGER.getModule(AtmosphereModule.class);
        if (!atmosphere.isToggled() || !atmosphere.starGlow.getValue()) {
            return;
        }
        TextureTarget starTarget = StarCapture.get();
        if (starTarget == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        PostChain chain = mc.getShaderManager().getPostChain(Identifier.fromNamespaceAndPath((String)"night", (String)"star_glow"), LevelTargetBundle.MAIN_TARGETS);
        if (chain == null) {
            return;
        }
        EspShader.writeStarGlowSettings(chain, atmosphere.starGlowIntensity.getValue().floatValue());
        chain.process((RenderTarget)starTarget, GraphicsResourceAllocator.UNPOOLED);
        starTarget.blitAndBlendToTexture(mc.gameRenderer.mainRenderTarget().getColorTextureView(), mc.gameRenderer.mainRenderTarget().getDepthTextureView());
    }

    @Inject(method={"extractRenderState"}, at={@At(value="RETURN")})
    private void night$forceSkybox(ClientLevel clientLevel, float partialTicks, Camera camera, SkyRenderState skyRenderState, CallbackInfo ci) {
        AtmosphereModule atmosphere;
        AtmosphereModule atmosphereModule = atmosphere = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AtmosphereModule.class) : null;
        if (atmosphere != null && atmosphere.isToggled() && atmosphere.modifySky.getValue()) {
            skyRenderState.skybox = DimensionType.Skybox.OVERWORLD;
        }
    }

    @Inject(method={"renderSkyDisc"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$renderCustomSkyDisc(int color, CallbackInfo ci) {
        AtmosphereModule atmosphere;
        AtmosphereModule atmosphereModule = atmosphere = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AtmosphereModule.class) : null;
        if (atmosphere != null && atmosphere.isToggled() && atmosphere.modifySky.getValue() && atmosphere.skyMode.getValue().equalsIgnoreCase("Custom")) {
            SkyShader.renderSky();
            ci.cancel();
        }
    }

    @ModifyVariable(method={"renderSkyDisc"}, at=@At(value="HEAD"), argsOnly=true, ordinal=0)
    private int night$modifySkyColor(int color) {
        AtmosphereModule atmosphere;
        AtmosphereModule atmosphereModule = atmosphere = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AtmosphereModule.class) : null;
        if (atmosphere != null && atmosphere.isToggled() && atmosphere.modifySky.getValue() && atmosphere.skyMode.getValue().equalsIgnoreCase("Vanilla")) {
            return atmosphere.skyColor.getColor().getRGB();
        }
        return color;
    }

    @Inject(method={"renderDarkDisc"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$cancelDarkDisc(CallbackInfo ci) {
        AtmosphereModule atmosphere;
        AtmosphereModule atmosphereModule = atmosphere = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AtmosphereModule.class) : null;
        if (atmosphere != null && atmosphere.isToggled() && atmosphere.modifySky.getValue()) {
            ci.cancel();
        }
    }

    @Inject(method={"renderSunriseAndSunset"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$cancelSunriseAndSunset(CallbackInfo ci) {
        AtmosphereModule atmosphere;
        AtmosphereModule atmosphereModule = atmosphere = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AtmosphereModule.class) : null;
        if (atmosphere != null && atmosphere.isToggled() && atmosphere.modifySky.getValue()) {
            ci.cancel();
        }
    }

    @Inject(method={"renderSunMoonAndStars"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$cancelSunMoonAndStars(PoseStack poseStack, float sunAngle, float moonAngle, float starAngle, MoonPhase moonPhase, float rainBrightness, float starBrightness, CallbackInfo ci) {
        AtmosphereModule atmosphere;
        AtmosphereModule atmosphereModule = atmosphere = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AtmosphereModule.class) : null;
        if (atmosphere != null && atmosphere.isToggled() && atmosphere.modifySky.getValue()) {
            boolean isNether;
            Minecraft mc = Minecraft.getInstance();
            boolean bl = isNether = mc.level != null && mc.level.dimensionType().skybox() == DimensionType.Skybox.NONE;
            if (isNether || atmosphere.skyMode.getValue().equalsIgnoreCase("Custom") && !atmosphere.starGlow.getValue()) {
                ci.cancel();
            }
        }
    }

    @Inject(method={"renderEndSky"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$renderCustomEndSky(CallbackInfo ci) {
        AtmosphereModule atmosphere;
        AtmosphereModule atmosphereModule = atmosphere = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AtmosphereModule.class) : null;
        if (atmosphere != null && atmosphere.isToggled() && atmosphere.modifySky.getValue() && atmosphere.skyMode.getValue().equalsIgnoreCase("Custom")) {
            SkyShader.renderSky();
            ci.cancel();
        }
    }
}

