/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.util.Mth
 *  net.minecraft.world.phys.AABB
 */
package night.modules.impl.visuals;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.RenderWorldEvent;
import night.events.impl.TickEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.animations.Easing;
import night.utils.color.ColorUtils;
import night.utils.graphics.EspShader;
import night.utils.graphics.Renderer3D;
import night.utils.minecraft.HoleUtils;

@RegisterModule(name="HoleESP", description="Highlights all holes in a specified radius.", category=Module.Category.VISUALS)
public class HoleESPModule
extends Module {
    public NumberSetting range = new NumberSetting("Range", "The maximum range at which holes will be rendered.", 10, 1, 50);
    public BooleanSetting asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
    public BooleanSetting doubleHoles = new BooleanSetting("DoubleHoles", "Whether or not to render the ESP on double holes.", true);
    public BooleanSetting quadHoles = new BooleanSetting("QuadHoles", "Whether or not to render the ESP on quad holes.", true);
    public BooleanSetting partialHoles = new BooleanSetting("PartialHoles", "Whether or not to render the ESP on partial (incomplete) holes.", true);
    public BooleanSetting fade = new BooleanSetting("Fade", "Fades the holes in and out based on your distance to them.", false);
    public ModeSetting fill = new ModeSetting("Fill", "The mode for the fill rendering on the hole boxes.", "Normal", new String[]{"None", "Normal", "Gradient"});
    public NumberSetting fillHeight = new NumberSetting("FillHeight", "The height of the fill rendering on the holes.", new ModeSetting.Visibility(this.fill, "Normal", "Gradient"), (Number)1.0, (Number)(-2.0), (Number)2.0);
    public ModeSetting outline = new ModeSetting("Outline", "The mode for the outline rendering on the hole boxes.", "Normal", new String[]{"None", "Normal", "Gradient"});
    public NumberSetting outlineHeight = new NumberSetting("OutlineHeight", "The height of the outline rendering on the holes.", new ModeSetting.Visibility(this.outline, "Normal", "Gradient"), (Number)1.0, (Number)(-2.0), (Number)2.0);
    public BooleanSetting wireframe = new BooleanSetting("Wireframe", "Renders an X wireframe on the hole boxes.", false);
    public CategorySetting safeColorsCategory = new CategorySetting("Safe", "The category that contains the settings for coloring of safe (all-bedrock) holes.");
    public ColorSetting safeFillColor = new ColorSetting("SafeFillColor", "Fill", "The color for the fill rendering on safe holes.", new CategorySetting.Visibility(this.safeColorsCategory), new ColorSetting.Color(new Color(0, 255, 0, ColorUtils.getDefaultFillColor().getColor().getAlpha()), false, false));
    public ColorSetting safeOutlineColor = new ColorSetting("SafeOutlineColor", "Outline", "The color for the outline rendering on safe holes.", new CategorySetting.Visibility(this.safeColorsCategory), new ColorSetting.Color(new Color(0, 255, 0, ColorUtils.getDefaultOutlineColor().getColor().getAlpha()), false, false));
    public ColorSetting safeWireframeColor = new ColorSetting("SafeWireframeColor", "Wireframe", "The color for the wireframe rendering on safe holes.", new CategorySetting.Visibility(this.safeColorsCategory), new ColorSetting.Color(new Color(0, 255, 0, 125), false, false));
    public ModeSetting safeShader = new ModeSetting("SafeShader", "Shader", "The animated shader for safe holes.", new CategorySetting.Visibility(this.safeColorsCategory), "None", EspShader.MODES);
    public NumberSetting safeShaderOpacity = new NumberSetting("SafeShaderOpacity", "Opacity", "Safe hole shader opacity.", new ModeSetting.Visibility(this.safeShader, EspShader.SHADER_ACTIVE_MODES), 100, 0, 100);
    public NumberSetting safeShaderSpeed = new NumberSetting("SafeShaderSpeed", "Speed", "Safe hole shader speed.", new ModeSetting.Visibility(this.safeShader, EspShader.SHADER_ACTIVE_MODES), 1.0, 0.1, 10.0);
    public NumberSetting safeShaderStep = new NumberSetting("SafeShaderStep", "Step", "Safe hole gradient step size.", new ModeSetting.Visibility(this.safeShader, "Gradient"), 50.0, 0.1, 200.0);
    public ColorSetting safeShaderColor1 = new ColorSetting("SafeShaderColor1", "The first gradient color for safe holes.", new ModeSetting.Visibility(this.safeShader, "Gradient"), new ColorSetting.Color(new Color(0, 255, 0, 255), false, false));
    public ColorSetting safeShaderColor2 = new ColorSetting("SafeShaderColor2", "The second gradient color for safe holes.", new ModeSetting.Visibility(this.safeShader, "Gradient"), new ColorSetting.Color(new Color(0, 200, 100, 255), false, false));
    public ColorSetting safeShaderColor3 = new ColorSetting("SafeShaderColor3", "The third gradient color for safe holes.", new ModeSetting.Visibility(this.safeShader, "Gradient"), new ColorSetting.Color(new Color(0, 255, 200, 255), false, false));
    public ColorSetting safeShaderColor4 = new ColorSetting("SafeShaderColor4", "The fourth gradient color for safe holes.", new ModeSetting.Visibility(this.safeShader, "Gradient"), new ColorSetting.Color(new Color(100, 255, 0, 255), false, false));
    public ColorSetting safeShaderGlowColor = new ColorSetting("SafeShaderGlowColor", "GlowColor", "The glow tint color for safe holes.", new ModeSetting.Visibility(this.safeShader, "Glowing"), new ColorSetting.Color(new Color(0, 255, 0, 255), false, false));
    public CategorySetting unsafeColorsCategory = new CategorySetting("Unsafe", "The category that contains the settings for coloring of unsafe (blast-proof but not pure bedrock) holes.");
    public ColorSetting unsafeFillColor = new ColorSetting("UnsafeFillColor", "Fill", "The color for the fill rendering on unsafe holes.", new CategorySetting.Visibility(this.unsafeColorsCategory), new ColorSetting.Color(new Color(255, 0, 0, ColorUtils.getDefaultFillColor().getColor().getAlpha()), false, false));
    public ColorSetting unsafeOutlineColor = new ColorSetting("UnsafeOutlineColor", "Outline", "The color for the outline rendering on unsafe holes.", new CategorySetting.Visibility(this.unsafeColorsCategory), new ColorSetting.Color(new Color(255, 0, 0, ColorUtils.getDefaultOutlineColor().getColor().getAlpha()), false, false));
    public ColorSetting unsafeWireframeColor = new ColorSetting("UnsafeWireframeColor", "Wireframe", "The color for the wireframe rendering on unsafe holes.", new CategorySetting.Visibility(this.unsafeColorsCategory), new ColorSetting.Color(new Color(255, 0, 0, 125), false, false));
    public ModeSetting unsafeShader = new ModeSetting("UnsafeShader", "Shader", "The animated shader for unsafe holes.", new CategorySetting.Visibility(this.unsafeColorsCategory), "None", EspShader.MODES);
    public NumberSetting unsafeShaderOpacity = new NumberSetting("UnsafeShaderOpacity", "Opacity", "Unsafe hole shader opacity.", new ModeSetting.Visibility(this.unsafeShader, EspShader.SHADER_ACTIVE_MODES), 100, 0, 100);
    public NumberSetting unsafeShaderSpeed = new NumberSetting("UnsafeShaderSpeed", "Speed", "Unsafe hole shader speed.", new ModeSetting.Visibility(this.unsafeShader, EspShader.SHADER_ACTIVE_MODES), 1.0, 0.1, 10.0);
    public NumberSetting unsafeShaderStep = new NumberSetting("UnsafeShaderStep", "Step", "Unsafe hole gradient step size.", new ModeSetting.Visibility(this.unsafeShader, "Gradient"), 50.0, 0.1, 200.0);
    public ColorSetting unsafeShaderColor1 = new ColorSetting("UnsafeShaderColor1", "The first gradient color for unsafe holes.", new ModeSetting.Visibility(this.unsafeShader, "Gradient"), new ColorSetting.Color(new Color(255, 0, 0, 255), false, false));
    public ColorSetting unsafeShaderColor2 = new ColorSetting("UnsafeShaderColor2", "The second gradient color for unsafe holes.", new ModeSetting.Visibility(this.unsafeShader, "Gradient"), new ColorSetting.Color(new Color(255, 100, 0, 255), false, false));
    public ColorSetting unsafeShaderColor3 = new ColorSetting("UnsafeShaderColor3", "The third gradient color for unsafe holes.", new ModeSetting.Visibility(this.unsafeShader, "Gradient"), new ColorSetting.Color(new Color(255, 0, 100, 255), false, false));
    public ColorSetting unsafeShaderColor4 = new ColorSetting("UnsafeShaderColor4", "The fourth gradient color for unsafe holes.", new ModeSetting.Visibility(this.unsafeShader, "Gradient"), new ColorSetting.Color(new Color(200, 0, 0, 255), false, false));
    public ColorSetting unsafeShaderGlowColor = new ColorSetting("UnsafeShaderGlowColor", "GlowColor", "The glow tint color for unsafe holes.", new ModeSetting.Visibility(this.unsafeShader, "Glowing"), new ColorSetting.Color(new Color(255, 0, 0, 255), false, false));
    public CategorySetting partialColorsCategory = new CategorySetting("Partial", "The category that contains the settings for coloring of partial (incomplete) holes.");
    public ColorSetting partialFillColor = new ColorSetting("PartialFillColor", "Fill", "The color for the fill rendering on partial holes.", new CategorySetting.Visibility(this.partialColorsCategory), new ColorSetting.Color(new Color(255, 255, 255, ColorUtils.getDefaultFillColor().getColor().getAlpha()), false, false));
    public ColorSetting partialOutlineColor = new ColorSetting("PartialOutlineColor", "Outline", "The color for the outline rendering on partial holes.", new CategorySetting.Visibility(this.partialColorsCategory), new ColorSetting.Color(new Color(255, 255, 255, ColorUtils.getDefaultOutlineColor().getColor().getAlpha()), false, false));
    public ColorSetting partialWireframeColor = new ColorSetting("PartialWireframeColor", "Wireframe", "The color for the wireframe rendering on partial holes.", new CategorySetting.Visibility(this.partialColorsCategory), new ColorSetting.Color(new Color(255, 255, 255, 125), false, false));
    public ModeSetting partialShader = new ModeSetting("PartialShader", "Shader", "The animated shader for partial holes.", new CategorySetting.Visibility(this.partialColorsCategory), "None", EspShader.MODES);
    public NumberSetting partialShaderOpacity = new NumberSetting("PartialShaderOpacity", "Opacity", "Partial hole shader opacity.", new ModeSetting.Visibility(this.partialShader, EspShader.SHADER_ACTIVE_MODES), 100, 0, 100);
    public NumberSetting partialShaderSpeed = new NumberSetting("PartialShaderSpeed", "Speed", "Partial hole shader speed.", new ModeSetting.Visibility(this.partialShader, EspShader.SHADER_ACTIVE_MODES), 1.0, 0.1, 10.0);
    public NumberSetting partialShaderStep = new NumberSetting("PartialShaderStep", "Step", "Partial hole gradient step size.", new ModeSetting.Visibility(this.partialShader, "Gradient"), 50.0, 0.1, 200.0);
    public ColorSetting partialShaderColor1 = new ColorSetting("PartialShaderColor1", "The first gradient color for partial holes.", new ModeSetting.Visibility(this.partialShader, "Gradient"), new ColorSetting.Color(new Color(255, 255, 255, 255), false, false));
    public ColorSetting partialShaderColor2 = new ColorSetting("PartialShaderColor2", "The second gradient color for partial holes.", new ModeSetting.Visibility(this.partialShader, "Gradient"), new ColorSetting.Color(new Color(200, 200, 200, 255), false, false));
    public ColorSetting partialShaderColor3 = new ColorSetting("PartialShaderColor3", "The third gradient color for partial holes.", new ModeSetting.Visibility(this.partialShader, "Gradient"), new ColorSetting.Color(new Color(220, 220, 220, 255), false, false));
    public ColorSetting partialShaderColor4 = new ColorSetting("PartialShaderColor4", "The fourth gradient color for partial holes.", new ModeSetting.Visibility(this.partialShader, "Gradient"), new ColorSetting.Color(new Color(180, 180, 180, 255), false, false));
    public ColorSetting partialShaderGlowColor = new ColorSetting("PartialShaderGlowColor", "GlowColor", "The glow tint color for partial holes.", new ModeSetting.Visibility(this.partialShader, "Glowing"), new ColorSetting.Color(new Color(255, 255, 255, 255), false, false));
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<HoleUtils.Hole> holes = Collections.synchronizedList(new ArrayList());

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (HoleESPModule.mc.player == null || HoleESPModule.mc.level == null) {
            return;
        }
        BlockPos playerPos = HoleESPModule.mc.player.blockPosition();
        int count = Night.WORLD_MANAGER.getRadius(this.range.getValue().doubleValue());
        Runnable runnable = () -> {
            ArrayList<HoleUtils.Hole> newHoles = new ArrayList<HoleUtils.Hole>();
            for (int i = 0; i < count; ++i) {
                HoleUtils.Hole partialHole;
                HoleUtils.Hole quadHole;
                HoleUtils.Hole doubleHole;
                BlockPos position = playerPos.offset(Night.WORLD_MANAGER.getOffset(i));
                HoleUtils.Hole singleHole = HoleUtils.getSingleHole(position, 0.0);
                if (singleHole != null) {
                    newHoles.add(singleHole);
                    continue;
                }
                if (this.doubleHoles.getValue() && (doubleHole = HoleUtils.getDoubleHole(position, 0.0)) != null) {
                    newHoles.add(doubleHole);
                    continue;
                }
                if (this.quadHoles.getValue() && (quadHole = HoleUtils.getQuadHole(position, 0.0)) != null) {
                    newHoles.add(quadHole);
                    continue;
                }
                if (!this.partialHoles.getValue() || (partialHole = HoleUtils.getPartialHole(position, 0.0)) == null) continue;
                newHoles.add(partialHole);
            }
            List<HoleUtils.Hole> list = this.holes;
            synchronized (list) {
                this.holes.clear();
                this.holes.addAll(newHoles);
            }
        };
        if (this.asynchronous.getValue()) {
            this.executor.submit(runnable);
        } else {
            runnable.run();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (HoleESPModule.mc.level == null) {
            return;
        }
        List<HoleUtils.Hole> list = this.holes;
        synchronized (list) {
            if (this.holes.isEmpty()) {
                return;
            }
            boolean hasSafeShader = !this.safeShader.getValue().equalsIgnoreCase("None");
            boolean hasUnsafeShader = !this.unsafeShader.getValue().equalsIgnoreCase("None");
            boolean hasPartialShader = !this.partialShader.getValue().equalsIgnoreCase("None");
            ArrayList<Renderer3D.VertexCollection> safeQuads = new ArrayList<Renderer3D.VertexCollection>();
            ArrayList<Renderer3D.VertexCollection> safeLines = new ArrayList<Renderer3D.VertexCollection>();
            ArrayList<Renderer3D.VertexCollection> unsafeQuads = new ArrayList<Renderer3D.VertexCollection>();
            ArrayList<Renderer3D.VertexCollection> unsafeLines = new ArrayList<Renderer3D.VertexCollection>();
            ArrayList<Renderer3D.VertexCollection> partialQuads = new ArrayList<Renderer3D.VertexCollection>();
            ArrayList<Renderer3D.VertexCollection> partialLines = new ArrayList<Renderer3D.VertexCollection>();
            boolean fillNormal = this.fill.getValue().equalsIgnoreCase("Normal");
            boolean fillGradient = this.fill.getValue().equalsIgnoreCase("Gradient");
            boolean outlineNormal = this.outline.getValue().equalsIgnoreCase("Normal");
            boolean outlineGradient = this.outline.getValue().equalsIgnoreCase("Gradient");
            for (HoleUtils.Hole hole : this.holes) {
                boolean isPartial;
                if (!Renderer3D.isFrustumVisible(hole.box())) continue;
                boolean isSafe = hole.safety() == HoleUtils.HoleSafety.SAFE;
                boolean bl = isPartial = hole.safety() == HoleUtils.HoleSafety.PARTIAL;
                boolean useShader = isSafe ? hasSafeShader : (isPartial ? hasPartialShader : hasUnsafeShader);
                AABB filledBox = new AABB(hole.box().minX, hole.box().minY, hole.box().minZ, hole.box().maxX, hole.box().minY + this.fillHeight.getValue().doubleValue(), hole.box().maxZ);
                AABB outlinedBox = new AABB(hole.box().minX, hole.box().minY, hole.box().minZ, hole.box().maxX, hole.box().minY + this.outlineHeight.getValue().doubleValue(), hole.box().maxZ);
                AABB wireframeBox = new AABB(hole.box().minX, hole.box().minY, hole.box().minZ, hole.box().maxX, hole.box().minY, hole.box().maxZ);
                Color fillColor = this.getFillColor(hole);
                Color outlineColor = this.getOutlineColor(hole);
                Color wireColor = this.getWireframeColor(hole);
                if (useShader) {
                    ArrayList<Renderer3D.VertexCollection> lines;
                    ArrayList<Renderer3D.VertexCollection> quads;
                    quads = isSafe ? safeQuads : (isPartial ? partialQuads : unsafeQuads);
                    lines = isSafe ? safeLines : (isPartial ? partialLines : unsafeLines);
                    if (fillNormal) {
                        Renderer3D.renderGradientBox(quads, event.getMatrices(), filledBox, fillColor, fillColor);
                    }
                    if (fillGradient) {
                        Renderer3D.renderGradientBox(quads, event.getMatrices(), filledBox, this.fillHeight.getValue().floatValue() < 0.0f ? fillColor : new Color(0, 0, 0, 0), this.fillHeight.getValue().floatValue() < 0.0f ? new Color(0, 0, 0, 0) : fillColor);
                    }
                    if (outlineNormal) {
                        Renderer3D.renderGradientBoxOutline(lines, event.getMatrices(), outlinedBox, outlineColor, outlineColor);
                    }
                    if (outlineGradient) {
                        Renderer3D.renderGradientBoxOutline(lines, event.getMatrices(), outlinedBox, this.outlineHeight.getValue().floatValue() < 0.0f ? outlineColor : new Color(0, 0, 0, 0), this.outlineHeight.getValue().floatValue() < 0.0f ? new Color(0, 0, 0, 0) : outlineColor);
                    }
                    if (!this.wireframe.getValue()) continue;
                    Renderer3D.renderBoxWireframe(lines, event.getMatrices(), wireframeBox, wireColor, wireColor);
                    continue;
                }
                if (fillNormal) {
                    Renderer3D.renderBox(event.getMatrices(), filledBox, fillColor);
                }
                if (fillGradient) {
                    Renderer3D.renderGradientBox(event.getMatrices(), filledBox, this.fillHeight.getValue().floatValue() < 0.0f ? fillColor : new Color(0, 0, 0, 0), this.fillHeight.getValue().floatValue() < 0.0f ? new Color(0, 0, 0, 0) : fillColor);
                }
                if (outlineNormal) {
                    Renderer3D.renderBoxOutline(event.getMatrices(), outlinedBox, outlineColor);
                }
                if (outlineGradient) {
                    Renderer3D.renderGradientBoxOutline(event.getMatrices(), outlinedBox, this.outlineHeight.getValue().floatValue() < 0.0f ? outlineColor : new Color(0, 0, 0, 0), this.outlineHeight.getValue().floatValue() < 0.0f ? new Color(0, 0, 0, 0) : outlineColor);
                }
                if (!this.wireframe.getValue()) continue;
                Renderer3D.renderBoxWireframe(event.getMatrices(), wireframeBox, wireColor);
            }
            if (!(!hasSafeShader || safeQuads.isEmpty() && safeLines.isEmpty())) {
                EspShader.Settings safeSettings = EspShader.createSettings(this.safeShader.getValue(), this.safeShaderOpacity.getValue().floatValue(), this.safeShaderSpeed.getValue().floatValue(), this.safeShaderStep.getValue().floatValue(), this.safeShaderColor1.getColor(), this.safeShaderColor2.getColor(), this.safeShaderColor3.getColor(), this.safeShaderColor4.getColor(), this.safeShaderGlowColor.getColor(), null);
                EspShader.draw(safeQuads, safeLines, safeSettings);
            }
            if (!(!hasUnsafeShader || unsafeQuads.isEmpty() && unsafeLines.isEmpty())) {
                EspShader.Settings unsafeSettings = EspShader.createSettings(this.unsafeShader.getValue(), this.unsafeShaderOpacity.getValue().floatValue(), this.unsafeShaderSpeed.getValue().floatValue(), this.unsafeShaderStep.getValue().floatValue(), this.unsafeShaderColor1.getColor(), this.unsafeShaderColor2.getColor(), this.unsafeShaderColor3.getColor(), this.unsafeShaderColor4.getColor(), this.unsafeShaderGlowColor.getColor(), null);
                EspShader.draw(unsafeQuads, unsafeLines, unsafeSettings);
            }
            if (!(!hasPartialShader || partialQuads.isEmpty() && partialLines.isEmpty())) {
                EspShader.Settings partialSettings = EspShader.createSettings(this.partialShader.getValue(), this.partialShaderOpacity.getValue().floatValue(), this.partialShaderSpeed.getValue().floatValue(), this.partialShaderStep.getValue().floatValue(), this.partialShaderColor1.getColor(), this.partialShaderColor2.getColor(), this.partialShaderColor3.getColor(), this.partialShaderColor4.getColor(), this.partialShaderGlowColor.getColor(), null);
                EspShader.draw(partialQuads, partialLines, partialSettings);
            }
        }
    }

    @Override
    public String getMetaData() {
        return String.valueOf(this.holes.size());
    }

    private Color getFillColor(HoleUtils.Hole hole) {
        Color color;
        switch (hole.safety()) {
            case SAFE: {
                color = this.safeFillColor.getColor();
                break;
            }
            case PARTIAL: {
                color = this.partialFillColor.getColor();
                break;
            }
            default: {
                Color color2 = color = this.unsafeFillColor.getColor();
            }
        }
        if (!this.fade.getValue()) {
            return color;
        }
        return ColorUtils.getColor(color, (int)((float)color.getAlpha() * this.getEasing(hole)));
    }

    private Color getOutlineColor(HoleUtils.Hole hole) {
        Color color;
        switch (hole.safety()) {
            case SAFE: {
                color = this.safeOutlineColor.getColor();
                break;
            }
            case PARTIAL: {
                color = this.partialOutlineColor.getColor();
                break;
            }
            default: {
                Color color2 = color = this.unsafeOutlineColor.getColor();
            }
        }
        if (!this.fade.getValue()) {
            return color;
        }
        return ColorUtils.getColor(color, (int)((float)color.getAlpha() * this.getEasing(hole)));
    }

    private Color getWireframeColor(HoleUtils.Hole hole) {
        Color color;
        switch (hole.safety()) {
            case SAFE: {
                color = this.safeWireframeColor.getColor();
                break;
            }
            case PARTIAL: {
                color = this.partialWireframeColor.getColor();
                break;
            }
            default: {
                Color color2 = color = this.unsafeWireframeColor.getColor();
            }
        }
        if (!this.fade.getValue()) {
            return color;
        }
        return ColorUtils.getColor(color, (int)((float)color.getAlpha() * this.getEasing(hole)));
    }

    private float getEasing(HoleUtils.Hole hole) {
        float scale = (float)(1.0 - Mth.clamp((double)(Math.sqrt(HoleESPModule.mc.player.distanceToSqr(hole.box().getCenter())) / this.range.getValue().doubleValue()), (double)0.0, (double)1.0));
        return Easing.ease(scale, Easing.Method.EASE_OUT_CUBIC);
    }
}

