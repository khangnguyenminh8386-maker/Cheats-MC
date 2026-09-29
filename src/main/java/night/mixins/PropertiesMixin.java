/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.ClientClockManager
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package night.mixins;

import net.minecraft.client.ClientClockManager;
import night.Night;
import night.modules.impl.visuals.AtmosphereModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ClientClockManager.class})
public class PropertiesMixin {
    @Inject(method={"getTotalTicks"}, at={@At(value="HEAD")}, cancellable=true)
    private void getTotalTicks(CallbackInfoReturnable<Long> info) {
        AtmosphereModule atmosphere;
        AtmosphereModule atmosphereModule = atmosphere = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AtmosphereModule.class) : null;
        if (atmosphere != null && atmosphere.isToggled() && atmosphere.modifyTime.getValue()) {
            info.setReturnValue((atmosphere.time.getValue().longValue() * 100L));
        }
    }
}

