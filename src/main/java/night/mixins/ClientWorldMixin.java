/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.Entity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import night.Night;
import night.events.impl.EntitySpawnEvent;
import night.events.impl.PlayerMineEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientLevel.class})
public class ClientWorldMixin {
    @Inject(method={"addEntity"}, at={@At(value="HEAD")})
    private void addEntity(Entity entity, CallbackInfo info) {
        Night.EVENT_HANDLER.post(new EntitySpawnEvent(entity));
    }

    @Inject(method={"destroyBlockProgress"}, at={@At(value="HEAD")})
    private void destroyBlockProgress(int id, BlockPos pos, int progress, CallbackInfo info) {
        if (progress >= 0 && progress <= 9) {
            Night.EVENT_HANDLER.post(new PlayerMineEvent(id, pos));
        }
    }
}

