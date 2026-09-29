/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.ColorTargetState
 *  com.mojang.blaze3d.pipeline.DepthStencilState
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.CompareOp
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.ByteBufferBuilder
 *  com.mojang.blaze3d.vertex.MeshData
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  net.minecraft.client.Camera
 *  net.minecraft.client.renderer.BindGroupLayouts
 *  net.minecraft.client.renderer.rendertype.RenderSetup
 *  net.minecraft.client.renderer.rendertype.RenderSetup$OutlineProperty
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package night.utils.graphics;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import night.mixins.accessors.RenderPipelinesAccessor;
import night.utils.IMinecraft;
import night.utils.graphics.Renderer3D;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class KillEffectMemeRenderer
implements IMinecraft {
    public static final KillEffectMemeRenderer INSTANCE = new KillEffectMemeRenderer();
    private static final Identifier CIRCLE_TEXTURE_ID = Identifier.fromNamespaceAndPath((String)"night", (String)"textures/effect/meme_circle.png");
    private static final Identifier ARROW_TEXTURE_ID = Identifier.fromNamespaceAndPath((String)"night", (String)"textures/effect/meme_arrow.png");
    private static final int BLINK_COUNT = 3;
    private static final float BLINK_ON_SEC = 0.18f;
    private static final float BLINK_OFF_SEC = 0.12f;
    private static final float CYCLE_SEC = 0.3f;
    private static RenderType circleLayer;
    private static RenderType arrowLayer;
    private static final RenderPipeline NO_DEPTH_MEME_PIPELINE;
    private final List<MemeInstance> instances = new ArrayList<MemeInstance>();
    private long lastNanos = 0L;

    private static RenderType buildLayer(String name, Identifier texture) {
        return RenderType.create((String)("night_" + name), (RenderSetup)RenderSetup.builder((RenderPipeline)NO_DEPTH_MEME_PIPELINE).withTexture("Sampler0", texture).useLightmap().useOverlay().affectsCrumbling().sortOnUpload().setOutline(RenderSetup.OutlineProperty.NONE).createRenderSetup());
    }

    private static RenderType getCircleLayer() {
        if (circleLayer != null) {
            return circleLayer;
        }
        circleLayer = KillEffectMemeRenderer.buildLayer("kill_effect_meme_circle", CIRCLE_TEXTURE_ID);
        return circleLayer;
    }

    private static RenderType getArrowLayer() {
        if (arrowLayer != null) {
            return arrowLayer;
        }
        arrowLayer = KillEffectMemeRenderer.buildLayer("kill_effect_meme_arrow", ARROW_TEXTURE_ID);
        return arrowLayer;
    }

    public void spawn(Vector3f worldPos, float size) {
        MemeInstance inst = new MemeInstance();
        inst.pos.set((Vector3fc)worldPos);
        inst.size = size;
        this.instances.add(inst);
    }

    public void render(PoseStack matrices) {
        if (this.instances.isEmpty()) {
            this.lastNanos = 0L;
            return;
        }
        if (KillEffectMemeRenderer.mc.player == null || KillEffectMemeRenderer.mc.level == null) {
            return;
        }
        long now = System.nanoTime();
        float dt = this.lastNanos == 0L ? 0.0f : Math.min((float)(now - this.lastNanos) / 1.0E9f, 0.1f);
        this.lastNanos = now;
        float totalLife = 0.90000004f;
        Iterator<MemeInstance> it = this.instances.iterator();
        while (it.hasNext()) {
            MemeInstance inst = it.next();
            inst.age += dt;
            if (!(inst.age >= totalLife)) continue;
            it.remove();
        }
        if (this.instances.isEmpty()) {
            return;
        }
        PoseStack.Pose pose = matrices.last();
        Camera camera = KillEffectMemeRenderer.mc.gameRenderer.mainCamera();
        Vector3f camUp = new Vector3f(camera.upVector());
        Vector3f camLeft = new Vector3f(camera.leftVector());
        Vec3 camPosD = camera.position();
        Vector3f camPos = new Vector3f((float)camPosD.x, (float)camPosD.y, (float)camPosD.z);
        Vector3f rel = new Vector3f();
        RenderType circleType = KillEffectMemeRenderer.getCircleLayer();
        try (ByteBufferBuilder circleBytes = new ByteBufferBuilder(1024);){
            BufferBuilder circleVc = new BufferBuilder(circleBytes, circleType.primitiveTopology(), circleType.format());
            for (MemeInstance inst : this.instances) {
                if (inst.age % 0.3f >= 0.18f) continue;
                rel.set((Vector3fc)inst.pos).sub((Vector3fc)camPos);
                KillEffectMemeRenderer.emitBillboard(circleVc, pose, rel, camUp, camLeft, inst.size, 255);
            }
            MeshData mesh = circleVc.build();
            if (mesh != null) {
                Renderer3D.draw(circleType, mesh);
            }
        }
        Vector3f arrowRel = new Vector3f();
        RenderType arrowType = KillEffectMemeRenderer.getArrowLayer();
        try (ByteBufferBuilder arrowBytes = new ByteBufferBuilder(1024);){
            BufferBuilder arrowVc = new BufferBuilder(arrowBytes, arrowType.primitiveTopology(), arrowType.format());
            for (MemeInstance inst : this.instances) {
                if (inst.age % 0.3f >= 0.18f) continue;
                rel.set((Vector3fc)inst.pos).sub((Vector3fc)camPos);
                arrowRel.set((Vector3fc)camLeft).mul(-inst.size * 1.1f).add(camUp.x * inst.size * 1.1f, camUp.y * inst.size * 1.1f, camUp.z * inst.size * 1.1f).add((Vector3fc)rel);
                KillEffectMemeRenderer.emitBillboard(arrowVc, pose, arrowRel, camUp, camLeft, inst.size * 0.8f, 255);
            }
            MeshData mesh = arrowVc.build();
            if (mesh != null) {
                Renderer3D.draw(arrowType, mesh);
            }
        }
    }

    private static void emitBillboard(BufferBuilder vc, PoseStack.Pose pose, Vector3f c, Vector3f camUp, Vector3f camLeft, float size, int alpha) {
        Vector3f u = new Vector3f((Vector3fc)camUp).mul(size);
        Vector3f l = new Vector3f((Vector3fc)camLeft).mul(size);
        Vector3f v0 = new Vector3f((Vector3fc)c).sub((Vector3fc)u).sub((Vector3fc)l);
        Vector3f v1 = new Vector3f((Vector3fc)c).sub((Vector3fc)u).add((Vector3fc)l);
        Vector3f v2 = new Vector3f((Vector3fc)c).add((Vector3fc)u).add((Vector3fc)l);
        Vector3f v3 = new Vector3f((Vector3fc)c).add((Vector3fc)u).sub((Vector3fc)l);
        int argb = alpha << 24 | 0xFFFFFF;
        vc.addVertex(pose, v0.x, v0.y, v0.z).setColor(argb).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0f, 1.0f, 0.0f);
        vc.addVertex(pose, v1.x, v1.y, v1.z).setColor(argb).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0f, 1.0f, 0.0f);
        vc.addVertex(pose, v2.x, v2.y, v2.z).setColor(argb).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0f, 1.0f, 0.0f);
        vc.addVertex(pose, v3.x, v3.y, v3.z).setColor(argb).setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    static {
        NO_DEPTH_MEME_PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getEntitySnippet()}).withLocation("night/no_depth_meme").withShaderDefine("ALPHA_CUTOUT", 0.1f).withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER1_SAMPLER2).withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).build();
    }

    private static final class MemeInstance {
        final Vector3f pos = new Vector3f();
        float age;
        float size;

        private MemeInstance() {
        }
    }
}

