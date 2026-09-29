/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.world.level.BlockCollisions
 *  net.minecraft.world.level.block.state.BlockState
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 */
package night.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.block.state.BlockState;
import night.utils.minecraft.WorldUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={BlockCollisions.class})
public class BlockCollisionsMixin {
    @Shadow
    @Final
    private BlockPos.MutableBlockPos pos;

    @ModifyExpressionValue(method={"computeNext"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/level/BlockGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")})
    private BlockState night$predictPlacedCollision(BlockState original) {
        if (!original.isAir()) {
            return original;
        }
        BlockState predicted = WorldUtils.getPredictedCollisionState((BlockPos)this.pos);
        return predicted != null ? predicted : original;
    }
}

