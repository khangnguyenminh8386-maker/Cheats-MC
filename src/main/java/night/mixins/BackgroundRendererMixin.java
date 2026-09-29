/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Camera
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.fog.FogData
 *  net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment
 *  net.minecraft.util.ARGB
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package night.mixins;

import java.awt.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.util.ARGB;
import night.Night;
import night.modules.impl.visuals.AtmosphereModule;
import night.modules.impl.visuals.NoRenderModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={AtmosphericFogEnvironment.class})
public class BackgroundRendererMixin {
    @Inject(method={"setupFog"}, at={@At(value="TAIL")})
    private void night$setupFog(FogData fog, Camera camera, ClientLevel level, float renderDistance, DeltaTracker deltaTracker, CallbackInfo info) {
        NoRenderModule noRender = Night.MODULE_MANAGER.getModule(NoRenderModule.class);
        AtmosphereModule atmosphere = Night.MODULE_MANAGER.getModule(AtmosphereModule.class);
        if (noRender.isToggled() && noRender.fog.getValue()) {
            fog.environmentalStart = renderDistance * 4.0f;
            fog.environmentalEnd = renderDistance * 4.25f;
        } else if (atmosphere != null && atmosphere.isToggled() && atmosphere.modifyFog.getValue()) {
            fog.environmentalStart = atmosphere.fogStart.getValue().floatValue();
            fog.environmentalEnd = atmosphere.fogEnd.getValue().floatValue();
        }
    }

    @Inject(method={"getBaseColor"}, at={@At(value="TAIL")}, cancellable=true)
    private void night$getBaseColor(ClientLevel level, Camera camera, int renderDistance, float partialTicks, CallbackInfoReturnable<Integer> info) {
        AtmosphereModule atmosphere = Night.MODULE_MANAGER.getModule(AtmosphereModule.class);
        if (atmosphere != null && atmosphere.isToggled() && atmosphere.modifyFog.getValue()) {
            Color color = atmosphere.fogColor.getColor();
            info.setReturnValue(ARGB.color((int)255, (int)color.getRed(), (int)color.getGreen(), (int)color.getBlue()));
        }
    }
}

