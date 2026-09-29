/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package night.events.impl;

import lombok.Generated;
import night.events.Event;

public class UnfilteredMouseInputEvent
extends Event {
    private final int button;
    private final int action;
    private final int mods;

    @Generated
    public int getButton() {
        return this.button;
    }

    @Generated
    public int getAction() {
        return this.action;
    }

    @Generated
    public int getMods() {
        return this.mods;
    }

    @Generated
    public UnfilteredMouseInputEvent(int button, int action, int mods) {
        this.button = button;
        this.action = action;
        this.mods = mods;
    }
}

