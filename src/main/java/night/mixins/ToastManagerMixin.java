/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.components.toasts.AdvancementToast
 *  net.minecraft.client.gui.components.toasts.Toast
 *  net.minecraft.client.gui.components.toasts.ToastManager
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import night.Night;
import night.modules.impl.visuals.NoRenderModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ToastManager.class})
public class ToastManagerMixin {
    @Inject(method={"addToast"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$noAdvancementToast(Toast toast, CallbackInfo info) {
        NoRenderModule noRender;
        if (toast instanceof AdvancementToast && Night.MODULE_MANAGER != null && (noRender = Night.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && noRender.advancements.getValue()) {
            info.cancel();
        }
    }
}

