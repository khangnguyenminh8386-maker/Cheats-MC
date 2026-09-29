/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.util.FormattedCharSequence
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package night.mixins;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import night.Night;
import night.modules.impl.core.FontModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Font.class})
public class FontMixin {
    @Inject(method={"width(Ljava/lang/String;)I"}, at={@At(value="HEAD")}, cancellable=true)
    private void width$String(String text, CallbackInfoReturnable<Integer> info) {
        if (Night.MODULE_MANAGER == null) {
            return;
        }
        FontModule module = Night.MODULE_MANAGER.getModule(FontModule.class);
        if (module != null && module.isToggled() && module.customFont.getValue() && module.global.getValue() && Night.FONT_MANAGER.getFontRenderer() != null) {
            info.setReturnValue(((int)Night.FONT_MANAGER.getFontRenderer().getTextWidth(text)));
        }
    }

    @Inject(method={"width(Lnet/minecraft/network/chat/FormattedText;)I"}, at={@At(value="HEAD")}, cancellable=true)
    private void width$FormattedText(FormattedText text, CallbackInfoReturnable<Integer> info) {
        if (Night.MODULE_MANAGER == null) {
            return;
        }
        FontModule module = Night.MODULE_MANAGER.getModule(FontModule.class);
        if (module != null && module.isToggled() && module.customFont.getValue() && module.global.getValue() && Night.FONT_MANAGER.getFontRenderer() != null) {
            info.setReturnValue(((int)Night.FONT_MANAGER.getFontRenderer().getTextWidth(text.getString())));
        }
    }

    @Inject(method={"width(Lnet/minecraft/util/FormattedCharSequence;)I"}, at={@At(value="HEAD")}, cancellable=true)
    private void width$FormattedCharSequence(FormattedCharSequence text, CallbackInfoReturnable<Integer> info) {
        if (Night.MODULE_MANAGER == null) {
            return;
        }
        FontModule module = Night.MODULE_MANAGER.getModule(FontModule.class);
        if (module != null && module.isToggled() && module.customFont.getValue() && module.global.getValue() && Night.FONT_MANAGER.getFontRenderer() != null) {
            info.setReturnValue(((int)Night.FONT_MANAGER.getFontRenderer().getTextWidth(text)));
        }
    }
}

