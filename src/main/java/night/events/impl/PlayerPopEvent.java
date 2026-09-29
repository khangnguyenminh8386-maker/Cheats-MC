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

public class PlayerPopEvent
extends Event {
    private final Player player;
    private final int pops;

    @Generated
    public Player getPlayer() {
        return this.player;
    }

    @Generated
    public int getPops() {
        return this.pops;
    }

    @Generated
    public PlayerPopEvent(Player player, int pops) {
        this.player = player;
        this.pops = pops;
    }
}

