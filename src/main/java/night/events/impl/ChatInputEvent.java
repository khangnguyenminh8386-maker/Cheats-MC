/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package night.events.impl;

import lombok.Generated;
import night.events.Event;

public class ChatInputEvent
extends Event {
    private String message;

    @Generated
    public String getMessage() {
        return this.message;
    }

    @Generated
    public void setMessage(String message) {
        this.message = message;
    }

    @Generated
    public ChatInputEvent(String message) {
        this.message = message;
    }
}

