/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.item.ItemStackRenderState
 *  net.minecraft.client.renderer.item.ItemStackRenderState$LayerRenderState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package night.mixins.accessors;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ItemStackRenderState.class})
public interface ItemStackRenderStateAccessor {
    @Accessor(value="layers")
    public ItemStackRenderState.LayerRenderState[] night$getLayers();

    @Accessor(value="activeLayerCount")
    public int night$getActiveLayerCount();
}

