/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.BossHealthOverlay
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import night.Night;
import night.modules.impl.visuals.NoRenderModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BossHealthOverlay.class})
public class BossBarHudMixin {
    @Inject(method={"extractRenderState"}, at={@At(value="HEAD")}, cancellable=true)
    private void render(GuiGraphicsExtractor context, CallbackInfo info) {
        if (Night.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() && Night.MODULE_MANAGER.getModule(NoRenderModule.class).bossBar.getValue()) {
            info.cancel();
        }
    }
}

