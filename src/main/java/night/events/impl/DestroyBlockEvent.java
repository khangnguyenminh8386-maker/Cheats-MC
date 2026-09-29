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

public class DestroyBlockEvent
extends Event {
    private final BlockPos position;

    @Generated
    public DestroyBlockEvent(BlockPos position) {
        this.position = position;
    }

    @Generated
    public BlockPos getPosition() {
        return this.position;
    }
}

