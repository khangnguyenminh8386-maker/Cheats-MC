/*
 * Decompiled with CFR 0.152.
 */
package night.modules.impl.visuals;

import java.awt.Color;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.api.IAmbienceModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.NumberSetting;

@RegisterModule(name="Ambience", description="Modifies the world ambient lighting and atmosphere color.", category=Module.Category.VISUALS)
public class AmbienceModule
extends Module
implements IAmbienceModule {
    public ColorSetting color = new ColorSetting("Color", "The ambience color applied to the world.", new ColorSetting.Color(new Color(255, 128, 128, 255), true, false));
    public BooleanSetting useSaturation = new BooleanSetting("UseSaturation", "Overrides the saturation of the ambience color.", false);
    public NumberSetting saturation = new NumberSetting("Saturation", "The custom saturation level.", new BooleanSetting.Visibility(this.useSaturation, true), (Number)Float.valueOf(0.5f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(1.0f));
    public BooleanSetting lightmap = new BooleanSetting("Lightmap", "Modifies the world lightmap lighting.", true);
    public BooleanSetting sky = new BooleanSetting("Sky", "Tints sky lighting with ambience.", true);
    public BooleanSetting blocks = new BooleanSetting("Blocks", "Tints block lighting with ambience.", true);

    @Override
    public Color getColor() {
        Color c = this.color.getColor();
        if (this.useSaturation.getValue()) {
            float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
            Color satColor = Color.getHSBColor(hsb[0], this.saturation.getValue().floatValue(), hsb[2]);
            return new Color(satColor.getRed(), satColor.getGreen(), satColor.getBlue(), c.getAlpha());
        }
        return c;
    }

    public boolean useSaturation() {
        return this.useSaturation.getValue();
    }

    public float getSaturation() {
        return this.saturation.getValue().floatValue();
    }

    @Override
    public boolean isLightmapEnabled() {
        return this.lightmap.getValue();
    }

    @Override
    public boolean isSkyEnabled() {
        return this.sky.getValue();
    }

    @Override
    public boolean isBlocksEnabled() {
        return this.blocks.getValue();
    }
}

