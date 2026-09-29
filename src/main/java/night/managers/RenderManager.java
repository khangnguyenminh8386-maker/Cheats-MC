/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  lombok.Generated
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.resources.Identifier
 *  net.minecraft.util.Mth
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionfc
 */
package night.managers;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.RenderOverlayEvent;
import night.events.impl.RenderWorldEvent;
import night.modules.impl.combat.AutoCrystalModule;
import night.modules.impl.core.RendersModule;
import night.pingbypass.PingBypassFlags;
import night.pingbypass.protocol.PbCustomPayload;
import night.pingbypass.protocol.packets.S2CRenderPositionPacket;
import night.utils.IMinecraft;
import night.utils.animations.Easing;
import night.utils.graphics.EspShader;
import night.utils.graphics.Renderer2D;
import night.utils.graphics.Renderer3D;
import night.utils.minecraft.WorldUtils;
import night.utils.miscellaneous.RenderPosition;
import night.utils.system.Counter;
import night.utils.system.MathUtils;
import org.joml.Quaternionfc;

public class RenderManager
implements IMinecraft {
    public static final List<Runnable> POST_GUI_RENDERERS = new CopyOnWriteArrayList<Runnable>();
    private final Counter counter = new Counter();
    private int fps;
    public CopyOnWriteArrayList<RenderPosition> renderPositions = new CopyOnWriteArrayList();
    private Target crystalTarget;
    private BlockPos prevPosition = null;
    private Vec3 renderPosition = null;
    private long animationStart = 0L;

    public RenderManager() {
        Night.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderOverlayEvent event) {
        this.fps = this.counter.getCount();
        this.counter.increment();
    }

    @SubscribeEvent
    public void onRenderWorld$placePositions(RenderWorldEvent event) {
        if (RenderManager.mc.player == null || RenderManager.mc.level == null || this.renderPositions.isEmpty()) {
            return;
        }
        RendersModule module = Night.MODULE_MANAGER.getModule(RendersModule.class);
        for (RenderPosition position : this.renderPositions) {
            float scale = position.get();
            AABB box = new AABB(position.getPos());
            if (module.mode.getValue().equals("Shrink")) {
                box = new AABB(position.getPos()).deflate(0.5).inflate(Mth.clamp((double)((double)scale / 2.0), (double)0.0, (double)0.5));
            }
            if (module.renderMode.getValue().equalsIgnoreCase("Fill") || module.renderMode.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBox(event.getMatrices(), box, module.getColor(module.mode.getValue(), module.fillColor.getColor(), scale));
            }
            if (!module.renderMode.getValue().equalsIgnoreCase("Outline") && !module.renderMode.getValue().equalsIgnoreCase("Both")) continue;
            Renderer3D.renderBoxOutline(event.getMatrices(), box, module.getColor(module.mode.getValue(), module.outlineColor.getColor(), scale));
        }
        this.renderPositions.removeIf(p -> p.get() <= 0.0f);
    }

    @SubscribeEvent
    public void onRenderWorld$autoCrystal(RenderWorldEvent event) {
        List<Renderer3D.VertexCollection> lineSink;
        if (RenderManager.mc.player == null || RenderManager.mc.level == null) {
            return;
        }
        if (this.crystalTarget == null || this.crystalTarget.getPosition() == null) {
            return;
        }
        AutoCrystalModule autoCrystalModule = Night.MODULE_MANAGER.getModule(AutoCrystalModule.class);
        float scale = this.crystalTarget.getTarget() == 1 ? Easing.ease(Easing.toDelta(this.crystalTarget.getTime(), autoCrystalModule.duration.getValue().intValue()), Easing.Method.EASE_OUT_CUBIC) : 1.0f - Easing.ease(Easing.toDelta(this.crystalTarget.getTime(), autoCrystalModule.duration.getValue().intValue()), Easing.Method.EASE_IN_CUBIC);
        AABB box = new AABB(this.crystalTarget.getPosition());
        if (autoCrystalModule.mode.getValue().equals("Shrink")) {
            box = new AABB(this.crystalTarget.getPosition()).deflate(0.5).inflate(Mth.clamp((double)((double)scale / 2.0), (double)0.0, (double)0.5));
        }
        if (autoCrystalModule.animationMode.getValue().equals("Slide")) {
            if (this.renderPosition == null) {
                this.renderPosition = MathUtils.getVec(this.crystalTarget.getPosition());
            }
            if (!WorldUtils.equals(this.crystalTarget.getPosition(), this.prevPosition)) {
                this.animationStart = System.currentTimeMillis();
                this.prevPosition = this.crystalTarget.getPosition();
            }
            float easing = Easing.ease(Easing.toDelta(this.animationStart, (int)(Math.pow(autoCrystalModule.slideSmoothness.getValue().doubleValue(), 1.4) * 1000.0)), Easing.Method.EASE_OUT_QUART);
            this.renderPosition = this.renderPosition.add(MathUtils.scale(MathUtils.getVec(this.crystalTarget.getPosition()).subtract(this.renderPosition), easing));
            box = MathUtils.getBox(this.renderPosition);
            if (autoCrystalModule.mode.getValue().equals("Shrink")) {
                box = MathUtils.getBox(this.renderPosition).deflate(0.5).inflate(Mth.clamp((double)((double)scale / 2.0), (double)0.0, (double)0.5));
            }
        }
        RendersModule renders = Night.MODULE_MANAGER.getModule(RendersModule.class);
        boolean fill = autoCrystalModule.renderMode.getValue().equalsIgnoreCase("Fill") || autoCrystalModule.renderMode.getValue().equalsIgnoreCase("Both");
        boolean outline = autoCrystalModule.renderMode.getValue().equalsIgnoreCase("Outline") || autoCrystalModule.renderMode.getValue().equalsIgnoreCase("Both");
        int effect = EspShader.modeIndex(autoCrystalModule.shader.getValue());
        List<Renderer3D.VertexCollection> quadSink = effect == 0 ? Renderer3D.QUADS : Renderer3D.SHADER_QUADS;
        List<Renderer3D.VertexCollection> list = lineSink = effect == 0 ? Renderer3D.DEBUG_LINES : Renderer3D.SHADER_DEBUG_LINES;
        if (effect != 0) {
            float speed = autoCrystalModule.shaderSpeed.getValue().floatValue();
            float step = autoCrystalModule.shaderStep.getValue().floatValue();
            float time = (float)(System.currentTimeMillis() % 2000000L) / 1000.0f * speed * (effect == 1 ? step : 1.0f);
            float distance = autoCrystalModule.shaderDistanceScaling.getValue() ? (float)Math.sqrt(RenderManager.mc.player.distanceToSqr(box.getCenter())) : 1.0f;
            EspShader.setSettings(new EspShader.Settings(effect, time, step, autoCrystalModule.shaderOpacity.getValue().floatValue() / 100.0f, distance, effect == 7 ? autoCrystalModule.shaderGlowColor.getColor() : autoCrystalModule.shaderColor1.getColor(), autoCrystalModule.shaderColor2.getColor(), autoCrystalModule.shaderColor3.getColor(), autoCrystalModule.shaderColor4.getColor()));
        }
        if (fill) {
            Renderer3D.renderGradientBox(quadSink, event.getMatrices(), box, renders.getColor(autoCrystalModule.mode.getValue(), autoCrystalModule.fillColorUp.getColor(), scale), renders.getColor(autoCrystalModule.mode.getValue(), autoCrystalModule.fillColorDown.getColor(), scale));
        }
        if (outline) {
            Renderer3D.renderGradientBoxOutline(lineSink, event.getMatrices(), box, renders.getColor(autoCrystalModule.mode.getValue(), autoCrystalModule.outlineColorUp.getColor(), scale), renders.getColor(autoCrystalModule.mode.getValue(), autoCrystalModule.outlineColorDown.getColor(), scale));
        }
    }

    @SubscribeEvent
    public void onRenderWorld$autoCrystalExtra(RenderWorldEvent.Post event) {
        if (RenderManager.mc.player == null || RenderManager.mc.level == null) {
            return;
        }
        if (this.crystalTarget == null || this.crystalTarget.getPosition() == null) {
            return;
        }
        if (this.crystalTarget.getTarget() != 1) {
            return;
        }
        PoseStack matrices = event.getMatrices();
        AutoCrystalModule module = Night.MODULE_MANAGER.getModule(AutoCrystalModule.class);
        Vec3 crystalCenter = Vec3.atCenterOf((Vec3i)this.crystalTarget.getPosition());
        Vec3 vec3d = new Vec3(crystalCenter.x - RenderManager.mc.gameRenderer.mainCamera().position().x, crystalCenter.y - RenderManager.mc.gameRenderer.mainCamera().position().y, crystalCenter.z - RenderManager.mc.gameRenderer.mainCamera().position().z);
        if (module.animationMode.getValue().equals("Slide")) {
            vec3d = new Vec3(this.renderPosition.x + 0.5 - RenderManager.mc.gameRenderer.mainCamera().position().x, this.renderPosition.y + 0.5 - RenderManager.mc.gameRenderer.mainCamera().position().y, this.renderPosition.z + 0.5 - RenderManager.mc.gameRenderer.mainCamera().position().z);
        }
        if (module.icon.getValue()) {
            float scaling = module.iconScale.getValue().floatValue() / 100.0f;
            matrices.pushPose();
            matrices.translate(vec3d.x, vec3d.y, vec3d.z);
            matrices.mulPose((Quaternionfc)RenderManager.mc.gameRenderer.mainCamera().rotation());
            matrices.scale(scaling, -scaling, scaling);
            Renderer2D.renderCircle(matrices, 0.0f, 0.0f, 12.0f, new Color(0, 0, 0, 100));
            Renderer2D.renderCircle(matrices, 0.0f, 0.0f, 12.0f - module.iconRadius.getValue().floatValue(), module.iconColor.getColor());
            if (module.renderDamage.getValue()) {
                Renderer2D.renderTexture(matrices, -5.5f, -8.5f, 5.5f, 2.5f, Identifier.fromNamespaceAndPath((String)"night", (String)"textures/crystal.png"), Color.WHITE);
                matrices.pushPose();
                matrices.scale(0.45f, 0.45f, 0.45f);
                String text = module.getCalculationDamage();
                Night.FONT_MANAGER.drawTextWithShadow(matrices, text, -Night.FONT_MANAGER.getWidth(text) / 2 - 1, 7, Color.WHITE);
                matrices.popPose();
            } else {
                Renderer2D.renderTexture(matrices, -6.5f, -6.5f, 6.5f, 6.5f, Identifier.fromNamespaceAndPath((String)"night", (String)"textures/crystal.png"), Color.WHITE);
            }
            matrices.popPose();
        } else if (module.renderDamage.getValue()) {
            matrices.pushPose();
            matrices.translate(vec3d.x, vec3d.y, vec3d.z);
            matrices.mulPose((Quaternionfc)RenderManager.mc.gameRenderer.mainCamera().rotation());
            matrices.scale(0.025f, -0.025f, 0.025f);
            String text = module.getCalculationDamage();
            Night.FONT_MANAGER.drawTextWithShadow(matrices, text, -Night.FONT_MANAGER.getWidth(text) / 2, -Night.FONT_MANAGER.getHeight() / 2, Color.WHITE);
            matrices.popPose();
        }
    }

    public void setRenderPosition(BlockPos position) {
        if (!Night.MODULE_MANAGER.getModule(AutoCrystalModule.class).isToggled()) {
            position = null;
        }
        if (position == null) {
            if (this.crystalTarget != null) {
                if (this.crystalTarget.getTarget() != 0) {
                    this.crystalTarget.setTarget(0);
                    this.crystalTarget.setTime(System.currentTimeMillis());
                }
            } else {
                this.crystalTarget = new Target(null, 0, System.currentTimeMillis());
            }
        } else if (this.crystalTarget == null || this.crystalTarget.getTarget() == 0) {
            this.crystalTarget = new Target(position, 1, System.currentTimeMillis());
        } else {
            this.crystalTarget.setPosition(position);
        }
        if (PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer() && Night.PROXY_SERVER != null) {
            this.sendRenderPositionToClient(position);
        }
    }

    private void sendRenderPositionToClient(BlockPos position) {
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(new S2CRenderPositionPacket(position)));
        for (Connection conn : Night.PROXY_SERVER.getConnections()) {
            if (!conn.isConnected()) continue;
            conn.send((Packet)packet);
        }
    }

    @Generated
    public int getFps() {
        return this.fps;
    }

    @Generated
    public CopyOnWriteArrayList<RenderPosition> getRenderPositions() {
        return this.renderPositions;
    }

    public static class Target {
        private BlockPos position;
        private int target;
        private long time;

        @Generated
        public Target(BlockPos position, int target, long time) {
            this.position = position;
            this.target = target;
            this.time = time;
        }

        @Generated
        public BlockPos getPosition() {
            return this.position;
        }

        @Generated
        public int getTarget() {
            return this.target;
        }

        @Generated
        public long getTime() {
            return this.time;
        }

        @Generated
        public void setPosition(BlockPos position) {
            this.position = position;
        }

        @Generated
        public void setTarget(int target) {
            this.target = target;
        }

        @Generated
        public void setTime(long time) {
            this.time = time;
        }
    }
}

