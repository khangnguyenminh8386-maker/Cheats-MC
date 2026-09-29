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

public class BreakBlockEvent
extends Event {
    private final BlockPos pos;

    @Generated
    public BlockPos getPos() {
        return this.pos;
    }

    @Generated
    public BreakBlockEvent(BlockPos pos) {
        this.pos = pos;
    }
}

