/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  it.unimi.dsi.fastutil.ints.IntList
 *  net.minecraft.client.renderer.item.ItemStackRenderState$FoilType
 *  net.minecraft.client.renderer.item.ItemStackRenderState$LayerRenderState
 *  net.minecraft.client.resources.model.geometry.BakedQuad
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package night.mixins.accessors;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={ItemStackRenderState.LayerRenderState.class})
public interface LayerRenderStateAccessor {
    @Accessor(value="quads")
    public List<BakedQuad> night$getQuads();

    @Accessor(value="foilType")
    public ItemStackRenderState.FoilType night$getFoilType();

    @Accessor(value="tintLayers")
    public IntList night$getTintLayers();

    @Invoker(value="applyTransform")
    public void night$applyTransform(PoseStack.Pose var1);
}

