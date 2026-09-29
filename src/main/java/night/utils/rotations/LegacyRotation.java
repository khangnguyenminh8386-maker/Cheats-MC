/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package night.utils.rotations;

import lombok.Generated;
import night.modules.Module;

public class LegacyRotation {
    private float yaw;
    private float pitch;
    private final Module module;
    private final int priority;
    private long time;

    public LegacyRotation(float yaw, float pitch, int priority) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.module = null;
        this.priority = priority;
        this.time = System.currentTimeMillis();
    }

    public LegacyRotation(float yaw, float pitch, Module module, int priority) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.module = module;
        this.priority = priority;
        this.time = System.currentTimeMillis();
    }

    @Generated
    public float getYaw() {
        return this.yaw;
    }

    @Generated
    public float getPitch() {
        return this.pitch;
    }

    @Generated
    public Module getModule() {
        return this.module;
    }

    @Generated
    public int getPriority() {
        return this.priority;
    }

    @Generated
    public long getTime() {
        return this.time;
    }

    @Generated
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    @Generated
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Generated
    public void setTime(long time) {
        this.time = time;
    }
}

