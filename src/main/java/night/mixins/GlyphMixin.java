/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.font.GlyphInfo
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package night.mixins;

import com.mojang.blaze3d.font.GlyphInfo;
import night.Night;
import night.managers.FontManager;
import night.modules.impl.core.FontModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={GlyphInfo.class})
public interface GlyphMixin {
    @Inject(method={"getShadowOffset"}, at={@At(value="HEAD")}, cancellable=true)
    private void getShadowOffset(CallbackInfoReturnable<Float> info) {
        FontModule module;
        if (!FontManager.isClientTextRendering()) {
            return;
        }
        if (Night.MODULE_MANAGER != null && (module = Night.MODULE_MANAGER.getModule(FontModule.class)) != null && module.isToggled() && !module.shadowMode.getValue().equalsIgnoreCase("Default")) {
            info.setReturnValue(Float.valueOf(Night.FONT_MANAGER.getShadowOffset()));
        }
    }
}

