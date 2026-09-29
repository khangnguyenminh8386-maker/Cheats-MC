/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.textures.GpuSampler
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package night.mixins.accessors;

import com.mojang.blaze3d.textures.GpuSampler;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={AbstractTexture.class})
public interface AbstractTextureAccessor {
    @Accessor(value="sampler")
    public void setSampler(GpuSampler var1);
}

