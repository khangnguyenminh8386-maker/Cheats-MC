/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.ItemEntityRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import night.Night;
import night.modules.impl.visuals.NoRenderModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ItemEntityRenderer.class})
public class ItemEntityRendererMixin {
    @Inject(method={"submit"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$limitItems(CallbackInfo info) {
        NoRenderModule noRender = Night.MODULE_MANAGER.getModule(NoRenderModule.class);
        if (noRender == null || !noRender.isToggled()) {
            return;
        }
        if (!noRender.shouldRenderItem()) {
            info.cancel();
        }
    }
}

