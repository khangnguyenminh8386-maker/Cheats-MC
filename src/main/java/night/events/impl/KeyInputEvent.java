/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package night.events.impl;

import lombok.Generated;
import night.events.Event;

public class KeyInputEvent
extends Event {
    private final int key;
    private final int modifiers;

    @Override
    @Generated
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof KeyInputEvent)) {
            return false;
        }
        KeyInputEvent other = (KeyInputEvent)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        if (this.getKey() != other.getKey()) {
            return false;
        }
        return this.getModifiers() == other.getModifiers();
    }

    @Override
    @Generated
    protected boolean canEqual(Object other) {
        return other instanceof KeyInputEvent;
    }

    @Override
    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = super.hashCode();
        result = result * 59 + this.getKey();
        result = result * 59 + this.getModifiers();
        return result;
    }

    @Generated
    public KeyInputEvent(int key, int modifiers) {
        this.key = key;
        this.modifiers = modifiers;
    }

    @Generated
    public int getKey() {
        return this.key;
    }

    @Generated
    public int getModifiers() {
        return this.modifiers;
    }

    @Override
    @Generated
    public String toString() {
        return "KeyInputEvent(key=" + this.getKey() + ", modifiers=" + this.getModifiers() + ")";
    }
}

