/*
 * Decompiled with CFR 0.152.
 */
package night.utils.mixins;

import night.utils.graphics.Glint;

public interface IChamsCapture {
    public boolean night$chamsFill();

    public int night$chamsFillColor();

    public boolean night$chamsOutline();

    public int night$chamsOutlineColor();

    public boolean night$chamsShine();

    public boolean night$chamsGlint();

    public int night$chamsGlintColor();

    public Glint.GlintParams night$chamsGlintParams();

    public boolean night$chamsSuppressReal();

    public float night$chamsRealAlpha();

    public void night$setChamsRealAlpha(float var1);

    public float night$chamsYOffset();

    public void night$setChamsYOffset(float var1);

    public void night$setChams(boolean var1, int var2, boolean var3, int var4, boolean var5);

    public void night$setChams(boolean var1, int var2, boolean var3, int var4, boolean var5, boolean var6);

    public void night$setChamsGlint(boolean var1, int var2);

    public void night$setChamsGlint(boolean var1, int var2, Glint.GlintParams var3);
}

