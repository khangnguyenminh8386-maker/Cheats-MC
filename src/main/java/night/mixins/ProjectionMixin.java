/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.Projection
 *  org.joml.Matrix4f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package night.mixins;

import net.minecraft.client.renderer.Projection;
import night.Night;
import night.modules.impl.visuals.AspectRatioModule;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={Projection.class})
public class ProjectionMixin {
    @Shadow
    private boolean isMatrixDirty;

    @Inject(method={"getMatrix"}, at={@At(value="HEAD")})
    private void getMatrix$forceDirty(Matrix4f dest, CallbackInfoReturnable<Matrix4f> info) {
        this.isMatrixDirty = true;
    }

    @ModifyArgs(method={"getMatrix"}, at=@At(value="INVOKE", target="Lorg/joml/Matrix4f;setPerspective(FFFFZ)Lorg/joml/Matrix4f;"))
    private void getMatrix$aspectRatio(Args args) {
        if (Night.MODULE_MANAGER == null) {
            return;
        }
        AspectRatioModule module = Night.MODULE_MANAGER.getModule(AspectRatioModule.class);
        if (module != null && module.isToggled()) {
            args.set(1, (Object)Float.valueOf(module.ratio.getValue().floatValue()));
        }
    }
}

