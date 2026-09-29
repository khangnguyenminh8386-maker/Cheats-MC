/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.SlimeBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import night.Night;
import night.modules.impl.movement.NoSlowModule;
import night.utils.IMinecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={SlimeBlock.class})
public class SlimeBlockMixin
implements IMinecraft {
    @Inject(method={"stepOn"}, at={@At(value="HEAD")}, cancellable=true)
    private void onSteppedOn(Level world, BlockPos pos, BlockState state, Entity entity, CallbackInfo info) {
        if (entity == SlimeBlockMixin.mc.player && Night.MODULE_MANAGER.getModule(NoSlowModule.class).isToggled() && Night.MODULE_MANAGER.getModule(NoSlowModule.class).slimeBlocks.getValue()) {
            info.cancel();
        }
    }
}

