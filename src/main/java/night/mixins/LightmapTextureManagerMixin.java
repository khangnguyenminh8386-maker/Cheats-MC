/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.Lightmap
 *  net.minecraft.client.renderer.state.LightmapRenderState
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import java.awt.Color;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.state.LightmapRenderState;
import night.Night;
import night.modules.api.IAmbienceModule;
import night.modules.api.IFullBrightModule;
import night.modules.impl.visuals.NoRenderModule;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Lightmap.class})
public class LightmapTextureManagerMixin {
    @Inject(method={"render"}, at={@At(value="HEAD")})
    private void night$render(LightmapRenderState renderState, CallbackInfo info) {
        IAmbienceModule ambience;
        NoRenderModule noRender;
        IFullBrightModule fullBright = (IFullBrightModule)((Object)Night.MODULE_MANAGER.getModule("FullBright"));
        if (fullBright.isToggled() && fullBright.getModeValue().equalsIgnoreCase("Gamma")) {
            renderState.blockFactor = 1.0f;
            renderState.skyFactor = 1.0f;
            renderState.brightness = 1.0f;
            renderState.nightVisionEffectIntensity = 1.0f;
            renderState.darknessEffectScale = 0.0f;
            renderState.bossOverlayWorldDarkening = 0.0f;
            renderState.needsUpdate = true;
        }
        if ((noRender = Night.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && noRender.blindness.getValue()) {
            renderState.darknessEffectScale = 0.0f;
            renderState.needsUpdate = true;
        }
        IAmbienceModule iAmbienceModule = ambience = Night.MODULE_MANAGER != null ? (IAmbienceModule)((Object)Night.MODULE_MANAGER.getModule("Ambience")) : null;
        if (ambience != null && ambience.isToggled()) {
            Color color = ambience.getColor();
            float r = (float)color.getRed() / 255.0f;
            float g = (float)color.getGreen() / 255.0f;
            float b = (float)color.getBlue() / 255.0f;
            float alpha = (float)color.getAlpha() / 255.0f;
            if (ambience.isLightmapEnabled()) {
                if (ambience.isSkyEnabled()) {
                    Vector3fc origSky = renderState.skyLightColor != null ? renderState.skyLightColor : new Vector3f(1.0f, 1.0f, 1.0f);
                    renderState.skyLightColor = new Vector3f(origSky.x() * (1.0f - alpha) + r * alpha, origSky.y() * (1.0f - alpha) + g * alpha, origSky.z() * (1.0f - alpha) + b * alpha);
                    Vector3fc origAmbient = renderState.ambientColor != null ? renderState.ambientColor : new Vector3f(0.0f, 0.0f, 0.0f);
                    renderState.ambientColor = new Vector3f(origAmbient.x() * (1.0f - alpha) + r * alpha, origAmbient.y() * (1.0f - alpha) + g * alpha, origAmbient.z() * (1.0f - alpha) + b * alpha);
                }
                if (ambience.isBlocksEnabled()) {
                    Vector3fc origBlock = renderState.blockLightTint != null ? renderState.blockLightTint : new Vector3f(1.0f, 1.0f, 1.0f);
                    renderState.blockLightTint = new Vector3f(origBlock.x() * (1.0f - alpha) + r * alpha, origBlock.y() * (1.0f - alpha) + g * alpha, origBlock.z() * (1.0f - alpha) + b * alpha);
                }
                renderState.needsUpdate = true;
            }
        }
    }
}

