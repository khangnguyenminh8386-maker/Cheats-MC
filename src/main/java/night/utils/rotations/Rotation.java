/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.world.entity.Entity
 */
package night.utils.rotations;

import lombok.Generated;
import net.minecraft.world.entity.Entity;

public class Rotation {
    private float yaw;
    private float pitch;

    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Rotation(Entity entity) {
        this(entity.getYRot(), entity.getXRot());
    }

    public void apply(Entity entity) {
        entity.setYRot(this.yaw);
        entity.setXRot(this.pitch);
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
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    @Generated
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}

