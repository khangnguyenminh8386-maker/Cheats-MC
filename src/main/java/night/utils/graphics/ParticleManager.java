/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.PrimitiveTopology
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.ColorTargetState
 *  com.mojang.blaze3d.pipeline.DepthStencilState
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.BlendFactor
 *  com.mojang.blaze3d.platform.CompareOp
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.ByteBufferBuilder
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.MeshData
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Camera
 *  net.minecraft.client.renderer.rendertype.RenderSetup
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package night.utils.graphics;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.mixins.accessors.RenderPipelinesAccessor;
import night.utils.IMinecraft;
import night.utils.graphics.Renderer3D;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public class ParticleManager
implements IMinecraft {
    private static final int MAX_PARTICLES = 600;
    private static final double CULL_DISTANCE_SQ = 4096.0;
    private static final int CIRCLE_SEGMENTS = 16;
    private static final float[] CIRCLE_X = new float[17];
    private static final float[] CIRCLE_Y = new float[17];
    private static final RenderType NO_DEPTH_PARTICLES;
    private final List<Particle> particles = new CopyOnWriteArrayList<Particle>();
    private static final RandomSource RANDOM;
    private final Quaternionf rotYaw = new Quaternionf();
    private final Quaternionf rotPitch = new Quaternionf();

    public void addBouncy(Vec3 pos, Vec3 motion, double time, int color, int shadowColor, boolean glow) {
        if (this.particles.size() >= 600) {
            return;
        }
        this.particles.add(new BouncyParticle(pos, motion, time, color, shadowColor, glow));
    }

    public void addFriction(Vec3 pos, Vec3 motion, double friction, double time, int color, int shadowColor, boolean glow) {
        if (this.particles.size() >= 600) {
            return;
        }
        this.particles.add(new FrictionParticle(pos, motion, friction, time, color, shadowColor, glow));
    }

    public void tick() {
        this.particles.removeIf(Particle::tick);
    }

    public void render(PoseStack matrices, float tickDelta) {
        if (this.particles.isEmpty() || ParticleManager.mc.gameRenderer == null) {
            return;
        }
        Camera camera = ParticleManager.mc.gameRenderer.mainCamera();
        if (camera == null) {
            return;
        }
        Vec3 cameraPos = camera.position();
        long now = System.currentTimeMillis();
        int visibleCount = 0;
        for (Particle particle : this.particles) {
            if (!particle.canRender(cameraPos, tickDelta, now)) continue;
            visibleCount += particle.getCircleCount();
        }
        if (visibleCount == 0) {
            return;
        }
        this.rotYaw.identity().rotationY((float)Math.toRadians(-camera.yRot()));
        this.rotPitch.identity().rotationX((float)Math.toRadians(camera.xRot()));
        int totalVertices = visibleCount * 16 * 3;
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(totalVertices * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));){
            BufferBuilder buffer = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            for (Particle particle : this.particles) {
                if (!particle.canRender(cameraPos, tickDelta, now)) continue;
                particle.render(matrices, buffer, cameraPos, tickDelta, now, this.rotYaw, this.rotPitch);
            }
            MeshData mesh = buffer.build();
            Renderer3D.draw(NO_DEPTH_PARTICLES, mesh);
        }
    }

    public void clear() {
        this.particles.clear();
    }

    public boolean isEmpty() {
        return this.particles.isEmpty();
    }

    public int size() {
        return this.particles.size();
    }

    private static int alphaMulti(int color, float alpha) {
        int a = color >> 24 & 0xFF;
        int alp = (int)((float)a * alpha);
        return (alp & 0xFF) << 24 | color & 0xFFFFFF;
    }

    private static void renderCircle(Matrix4f matrix, BufferBuilder buffer, float radius, float z, int color) {
        for (int i = 0; i < 16; ++i) {
            float x1 = CIRCLE_X[i] * radius;
            float y1 = CIRCLE_Y[i] * radius;
            float x2 = CIRCLE_X[i + 1] * radius;
            float y2 = CIRCLE_Y[i + 1] * radius;
            buffer.addVertex((Matrix4fc)matrix, 0.0f, 0.0f, z).setColor(color);
            buffer.addVertex((Matrix4fc)matrix, x1, y1, z).setColor(color);
            buffer.addVertex((Matrix4fc)matrix, x2, y2, z).setColor(color);
        }
    }

    private static AABB expandTowards(AABB box, double x, double y, double z) {
        return new AABB(box.minX + (x < 0.0 ? x : 0.0), box.minY + (y < 0.0 ? y : 0.0), box.minZ + (z < 0.0 ? z : 0.0), box.maxX + (x > 0.0 ? x : 0.0), box.maxY + (y > 0.0 ? y : 0.0), box.maxZ + (z > 0.0 ? z : 0.0));
    }

    private static float calcAlpha(double delta) {
        if (delta < 0.1) {
            return (float)(delta * 10.0);
        }
        if (delta > 0.5) {
            return (float)(1.0 - (delta - 0.5) * 2.0);
        }
        return 1.0f;
    }

    public static Vec3 randomMotion(double speed) {
        double yaw = RANDOM.nextDouble() * 2.0 * Math.PI;
        double pitch = RANDOM.nextDouble() * 2.0 * Math.PI;
        double c = Math.abs(Math.cos(pitch));
        return new Vec3(speed * Math.cos(yaw) * c, speed * -Math.sin(pitch), speed * Math.sin(yaw) * c);
    }

    static {
        for (int i = 0; i <= 16; ++i) {
            double angle = Math.PI * 2 * (double)i / 16.0;
            ParticleManager.CIRCLE_X[i] = (float)Math.cos(angle);
            ParticleManager.CIRCLE_Y[i] = (float)Math.sin(angle);
        }
        NO_DEPTH_PARTICLES = RenderType.create((String)"night_no_depth_particles", (RenderSetup)RenderSetup.builder((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getDebugFilledSnippet()}).withLocation("night/no_depth_particles").withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA))).build()).createRenderSetup());
        RANDOM = RandomSource.create();
    }

    private static class BouncyParticle
    extends BaseParticle {
        private double motionX;
        private double motionY;
        private double motionZ;

        private BouncyParticle(Vec3 pos, Vec3 motion, double time, int color, int shadowColor, boolean glow) {
            super(pos, time, color, shadowColor, glow);
            this.motionX = motion.x;
            this.motionY = motion.y;
            this.motionZ = motion.z;
            this.tick();
        }

        @Override
        public boolean tick() {
            AABB box;
            if (System.currentTimeMillis() - this.startTime > this.lifetimeMillis) {
                return true;
            }
            this.prevPos = this.pos;
            if (IMinecraft.mc.level != null && IMinecraft.mc.player != null && IMinecraft.mc.level.getBlockCollisions((Entity)IMinecraft.mc.player, ParticleManager.expandTowards(box = new AABB(this.pos.x - 0.025, this.pos.y - 0.025, this.pos.z - 0.025, this.pos.x + 0.025, this.pos.y + 0.025, this.pos.z + 0.025), this.motionX, this.motionY, this.motionZ)).iterator().hasNext()) {
                if (this.motionX != 0.0 && IMinecraft.mc.level.getBlockCollisions((Entity)IMinecraft.mc.player, ParticleManager.expandTowards(box, this.motionX, 0.0, 0.0)).iterator().hasNext()) {
                    this.motionX *= -0.7;
                }
                if (this.motionY != 0.0 && IMinecraft.mc.level.getBlockCollisions((Entity)IMinecraft.mc.player, ParticleManager.expandTowards(box, 0.0, this.motionY, 0.0)).iterator().hasNext()) {
                    this.motionY *= -0.7;
                }
                if (this.motionZ != 0.0 && IMinecraft.mc.level.getBlockCollisions((Entity)IMinecraft.mc.player, ParticleManager.expandTowards(box, 0.0, 0.0, this.motionZ)).iterator().hasNext()) {
                    this.motionZ *= -0.7;
                }
            }
            this.pos = this.pos.add(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.98;
            this.motionZ *= 0.98;
            this.motionY = (this.motionY - 0.08) * 0.98;
            return false;
        }
    }

    private static class FrictionParticle
    extends BaseParticle {
        private final double friction;
        private Vec3 motion;

        private FrictionParticle(Vec3 pos, Vec3 motion, double friction, double time, int color, int shadowColor, boolean glow) {
            super(pos, time, color, shadowColor, glow);
            this.motion = motion;
            this.friction = friction;
            this.tick();
        }

        @Override
        public boolean tick() {
            if (System.currentTimeMillis() - this.startTime > this.lifetimeMillis) {
                return true;
            }
            this.prevPos = this.pos;
            this.motion = this.motion.scale(this.friction);
            this.pos = this.pos.add(this.motion);
            return false;
        }
    }

    private static interface Particle {
        public boolean tick();

        public boolean canRender(Vec3 var1, float var2, long var3);

        public int getCircleCount();

        public void render(PoseStack var1, BufferBuilder var2, Vec3 var3, float var4, long var5, Quaternionf var7, Quaternionf var8);
    }

    private static abstract class BaseParticle
    implements Particle {
        protected final int color;
        protected final int shadowColor;
        protected final boolean glow;
        protected final long startTime;
        protected final double lifetime;
        protected final long lifetimeMillis;
        protected Vec3 pos;
        protected Vec3 prevPos;

        protected BaseParticle(Vec3 pos, double time, int color, int shadowColor, boolean glow) {
            this.pos = pos;
            this.prevPos = pos;
            this.color = color;
            this.shadowColor = shadowColor;
            this.glow = glow;
            this.startTime = System.currentTimeMillis();
            this.lifetime = time;
            this.lifetimeMillis = (long)(time * 1000.0);
        }

        @Override
        public int getCircleCount() {
            return this.glow ? 4 : 2;
        }

        @Override
        public boolean canRender(Vec3 cameraPos, float tickDelta, long now) {
            double z;
            double y;
            long elapsed = now - this.startTime;
            if (elapsed > this.lifetimeMillis) {
                return false;
            }
            double age = (double)elapsed / 1000.0 / this.lifetime;
            float alpha = ParticleManager.calcAlpha(Mth.clamp((double)age, (double)0.0, (double)1.0));
            if (alpha <= 0.0f) {
                return false;
            }
            double x = Mth.lerp((double)tickDelta, (double)this.prevPos.x, (double)this.pos.x) - cameraPos.x;
            return x * x + (y = Mth.lerp((double)tickDelta, (double)this.prevPos.y, (double)this.pos.y) - cameraPos.y) * y + (z = Mth.lerp((double)tickDelta, (double)this.prevPos.z, (double)this.pos.z) - cameraPos.z) * z <= 4096.0;
        }

        @Override
        public void render(PoseStack matrices, BufferBuilder buffer, Vec3 cameraPos, float tickDelta, long now, Quaternionf rotYaw, Quaternionf rotPitch) {
            long elapsed = now - this.startTime;
            double age = (double)elapsed / 1000.0 / this.lifetime;
            float alpha = ParticleManager.calcAlpha(Mth.clamp((double)age, (double)0.0, (double)1.0));
            double x = Mth.lerp((double)tickDelta, (double)this.prevPos.x, (double)this.pos.x) - cameraPos.x;
            double y = Mth.lerp((double)tickDelta, (double)this.prevPos.y, (double)this.pos.y) - cameraPos.y;
            double z = Mth.lerp((double)tickDelta, (double)this.prevPos.z, (double)this.pos.z) - cameraPos.z;
            matrices.pushPose();
            matrices.translate(x, y, z);
            matrices.scale(0.02f, 0.02f, 0.02f);
            matrices.mulPose((Quaternionfc)rotYaw);
            matrices.mulPose((Quaternionfc)rotPitch);
            Matrix4f matrix = matrices.last().pose();
            int shadow = ParticleManager.alphaMulti(this.shadowColor, alpha);
            int main = ParticleManager.alphaMulti(this.color, alpha);
            if (this.glow) {
                int glowColor = ParticleManager.alphaMulti(this.color, alpha * 0.4f);
                ParticleManager.renderCircle(matrix, buffer, 4.0f, -0.02f, glowColor);
                ParticleManager.renderCircle(matrix, buffer, 2.5f, -0.015f, glowColor);
            }
            ParticleManager.renderCircle(matrix, buffer, 2.0f, -0.01f, shadow);
            ParticleManager.renderCircle(matrix, buffer, 1.0f, 0.0f, main);
            matrices.popPose();
        }
    }
}

