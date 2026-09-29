/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package night.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import night.Night;
import night.modules.impl.visuals.NoRenderModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets={"net.minecraft.client.gui.Hud$HeartType"})
public class HudHeartTypeMixin {
    @ModifyExpressionValue(method={"forPlayer"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;hasEffect(Lnet/minecraft/core/Holder;)Z", ordinal=1)})
    private static boolean night$hideWitherHeart(boolean original) {
        NoRenderModule noRender;
        if (original && Night.MODULE_MANAGER != null && (noRender = Night.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && noRender.wither.getValue()) {
            return false;
        }
        return original;
    }
}

