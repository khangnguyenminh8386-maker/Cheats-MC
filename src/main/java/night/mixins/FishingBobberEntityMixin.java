/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.projectile.FishingHook
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package night.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import night.Night;
import night.modules.impl.movement.VelocityModule;
import night.utils.IMinecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={FishingHook.class})
public class FishingBobberEntityMixin
implements IMinecraft {
    @WrapOperation(method={"handleEntityEvent"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/projectile/FishingHook;pullEntity(Lnet/minecraft/world/entity/Entity;)V")})
    private void pushOutOfBlocks(FishingHook instance, Entity entity, Operation<Void> original) {
        if (entity == FishingBobberEntityMixin.mc.player && Night.MODULE_MANAGER.getModule(VelocityModule.class).isToggled() && Night.MODULE_MANAGER.getModule(VelocityModule.class).antiFishingRod.getValue()) {
            return;
        }
        original.call(new Object[]{instance, entity});
    }
}

