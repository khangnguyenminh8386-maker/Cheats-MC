/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package night.mixins.accessors;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={Entity.class})
public interface EntityFlagAccessor {
    @Accessor(value="FLAG_FALL_FLYING")
    public static int getFlagFallFlying() {
        throw new AssertionError();
    }

    @Invoker(value="getSharedFlag")
    public boolean invokeGetSharedFlag(int var1);

    @Invoker(value="setSharedFlag")
    public void invokeSetSharedFlag(int var1, boolean var2);
}

