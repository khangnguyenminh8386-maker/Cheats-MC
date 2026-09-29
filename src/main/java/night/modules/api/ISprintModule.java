/*
 * Decompiled with CFR 0.152.
 */
package night.modules.api;

public interface ISprintModule {
    public boolean isToggled();

    public boolean shouldSprint();

    public boolean isGrimCompensating();

    public int getGrimStrafe();

    public float getGrimYaw();

    public boolean isInstantMode();
}

