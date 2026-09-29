/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package night.mixins;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import night.Night;
import night.modules.impl.movement.NoSlowModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Block.class})
public class BlockMixin {
    @Inject(method={"getFriction"}, at={@At(value="HEAD")}, cancellable=true)
    private void getSlipperiness(CallbackInfoReturnable<Float> info) {
        if ((Object)this == Blocks.SLIME_BLOCK && Night.MODULE_MANAGER.getModule(NoSlowModule.class).isToggled() && Night.MODULE_MANAGER.getModule(NoSlowModule.class).slimeBlocks.getValue()) {
            info.setReturnValue(Float.valueOf(0.6f));
        }
    }
}

