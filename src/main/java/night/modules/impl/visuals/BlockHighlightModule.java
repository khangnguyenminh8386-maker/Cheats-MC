/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package night.modules.impl.visuals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import night.events.SubscribeEvent;
import night.events.impl.RenderWorldEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.animations.Easing;
import night.utils.color.ColorUtils;
import night.utils.graphics.Renderer3D;
import night.utils.minecraft.WorldUtils;
import night.utils.system.MathUtils;

@RegisterModule(name="BlockHighlight", description="Replaces the default Minecraft block highlight with a more customizable one.", category=Module.Category.VISUALS)
public class BlockHighlightModule
extends Module {
    public ModeSetting animationMode = new ModeSetting("Animation", "The animation that will be applied to the rendering.", "Static", new String[]{"Static", "Slide"});
    public ModeSetting mode = new ModeSetting("Mode", "The rendering that will be applied to the target block.", "Outline", new String[]{"None", "Fill", "Outline", "Both", "Complex"});
    public NumberSetting slideSmoothness = new NumberSetting("Smoothness", "The smoothness for the slide while target block is changing.", 1, 0, 20);
    public ColorSetting fillColor = new ColorSetting("FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(this.mode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(this.mode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    private BlockPos prevPosition = null;
    private Vec3 renderPosition = null;
    private long animationStart = 0L;

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        BlockState state;
        if (BlockHighlightModule.mc.player == null || BlockHighlightModule.mc.level == null) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("None")) {
            return;
        }
        HitResult hitResult = BlockHighlightModule.mc.hitResult;
        if (!(hitResult instanceof BlockHitResult)) {
            return;
        }
        BlockHitResult hitResult2 = (BlockHitResult)hitResult;
        BlockPos position = hitResult2.getBlockPos();
        if (this.animationMode.getValue().equals("Slide") && position != null) {
            if (this.renderPosition == null) {
                this.renderPosition = MathUtils.getVec(position);
            }
            if (!WorldUtils.equals(position, this.prevPosition)) {
                this.animationStart = System.currentTimeMillis();
                this.prevPosition = position;
            }
        }
        Vec3 offset = MathUtils.getVec(position);
        if (this.animationMode.getValue().equalsIgnoreCase("Slide") && this.renderPosition != null) {
            float easing = Easing.ease(Easing.toDelta(this.animationStart, (int)(Math.pow(this.slideSmoothness.getValue().doubleValue(), 1.4) * 1000.0)), Easing.Method.EASE_OUT_QUART);
            offset = this.renderPosition = this.renderPosition.add(MathUtils.scale(MathUtils.getVec(position).subtract(this.renderPosition), easing));
        }
        if ((state = BlockHighlightModule.mc.level.getBlockState(position)).isAir() || !BlockHighlightModule.mc.level.getWorldBorder().isWithinBounds(position)) {
            return;
        }
        VoxelShape shape = state.getShape((BlockGetter)BlockHighlightModule.mc.level, position);
        if (shape.isEmpty()) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("Complex")) {
            for (AABB part : shape.toAabbs()) {
                Renderer3D.renderBox(event.getMatrices(), part.move(offset), this.fillColor.getColor());
                Renderer3D.renderBoxOutline(event.getMatrices(), part.move(offset), this.outlineColor.getColor());
            }
        } else {
            if (this.mode.getValue().equalsIgnoreCase("Fill") || this.mode.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBox(event.getMatrices(), shape.bounds().move(offset), this.fillColor.getColor());
            }
            if (this.mode.getValue().equalsIgnoreCase("Outline") || this.mode.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBoxOutline(event.getMatrices(), shape.bounds().move(offset), this.outlineColor.getColor());
            }
        }
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue();
    }
}

