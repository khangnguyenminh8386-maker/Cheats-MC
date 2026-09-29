/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.client.particle.ParticleEngine
 *  net.minecraft.client.particle.TotemParticle
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.world.entity.Entity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package night.mixins;

import java.awt.Color;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TotemParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import night.Night;
import night.modules.impl.visuals.NoRenderModule;
import night.modules.impl.visuals.ParticlesModule;
import night.utils.IMinecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ParticleEngine.class})
public class ParticleManagerMixin
implements IMinecraft {
    @Unique
    private static int night$totemCounter = 0;
    @Unique
    private static long night$lastTotemTime = 0L;

    @Inject(method={"createTrackingEmitter(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/particles/ParticleOptions;I)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$onCreateTrackingEmitter(Entity entity, ParticleOptions particle, int lifeTime, CallbackInfo ci) {
        NoRenderModule noRender;
        NoRenderModule noRenderModule = noRender = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(NoRenderModule.class) : null;
        if (noRender != null && noRender.isToggled() && noRender.totemPop.getValue() && particle.getType() == ParticleTypes.TOTEM_OF_UNDYING && entity == ParticleManagerMixin.mc.player) {
            ci.cancel();
        }
    }

    @Inject(method={"createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;"}, at={@At(value="HEAD")}, cancellable=true)
    private void addParticle(ParticleOptions parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable<Particle> info) {
        ParticlesModule particles;
        NoRenderModule noRender;
        NoRenderModule noRenderModule = noRender = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(NoRenderModule.class) : null;
        if (noRender != null && noRender.isToggled() && noRender.explosions.getValue() && parameters.getType() == ParticleTypes.EXPLOSION) {
            info.cancel();
            return;
        }
        ParticlesModule particlesModule = particles = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ParticlesModule.class) : null;
        if (particles != null && particles.isToggled() && particles.totem.getValue() && parameters.getType() == ParticleTypes.TOTEM_OF_UNDYING) {
            if (particles.totemMode.getValue().equalsIgnoreCase("Custom")) {
                info.cancel();
            } else if (particles.totemMode.getValue().equalsIgnoreCase("Vanilla")) {
                long now = System.currentTimeMillis();
                if (now - night$lastTotemTime > 400L) {
                    night$totemCounter = 0;
                    night$lastTotemTime = now;
                }
                if (night$totemCounter >= particles.totemNumber.getValue().intValue()) {
                    info.cancel();
                    return;
                }
                ++night$totemCounter;
            }
        }
    }

    @Inject(method={"createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;"}, at={@At(value="RETURN")})
    private void onCreatedParticle(ParticleOptions parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable<Particle> info) {
        if (parameters.getType() == ParticleTypes.TOTEM_OF_UNDYING) {
            Particle particle;
            ParticlesModule particles;
            ParticlesModule particlesModule = particles = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ParticlesModule.class) : null;
            if (particles != null && particles.isToggled() && particles.totem.getValue() && particles.totemMode.getValue().equalsIgnoreCase("Vanilla") && (particle = (Particle)info.getReturnValue()) instanceof TotemParticle) {
                TotemParticle totemParticle = (TotemParticle)particle;
                Color cA = particles.vanillaColorA.getColor();
                Color cB = particles.vanillaColorB.getColor();
                totemParticle.setColor(cA.getRGB());
                totemParticle.setFadeColor(cB.getRGB());
            }
        }
    }
}

