/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.AABB
 */
package night.modules.impl.visuals;

import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
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
import night.utils.graphics.Renderer3D;
import night.utils.minecraft.PositionUtils;

@RegisterModule(name="PhaseESP", description="Renders ESP on blocks which have a safe base.", category=Module.Category.VISUALS)
public class PhaseESPModule
extends Module {
    public ModeSetting fill = new ModeSetting("Fill", "The mode for the fill rendering on the phase blocks.", "Normal", new String[]{"None", "Normal"});
    public NumberSetting fillHeight = new NumberSetting("FillHeight", "The height of the fill rendering on the phase blocks.", new ModeSetting.Visibility(this.fill, "Normal"), (Number)1.0, (Number)(-2.0), (Number)2.0);
    public ModeSetting outline = new ModeSetting("Outline", "The mode for the outline rendering on the phase blocks.", "Normal", new String[]{"None", "Normal"});
    public NumberSetting outlineHeight = new NumberSetting("OutlineHeight", "The height of the outline rendering on the phase blocks.", new ModeSetting.Visibility(this.outline, "Normal"), (Number)1.0, (Number)(-2.0), (Number)2.0);
    public CategorySetting bedrockCategory = new CategorySetting("Bedrock", "The bedrock base category.");
    public BooleanSetting bedrock = new BooleanSetting("Bedrock", "Enabled", "Renders blocks which have a bedrock base.", new CategorySetting.Visibility(this.bedrockCategory), true);
    public ColorSetting bedrockFillColor = new ColorSetting("BedrockFillColor", "Fill", "The color for the fill rendering of bedrock base blocks.", new CategorySetting.Visibility(this.bedrockCategory), new ColorSetting.Color(new Color(0, 255, 0, ColorUtils.getDefaultFillColor().getColor().getAlpha()), false, false));
    public ColorSetting bedrockOutlineColor = new ColorSetting("BedrockOutlineColor", "Outline", "The color for the outline rendering of bedrock base blocks.", new CategorySetting.Visibility(this.bedrockCategory), new ColorSetting.Color(new Color(0, 255, 0, ColorUtils.getDefaultOutlineColor().getColor().getAlpha()), false, false));
    public CategorySetting obsidianCategory = new CategorySetting("Obsidian", "The obsidian base category.");
    public BooleanSetting obsidian = new BooleanSetting("Obsidian", "Enabled", "Renders blocks which have a obsidian base.", new CategorySetting.Visibility(this.obsidianCategory), true);
    public ColorSetting obsidianFillColor = new ColorSetting("ObsidianFillColor", "Fill", "The color for the fill rendering of obsidian base blocks.", new CategorySetting.Visibility(this.obsidianCategory), new ColorSetting.Color(new Color(255, 255, 0, ColorUtils.getDefaultFillColor().getColor().getAlpha()), false, false));
    public ColorSetting obsidianOutlineColor = new ColorSetting("ObsidianOutlineColor", "Outline", "The color for the outline rendering of obsidian base blocks.", new CategorySetting.Visibility(this.obsidianCategory), new ColorSetting.Color(new Color(255, 255, 0, ColorUtils.getDefaultOutlineColor().getColor().getAlpha()), false, false));
    public CategorySetting airCategory = new CategorySetting("Air", "The air base category.");
    public BooleanSetting air = new BooleanSetting("Air", "Enabled", "Renders blocks which have a air base.", new CategorySetting.Visibility(this.airCategory), true);
    public ColorSetting airFillColor = new ColorSetting("AirFillColor", "Fill", "The color for the fill rendering of air base blocks.", new CategorySetting.Visibility(this.airCategory), new ColorSetting.Color(new Color(255, 0, 0, ColorUtils.getDefaultFillColor().getColor().getAlpha()), false, false));
    public ColorSetting airOutlineColor = new ColorSetting("AirOutlineColor", "Outline", "The color for the outline rendering of air base blocks.", new CategorySetting.Visibility(this.airCategory), new ColorSetting.Color(new Color(255, 0, 0, ColorUtils.getDefaultOutlineColor().getColor().getAlpha()), false, false));
    private ArrayList<PhaseBlock> phaseBlocks = new ArrayList();

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (this.getNull()) {
            return;
        }
        ArrayList<PhaseBlock> phaseBlocks = new ArrayList<PhaseBlock>();
        BlockPos playerPos = PositionUtils.getFlooredPosition((Entity)PhaseESPModule.mc.player);
        for (Direction direction : Direction.values()) {
            PhaseType type;
            BlockPos offsetPos;
            if (!direction.getAxis().isHorizontal() || PhaseESPModule.mc.level.getBlockState(offsetPos = playerPos.relative(direction)).getBlock() != Blocks.BEDROCK && PhaseESPModule.mc.level.getBlockState(offsetPos).getBlock() != Blocks.OBSIDIAN || (type = this.getPhaseType(offsetPos)) == null) continue;
            phaseBlocks.add(new PhaseBlock(new AABB(offsetPos), type));
        }
        this.phaseBlocks.clear();
        this.phaseBlocks.addAll(phaseBlocks);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (this.getNull() || this.phaseBlocks.isEmpty()) {
            return;
        }
        for (PhaseBlock phaseBlock : this.phaseBlocks) {
            AABB filledBox = new AABB(phaseBlock.box().minX, phaseBlock.box().minY, phaseBlock.box().minZ, phaseBlock.box().maxX, phaseBlock.box().minY + this.fillHeight.getValue().doubleValue(), phaseBlock.box().maxZ);
            AABB outlinedBox = new AABB(phaseBlock.box().minX, phaseBlock.box().minY, phaseBlock.box().minZ, phaseBlock.box().maxX, phaseBlock.box().minY + this.outlineHeight.getValue().doubleValue(), phaseBlock.box().maxZ);
            if (this.fill.getValue().equalsIgnoreCase("Normal")) {
                Renderer3D.renderBox(event.getMatrices(), filledBox, this.getFillColor(phaseBlock.type()));
            }
            if (!this.outline.getValue().equalsIgnoreCase("Normal")) continue;
            Renderer3D.renderBoxOutline(event.getMatrices(), outlinedBox, this.getOutlineColor(phaseBlock.type()));
        }
    }

    private Color getFillColor(PhaseType type) {
        return switch (type.ordinal()) {
            case 0 -> this.bedrockFillColor.getColor();
            case 1 -> this.obsidianFillColor.getColor();
            default -> this.airFillColor.getColor();
        };
    }

    private Color getOutlineColor(PhaseType type) {
        return switch (type.ordinal()) {
            case 0 -> this.bedrockOutlineColor.getColor();
            case 1 -> this.obsidianOutlineColor.getColor();
            default -> this.airOutlineColor.getColor();
        };
    }

    private PhaseType getPhaseType(BlockPos pos) {
        if (PhaseESPModule.mc.level.getBlockState(pos.below()).getBlock() == Blocks.BEDROCK && this.bedrock.getValue()) {
            return PhaseType.BEDROCK;
        }
        if (PhaseESPModule.mc.level.getBlockState(pos.below()).getBlock() == Blocks.OBSIDIAN && this.obsidian.getValue()) {
            return PhaseType.OBSIDIAN;
        }
        if (PhaseESPModule.mc.level.getBlockState(pos.below()).getBlock() == Blocks.AIR && this.air.getValue()) {
            return PhaseType.AIR;
        }
        return null;
    }

    private static enum PhaseType {
        BEDROCK,
        OBSIDIAN,
        AIR;

    }

    public record PhaseBlock(AABB box, PhaseType type) {
    }
}

