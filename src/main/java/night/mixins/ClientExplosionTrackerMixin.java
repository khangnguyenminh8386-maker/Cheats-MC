/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientExplosionTracker
 *  net.minecraft.core.particles.ExplosionParticleInfo
 *  net.minecraft.util.random.WeightedList
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import net.minecraft.client.multiplayer.ClientExplosionTracker;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.modules.impl.visuals.NoRenderModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientExplosionTracker.class})
public class ClientExplosionTrackerMixin {
    @Inject(method={"track"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$cancelDebris(Vec3 center, float radius, int blockCount, WeightedList<ExplosionParticleInfo> blockParticles, CallbackInfo ci) {
        NoRenderModule noRender = Night.MODULE_MANAGER.getModule(NoRenderModule.class);
        if (noRender.isToggled() && noRender.explosions.getValue()) {
            ci.cancel();
        }
    }
}

