/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.BlockDestructionProgress
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import net.minecraft.server.level.BlockDestructionProgress;
import night.Night;
import night.events.impl.PlayerMineEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BlockDestructionProgress.class})
public class BlockBreakingInfoMixin {
    @Inject(method={"setProgress"}, at={@At(value="HEAD")})
    private void setProgress(int progress, CallbackInfo ci) {
        BlockDestructionProgress self = (BlockDestructionProgress)(Object)this;
        if (progress >= 0 && progress <= 9) {
            Night.EVENT_HANDLER.post(new PlayerMineEvent(self.getId(), self.getPos()));
        }
    }
}

