/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.projectile.FireworkRocketEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import night.Night;
import night.events.impl.RemoveFireworkEvent;
import night.utils.IMinecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={FireworkRocketEntity.class})
public class FireworkRocketEntityMixin
implements IMinecraft {
    @Shadow
    private int life;

    @Inject(method={"tick"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/projectile/FireworkRocketEntity;updateRotation()V", shift=At.Shift.AFTER)}, cancellable=true)
    private void tick(CallbackInfo info) {
        FireworkRocketEntity entity = (FireworkRocketEntity)(Object)this;
        RemoveFireworkEvent event = new RemoveFireworkEvent(entity);
        Night.EVENT_HANDLER.post(event);
        if (event.isCancelled()) {
            info.cancel();
            if (this.life == 0 && !entity.isSilent()) {
                FireworkRocketEntityMixin.mc.level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.AMBIENT, 3.0f, 1.0f);
            }
            ++this.life;
            if (this.life % 2 < 2) {
                FireworkRocketEntityMixin.mc.level.addParticle((ParticleOptions)ParticleTypes.FIREWORK, entity.getX(), entity.getY(), entity.getZ(), FireworkRocketEntityMixin.mc.level.getRandom().nextGaussian() * 0.05, -entity.getDeltaMovement().y * 0.5, FireworkRocketEntityMixin.mc.level.getRandom().nextGaussian() * 0.05);
            }
        }
    }
}

