/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package night.mixins.accessors;

import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={Vec3.class})
public interface Vec3dAccessor {
    @Accessor(value="x")
    @Mutable
    public void setX(double var1);

    @Accessor(value="y")
    @Mutable
    public void setY(double var1);

    @Accessor(value="z")
    @Mutable
    public void setZ(double var1);
}

