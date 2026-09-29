/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.core.BlockPos
 */
package night.events.impl;

import lombok.Generated;
import net.minecraft.core.BlockPos;
import night.events.Event;

public class PlayerMineEvent
extends Event {
    private final int actorID;
    private final BlockPos position;

    @Generated
    public int getActorID() {
        return this.actorID;
    }

    @Generated
    public BlockPos getPosition() {
        return this.position;
    }

    @Generated
    public PlayerMineEvent(int actorID, BlockPos position) {
        this.actorID = actorID;
        this.position = position;
    }
}

