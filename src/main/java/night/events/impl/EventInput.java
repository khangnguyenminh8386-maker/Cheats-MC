/*
 * Decompiled with CFR 0.152.
 */
package night.events.impl;

import night.events.Event;

public class EventInput
extends Event {
    public boolean forward;
    public boolean backward;
    public boolean left;
    public boolean right;
    public boolean jumping;
    public boolean sneaking;
    public boolean sprinting;

    public EventInput(boolean forward, boolean backward, boolean left, boolean right, boolean jumping, boolean sneaking, boolean sprinting) {
        this.forward = forward;
        this.backward = backward;
        this.left = left;
        this.right = right;
        this.jumping = jumping;
        this.sneaking = sneaking;
        this.sprinting = sprinting;
    }
}

