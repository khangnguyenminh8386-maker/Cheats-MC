/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.block.Blocks
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
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
import night.utils.color.ColorUtils;
import night.utils.graphics.EspShader;
import night.utils.graphics.Renderer3D;

@RegisterModule(name="VoidESP", description="Highlights any non-bedrock blocks that can drop you into the void.", category=Module.Category.VISUALS)
public class VoidESPModule
extends Module {
    public NumberSetting range = new NumberSetting("Range", "The maximum range at which void blocks will be rendered.", 10, 1, 50);
    public BooleanSetting asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
    public CategorySetting fillCategory = new CategorySetting("Fill", "The category for settings related to fill rendering.");
    public ModeSetting fill = new ModeSetting("Fill", "Mode", "The mode for the fill rendering on the void blocks.", new CategorySetting.Visibility(this.fillCategory), "Normal", new String[]{"None", "Normal", "Gradient"});
    public NumberSetting fillHeight = new NumberSetting("FillHeight", "Height", "The height of the fill rendering on the void blocks.", new ModeSetting.Visibility(this.fill, "Normal", "Gradient"), 1.0, 0.0, 2.0);
    public ColorSetting fillColor = new ColorSetting("FillColor", "Color", "The color for the fill rendering on the void blocks.", new ModeSetting.Visibility(this.fill, "Normal", "Gradient"), new ColorSetting.Color(new Color(255, 0, 0, ColorUtils.getDefaultFillColor().getColor().getAlpha()), false, false));
    public CategorySetting outlineCategory = new CategorySetting("Outline", "The category for settings related to outline rendering.");
    public ModeSetting outline = new ModeSetting("Outline", "Mode", "The mode for the outline rendering on the void blocks.", new CategorySetting.Visibility(this.outlineCategory), "Normal", new String[]{"None", "Normal", "Gradient"});
    public NumberSetting outlineHeight = new NumberSetting("OutlineHeight", "Height", "The height of the outline rendering on the void blocks.", new ModeSetting.Visibility(this.outline, "Normal", "Gradient"), 1.0, 0.0, 2.0);
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "Color", "The color for the outline rendering on the void blocks.", new ModeSetting.Visibility(this.outline, "Normal", "Gradient"), new ColorSetting.Color(new Color(255, 0, 0, ColorUtils.getDefaultOutlineColor().getColor().getAlpha()), false, false));
    public CategorySetting shaderCategory = new CategorySetting("Shader", "The category for settings related to shader rendering.");
    public ModeSetting shader = new ModeSetting("Shader", "Mode", "The animated shader that will be rendered on the void blocks.", new CategorySetting.Visibility(this.shaderCategory), "None", EspShader.MODES);
    public NumberSetting shaderOpacity = new NumberSetting("ShaderOpacity", "Opacity", "Void block shader opacity.", new ModeSetting.Visibility(this.shader, EspShader.SHADER_ACTIVE_MODES), 100, 0, 100);
    public NumberSetting shaderSpeed = new NumberSetting("ShaderSpeed", "Speed", "Void block shader animation speed.", new ModeSetting.Visibility(this.shader, EspShader.SHADER_ACTIVE_MODES), 1.0, 0.1, 10.0);
    public NumberSetting shaderStep = new NumberSetting("ShaderStep", "Step", "Void block gradient step size.", new ModeSetting.Visibility(this.shader, "Gradient"), 50.0, 0.1, 200.0);
    public ColorSetting shaderColor1 = new ColorSetting("ShaderColor1", "The first gradient color.", new ModeSetting.Visibility(this.shader, "Gradient"), new ColorSetting.Color(new Color(255, 0, 0, 255), false, false));
    public ColorSetting shaderColor2 = new ColorSetting("ShaderColor2", "The second gradient color.", new ModeSetting.Visibility(this.shader, "Gradient"), new ColorSetting.Color(new Color(180, 0, 255, 255), false, false));
    public ColorSetting shaderColor3 = new ColorSetting("ShaderColor3", "The third gradient color.", new ModeSetting.Visibility(this.shader, "Gradient"), new ColorSetting.Color(new Color(255, 0, 100, 255), false, false));
    public ColorSetting shaderColor4 = new ColorSetting("ShaderColor4", "The fourth gradient color.", new ModeSetting.Visibility(this.shader, "Gradient"), new ColorSetting.Color(new Color(100, 0, 255, 255), false, false));
    public ColorSetting shaderGlowColor = new ColorSetting("ShaderGlowColor", "GlowColor", "The glow tint color for void blocks.", new ModeSetting.Visibility(this.shader, "Glowing"), new ColorSetting.Color(new Color(255, 0, 0, 255), false, false));
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<BlockPos> positions = Collections.synchronizedList(new ArrayList());

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (VoidESPModule.mc.player == null || VoidESPModule.mc.level == null) {
            return;
        }
        Runnable runnable = () -> {
            ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
            int x = (int)(VoidESPModule.mc.player.getX() - (double)this.range.getValue().intValue());
            while ((double)x < VoidESPModule.mc.player.getX() + (double)this.range.getValue().intValue()) {
                int z = (int)(VoidESPModule.mc.player.getZ() - (double)this.range.getValue().intValue());
                while ((double)z < VoidESPModule.mc.player.getZ() + (double)this.range.getValue().intValue()) {
                    BlockPos position = BlockPos.containing((double)x, (double)VoidESPModule.mc.level.getMinY(), (double)z);
                    if (VoidESPModule.mc.level.getBlockState(position).getBlock() != Blocks.BEDROCK && VoidESPModule.mc.level.getWorldBorder().isWithinBounds(position)) {
                        positions.add(position);
                    }
                    ++z;
                }
                ++x;
            }
            List<BlockPos> list = this.positions;
            synchronized (list) {
                this.positions.clear();
                this.positions.addAll(positions);
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
        if (VoidESPModule.mc.level == null) {
            return;
        }
        List<BlockPos> list = this.positions;
        synchronized (list) {
            if (this.positions.isEmpty()) {
                return;
            }
            boolean hasShader = !this.shader.getValue().equalsIgnoreCase("None");
            ArrayList<Renderer3D.VertexCollection> quads = new ArrayList<Renderer3D.VertexCollection>();
            ArrayList<Renderer3D.VertexCollection> lines = new ArrayList<Renderer3D.VertexCollection>();
            boolean fillNormal = this.fill.getValue().equalsIgnoreCase("Normal");
            boolean fillGradient = this.fill.getValue().equalsIgnoreCase("Gradient");
            boolean outlineNormal = this.outline.getValue().equalsIgnoreCase("Normal");
            boolean outlineGradient = this.outline.getValue().equalsIgnoreCase("Gradient");
            for (BlockPos position : this.positions) {
                AABB box = new AABB(position);
                AABB filledBox = new AABB(box.minX, box.minY, box.minZ, box.maxX, box.minY + this.fillHeight.getValue().doubleValue(), box.maxZ);
                AABB outlinedBox = new AABB(box.minX, box.minY, box.minZ, box.maxX, box.minY + this.outlineHeight.getValue().doubleValue(), box.maxZ);
                if (hasShader) {
                    if (fillNormal) {
                        Renderer3D.renderGradientBox(quads, event.getMatrices(), filledBox, this.fillColor.getColor(), this.fillColor.getColor());
                    }
                    if (fillGradient) {
                        Renderer3D.renderGradientBox(quads, event.getMatrices(), filledBox, this.fillHeight.getValue().floatValue() < 0.0f ? this.fillColor.getColor() : new Color(0, 0, 0, 0), this.fillHeight.getValue().floatValue() < 0.0f ? new Color(0, 0, 0, 0) : this.fillColor.getColor());
                    }
                    if (outlineNormal) {
                        Renderer3D.renderGradientBoxOutline(lines, event.getMatrices(), outlinedBox, this.outlineColor.getColor(), this.outlineColor.getColor());
                    }
                    if (!outlineGradient) continue;
                    Renderer3D.renderGradientBoxOutline(lines, event.getMatrices(), outlinedBox, this.outlineHeight.getValue().floatValue() < 0.0f ? this.outlineColor.getColor() : new Color(0, 0, 0, 0), this.outlineHeight.getValue().floatValue() < 0.0f ? new Color(0, 0, 0, 0) : this.outlineColor.getColor());
                    continue;
                }
                if (fillNormal) {
                    Renderer3D.renderBox(event.getMatrices(), filledBox, this.fillColor.getColor());
                }
                if (fillGradient) {
                    Renderer3D.renderGradientBox(event.getMatrices(), filledBox, this.fillHeight.getValue().floatValue() < 0.0f ? this.fillColor.getColor() : new Color(0, 0, 0, 0), this.fillHeight.getValue().floatValue() < 0.0f ? new Color(0, 0, 0, 0) : this.fillColor.getColor());
                }
                if (outlineNormal) {
                    Renderer3D.renderBoxOutline(event.getMatrices(), outlinedBox, this.outlineColor.getColor());
                }
                if (!outlineGradient) continue;
                Renderer3D.renderGradientBoxOutline(event.getMatrices(), outlinedBox, this.outlineHeight.getValue().floatValue() < 0.0f ? this.outlineColor.getColor() : new Color(0, 0, 0, 0), this.outlineHeight.getValue().floatValue() < 0.0f ? new Color(0, 0, 0, 0) : this.outlineColor.getColor());
            }
            if (!(!hasShader || quads.isEmpty() && lines.isEmpty())) {
                EspShader.Settings shaderSettings = EspShader.createSettings(this.shader.getValue(), this.shaderOpacity.getValue().floatValue(), this.shaderSpeed.getValue().floatValue(), this.shaderStep.getValue().floatValue(), this.shaderColor1.getColor(), this.shaderColor2.getColor(), this.shaderColor3.getColor(), this.shaderColor4.getColor(), this.shaderGlowColor.getColor(), null);
                EspShader.draw(quads, lines, shaderSettings);
            }
        }
    }

    @Override
    public String getMetaData() {
        return String.valueOf(this.positions.size());
    }
}

