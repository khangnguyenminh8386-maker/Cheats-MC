/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher
 *  net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState
 *  net.minecraft.client.renderer.state.level.CameraRenderState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import night.Night;
import night.modules.impl.visuals.NoRenderModule;
import night.utils.IMinecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BlockEntityRenderDispatcher.class})
public class BlockEntityRenderDispatcherMixin
implements IMinecraft {
    @Inject(method={"submit"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$submit(BlockEntityRenderState state, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState cameraState, CallbackInfo info) {
        NoRenderModule noRender = Night.MODULE_MANAGER.getModule(NoRenderModule.class);
        if (noRender.isToggled() && !noRender.tileEntities.getValue().equals("Never") && (noRender.tileEntities.getValue().equals("Always") || noRender.tileEntities.getValue().equals("Distance") && Math.sqrt(BlockEntityRenderDispatcherMixin.mc.player.distanceToSqr((double)state.blockPos.getX(), (double)state.blockPos.getY(), (double)state.blockPos.getZ())) > (double)noRender.tileDistance.getValue().floatValue())) {
            info.cancel();
        }
    }
}

