/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package night.mixins;

import net.minecraft.world.level.Level;
import night.Night;
import night.modules.impl.visuals.AtmosphereModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Level.class})
public abstract class WeatherMixin {
    @Inject(method={"isRaining"}, at={@At(value="HEAD")}, cancellable=true)
   private void night$isRaining(CallbackInfoReturnable<Boolean> info) {
      AtmosphereModule module = Night.MODULE_MANAGER.getModule(AtmosphereModule.class);
      if (module.isToggled() && !module.weather.getValue().equalsIgnoreCase("Unchanged")) {
         String weather = module.weather.getValue();
         info.setReturnValue(weather.equalsIgnoreCase("Rain") || weather.equalsIgnoreCase("Thunder"));
      }
   }

    @Inject(method={"isThundering"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$isThundering(CallbackInfoReturnable<Boolean> info) {
        AtmosphereModule module = Night.MODULE_MANAGER.getModule(AtmosphereModule.class);
        if (!module.isToggled() || module.weather.getValue().equalsIgnoreCase("Unchanged")) {
            return;
        }
        info.setReturnValue(module.weather.getValue().equalsIgnoreCase("Thunder"));
    }

    @Inject(method={"getRainLevel"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$getRainLevel(float delta, CallbackInfoReturnable<Float> info) {
        AtmosphereModule module = Night.MODULE_MANAGER.getModule(AtmosphereModule.class);
        if (!module.isToggled() || module.weather.getValue().equalsIgnoreCase("Unchanged")) {
            return;
        }
        String weather = module.weather.getValue();
        info.setReturnValue(Float.valueOf(weather.equalsIgnoreCase("Rain") || weather.equalsIgnoreCase("Thunder") ? 1.0f : 0.0f));
    }

    @Inject(method={"getThunderLevel"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$getThunderLevel(float delta, CallbackInfoReturnable<Float> info) {
        AtmosphereModule module = Night.MODULE_MANAGER.getModule(AtmosphereModule.class);
        if (!module.isToggled() || module.weather.getValue().equalsIgnoreCase("Unchanged")) {
            return;
        }
        info.setReturnValue(Float.valueOf(module.weather.getValue().equalsIgnoreCase("Thunder") ? 1.0f : 0.0f));
    }
}

