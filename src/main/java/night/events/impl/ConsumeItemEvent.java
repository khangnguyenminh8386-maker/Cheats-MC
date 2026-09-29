/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.world.item.ItemStack
 */
package night.events.impl;

import lombok.Generated;
import net.minecraft.world.item.ItemStack;
import night.events.Event;

public class ConsumeItemEvent
extends Event {
    private final ItemStack stack;

    @Generated
    public ItemStack getStack() {
        return this.stack;
    }

    @Generated
    public ConsumeItemEvent(ItemStack stack) {
        this.stack = stack;
    }
}

