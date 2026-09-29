/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 */
package night.events.impl;

import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import night.events.Event;

public class AttackBlockEvent
extends Event {
    private final BlockPos position;
    private final Direction direction;

    @Generated
    public AttackBlockEvent(BlockPos position, Direction direction) {
        this.position = position;
        this.direction = direction;
    }

    @Generated
    public BlockPos getPosition() {
        return this.position;
    }

    @Generated
    public Direction getDirection() {
        return this.direction;
    }
}

