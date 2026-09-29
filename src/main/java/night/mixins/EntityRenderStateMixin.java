/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.state.EntityRenderState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 */
package night.mixins;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import night.utils.graphics.Glint;
import night.utils.mixins.IChamsCapture;
import night.utils.mixins.ISelfState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={EntityRenderState.class})
public class EntityRenderStateMixin
implements IChamsCapture,
ISelfState {
    @Unique
    private boolean chamsFill = false;
    @Unique
    private int chamsFillColor = 0;
    @Unique
    private boolean chamsOutline = false;
    @Unique
    private int chamsOutlineColor = 0;
    @Unique
    private boolean chamsShine = false;
    @Unique
    private boolean chamsGlint = false;
    @Unique
    private int chamsGlintColor = -1;
    @Unique
    private Glint.GlintParams chamsGlintParams = Glint.ENTITY;
    @Unique
    private boolean chamsSuppressReal = false;
    @Unique
    private float chamsRealAlpha = 1.0f;
    @Unique
    private float chamsYOffset = 0.0f;
    @Unique
    private boolean isSelf = false;

    @Override
    public float night$chamsYOffset() {
        return this.chamsYOffset;
    }

    @Override
    public void night$setChamsYOffset(float offset) {
        this.chamsYOffset = offset;
    }

    @Override
    public boolean night$isSelf() {
        return this.isSelf;
    }

    @Override
    public void night$setSelf(boolean self) {
        this.isSelf = self;
    }

    @Override
    public boolean night$chamsFill() {
        return this.chamsFill;
    }

    @Override
    public int night$chamsFillColor() {
        return this.chamsFillColor;
    }

    @Override
    public boolean night$chamsOutline() {
        return this.chamsOutline;
    }

    @Override
    public int night$chamsOutlineColor() {
        return this.chamsOutlineColor;
    }

    @Override
    public boolean night$chamsShine() {
        return this.chamsShine;
    }

    @Override
    public boolean night$chamsGlint() {
        return this.chamsGlint;
    }

    @Override
    public int night$chamsGlintColor() {
        return this.chamsGlintColor;
    }

    @Override
    public Glint.GlintParams night$chamsGlintParams() {
        return this.chamsGlintParams;
    }

    @Override
    public boolean night$chamsSuppressReal() {
        return this.chamsSuppressReal;
    }

    @Override
    public float night$chamsRealAlpha() {
        return this.chamsRealAlpha;
    }

    @Override
    public void night$setChamsRealAlpha(float alpha) {
        this.chamsRealAlpha = alpha;
    }

    @Override
    public void night$setChamsGlint(boolean glint, int glintColor) {
        this.chamsGlint = glint;
        this.chamsGlintColor = glintColor;
        this.chamsGlintParams = Glint.ENTITY;
    }

    @Override
    public void night$setChamsGlint(boolean glint, int glintColor, Glint.GlintParams params) {
        this.chamsGlint = glint;
        this.chamsGlintColor = glintColor;
        this.chamsGlintParams = params != null ? params : Glint.ENTITY;
    }

    @Override
    public void night$setChams(boolean fill, int fillColor, boolean outline, int outlineColor, boolean shine) {
        this.night$setChams(fill, fillColor, outline, outlineColor, shine, false);
    }

    @Override
    public void night$setChams(boolean fill, int fillColor, boolean outline, int outlineColor, boolean shine, boolean suppressReal) {
        this.chamsFill = fill;
        this.chamsFillColor = fillColor;
        this.chamsOutline = outline;
        this.chamsOutlineColor = outlineColor;
        this.chamsShine = shine;
        this.chamsSuppressReal = suppressReal;
        this.chamsRealAlpha = 1.0f;
        this.chamsGlint = false;
        this.chamsGlintColor = -1;
        this.chamsGlintParams = Glint.ENTITY;
    }
}

