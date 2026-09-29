/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.particles.SimpleParticleType
 */
package night.modules.impl.visuals;

import java.awt.Color;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import night.events.SubscribeEvent;
import night.events.impl.TickEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.color.ColorUtils;

@RegisterModule(name="Atmosphere", description="Modifies the world's atmosphere, such as time and color.", category=Module.Category.VISUALS)
public class AtmosphereModule
extends Module {
    public BooleanSetting modifyTime = new BooleanSetting("ModifyTime", "Modifies the world's time.", true);
    public NumberSetting time = new NumberSetting("Time", "The time that the world will be set to.", new BooleanSetting.Visibility(this.modifyTime, true), (Number)200, (Number)(-200), (Number)200);
    public BooleanSetting modifyFog = new BooleanSetting("ModifyFog", "Modifies certain things about the world's fog.", false);
    public NumberSetting fogStart = new NumberSetting("FogStart", "The start value of the world's fog.", new BooleanSetting.Visibility(this.modifyFog, true), (Number)50, (Number)0, (Number)300);
    public NumberSetting fogEnd = new NumberSetting("FogEnd", "The end value of the world's fog.", new BooleanSetting.Visibility(this.modifyFog, true), (Number)150, (Number)0, (Number)300);
    public ColorSetting fogColor = new ColorSetting("FogColor", "Modifies the color of the world's fog.", new BooleanSetting.Visibility(this.modifyFog, true), ColorUtils.getDefaultOutlineColor());
    public BooleanSetting modifySky = new BooleanSetting("ModifySky", "Modifies the sky rendering.", false);
    public ModeSetting skyMode = new ModeSetting("SkyMode", "The sky rendering mode.", new BooleanSetting.Visibility(this.modifySky, true), "Vanilla", new String[]{"Vanilla", "Custom"});
    public ColorSetting skyColor = new ColorSetting("SkyColor", "The color of the vanilla sky.", new ModeSetting.Visibility(this.skyMode, "Vanilla"), new ColorSetting.Color(new Color(0, 0, 0, 255), true, false));
    public ModeSetting customSkyMode = new ModeSetting("CustomSky", "The custom sky shader to use.", new ModeSetting.Visibility(this.skyMode, "Custom"), "Nebula", new String[]{"Nebula", "Smoke"});
    public NumberSetting intensity = new NumberSetting("Intensity", "The intensity of the sky shader.", new ModeSetting.Visibility(this.skyMode, "Custom"), (Number)Float.valueOf(1.0f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(10.0f));
    public NumberSetting speed = new NumberSetting("Speed", "The speed of the sky shader animation.", new ModeSetting.Visibility(this.skyMode, "Custom"), (Number)Float.valueOf(1.0f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(5.0f));
    public ColorSetting skyTint = new ColorSetting("SkyTint", "The tint of the sky shader.", new ModeSetting.Visibility(this.skyMode, "Custom"), new ColorSetting.Color(new Color(255, 255, 255), false, false));
    public ModeSetting weather = new ModeSetting("Weather", "Overrides the world's weather (visual only, not sent to the server).", "Unchanged", new String[]{"Unchanged", "Clear", "Rain", "Thunder", "Snow", "Dust"});
    public CategorySetting starGlowCategory = new CategorySetting("StarGlow", "The category for the star glow effect.");
    public BooleanSetting starGlow = new BooleanSetting("StarGlow", "Enabled", "Adds a soft glow to vanilla sky stars.", new CategorySetting.Visibility(this.starGlowCategory), false);
    public NumberSetting starGlowIntensity = new NumberSetting("StarGlowIntensity", "Intensity", "How strong the glow is.", new CategorySetting.Visibility(this.starGlowCategory), Float.valueOf(3.0f), Float.valueOf(0.0f), Float.valueOf(1000.0f));

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (AtmosphereModule.mc.player == null || AtmosphereModule.mc.level == null) {
            return;
        }
        if (!this.weather.getValue().equalsIgnoreCase("Snow") && !this.weather.getValue().equalsIgnoreCase("Dust")) {
            return;
        }
        boolean snow = this.weather.getValue().equalsIgnoreCase("Snow");
        SimpleParticleType particle = snow ? ParticleTypes.SNOWFLAKE : ParticleTypes.MYCELIUM;
        for (int i = 0; i < 6; ++i) {
            double x = AtmosphereModule.mc.player.getX() + (AtmosphereModule.mc.player.getRandom().nextDouble() - 0.5) * 20.0;
            double y = AtmosphereModule.mc.player.getY() + (snow ? 8.0 + AtmosphereModule.mc.player.getRandom().nextDouble() * 4.0 : AtmosphereModule.mc.player.getRandom().nextDouble() * 6.0 - 1.0);
            double z = AtmosphereModule.mc.player.getZ() + (AtmosphereModule.mc.player.getRandom().nextDouble() - 0.5) * 20.0;
            AtmosphereModule.mc.level.addParticle((ParticleOptions)particle, x, y, z, 0.0, snow ? -0.15 : 0.01, 0.0);
        }
    }
}

