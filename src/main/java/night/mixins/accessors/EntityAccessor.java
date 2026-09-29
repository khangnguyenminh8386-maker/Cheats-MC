/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package night.mixins.accessors;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={Entity.class})
public interface EntityAccessor {
    @Invoker(value="getInputVector")
    public static Vec3 invokeMovementInputToVelocity(Vec3 movementInput, float speed, float yaw) {
        throw new AssertionError();
    }

    @Invoker(value="setSharedFlag")
    public void invokeSetSharedFlag(int var1, boolean var2);

    @Accessor(value="yRot")
    public float getRawYRot();

    @Accessor(value="xRot")
    public float getRawXRot();

    @Accessor(value="yRot")
    public void setRawYRot(float var1);

    @Accessor(value="xRot")
    public void setRawXRot(float var1);
}

