/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.animal.pig.Pig
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package night.mixins;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.pig.Pig;
import night.Night;
import night.modules.impl.movement.EntityControlModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Pig.class})
public abstract class PigMixin {
    @Inject(method={"getControllingPassenger"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$onGetControllingPassenger(CallbackInfoReturnable<LivingEntity> cir) {
        Entity first;
        EntityControlModule entityControl;
        if (Night.MODULE_MANAGER != null && (entityControl = Night.MODULE_MANAGER.getModule(EntityControlModule.class)) != null && entityControl.isToggled() && entityControl.control.getValue() && (first = ((Pig)(Object)this).getFirstPassenger()) instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)first;
            cir.setReturnValue(living);
        }
    }
}

