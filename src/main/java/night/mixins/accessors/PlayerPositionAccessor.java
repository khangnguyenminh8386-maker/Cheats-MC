/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.PositionMoveRotation
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package night.mixins.accessors;

import net.minecraft.world.entity.PositionMoveRotation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={PositionMoveRotation.class})
public interface PlayerPositionAccessor {
    @Accessor(value="yRot")
    @Mutable
    public void setYaw(float var1);

    @Accessor(value="xRot")
    @Mutable
    public void setPitch(float var1);
}

