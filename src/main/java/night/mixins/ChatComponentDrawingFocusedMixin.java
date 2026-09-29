/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.util.FormattedCharSequence
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package night.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.util.FormattedCharSequence;
import night.Night;
import night.modules.impl.miscellaneous.BetterChatModule;
import night.utils.animations.Easing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets={"net.minecraft.client.gui.components.ChatComponent$DrawingFocusedGraphicsAccess"})
public class ChatComponentDrawingFocusedMixin {
    @Unique
    private int night$xOffset = 0;

    @Inject(method={"handleMessage"}, at={@At(value="HEAD")})
    private void night$computeOffset(int textTop, float opacity, FormattedCharSequence message, CallbackInfoReturnable<Boolean> cir) {
        float progress;
        this.night$xOffset = 0;
        BetterChatModule module = Night.MODULE_MANAGER.getModule(BetterChatModule.class);
        if (!module.isToggled() || !module.animation.getValue()) {
            return;
        }
        Long start = module.getAnimationStart(message);
        if (start == null) {
            return;
        }
        int delay = module.delay.getValue().intValue();
        float f = progress = delay <= 0 ? 1.0f : Easing.toDelta(start, delay);
        if (progress >= 1.0f) {
            return;
        }
        float width = Minecraft.getInstance().font.width(message);
        this.night$xOffset = Math.round(-width * (1.0f - progress));
    }

    @ModifyArg(method={"handleMessage"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/ActiveTextCollector;accept(Lnet/minecraft/client/gui/TextAlignment;IILnet/minecraft/client/gui/ActiveTextCollector$Parameters;Lnet/minecraft/util/FormattedCharSequence;)V"), index=1)
    private int night$slideInX(int x) {
        return x + this.night$xOffset;
    }
}

