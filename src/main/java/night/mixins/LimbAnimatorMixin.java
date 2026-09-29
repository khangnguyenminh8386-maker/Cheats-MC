/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.world.entity.WalkAnimationState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package night.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.WalkAnimationState;
import night.Night;
import night.modules.impl.visuals.NoRenderModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={WalkAnimationState.class})
public class LimbAnimatorMixin {
    @ModifyReturnValue(method={"position()F"}, at={@At(value="RETURN")})
    private float getPos(float original) {
        if (Night.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() && Night.MODULE_MANAGER.getModule(NoRenderModule.class).limbSwing.getValue()) {
            return 0.0f;
        }
        return original;
    }

    @ModifyReturnValue(method={"position(F)F"}, at={@At(value="RETURN")})
    private float getPos2(float original) {
        if (Night.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() && Night.MODULE_MANAGER.getModule(NoRenderModule.class).limbSwing.getValue()) {
            return 0.0f;
        }
        return original;
    }

    @ModifyReturnValue(method={"speed()F"}, at={@At(value="RETURN")})
    private float getSpeed(float original) {
        if (Night.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() && Night.MODULE_MANAGER.getModule(NoRenderModule.class).limbSwing.getValue()) {
            return 0.0f;
        }
        return original;
    }

    @ModifyReturnValue(method={"speed(F)F"}, at={@At(value="RETURN")})
    private float getSpeed2(float original) {
        if (Night.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() && Night.MODULE_MANAGER.getModule(NoRenderModule.class).limbSwing.getValue()) {
            return 0.0f;
        }
        return original;
    }
}

