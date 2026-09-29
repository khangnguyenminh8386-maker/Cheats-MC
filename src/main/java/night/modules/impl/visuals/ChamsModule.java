/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityTypes
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 */
package night.modules.impl.visuals;

import java.awt.Color;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.RenderWorldEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.color.ColorUtils;
import night.utils.graphics.Glint;
import night.utils.minecraft.EntityUtils;
import night.utils.mixins.IChamsCapture;

@RegisterModule(name="Chams", description="Adds a customizable render on top of the default Minecraft rendering.", category=Module.Category.VISUALS)
public class ChamsModule
extends Module {
    public NumberSetting range = new NumberSetting("Range", "The maximum range at which chams will be rendered.", Float.valueOf(64.0f), Float.valueOf(1.0f), Float.valueOf(256.0f));
    public CategorySetting entitiesCategory = new CategorySetting("Entities", "The category for settings related to chams rendered on living entities.");
    public BooleanSetting players = new BooleanSetting("Players", "Renders the chams on player entities.", new CategorySetting.Visibility(this.entitiesCategory), true);
    public BooleanSetting hostiles = new BooleanSetting("Hostiles", "Renders the chams on hostile entities.", new CategorySetting.Visibility(this.entitiesCategory), false);
    public BooleanSetting passives = new BooleanSetting("Passives", "Renders the chams on passive entities.", new CategorySetting.Visibility(this.entitiesCategory), false);
    public BooleanSetting entityPulse = new BooleanSetting("EntityPulse", "Pulse", "Adds a pulsing effect to the chams opacity.", new CategorySetting.Visibility(this.entitiesCategory), false);
    public BooleanSetting entityShine = new BooleanSetting("EntityShine", "Shine", "Adds a shine effect to the chams.", new CategorySetting.Visibility(this.entitiesCategory), false);
    public BooleanSetting entityGlint = new BooleanSetting("EntityGlint", "Glint", "Adds a scrolling texture overlay to living entities.", new CategorySetting.Visibility(this.entitiesCategory), false);
    public ColorSetting entityGlintColor = new ColorSetting("EntityGlintColor", "GlintColor", "Color tint for the entity glint overlay.", new BooleanSetting.Visibility(this.entityGlint, true), new ColorSetting.Color(new Color(255, 255, 255, 180), false, false));
    public NumberSetting entityGlintSpeedU = new NumberSetting("EntityGlintSpeedU", "GlintSpeedU", "Horizontal scroll speed of entity glint.", new BooleanSetting.Visibility(this.entityGlint, true), Float.valueOf(0.25f), Float.valueOf(-5.0f), Float.valueOf(5.0f));
    public NumberSetting entityGlintSpeedV = new NumberSetting("EntityGlintSpeedV", "GlintSpeedV", "Vertical scroll speed of entity glint.", new BooleanSetting.Visibility(this.entityGlint, true), Float.valueOf(0.1f), Float.valueOf(-5.0f), Float.valueOf(5.0f));
    public NumberSetting entityGlintScale = new NumberSetting("EntityGlintScale", "GlintScale", "Scale of the glint texture on entity models.", new BooleanSetting.Visibility(this.entityGlint, true), Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(5.0f));
    public ModeSetting entityGlintDirection = new ModeSetting("EntityGlintDirection", "GlintDirection", "Projection mode for entity glint UVs.", new BooleanSetting.Visibility(this.entityGlint, true), "Diagonal", new String[]{"Diagonal", "Horizontal", "Vertical"});
    public ModeSetting entityMode = new ModeSetting("EntityMode", "Mode", "The rendering that will be applied to living entities.", new CategorySetting.Visibility(this.entitiesCategory), "Both", new String[]{"Fill", "Outline", "Both"});
    public ColorSetting entityFillColor = new ColorSetting("EntityFillColor", "FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(this.entityMode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting entityOutlineColor = new ColorSetting("EntityOutlineColor", "OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(this.entityMode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    public ModeSetting friendMode = new ModeSetting("FriendMode", "The mode for the friend color.", new ModeSetting.Visibility(this.entityMode, "Fill", "Both"), "Default", new String[]{"Default", "Custom", "Sync"});
    public ColorSetting friendFillColor = new ColorSetting("FriendFillColor", "FriendFillColor", "The color that will be used for the fill rendering on friends.", new ModeSetting.Visibility(this.entityMode, "Fill", "Both"), new ColorSetting.Color(new Color(85, 255, 255, ColorUtils.getDefaultFillColor().getColor().getAlpha()), false, false));
    public ColorSetting friendOutlineColor = new ColorSetting("FriendOutlineColor", "FriendOutlineColor", "The color that will be used for the outline rendering on friends.", new ModeSetting.Visibility(this.entityMode, "Outline", "Both"), new ColorSetting.Color(new Color(85, 255, 255, ColorUtils.getDefaultOutlineColor().getColor().getAlpha()), false, false));
    public BooleanSetting damageModify = new BooleanSetting("DamageModify", "Changes the color of the chams when a player takes damage.", new ModeSetting.Visibility(this.entityMode, "Fill", "Both"), false);
    public ColorSetting damageColor = new ColorSetting("DamageColor", "The color to apply on chams when a player takes damage.", new BooleanSetting.Visibility(this.damageModify, true), new ColorSetting.Color(new Color(255, 0, 0), false, false));
    public NumberSetting entityOpacity = new NumberSetting("EntityOpacity", "Opacity", "How see-through the target's real skin renders (100 = untouched).", new CategorySetting.Visibility(this.entitiesCategory), 100, 0, 100);
    public CategorySetting crystalsCategory = new CategorySetting("Crystals", "The category for settings related to crystal chams.");
    public BooleanSetting crystals = new BooleanSetting("Crystals", "Enabled", "Renders the chams on crystal entities.", new CategorySetting.Visibility(this.crystalsCategory), true);
    public BooleanSetting crystalPulse = new BooleanSetting("CrystalPulse", "Pulse", "Adds a pulsing effect to the chams opacity.", new CategorySetting.Visibility(this.crystalsCategory), false);
    public BooleanSetting crystalShine = new BooleanSetting("CrystalShine", "Shine", "Adds a shine effect on crystal chams.", new CategorySetting.Visibility(this.crystalsCategory), false);
    public BooleanSetting crystalGlint = new BooleanSetting("CrystalGlint", "Glint", "Adds a scrolling texture overlay to crystals.", new CategorySetting.Visibility(this.crystalsCategory), false);
    public ColorSetting crystalGlintColor = new ColorSetting("CrystalGlintColor", "GlintColor", "Color tint for the crystal glint overlay.", new BooleanSetting.Visibility(this.crystalGlint, true), new ColorSetting.Color(new Color(255, 255, 255, 180), false, false));
    public NumberSetting crystalGlintSpeedU = new NumberSetting("CrystalGlintSpeedU", "GlintSpeedU", "Horizontal scroll speed of crystal glint.", new BooleanSetting.Visibility(this.crystalGlint, true), Float.valueOf(0.25f), Float.valueOf(-5.0f), Float.valueOf(5.0f));
    public NumberSetting crystalGlintSpeedV = new NumberSetting("CrystalGlintSpeedV", "GlintSpeedV", "Vertical scroll speed of crystal glint.", new BooleanSetting.Visibility(this.crystalGlint, true), Float.valueOf(0.1f), Float.valueOf(-5.0f), Float.valueOf(5.0f));
    public NumberSetting crystalGlintScale = new NumberSetting("CrystalGlintScale", "GlintScale", "Scale of the glint texture on crystal models.", new BooleanSetting.Visibility(this.crystalGlint, true), Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(5.0f));
    public ModeSetting crystalGlintDirection = new ModeSetting("CrystalGlintDirection", "GlintDirection", "Projection mode for crystal glint UVs.", new BooleanSetting.Visibility(this.crystalGlint, true), "Diagonal", new String[]{"Diagonal", "Horizontal", "Vertical"});
    public ModeSetting crystalMode = new ModeSetting("CrystalMode", "Mode", "The rendering that will be applied to crystal entities.", new CategorySetting.Visibility(this.crystalsCategory), "Both", new String[]{"Fill", "Outline", "Both"});
    public ColorSetting crystalFillColor = new ColorSetting("CrystalFillColor", "FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(this.crystalMode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting crystalOutlineColor = new ColorSetting("CrystalOutlineColor", "OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(this.crystalMode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    public NumberSetting crystalOpacity = new NumberSetting("CrystalOpacity", "Opacity", "How see-through the crystal's real texture renders (100 = untouched).", new CategorySetting.Visibility(this.crystalsCategory), 100, 0, 100);
    public CategorySetting vehiclesCategory = new CategorySetting("Vehicles", "The category for settings related to boat/minecart chams.");
    public BooleanSetting vehicles = new BooleanSetting("Vehicles", "Enabled", "Renders the chams on all boat and minecart types.", new CategorySetting.Visibility(this.vehiclesCategory), true);
    public BooleanSetting vehiclePulse = new BooleanSetting("VehiclePulse", "Pulse", "Adds a pulsing effect to the chams opacity.", new CategorySetting.Visibility(this.vehiclesCategory), false);
    public BooleanSetting vehicleShine = new BooleanSetting("VehicleShine", "Shine", "Adds a shine effect on vehicle chams.", new CategorySetting.Visibility(this.vehiclesCategory), false);
    public ModeSetting vehicleMode = new ModeSetting("VehicleMode", "Mode", "The rendering that will be applied to boat/minecart entities.", new CategorySetting.Visibility(this.vehiclesCategory), "Both", new String[]{"Fill", "Outline", "Both"});
    public ColorSetting vehicleFillColor = new ColorSetting("VehicleFillColor", "FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(this.vehicleMode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting vehicleOutlineColor = new ColorSetting("VehicleOutlineColor", "OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(this.vehicleMode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    public NumberSetting vehicleOpacity = new NumberSetting("VehicleOpacity", "Opacity", "How see-through the vehicle's real texture renders (100 = untouched).", new CategorySetting.Visibility(this.vehiclesCategory), 100, 0, 100);

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (this.entityGlint.getValue()) {
            Glint.ENTITY.speedU = this.entityGlintSpeedU.getValue().floatValue();
            Glint.ENTITY.speedV = this.entityGlintSpeedV.getValue().floatValue();
            Glint.ENTITY.scale = this.entityGlintScale.getValue().floatValue();
            Glint.ENTITY.direction = this.entityGlintDirection.getValue();
        }
        if (this.crystalGlint.getValue()) {
            Glint.CRYSTAL.speedU = this.crystalGlintSpeedU.getValue().floatValue();
            Glint.CRYSTAL.speedV = this.crystalGlintSpeedV.getValue().floatValue();
            Glint.CRYSTAL.scale = this.crystalGlintScale.getValue().floatValue();
            Glint.CRYSTAL.direction = this.crystalGlintDirection.getValue();
        }
        if (this.entityGlint.getValue() || this.crystalGlint.getValue()) {
            Glint.tick();
        }
    }

    public boolean inRange(Entity entity) {
        if (entity == null || ChamsModule.mc.player == null) {
            return true;
        }
        if (entity == ChamsModule.mc.player) {
            return true;
        }
        return ChamsModule.mc.player.distanceToSqr(entity) <= (double)(this.range.getValue().floatValue() * this.range.getValue().floatValue());
    }

    public boolean isValidEntity(Entity entity) {
        if (!this.inRange(entity)) {
            return false;
        }
        if (this.players.getValue() && entity.getType() == EntityTypes.PLAYER) {
            return true;
        }
        if (this.hostiles.getValue() && EntityUtils.isHostile(entity)) {
            return true;
        }
        return this.passives.getValue() && EntityUtils.isAnimal(entity) && !EntityUtils.isHostile(entity);
    }

    public void applyEntityChams(LivingEntity livingEntity, IChamsCapture capture) {
        boolean fill = this.entityMode.getValue().equals("Fill") || this.entityMode.getValue().equals("Both");
        boolean outline = this.entityMode.getValue().equals("Outline") || this.entityMode.getValue().equals("Both");
        capture.night$setChams(fill, this.pickEntityColor(livingEntity, this.entityFillColor.getColor(), this.friendFillColor.getColor()).getRGB(), outline, this.pickEntityColor(livingEntity, this.entityOutlineColor.getColor(), this.friendOutlineColor.getColor()).getRGB(), this.entityShine.getValue());
        capture.night$setChamsRealAlpha(this.entityOpacity.getValue().floatValue() / 100.0f);
        if (this.entityGlint.getValue()) {
            capture.night$setChamsGlint(true, this.entityGlintColor.getColor().getRGB(), Glint.ENTITY);
        }
    }

    public void applyCrystalChams(IChamsCapture capture) {
        boolean fill = this.crystalMode.getValue().equals("Fill") || this.crystalMode.getValue().equals("Both");
        boolean outline = this.crystalMode.getValue().equals("Outline") || this.crystalMode.getValue().equals("Both");
        Color fillColor = this.crystalPulse.getValue() ? ColorUtils.getPulse(this.crystalFillColor.getColor()) : this.crystalFillColor.getColor();
        Color outlineColor = this.crystalPulse.getValue() ? ColorUtils.getPulse(this.crystalOutlineColor.getColor()) : this.crystalOutlineColor.getColor();
        capture.night$setChams(fill, fillColor.getRGB(), outline, outlineColor.getRGB(), this.crystalShine.getValue());
        capture.night$setChamsRealAlpha(this.crystalOpacity.getValue().floatValue() / 100.0f);
        if (this.crystalGlint.getValue()) {
            capture.night$setChamsGlint(true, this.crystalGlintColor.getColor().getRGB(), Glint.CRYSTAL);
        }
    }

    public void applyVehicleChams(IChamsCapture capture) {
        boolean fill = this.vehicleMode.getValue().equals("Fill") || this.vehicleMode.getValue().equals("Both");
        boolean outline = this.vehicleMode.getValue().equals("Outline") || this.vehicleMode.getValue().equals("Both");
        Color fillColor = this.vehiclePulse.getValue() ? ColorUtils.getPulse(this.vehicleFillColor.getColor()) : this.vehicleFillColor.getColor();
        Color outlineColor = this.vehiclePulse.getValue() ? ColorUtils.getPulse(this.vehicleOutlineColor.getColor()) : this.vehicleOutlineColor.getColor();
        capture.night$setChams(fill, fillColor.getRGB(), outline, outlineColor.getRGB(), this.vehicleShine.getValue());
        capture.night$setChamsRealAlpha(this.vehicleOpacity.getValue().floatValue() / 100.0f);
    }

    private Color pickEntityColor(LivingEntity livingEntity, Color baseColor, Color friendBaseColor) {
        Player player;
        boolean flag;
        boolean bl = flag = this.damageModify.getValue() && livingEntity.hurtTime > 0;
        Color color = livingEntity instanceof Player && Night.FRIEND_MANAGER.contains((player = (Player)livingEntity).getName().getString()) && !this.friendMode.getValue().equals("Sync") ? (flag ? ColorUtils.getColor(this.damageColor.getColor(), friendBaseColor.getAlpha()) : (this.friendMode.getValue().equals("Default") ? Night.FRIEND_MANAGER.getDefaultFriendColor(friendBaseColor.getAlpha()) : friendBaseColor)) : (flag ? ColorUtils.getColor(this.damageColor.getColor(), baseColor.getAlpha()) : baseColor);
        return this.entityPulse.getValue() ? ColorUtils.getPulse(color) : color;
    }
}

