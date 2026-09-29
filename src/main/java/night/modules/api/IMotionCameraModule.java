/*
 * Decompiled with CFR 0.152.
 */
package night.modules.api;

public interface IMotionCameraModule {
    public boolean isToggled();

    public boolean isSmoothPerspective();

    public boolean on();

    public boolean shouldBeDetached();

    public double getDistance();

    public double getFakeX();

    public double getFakeY();

    public double getFakeZ();
}

