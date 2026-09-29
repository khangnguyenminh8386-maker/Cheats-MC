/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package night.mixins;

import net.minecraft.world.entity.decoration.ArmorStand;
import night.Night;
import night.modules.impl.visuals.NoRenderModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ArmorStand.class})
public abstract class ArmorStandMixin {
    @Inject(method={"isPickable"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$hideFromRaytrace(CallbackInfoReturnable<Boolean> cir) {
        NoRenderModule noRender;
        NoRenderModule noRenderModule = noRender = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(NoRenderModule.class) : null;
        if (noRender != null && noRender.isToggled() && noRender.armorStand.getValue()) {
            cir.setReturnValue(false);
        }
    }
}

