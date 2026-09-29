/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.client.renderer.feature.ItemFeatureRenderer
 *  net.minecraft.client.renderer.feature.ItemFeatureRenderer$Submit
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 */
package night.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={ItemFeatureRenderer.class})
public class ItemFeatureRendererMixin {
    @ModifyArg(method={"prepareMainSubmit"}, at=@At(value="INVOKE", target="Lcom/mojang/blaze3d/vertex/QuadInstance;setColor(I)V"))
    private int night$handsOpacity(int color, @Local(argsOnly=true) ItemFeatureRenderer.Submit submit) {
        if (!submit.displayContext().firstPerson()) {
            return color;
        }
        return ItemFeatureRendererMixin.applyHandsOpacity(color);
    }

    private static int applyHandsOpacity(int color) {
        return color;
    }
}

