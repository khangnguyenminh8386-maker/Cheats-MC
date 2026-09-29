/*
 * Decompiled with CFR 0.152.
 */
package night.modules.api;

public interface IFreecamModule {
    public boolean isToggled();

    public boolean isRotate();

    public void onMouseTurn(double var1, double var3);

    public float getFreeYaw();

    public float getFreePitch();

    public double getFreeX();

    public double getFreeY();

    public double getFreeZ();
}

