/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.navigation.ScreenRectangle
 *  net.minecraft.client.renderer.state.gui.GuiTextRenderState
 *  net.minecraft.util.FormattedCharSequence
 *  org.joml.Matrix3x2fc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package night.mixins.accessors;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={GuiTextRenderState.class})
public interface GuiTextRenderStateAccessor {
    @Accessor(value="text")
    public FormattedCharSequence getText();

    @Accessor(value="x")
    public int getX();

    @Accessor(value="y")
    public int getY();

    @Accessor(value="color")
    public int getColor();

    @Accessor(value="dropShadow")
    public boolean isDropShadow();

    @Accessor(value="pose")
    public Matrix3x2fc getPose();

    @Accessor(value="scissor")
    public ScreenRectangle getScissor();
}

