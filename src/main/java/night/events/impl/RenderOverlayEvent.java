/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  org.joml.Matrix3x2fStack
 */
package night.events.impl;

import lombok.Generated;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import night.events.Event;
import org.joml.Matrix3x2fStack;

public class RenderOverlayEvent
extends Event {
    private final GuiGraphicsExtractor context;
    private final float tickDelta;

    public Matrix3x2fStack getMatrices() {
        return this.context.pose();
    }

    @Generated
    public GuiGraphicsExtractor getContext() {
        return this.context;
    }

    @Generated
    public float getTickDelta() {
        return this.tickDelta;
    }

    @Generated
    public RenderOverlayEvent(GuiGraphicsExtractor context, float tickDelta) {
        this.context = context;
        this.tickDelta = tickDelta;
    }
}

