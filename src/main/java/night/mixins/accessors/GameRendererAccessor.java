/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.resource.CrossFrameResourcePool
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.client.renderer.fog.FogRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package night.mixins.accessors;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={GameRenderer.class})
public interface GameRendererAccessor {
    @Accessor(value="resourcePool")
    public CrossFrameResourcePool getPool();

    @Accessor(value="fogRenderer")
    public FogRenderer getFogRenderer();
}

