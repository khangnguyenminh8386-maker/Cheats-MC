/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.entity.player.AvatarRenderer
 *  net.minecraft.client.renderer.entity.state.AvatarRenderState
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.world.entity.Avatar
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package night.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.modules.impl.visuals.ChamsModule;
import night.modules.impl.visuals.ShadersModule;
import night.utils.mixins.IChamsCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={AvatarRenderer.class})
public class AvatarRendererMixin {
    @Inject(method={"extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V"}, at={@At(value="TAIL")})
    private void night$forceChamsOverlayLayers(Avatar entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        if (!(entity instanceof Entity)) {
            return;
        }
        Avatar realEntity = entity;
        ChamsModule chams = Night.MODULE_MANAGER.getModule(ChamsModule.class);
        if (!(chams != null && chams.isToggled() && chams.players.getValue() && chams.isValidEntity((Entity)realEntity))) {
            return;
        }
        state.showHat = true;
        state.showJacket = true;
        state.showLeftSleeve = true;
        state.showRightSleeve = true;
        state.showLeftPants = true;
        state.showRightPants = true;
    }

    @Redirect(method={"renderHand"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModelPart(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IILnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"), require=0)
    private void night$handOutline(SubmitNodeCollector collector, ModelPart part, PoseStack pose, RenderType type, int light, int overlay, TextureAtlasSprite sprite) {
        collector.submitModelPart(part, pose, type, light, overlay, sprite, -1, null, AvatarRendererMixin.night$outlineColor());
    }

    private static int night$outlineColor() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return 0;
        }
        ShadersModule shaders = Night.MODULE_MANAGER.getModule(ShadersModule.class);
        if (shaders.isToggled() && shaders.isValidEntity((Entity)mc.player)) {
            return shaders.getColor((Entity)mc.player).getRGB() | 0xFF000000;
        }
        return 0;
    }

    @Inject(method={"getRenderOffset(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Lnet/minecraft/world/phys/Vec3;"}, at={@At(value="RETURN")}, cancellable=true)
    private void night$applyPopChamYOffset(AvatarRenderState state, CallbackInfoReturnable<Vec3> cir) {
        IChamsCapture capture = (IChamsCapture)state;
        float yOffset = capture.night$chamsYOffset();
        if (yOffset != 0.0f) {
            Vec3 orig = (Vec3)cir.getReturnValue();
            cir.setReturnValue((orig == null ? new Vec3(0.0, (double)yOffset, 0.0) : orig.add(0.0, (double)yOffset, 0.0)));
        }
    }
}

