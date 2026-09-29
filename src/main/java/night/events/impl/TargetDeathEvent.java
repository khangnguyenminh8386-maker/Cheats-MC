/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.world.entity.player.Player
 */
package night.events.impl;

import lombok.Generated;
import net.minecraft.world.entity.player.Player;
import night.events.Event;

public class TargetDeathEvent
extends Event {
    private final Player player;

    @Generated
    public TargetDeathEvent(Player player) {
        this.player = player;
    }

    @Generated
    public Player getPlayer() {
        return this.player;
    }
}

