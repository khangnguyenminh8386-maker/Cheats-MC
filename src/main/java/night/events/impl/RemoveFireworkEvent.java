/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.world.entity.projectile.FireworkRocketEntity
 */
package night.events.impl;

import lombok.Generated;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import night.events.Event;

public class RemoveFireworkEvent
extends Event {
    private final FireworkRocketEntity entity;

    @Generated
    public FireworkRocketEntity getEntity() {
        return this.entity;
    }

    @Generated
    public RemoveFireworkEvent(FireworkRocketEntity entity) {
        this.entity = entity;
    }
}

