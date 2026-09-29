/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityTypes
 *  net.minecraft.world.entity.MobCategory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl
 *  net.minecraft.world.entity.vehicle.boat.AbstractBoat
 *  net.minecraft.world.entity.vehicle.minecart.AbstractMinecart
 */
package night.modules.impl.visuals;

import java.awt.Color;
import java.util.Arrays;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import night.Night;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.core.ColorModule;
import night.modules.impl.visuals.PopChamsModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ImageSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.color.ColorUtils;
import night.utils.graphics.EspShader;

@RegisterModule(name="Shaders", description="Overlays specified entities with a customizable shader.", category=Module.Category.VISUALS)
public class ShadersModule
extends Module {
    public NumberSetting range = new NumberSetting("Range", "The maximum range at which shaders will be rendered.", Float.valueOf(64.0f), Float.valueOf(1.0f), Float.valueOf(256.0f));
    public CategorySetting targets = new CategorySetting("Targets", "The things that the shader rendering will be applied onto.");
    public BooleanSetting players = new BooleanSetting("Players", "Renders the shader effect on player entities.", new CategorySetting.Visibility(this.targets), true);
    public BooleanSetting hands = new BooleanSetting("Hands", "Renders the shader effect on first-person hands.", new CategorySetting.Visibility(this.targets), true);
    public BooleanSetting hostiles = new BooleanSetting("Hostiles", "Renders the shader effect on hostile entities.", new CategorySetting.Visibility(this.targets), true);
    public BooleanSetting animals = new BooleanSetting("Animals", "Renders the shader effect on animal entities.", new CategorySetting.Visibility(this.targets), true);
    public BooleanSetting ambient = new BooleanSetting("Ambient", "Renders the shader effect on ambient entities.", new CategorySetting.Visibility(this.targets), false);
    public BooleanSetting invisibles = new BooleanSetting("Invisibles", "Renders the shader effect on invisible entities.", new CategorySetting.Visibility(this.targets), true);
    public BooleanSetting items = new BooleanSetting("Items", "Renders the shader effect on item entities.", new CategorySetting.Visibility(this.targets), true);
    public BooleanSetting crystals = new BooleanSetting("Crystals", "Renders the shader effect on crystal entities.", new CategorySetting.Visibility(this.targets), true);
    public BooleanSetting pearls = new BooleanSetting("Pearl", "Renders the shader effect on thrown ender pearls.", new CategorySetting.Visibility(this.targets), true);
    public BooleanSetting boats = new BooleanSetting("Boat", "Renders the shader effect on all boat types.", new CategorySetting.Visibility(this.targets), true);
    public BooleanSetting minecarts = new BooleanSetting("Minecart", "Renders the shader effect on all minecart types.", new CategorySetting.Visibility(this.targets), true);
    public BooleanSetting others = new BooleanSetting("Others", "Renders the shader effect on miscellaneous entities.", new CategorySetting.Visibility(this.targets), false);
    public ModeSetting mode = new ModeSetting("Mode", "The rendering that will be applied to the target entities.", "Both", new String[]{"Fill", "Outline", "Both", "Glow", "Bloom"});
    public NumberSetting glowIntensity = new NumberSetting("GlowIntensity", "Glow intensity", "How wide and how bright the glow around and over the target entities is.", new ModeSetting.Visibility(this.mode, "Glow"), 50, 0, 100);
    public ColorSetting glowColor = new ColorSetting("GlowColor", "The color that will be used for the outer glow effect.", new ModeSetting.Visibility(this.mode, "Glow"), new ColorSetting.Color(new Color(255, 0, 255, 255), false, false));
    public NumberSetting bloomWidth = new NumberSetting("BloomWidth", "Width", "Bloom outline width/radius.", new ModeSetting.Visibility(this.mode, "Bloom"), 3.0, 1.0, 10.0);
    public BooleanSetting bloomGlowInside = new BooleanSetting("BloomGlowInside", "GlowInside", "Enables bloom/glow on inside of silhouette.", new ModeSetting.Visibility(this.mode, "Bloom"), true);
    public NumberSetting bloomGlowQuality = new NumberSetting("BloomQuality", "Quality", "Glow sample step quality.", new ModeSetting.Visibility(this.mode, "Bloom"), 1, 1, 4);
    public NumberSetting bloomGlowMultiplier = new NumberSetting("BloomMultiplier", "Multiplier", "Glow brightness multiplier.", new ModeSetting.Visibility(this.mode, "Bloom"), 1.0, 0.1, 5.0);
    public NumberSetting bloomFillAlpha = new NumberSetting("BloomFillAlpha", "FillAlpha", "Alpha opacity of fill.", new ModeSetting.Visibility(this.mode, "Bloom"), 100, 0, 100);
    public NumberSetting bloomOutlineAlpha = new NumberSetting("BloomOutlineAlpha", "OutlineAlpha", "Alpha opacity of outline/glow.", new ModeSetting.Visibility(this.mode, "Bloom"), 100, 0, 100);
    public ModeSetting bloomFillMode = new ModeSetting("BloomFillMode", "FillMode", "Fill pattern mode.", new ModeSetting.Visibility(this.mode, "Bloom"), "Color", new String[]{"Color", "Gradient", "Rainbow"});
    public ColorSetting bloomGradientColor = new ColorSetting("BloomGradientColor", "GradientColor", "Gradient target color.", new ModeSetting.Visibility(this.mode, "Bloom"), new ColorSetting.Color(new Color(255, 0, 0, 255), false, false));
    public NumberSetting bloomGradientFactor = new NumberSetting("BloomGradientFactor", "GradientFactor", "Scale factor for gradient fill.", new ModeSetting.Visibility(this.mode, "Bloom"), 200.0, 10.0, 500.0);
    public ColorSetting color = new ColorSetting("Color", "The color that will be used for the fill rendering.", ColorUtils.getDefaultColor());
    public ModeSetting friends = new ModeSetting("Friends", "The color that will be applied to friended entities.", "Default", new String[]{"Default", "Custom", "Sync"});
    public ColorSetting friendColor = new ColorSetting("FriendColor", "The color that will be used for the shader effect on friends.", new ModeSetting.Visibility(this.friends, "Custom"), new ColorSetting.Color(new Color(85, 255, 255, ColorUtils.getDefaultFillColor().getColor().getAlpha()), false, false));
    private static final String[] SHADER_MODES = EspShader.MODES;
    private static final String[] SHADER_ACTIVE_MODES = Arrays.copyOfRange(SHADER_MODES, 1, SHADER_MODES.length);
    public ModeSetting shader = new ModeSetting("Shader", "The animated shader that will be drawn on the target entities instead of a flat color.", "None", SHADER_MODES);
    public NumberSetting shaderOpacity = new NumberSetting("ShaderOpacity", "Opacity", "The opacity of the animated shader pattern itself, independent of Color's own alpha.", new ModeSetting.Visibility(this.shader, SHADER_ACTIVE_MODES), 100, 0, 100);
    public NumberSetting shaderSpeed = new NumberSetting("ShaderSpeed", "Speed", "The speed at which the shader animates.", new ModeSetting.Visibility(this.shader, SHADER_ACTIVE_MODES), Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(10.0f));
    public NumberSetting shaderStep = new NumberSetting("ShaderStep", "Step", "The size of the gradient bands.", new ModeSetting.Visibility(this.shader, "Gradient"), Float.valueOf(50.0f), Float.valueOf(0.1f), Float.valueOf(200.0f));
    public ColorSetting shaderColor1 = new ColorSetting("ShaderColor1", "The first gradient color.", new ModeSetting.Visibility(this.shader, "Gradient"), new ColorSetting.Color(new Color(255, 0, 255, 255), false, false));
    public ColorSetting shaderColor2 = new ColorSetting("ShaderColor2", "The second gradient color.", new ModeSetting.Visibility(this.shader, "Gradient"), new ColorSetting.Color(new Color(255, 0, 0, 255), false, false));
    public ColorSetting shaderColor3 = new ColorSetting("ShaderColor3", "The third gradient color.", new ModeSetting.Visibility(this.shader, "Gradient"), new ColorSetting.Color(new Color(0, 255, 0, 255), false, false));
    public ColorSetting shaderColor4 = new ColorSetting("ShaderColor4", "The fourth gradient color.", new ModeSetting.Visibility(this.shader, "Gradient"), new ColorSetting.Color(new Color(0, 0, 255, 255), false, false));
    public ColorSetting shaderGlowColor = new ColorSetting("ShaderGlowColor", "GlowColor", "The color that the glow shader will be tinted with.", new ModeSetting.Visibility(this.shader, "Glowing"), new ColorSetting.Color(new Color(255, 0, 255, 255), false, false));
    public ImageSetting image = new ImageSetting("Image", "Select", "Select an image from the images directory to project onto targets.", new ModeSetting.Visibility(this.shader, "Image"), "None");

    @Override
    public void onEnable() {
        if (Night.IMAGE_MANAGER != null && this.image != null) {
            Night.IMAGE_MANAGER.setCurrentActiveImage(this.image.getValue());
        }
    }

    public boolean inRange(Entity entity) {
        if (entity == null || ShadersModule.mc.player == null) {
            return true;
        }
        if (entity == ShadersModule.mc.player) {
            return true;
        }
        return ShadersModule.mc.player.distanceToSqr(entity) <= (double)(this.range.getValue().floatValue() * this.range.getValue().floatValue());
    }

    public boolean isValidEntity(Entity entity) {
        if (!this.inRange(entity)) {
            return false;
        }
        if (this.players.getValue() && entity.getType() == EntityTypes.PLAYER) {
            return true;
        }
        if (this.hostiles.getValue() && entity.getType().getCategory() == MobCategory.MONSTER) {
            return true;
        }
        if (this.animals.getValue() && (entity.getType().getCategory() == MobCategory.CREATURE || entity.getType().getCategory() == MobCategory.WATER_CREATURE || entity.getType().getCategory() == MobCategory.WATER_AMBIENT || entity.getType().getCategory() == MobCategory.UNDERGROUND_WATER_CREATURE || entity.getType().getCategory() == MobCategory.AXOLOTLS)) {
            return true;
        }
        if (this.ambient.getValue() && entity.getType().getCategory() == MobCategory.AMBIENT) {
            return true;
        }
        if (this.invisibles.getValue() && entity.isInvisible()) {
            return true;
        }
        if (this.items.getValue() && (entity.getType() == EntityTypes.ITEM || entity.getType() == EntityTypes.EXPERIENCE_BOTTLE)) {
            return true;
        }
        if (this.crystals.getValue() && entity.getType() == EntityTypes.END_CRYSTAL) {
            return true;
        }
        if (this.pearls.getValue() && (entity instanceof ThrownEnderpearl || entity.getType() == EntityTypes.ENDER_PEARL)) {
            return true;
        }
        if (this.boats.getValue() && entity instanceof AbstractBoat) {
            return true;
        }
        if (this.minecarts.getValue() && entity instanceof AbstractMinecart) {
            return true;
        }
        return this.others.getValue();
    }

    public boolean shouldOutline() {
        return this.mode.getValue().equalsIgnoreCase("Outline") || this.mode.getValue().equalsIgnoreCase("Both");
    }

    public boolean shouldFill() {
        return this.mode.getValue().equalsIgnoreCase("Fill") || this.mode.getValue().equalsIgnoreCase("Both");
    }

    public int getFillColor(Entity entity) {
        return this.getColor(entity).getRGB() | 0xFF000000;
    }

    public Color getColor(Entity entity) {
        Player player;
        if (entity instanceof Player && Night.FRIEND_MANAGER.contains((player = (Player)entity).getName().getString()) && !this.friends.getValue().equals("Sync")) {
            return this.friends.getValue().equals("Default") ? Night.FRIEND_MANAGER.getDefaultFriendColor() : this.friendColor.getColor();
        }
        return this.color.getColor();
    }

    public static int getBloomFillModeIndex(String mode) {
        return switch (mode) {
            case "Gradient" -> 1;
            case "Rainbow" -> 4;
            default -> 0;
        };
    }

    public static Identifier pickActiveOutlineChain() {
        ShadersModule shaders = Night.MODULE_MANAGER.getModule(ShadersModule.class);
        if (shaders.isToggled()) {
            if (shaders.mode.getValue().equalsIgnoreCase("Glow")) {
                return Identifier.fromNamespaceAndPath((String)"night", (String)"outline_glow");
            }
            if (shaders.mode.getValue().equalsIgnoreCase("Bloom")) {
                return Identifier.fromNamespaceAndPath((String)"night", (String)"outline_bloom");
            }
            return ShadersModule.pickVariant(shaders.shouldFill(), shaders.shouldOutline(), shaders.color.getColor().getAlpha());
        }
        PopChamsModule popChams = Night.MODULE_MANAGER.getModule(PopChamsModule.class);
        if (popChams.isToggled()) {
            boolean fill = popChams.mode.getValue().equalsIgnoreCase("Fill") || popChams.mode.getValue().equalsIgnoreCase("Both");
            boolean outline = popChams.mode.getValue().equalsIgnoreCase("Outline") || popChams.mode.getValue().equalsIgnoreCase("Both");
            return ShadersModule.pickVariant(fill, outline, popChams.fillColor.getColor().getAlpha());
        }
        return null;
    }

    public static EspShader.Settings shaderSettings() {
        ShadersModule shaders = Night.MODULE_MANAGER.getModule(ShadersModule.class);
        if (shaders == null || !shaders.isToggled()) {
            return new EspShader.Settings(0, 0.0f, 1.0f, 1.0f, 1.0f, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE);
        }
        int effect = EspShader.modeIndex(shaders.shader.getValue());
        float colorAlpha = (float)shaders.color.getColor().getAlpha() / 255.0f;
        float opacity = shaders.shaderOpacity.getValue().floatValue() / 100.0f;
        ColorModule cm = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ColorModule.class) : null;
        float rainbowSpeed = cm != null ? cm.rainbowSpeed.getValue().floatValue() : 6.0f;
        float rainbowBri = cm != null ? cm.rainbowBrightness.getValue().floatValue() / 100.0f : 1.0f;
        float rainbowScale = cm != null && cm.rainbowScale != null ? cm.rainbowScale.getValue().floatValue() / 100.0f : 1.0f;
        float step = effect == 1 ? shaders.shaderStep.getValue().floatValue() : rainbowScale;
        float rainbowRate = (float)(1000.0 / Math.max(200.0, 10500.0 - 500.0 * (double)rainbowSpeed));
        float animSpeed = effect != 0 && effect != 2 && effect != 10 ? shaders.shaderSpeed.getValue().floatValue() : rainbowRate * (effect == 2 ? shaders.shaderSpeed.getValue().floatValue() : 1.0f);
        float time = (float)((double)(System.currentTimeMillis() % 20000000L) / 1000.0) * animSpeed * (effect == 1 ? step : 1.0f);
        if (effect != 0) {
            return new EspShader.Settings(effect, time, step, opacity, rainbowBri, effect == 7 ? shaders.shaderGlowColor.getColor() : shaders.color.getColor(), shaders.shaderColor2.getColor(), shaders.shaderColor3.getColor(), shaders.shaderColor4.getColor());
        }
        return new EspShader.Settings(0, time, step, colorAlpha, rainbowBri, shaders.color.getColor(), Color.WHITE, Color.WHITE, Color.WHITE);
    }

    public static float glowIntensity() {
        ShadersModule shaders = Night.MODULE_MANAGER.getModule(ShadersModule.class);
        if (!shaders.isToggled() || !shaders.mode.getValue().equalsIgnoreCase("Glow")) {
            return 0.0f;
        }
        return shaders.glowIntensity.getValue().floatValue() / 100.0f;
    }

    public static Identifier pickVariant(boolean fill, boolean outline, int alpha) {
        if (outline && !fill) {
            return Identifier.fromNamespaceAndPath((String)"night", (String)"outline_outline");
        }
        int bucket = Math.max(20, Math.min(100, (int)Math.round((double)alpha / 255.0 * 5.0) * 20));
        String prefix = fill && outline ? "outline_both_" : "outline_fill_";
        return Identifier.fromNamespaceAndPath((String)"night", (String)(prefix + bucket));
    }
}

