/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.client.DeltaTracker$Timer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package night.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.DeltaTracker;
import night.Night;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={DeltaTracker.Timer.class})
public class RenderTickCounterDynamicMixin {
    @ModifyExpressionValue(method={"advanceGameTime"}, at={@At(value="INVOKE", target="Lit/unimi/dsi/fastutil/floats/FloatUnaryOperator;apply(F)F")})
    private float night$scaleMspt(float mspt) {
        return mspt / Night.WORLD_MANAGER.getTimerMultiplier();
    }
}

