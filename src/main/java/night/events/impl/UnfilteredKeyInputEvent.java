/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package night.events.impl;

import lombok.Generated;
import night.events.Event;

public class UnfilteredKeyInputEvent
extends Event {
    private final int key;
    private final int scancode;
    private final int action;
    private final int modifiers;

    @Generated
    public int getKey() {
        return this.key;
    }

    @Generated
    public int getScancode() {
        return this.scancode;
    }

    @Generated
    public int getAction() {
        return this.action;
    }

    @Generated
    public int getModifiers() {
        return this.modifiers;
    }

    @Generated
    public UnfilteredKeyInputEvent(int key, int scancode, int action, int modifiers) {
        this.key = key;
        this.scancode = scancode;
        this.action = action;
        this.modifiers = modifiers;
    }
}

