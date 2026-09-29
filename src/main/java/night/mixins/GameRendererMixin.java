/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  com.mojang.blaze3d.ProjectionType
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.mojang.blaze3d.resource.GraphicsResourceAllocator
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.client.renderer.LevelTargetBundle
 *  net.minecraft.client.renderer.PostChain
 *  net.minecraft.client.renderer.ProjectionMatrixBuffer
 *  net.minecraft.client.renderer.state.level.CameraRenderState
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.ItemStack
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import night.Night;
import night.events.impl.RenderWorldEvent;
import night.mixins.accessors.LevelRendererAccessor;
import night.modules.impl.visuals.NoRenderModule;
import night.modules.impl.visuals.ShadersModule;
import night.utils.graphics.EspShader;
import night.utils.graphics.Glint;
import night.utils.graphics.Renderer2D;
import night.utils.graphics.Renderer3D;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={GameRenderer.class})
public abstract class GameRendererMixin {
    private static ProjectionMatrixBuffer night$noBobProjectionBuffer;
    private static final List<Renderer3D.VertexCollection> night$emptyQuads;

    @Inject(method={"renderLevel"}, at={@At(value="HEAD")})
    private void renderWorld$HEAD(DeltaTracker tickCounter, CallbackInfo info) {
        Renderer3D.prepare();
    }

    @Inject(method={"render(Lnet/minecraft/client/DeltaTracker;Z)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V")})
    private void night$resolveOutline(DeltaTracker tracker, boolean tick, CallbackInfo ci) {
        Identifier variant = ShadersModule.pickActiveOutlineChain();
        if (variant == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.levelRenderer == null) {
            return;
        }
        RenderTarget outline = ((LevelRendererAccessor)mc.levelRenderer).night$getEntityOutlineTarget();
        if (outline == null) {
            return;
        }
        PostChain chain = mc.getShaderManager().getPostChain(variant, LevelTargetBundle.MAIN_TARGETS);
        if (chain == null) {
            return;
        }
        EspShader.writeOutlineSettings(chain, ShadersModule.shaderSettings(), ShadersModule.glowIntensity());
        chain.process(outline, GraphicsResourceAllocator.UNPOOLED);
    }

    @Inject(method={"renderLevel"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/LevelRenderer;render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V", shift=At.Shift.AFTER)})
    private void renderWorld$swap(DeltaTracker tickCounter, CallbackInfo info, @Local(ordinal=0) Matrix4fc modelViewMatrix, @Local CameraRenderState cameraState) {
        float tickDelta = tickCounter.getGameTimeDeltaPartialTick(false);
        RenderSystem.getModelViewStack().pushMatrix();
        RenderSystem.getModelViewStack().mul(modelViewMatrix);
        PoseStack noBobStack = new PoseStack();
        Night.EVENT_HANDLER.post(new RenderWorldEvent(noBobStack, tickDelta));
        if (!Renderer3D.QUADS.isEmpty() || !Renderer3D.DEBUG_LINES.isEmpty()) {
            Renderer3D.draw(Renderer3D.QUADS, Renderer3D.DEBUG_LINES, false);
        }
        if (!Renderer3D.SHINE_QUADS.isEmpty() || !Renderer3D.SHINE_DEBUG_LINES.isEmpty()) {
            Renderer3D.draw(Renderer3D.SHINE_QUADS, Renderer3D.SHINE_DEBUG_LINES, true);
        }
        if (!Renderer3D.SHADER_QUADS.isEmpty() || !Renderer3D.SHADER_DEBUG_LINES.isEmpty()) {
            EspShader.draw(Renderer3D.SHADER_QUADS, Renderer3D.SHADER_DEBUG_LINES);
        }
        if (!Glint.QUADS.isEmpty()) {
            Glint.draw();
        }
        Night.EVENT_HANDLER.post(new RenderWorldEvent.Post(noBobStack, tickDelta));
        if (!Renderer3D.QUADS.isEmpty() || !Renderer3D.DEBUG_LINES.isEmpty()) {
            Renderer3D.draw(Renderer3D.QUADS, Renderer3D.DEBUG_LINES, false);
        }
        if (!Renderer3D.SHINE_QUADS.isEmpty() || !Renderer3D.SHINE_DEBUG_LINES.isEmpty()) {
            Renderer3D.draw(Renderer3D.SHINE_QUADS, Renderer3D.SHINE_DEBUG_LINES, true);
        }
        if (!Renderer3D.SHADER_QUADS.isEmpty() || !Renderer3D.SHADER_DEBUG_LINES.isEmpty()) {
            EspShader.draw(Renderer3D.SHADER_QUADS, Renderer3D.SHADER_DEBUG_LINES);
        }
        if (!Glint.QUADS.isEmpty()) {
            Glint.draw();
        }
        if (!Renderer3D.TRACER_DEBUG_LINES.isEmpty()) {
            if (night$noBobProjectionBuffer == null) {
                night$noBobProjectionBuffer = new ProjectionMatrixBuffer("night_no_bob_tracer");
            }
            RenderSystem.backupProjectionMatrix();
            RenderSystem.setProjectionMatrix((GpuBufferSlice)night$noBobProjectionBuffer.getBuffer(cameraState.projectionMatrix), (ProjectionType)ProjectionType.PERSPECTIVE);
            Renderer3D.draw(night$emptyQuads, Renderer3D.TRACER_DEBUG_LINES, false);
            RenderSystem.restoreProjectionMatrix();
        }
        RenderSystem.getModelViewStack().popMatrix();
    }

    @Inject(method={"bobHurt"}, at={@At(value="HEAD")}, cancellable=true)
    private void tiltViewWhenHurt(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo info) {
        if (Night.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() && Night.MODULE_MANAGER.getModule(NoRenderModule.class).hurtCamera.getValue()) {
            info.cancel();
        }
    }

    @Inject(method={"displayItemActivation"}, at={@At(value="HEAD")}, cancellable=true)
    private void showFloatingItem(ItemStack floatingItem, CallbackInfo info) {
        if (Night.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() && Night.MODULE_MANAGER.getModule(NoRenderModule.class).totemAnimation.getValue()) {
            info.cancel();
        }
    }

    @Inject(method={"renderLevel"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/LevelRenderer;render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V", shift=At.Shift.AFTER)})
    private void renderWorld(DeltaTracker tickCounter, CallbackInfo info, @Local(ordinal=0) Matrix4fc modelViewMatrix, @Local(ordinal=0) Matrix4f projectionMatrix) {
        PoseStack matrix = new PoseStack();
        matrix.last().pose().mul(modelViewMatrix);
        Renderer2D.LAST_PROJECTION_MATRIX.set((Matrix4fc)projectionMatrix);
        Renderer2D.LAST_MODEL_MATRIX.set((Matrix4fc)RenderSystem.getModelViewMatrixCopy());
        Renderer2D.LAST_WORLD_MATRIX.set((Matrix4fc)matrix.last().pose());
    }

    static {
        night$emptyQuads = new ArrayList<Renderer3D.VertexCollection>();
    }
}

