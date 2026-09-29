/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package night.events.impl;

import java.util.UUID;
import lombok.Generated;
import night.events.Event;

public class PlayerDisconnectEvent
extends Event {
    private final UUID id;

    @Generated
    public PlayerDisconnectEvent(UUID id) {
        this.id = id;
    }

    @Generated
    public UUID getId() {
        return this.id;
    }
}

