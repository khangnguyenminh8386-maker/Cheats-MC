/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.InsideBlockEffectApplier
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.WebBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.modules.impl.movement.FastWebModule;
import night.utils.IMinecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={WebBlock.class})
public class CobwebBlockMixin
implements IMinecraft {
    @Inject(method={"entityInside"}, at={@At(value="HEAD")}, cancellable=true)
    private void onEntityCollision(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean bl, CallbackInfo info) {
        if (Night.MODULE_MANAGER.getModule(FastWebModule.class).isToggled()) {
            if (Night.MODULE_MANAGER.getModule(FastWebModule.class).sneak.getValue() && !CobwebBlockMixin.mc.player.isShiftKeyDown()) {
                return;
            }
            if (Night.MODULE_MANAGER.getModule(FastWebModule.class).mode.getValue().equalsIgnoreCase("Ignore")) {
                entity.resetFallDistance();
                info.cancel();
            }
            if (Night.MODULE_MANAGER.getModule(FastWebModule.class).mode.getValue().equalsIgnoreCase("Strong")) {
                entity.makeStuckInBlock(state, new Vec3(Night.MODULE_MANAGER.getModule(FastWebModule.class).horizontal.getValue().doubleValue(), Night.MODULE_MANAGER.getModule(FastWebModule.class).vertical.getValue().doubleValue(), Night.MODULE_MANAGER.getModule(FastWebModule.class).horizontal.getValue().doubleValue()));
                info.cancel();
            }
        }
    }
}

