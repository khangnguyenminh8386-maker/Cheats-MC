/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$PosRot
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Rot
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 */
package night.managers;

import java.util.HashMap;
import java.util.concurrent.PriorityBlockingQueue;
import lombok.Generated;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.ClientRotationEvent;
import night.events.impl.KeyboardTickEvent;
import night.events.impl.PacketSendEvent;
import night.events.impl.PlayerJumpEvent;
import night.events.impl.PlayerUpdateEvent;
import night.events.impl.UpdateMovementEvent;
import night.events.impl.UpdateVelocityEvent;
import night.mixins.accessors.EntityAccessor;
import night.modules.Module;
import night.modules.impl.core.RotationsModule;
import night.pingbypass.PingBypassFlags;
import night.pingbypass.server.ProxyServerTickListener;
import night.utils.IMinecraft;
import night.utils.animations.Easing;
import night.utils.rotations.LegacyRotation;
import night.utils.rotations.Rotation;
import night.utils.system.MathUtils;

public class RotationManager
implements IMinecraft {
    private Rotation rotation = null;
    private Module rotationOwner = null;
    private float prevYaw;
    private float prevPitch;
    private float serverYaw;
    private float serverPitch;
    private float lastPacketTargetYaw = Float.NaN;
    private float lastPacketTargetPitch = Float.NaN;
    private float lastSilentTargetYaw = Float.NaN;
    private float lastSilentTargetPitch = Float.NaN;
    private double lastSilentX = Double.NaN;
    private double lastSilentY = Double.NaN;
    private double lastSilentZ = Double.NaN;
    private float prevRenderYaw;
    private float prevRenderPitch;
    private long lastRenderTime = 0L;
    private boolean batchingRotations = false;
    private boolean batchHasPending = false;
    private float batchYaw;
    private float batchPitch;
    public static boolean swapActive = false;
    private final PriorityBlockingQueue<LegacyRotation> legacyQueue = new PriorityBlockingQueue<>(11, this::compareLegacyRotations);
    private static final HashMap<String, Integer> LEGACY_PRIORITIES = new HashMap();
    private float prevFixYaw;
    private long lastPacketRotateTime = 0L;
    private boolean silentSyncRequired = false;

    public RotationManager() {
        Night.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent(priority=-2147483648)
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        int eventPriority;
        if (RotationManager.mc.player == null) {
            return;
        }
        this.legacyQueue.removeIf(r -> System.currentTimeMillis() - r.getTime() > 100L);
        LegacyRotation legacy = this.legacyQueue.peek();
        Rotation snapshot = new Rotation((Entity)RotationManager.mc.player);
        ClientRotationEvent rotationEvent = new ClientRotationEvent(snapshot);
        Night.EVENT_HANDLER.post(rotationEvent);
        eventPriority = rotationEvent.isCancelled() && rotationEvent.getOwner() != null ? this.getLegacyModulePriority(rotationEvent.getOwner()) : (rotationEvent.isCancelled() ? 0 : -1);
        if (legacy != null && legacy.getPriority() > eventPriority) {
            this.rotation = new Rotation(legacy.getYaw(), legacy.getPitch());
            this.rotationOwner = legacy.getModule();
            this.lastRenderTime = System.currentTimeMillis();
        } else if (rotationEvent.isCancelled()) {
            this.rotation = snapshot;
            this.rotationOwner = rotationEvent.getOwner();
            this.lastRenderTime = System.currentTimeMillis();
        } else {
            this.rotation = null;
            this.rotationOwner = null;
        }
    }

    @SubscribeEvent(priority=0x7FFFFFFF)
    public void onUpdateMovement(UpdateMovementEvent event) {
        if (this.rotation == null) {
            return;
        }
        if (PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer()) {
            return;
        }
        EntityAccessor raw = (EntityAccessor)RotationManager.mc.player;
        this.prevYaw = raw.getRawYRot();
        this.prevPitch = raw.getRawXRot();
        raw.setRawYRot(this.rotation.getYaw());
        raw.setRawXRot(this.rotation.getPitch());
        swapActive = true;
    }

    @SubscribeEvent(priority=-2147483648)
    public void onUpdateMovement$POST(UpdateMovementEvent.Post event) {
        if (this.rotation == null) {
            return;
        }
        if (PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer()) {
            return;
        }
        swapActive = false;
        EntityAccessor raw = (EntityAccessor)RotationManager.mc.player;
        raw.setRawYRot(this.prevYaw);
        raw.setRawXRot(this.prevPitch);
    }

    public int getLegacyModulePriority(Module module) {
        return LEGACY_PRIORITIES.getOrDefault(module.getName(), 0);
    }

    private int compareLegacyRotations(LegacyRotation target, LegacyRotation rotation) {
        if (target.getPriority() == rotation.getPriority()) {
            return -Long.compare(target.getTime(), rotation.getTime());
        }
        return -Integer.compare(target.getPriority(), rotation.getPriority());
    }

    public void legacyRotate(float[] rotations, int priority) {
        this.legacyRotate(rotations[0], rotations[1], priority);
    }

    public void legacyRotate(float yaw, float pitch, int priority) {
        this.legacyQueue.removeIf(r -> r.getModule() == null && r.getPriority() == priority);
        this.legacyQueue.add(new LegacyRotation(yaw, pitch, priority));
    }

    public void legacyRotate(float[] rotations, Module module, int priority) {
        this.legacyRotate(rotations[0], rotations[1], module, priority);
    }

    public void legacyRotate(float yaw, float pitch, Module module, int priority) {
        this.legacyQueue.removeIf(r -> r.getModule() == module);
        this.legacyQueue.add(new LegacyRotation(yaw, pitch, module, priority));
    }

    @SubscribeEvent
    public void onUpdateVelocity(UpdateVelocityEvent event) {
        if (RotationManager.mc.player == null) {
            return;
        }
        if (!Night.MODULE_MANAGER.getModule(RotationsModule.class).movementFix.getValue()) {
            return;
        }
        if (this.rotation == null) {
            return;
        }
        event.setVelocity(EntityAccessor.invokeMovementInputToVelocity(event.getMovementInput(), event.getSpeed(), this.rotation.getYaw()));
        event.setCancelled(true);
    }

    @SubscribeEvent
    public void onKeyboardTick(KeyboardTickEvent event) {
        if (RotationManager.mc.player == null || RotationManager.mc.level == null || RotationManager.mc.player.isPassenger()) {
            return;
        }
        if (!Night.MODULE_MANAGER.getModule(RotationsModule.class).movementFix.getValue()) {
            return;
        }
        if (this.rotation == null) {
            return;
        }
        float movementForward = event.getMovementForward();
        float movementSideways = event.getMovementSideways();
        float delta = (RotationManager.mc.player.getYRot() - this.rotation.getYaw()) * ((float)Math.PI / 180);
        float cos = Mth.cos((double)delta);
        float sin = Mth.sin((double)delta);
        event.setMovementForward(Math.round(movementForward * cos + movementSideways * sin));
        event.setMovementSideways(Math.round(movementSideways * cos - movementForward * sin));
        event.setCancelled(true);
    }

    @SubscribeEvent
    public void onPlayerJump(PlayerJumpEvent event) {
        if (RotationManager.mc.player == null || RotationManager.mc.level == null || RotationManager.mc.player.isPassenger()) {
            return;
        }
        if (!Night.MODULE_MANAGER.getModule(RotationsModule.class).movementFix.getValue()) {
            return;
        }
        if (this.rotation == null) {
            return;
        }
        this.prevFixYaw = RotationManager.mc.player.getYRot();
        RotationManager.mc.player.setYRot(this.rotation.getYaw());
    }

    @SubscribeEvent
    public void onPlayerJump$POST(PlayerJumpEvent.Post event) {
        if (RotationManager.mc.player == null || RotationManager.mc.level == null || RotationManager.mc.player.isPassenger()) {
            return;
        }
        if (!Night.MODULE_MANAGER.getModule(RotationsModule.class).movementFix.getValue()) {
            return;
        }
        if (this.rotation == null) {
            return;
        }
        RotationManager.mc.player.setYRot(this.prevFixYaw);
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent event) {
        if (RotationManager.mc.player == null) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof ServerboundMovePlayerPacket) {
            ServerboundMovePlayerPacket packet2 = (ServerboundMovePlayerPacket)packet;
            if (!packet2.hasRotation()) {
                return;
            }
            this.serverYaw = packet2.getYRot(RotationManager.mc.player.getYRot());
            this.serverPitch = packet2.getXRot(RotationManager.mc.player.getXRot());
        }
    }

    public void packetRotate(float[] rotations) {
        this.packetRotate(rotations[0], rotations[1]);
    }

    private float[] applyJitter(float yaw, float pitch) {
        if (pitch <= -89.5f || pitch >= 89.5f) {
            return new float[]{yaw, pitch};
        }
        String mode = Night.MODULE_MANAGER.getModule(RotationsModule.class).jitter.getValue();
        if ("Grim".equalsIgnoreCase(mode)) {
            float f = (float)((Math.random() * 2.0 - 1.0) * (double)0.001f);
            pitch = Mth.clamp((float)(pitch + f), (float)-90.0f, (float)90.0f);
        } else if ("Normal".equalsIgnoreCase(mode)) {
            float minJitter = 0.15f;
            float maxJitter = 0.35f;
            float jitterYaw = minJitter + (float)(Math.random() * (double)(maxJitter - minJitter));
            float jitterPitch = minJitter + (float)(Math.random() * (double)(maxJitter - minJitter));
            float f = Math.random() < 0.5 ? -1.0f : 1.0f;
            yaw += (jitterYaw *= Math.random() < 0.5 ? -1.0f : 1.0f);
            pitch = Mth.clamp((float)(pitch + (jitterPitch *= f)), (float)-90.0f, (float)90.0f);
        }
        return new float[]{yaw, pitch};
    }

    public boolean isPacketRotateActive() {
        return System.currentTimeMillis() - this.lastPacketRotateTime < 500L;
    }

    public void packetRotate(float yawIn, float pitchIn) {
        this.packetRotate(yawIn, pitchIn, false);
    }

    public void packetRotate(float yawIn, float pitchIn, boolean force) {
        if (!force && this.lastPacketTargetYaw == yawIn && this.lastPacketTargetPitch == pitchIn) {
            return;
        }
        this.lastPacketTargetYaw = yawIn;
        this.lastPacketTargetPitch = pitchIn;
        float[] jittered = this.applyJitter(yawIn, pitchIn);
        float yaw = jittered[0];
        float pitch = jittered[1];
        this.lastPacketRotateTime = System.currentTimeMillis();
        this.serverYaw = yaw;
        this.serverPitch = pitch;
        double x = PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer() ? Night.POSITION_MANAGER.getServerX() : RotationManager.mc.player.getX();
        double y = PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer() ? Night.POSITION_MANAGER.getServerY() : RotationManager.mc.player.getY();
        double z = PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer() ? Night.POSITION_MANAGER.getServerZ() : RotationManager.mc.player.getZ();
        boolean onGround = PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer() ? Night.POSITION_MANAGER.isServerOnGround() : RotationManager.mc.player.onGround();
        ProxyServerTickListener.allowSend(() -> mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.Rot(yaw, pitch, onGround, RotationManager.mc.player.horizontalCollision)));
    }

    public void silentRotate(float[] rotations) {
        this.silentRotate(rotations[0], rotations[1]);
    }

    public void beginBatchRotation() {
        this.batchingRotations = true;
        this.batchHasPending = false;
    }

    public void batchRotate(float yawIn, float pitchIn) {
        this.batchYaw = yawIn;
        this.batchPitch = pitchIn;
        this.batchHasPending = true;
        float[] jittered = this.applyJitter(yawIn, pitchIn);
        this.serverYaw = jittered[0];
        this.serverPitch = jittered[1];
        this.silentSyncRequired = true;
    }

    public void endBatchRotation() {
        this.batchingRotations = false;
        if (!this.batchHasPending) {
            return;
        }
        this.batchHasPending = false;
        this.silentRotate(this.batchYaw, this.batchPitch);
    }

    public void silentRotate(float yawIn, float pitchIn) {
        boolean onGround;
        if (this.batchingRotations) {
            this.batchRotate(yawIn, pitchIn);
            return;
        }
        boolean proxy = PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer();
        double x = proxy ? Night.POSITION_MANAGER.getServerX() : RotationManager.mc.player.getX();
        double y = proxy ? Night.POSITION_MANAGER.getServerY() : RotationManager.mc.player.getY();
        double z = proxy ? Night.POSITION_MANAGER.getServerZ() : RotationManager.mc.player.getZ();
        boolean bl = onGround = proxy ? Night.POSITION_MANAGER.isServerOnGround() : RotationManager.mc.player.onGround();
        if (this.lastSilentTargetYaw == yawIn && this.lastSilentTargetPitch == pitchIn && this.lastSilentX == x && this.lastSilentY == y && this.lastSilentZ == z) {
            return;
        }
        this.lastSilentTargetYaw = yawIn;
        this.lastSilentTargetPitch = pitchIn;
        this.lastSilentX = x;
        this.lastSilentY = y;
        this.lastSilentZ = z;
        float[] jittered = this.applyJitter(yawIn, pitchIn);
        float yaw = jittered[0];
        float pitch = jittered[1];
        this.lastPacketRotateTime = System.currentTimeMillis();
        this.silentSyncRequired = true;
        this.serverYaw = yaw;
        this.serverPitch = pitch;
        ProxyServerTickListener.allowSend(() -> mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.PosRot(x, y, z, yaw, pitch, onGround, RotationManager.mc.player.horizontalCollision)));
    }

    public void resetSilentRotation() {
        float playerPitch;
        if (RotationManager.mc.player == null) {
            return;
        }
        float playerYaw = this.rotation != null ? this.rotation.getYaw() : RotationManager.mc.player.getYRot();
        float f = playerPitch = this.rotation != null ? this.rotation.getPitch() : RotationManager.mc.player.getXRot();
        if (this.serverYaw == playerYaw && this.serverPitch == playerPitch) {
            return;
        }
        this.silentRotate(playerYaw, playerPitch);
    }

    public void packetRotateRot(float[] rotations) {
        this.packetRotateRot(rotations[0], rotations[1]);
    }

    public void packetRotateRot(float yawIn, float pitchIn) {
        if (this.lastPacketTargetYaw == yawIn && this.lastPacketTargetPitch == pitchIn) {
            return;
        }
        this.lastPacketTargetYaw = yawIn;
        this.lastPacketTargetPitch = pitchIn;
        float[] jittered = this.applyJitter(yawIn, pitchIn);
        float yaw = jittered[0];
        float pitch = jittered[1];
        this.lastPacketRotateTime = System.currentTimeMillis();
        this.serverYaw = yaw;
        this.serverPitch = pitch;
        boolean onGround = PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer() ? Night.POSITION_MANAGER.isServerOnGround() : RotationManager.mc.player.onGround();
        ProxyServerTickListener.allowSend(() -> mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.Rot(yaw, pitch, onGround, RotationManager.mc.player.horizontalCollision)));
    }

    public void wireRotate(String mode, float[] rotations) {
        if ("Silent".equalsIgnoreCase(mode)) {
            this.silentRotate(rotations);
        } else {
            this.packetRotate(rotations);
        }
    }

    public boolean inRenderTime() {
        return System.currentTimeMillis() - this.lastRenderTime < 1000L;
    }

    public float[] getRenderRotations() {
        float from = MathUtils.wrapAngle(this.prevRenderYaw);
        float to = MathUtils.wrapAngle(this.rotation == null ? RotationManager.mc.player.getYRot() : this.getServerYaw());
        float delta = to - from;
        if (delta > 180.0f) {
            delta -= 380.0f;
        } else if (delta < -180.0f) {
            delta += 360.0f;
        }
        float yaw = Mth.lerp((float)Easing.toDelta(this.lastRenderTime, 1000), (float)from, (float)(from + delta));
        float pitch = Mth.lerp((float)Easing.toDelta(this.lastRenderTime, 1000), (float)this.prevRenderPitch, (float)(this.rotation == null ? RotationManager.mc.player.getXRot() : this.getServerPitch()));
        this.prevRenderYaw = yaw;
        this.prevRenderPitch = pitch;
        return new float[]{yaw, pitch};
    }

    @Generated
    public Rotation getRotation() {
        return this.rotation;
    }

    @Generated
    public Module getRotationOwner() {
        return this.rotationOwner;
    }

    @Generated
    public float getServerYaw() {
        return this.serverYaw;
    }

    @Generated
    public float getServerPitch() {
        return this.serverPitch;
    }

    @Generated
    public boolean isBatchingRotations() {
        return this.batchingRotations;
    }

    @Generated
    public boolean isSilentSyncRequired() {
        return this.silentSyncRequired;
    }

    @Generated
    public void setSilentSyncRequired(boolean silentSyncRequired) {
        this.silentSyncRequired = silentSyncRequired;
    }

    static {
        LEGACY_PRIORITIES.put("KillAura", 1);
        LEGACY_PRIORITIES.put("AutoCrystal", 2);
        LEGACY_PRIORITIES.put("SpeedMine", 3);
        LEGACY_PRIORITIES.put("Sprint", 4);
        LEGACY_PRIORITIES.put("SelfFill", 5);
    }
}

