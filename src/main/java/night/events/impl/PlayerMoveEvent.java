/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.world.entity.MoverType
 *  net.minecraft.world.phys.Vec3
 */
package night.events.impl;

import lombok.Generated;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import night.events.Event;

public class PlayerMoveEvent
extends Event {
    private final MoverType movementType;
    private Vec3 movement;

    public double getX() {
        return this.movement.x();
    }

    public void setX(double x) {
        this.movement = new Vec3(x, this.movement.y(), this.movement.z());
    }

    public double getY() {
        return this.movement.y();
    }

    public void setY(double y) {
        this.movement = new Vec3(this.movement.x(), y, this.movement.z());
    }

    public double getZ() {
        return this.movement.z();
    }

    public void setZ(double z) {
        this.movement = new Vec3(this.movement.x(), this.movement.y(), z);
    }

    @Generated
    public PlayerMoveEvent(MoverType movementType, Vec3 movement) {
        this.movementType = movementType;
        this.movement = movement;
    }

    @Generated
    public MoverType getMovementType() {
        return this.movementType;
    }

    @Generated
    public Vec3 getMovement() {
        return this.movement;
    }

    @Generated
    public void setMovement(Vec3 movement) {
        this.movement = movement;
    }
}

