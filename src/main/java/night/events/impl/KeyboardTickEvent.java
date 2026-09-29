/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package night.events.impl;

import lombok.Generated;
import night.events.Event;

public class KeyboardTickEvent
extends Event {
    private float movementForward;
    private float movementSideways;

    @Generated
    public KeyboardTickEvent(float movementForward, float movementSideways) {
        this.movementForward = movementForward;
        this.movementSideways = movementSideways;
    }

    @Generated
    public float getMovementForward() {
        return this.movementForward;
    }

    @Generated
    public float getMovementSideways() {
        return this.movementSideways;
    }

    @Generated
    public void setMovementForward(float movementForward) {
        this.movementForward = movementForward;
    }

    @Generated
    public void setMovementSideways(float movementSideways) {
        this.movementSideways = movementSideways;
    }
}

