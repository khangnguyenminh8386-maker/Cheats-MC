package night.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import night.utils.BlockEntityInstanceTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class BlockEntityLifecycleMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void night$trackConstructed(BlockEntityType<?> type, BlockPos pos, BlockState state, CallbackInfo ci) {
        BlockEntityInstanceTracker.constructed((BlockEntity) (Object) this);
    }

    @Inject(method = "setRemoved", at = @At("HEAD"))
    private void night$trackRemoved(CallbackInfo ci) {
        BlockEntityInstanceTracker.removed((BlockEntity) (Object) this);
    }
}
