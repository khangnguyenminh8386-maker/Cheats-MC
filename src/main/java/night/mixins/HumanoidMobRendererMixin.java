/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.entity.HumanoidMobRenderer
 *  net.minecraft.client.renderer.entity.state.HumanoidRenderState
 *  net.minecraft.client.renderer.item.ItemModelResolver
 *  net.minecraft.world.entity.LivingEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;
import night.Night;
import night.modules.impl.movement.ElytraFlyModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={HumanoidMobRenderer.class})
public abstract class HumanoidMobRendererMixin {
    @Inject(method={"extractHumanoidRenderState"}, at={@At(value="TAIL")})
    private static void night$hideControlRocketGlide(LivingEntity entity, HumanoidRenderState state, float partialTicks, ItemModelResolver itemModelResolver, CallbackInfo ci) {
        if (entity != Minecraft.getInstance().player) {
            return;
        }
        ElytraFlyModule elytraFly = Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
        if (elytraFly.isToggled() && elytraFly.mode.getValue().equalsIgnoreCase("ControlRocket")) {
            state.isFallFlying = false;
            state.speedValue = 1.0f;
        }
    }
}

