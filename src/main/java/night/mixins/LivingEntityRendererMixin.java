/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.model.EntityModel
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.minecraft.client.renderer.entity.state.AvatarRenderState
 *  net.minecraft.client.renderer.entity.state.LivingEntityRenderState
 *  net.minecraft.client.renderer.state.level.CameraRenderState
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.LivingEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import night.Night;
import night.modules.impl.movement.ElytraFlyModule;
import night.modules.impl.visuals.NoRenderModule;
import night.utils.IMinecraft;
import night.utils.mixins.ISelfState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LivingEntityRenderer.class})
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
extends EntityRenderer<T, S>
implements IMinecraft {
    private static final double SAME_SPOT_RANGE_SQ = 1.0;

    public LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method={"submit"}, at={@At(value="HEAD")}, cancellable=true)
    private void submit(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo info) {
        double dz;
        double dx;
        NoRenderModule noRender = Night.MODULE_MANAGER.getModule(NoRenderModule.class);
        if (!noRender.isToggled()) {
            return;
        }
        if (noRender.corpses.getValue() && ((LivingEntityRenderState)state).deathTime > 0.0f) {
            info.cancel();
            return;
        }
        if (noRender.player.getValue() && state instanceof AvatarRenderState && !((ISelfState)state).night$isSelf() && LivingEntityRendererMixin.mc.player != null && (dx = ((LivingEntityRenderState)state).x - LivingEntityRendererMixin.mc.player.getX()) * dx + (dz = ((LivingEntityRenderState)state).z - LivingEntityRendererMixin.mc.player.getZ()) * dz <= 1.0) {
            info.cancel();
        }
    }

    @Inject(method={"extractRenderState"}, at={@At(value="TAIL")})
    private void night$extractRenderState(T entity, S state, float partialTicks, CallbackInfo info) {
        ElytraFlyModule ef;
        ((ISelfState)state).night$setSelf(entity == LivingEntityRendererMixin.mc.player);
        if (entity == LivingEntityRendererMixin.mc.player && Night.ROTATION_MANAGER.inRenderTime()) {
            ((LivingEntityRenderState)state).bodyRot = Night.ROTATION_MANAGER.getRenderRotations()[0];
            ((LivingEntityRenderState)state).yRot = Mth.wrapDegrees((float)(Night.ROTATION_MANAGER.getRenderRotations()[0] - ((LivingEntityRenderState)state).bodyRot));
            ((LivingEntityRenderState)state).xRot = Night.ROTATION_MANAGER.getRenderRotations()[1];
            return;
        }
        if (entity == LivingEntityRendererMixin.mc.player && Night.MODULE_MANAGER != null && (ef = Night.MODULE_MANAGER.getModule(ElytraFlyModule.class)) != null && ef.isToggled() && ef.mode.getValue().equalsIgnoreCase("Bounce") && ef.bounceAutoPitch.getValue()) {
            ((LivingEntityRenderState)state).xRot = ef.bouncePitch.getValue().floatValue();
        }
    }
}

