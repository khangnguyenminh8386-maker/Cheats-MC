/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.world.phys.Vec3
 */
package night.events.impl;

import lombok.Generated;
import net.minecraft.world.phys.Vec3;
import night.events.Event;

public class UpdateVelocityEvent
extends Event {
    private final Vec3 movementInput;
    private final float speed;
    private Vec3 velocity;

    @Generated
    public Vec3 getMovementInput() {
        return this.movementInput;
    }

    @Generated
    public float getSpeed() {
        return this.speed;
    }

    @Generated
    public Vec3 getVelocity() {
        return this.velocity;
    }

    @Generated
    public void setVelocity(Vec3 velocity) {
        this.velocity = velocity;
    }

    @Generated
    public UpdateVelocityEvent(Vec3 movementInput, float speed) {
        this.movementInput = movementInput;
        this.speed = speed;
    }
}

