/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 */
package night.modules.impl.core;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Font;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.SettingChangeEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.core.HUDModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.settings.impl.StringSetting;
import night.utils.font.FontRenderer;

@RegisterModule(name="Font", description="Manages the client and the game's font rendering.", category=Module.Category.CORE, toggled=true, drawn=false)
public class FontModule
extends Module {
    public CategorySetting customFontCategory = new CategorySetting("CustomFont", "The category for settings relating to custom fonts.");
    public BooleanSetting customFont = new BooleanSetting("CustomFont", "Enabled", "Enables custom font rendering.", new CategorySetting.Visibility(this.customFontCategory), false);
    public StringSetting name = new StringSetting("Name", "The name of the font that will be rendered.", new CategorySetting.Visibility(this.customFontCategory), "Verdana");
    public NumberSetting size = new NumberSetting("Size", "The size of the custom font that will be rendered.", new CategorySetting.Visibility(this.customFontCategory), (Number)18, (Number)8, (Number)48);
    public ModeSetting style = new ModeSetting("Style", "The style that will be used in the font's rendering.", new CategorySetting.Visibility(this.customFontCategory), "Plain", new String[]{"Plain", "Bold", "Italic", "BoldItalic"});
    public BooleanSetting global = new BooleanSetting("Global", "Applies the custom font rendering on every part of the game.", new CategorySetting.Visibility(this.customFontCategory), false);
    public CategorySetting offsetsCategory = new CategorySetting("Offsets", "Allows you to offset the custom font rendering to make it render perfectly.");
    public NumberSetting xOffset = new NumberSetting("XOffset", "The offset that will be applied to the font on the X axis.", new CategorySetting.Visibility(this.offsetsCategory), (Number)0, (Number)(-10), (Number)10);
    public NumberSetting yOffset = new NumberSetting("YOffset", "The offset that will be applied to the font on the Y axis.", new CategorySetting.Visibility(this.offsetsCategory), (Number)0, (Number)(-10), (Number)10);
    public NumberSetting widthOffset = new NumberSetting("WidthOffset", "The offset that will be applied to the font on the X axis.", new CategorySetting.Visibility(this.offsetsCategory), (Number)0, (Number)(-10), (Number)10);
    public NumberSetting heightOffset = new NumberSetting("HeightOffset", "The font's offset on the Y axis.", new CategorySetting.Visibility(this.offsetsCategory), (Number)0, (Number)(-10), (Number)10);
    public CategorySetting shadowsCategory = new CategorySetting("Shadows", "The category for settings related to font shadows.");
    public ModeSetting shadowMode = new ModeSetting("ShadowMode", "Mode", "The way that the shadow will be rendered.", new CategorySetting.Visibility(this.shadowsCategory), "Default", new String[]{"None", "Default", "Custom"});
    public NumberSetting shadowOffset = new NumberSetting("ShadowOffset", "Offset", "The distance of the shadow from the text being rendered.", new ModeSetting.Visibility(this.shadowMode, "Custom"), Float.valueOf(0.5f), Float.valueOf(-2.0f), Float.valueOf(2.0f));
    public BooleanSetting glow = new BooleanSetting("Glow", "Renders a glow shader effect for HUD text only.", false);

    @SubscribeEvent
    public void onSettingChange(SettingChangeEvent event) {
        HUDModule hud;
        if (event.getSetting() == this.glow && (hud = HUDModule.INSTANCE) != null && hud.textGlow.getValue() != this.glow.getValue()) {
            hud.textGlow.setValue(this.glow.getValue());
        }
        if (event.getSetting() == this.name || event.getSetting() == this.size || event.getSetting() == this.style || event.getSetting() == this.customFont || event.getSetting() == this.glow) {
            this.updateFontRenderer();
        }
    }

    @Override
    public void onEnable() {
        this.updateFontRenderer();
    }

    public void updateFontRenderer() {
        try {
            if (RenderSystem.getDevice() == null) {
                return;
            }
        }
        catch (Throwable ignored) {
            return;
        }
        int fontStyle = this.style.getValue().equalsIgnoreCase("BoldItalic") ? 3 : (this.style.getValue().equalsIgnoreCase("Bold") ? 1 : (this.style.getValue().equalsIgnoreCase("Italic") ? 2 : 0));
        int fontSize = this.size.getValue().intValue();
        Font primaryFont = new Font(this.name.getValue(), fontStyle, fontSize);
        Font segoeSymbol = new Font("Segoe UI Symbol", fontStyle, fontSize);
        Font segoeEmoji = new Font("Segoe UI Emoji", 0, fontSize);
        Font arialUnicode = new Font("Arial Unicode MS", fontStyle, fontSize);
        Font sansSerif = new Font("SansSerif", fontStyle, fontSize);
        Font dialog = new Font("Dialog", fontStyle, fontSize);
        Night.FONT_MANAGER.setFontRenderer(new FontRenderer(new Font[]{primaryFont, segoeSymbol, segoeEmoji, arialUnicode, sansSerif, dialog}, this.size.getValue().floatValue() / 2.0f));
    }
}

