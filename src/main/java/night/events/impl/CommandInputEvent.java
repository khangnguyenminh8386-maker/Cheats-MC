/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package night.events.impl;

import lombok.Generated;
import night.events.Event;

public class CommandInputEvent
extends Event {
    private final String message;

    @Generated
    public String getMessage() {
        return this.message;
    }

    @Generated
    public CommandInputEvent(String message) {
        this.message = message;
    }
}

