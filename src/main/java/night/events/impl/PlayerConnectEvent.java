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

public class PlayerConnectEvent
extends Event {
    private final UUID id;

    @Generated
    public PlayerConnectEvent(UUID id) {
        this.id = id;
    }

    @Generated
    public UUID getId() {
        return this.id;
    }
}

