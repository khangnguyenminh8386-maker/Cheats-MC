/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  lombok.Generated
 *  net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler
 *  net.minecraft.client.player.RemotePlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Plane
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$PosRot
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Rot
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action
 *  net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.GameType
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.player;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import lombok.Generated;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.AttackBlockEvent;
import night.events.impl.ClientDisconnectEvent;
import night.events.impl.DestroyBlockEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PacketSendEvent;
import night.events.impl.PlayerUpdateEvent;
import night.events.impl.RenderWorldEvent;
import night.mixins.accessors.ClientWorldAccessor;
import night.mixins.accessors.PlayerMoveC2SPacketAccessor;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.combat.AutoCrystalModule;
import night.modules.impl.combat.AutoMineModule;
import night.modules.impl.combat.AutoTrapModule;
import night.modules.impl.visuals.LogoutSpotModule;
import night.modules.impl.visuals.PopChamsModule;
import night.pingbypass.PingBypassFlags;
import night.pingbypass.protocol.PbCustomPayload;
import night.pingbypass.protocol.packets.S2CMiningStatePacket;
import night.pingbypass.server.ProxyServerTickListener;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.settings.impl.WhitelistSetting;
import night.utils.IMinecraft;
import night.utils.color.ColorUtils;
import night.utils.graphics.Renderer3D;
import night.utils.minecraft.EntityUtils;
import night.utils.minecraft.HoleUtils;
import night.utils.minecraft.InventoryUtils;
import night.utils.minecraft.NetworkUtils;
import night.utils.minecraft.WorldUtils;
import night.utils.rotations.RotationUtils;
import night.utils.system.Timer;

@RegisterModule(name="SpeedMine", description="Automatically mines blocks at a faster speed using packets.", category=Module.Category.PLAYER)
public class SpeedMineModule
extends Module {
    public ModeSetting switchMode = new ModeSetting("Switch", "The mode that will be used for automatically switching to the fastest item.", "Silent", InventoryUtils.SWITCH_MODES);
    public NumberSetting range = new NumberSetting("Range", "The maximum distance at which blocks will be mined.", 6.0, 0.0, 8.0);
    public NumberSetting speed = new NumberSetting("Speed", "The speed at which the module will mine blocks.", 1.0, 0.7, 1.0);
    public BooleanSetting farReach = new BooleanSetting("FarReach", "Bypass for Duration 0.7 on Grim. Try this if normal AutoMine doesn't work.", false);
    public BooleanSetting sixB = new BooleanSetting("6b", "Allows mining bedrock after 40 ticks and enables instant-mining when broken.", false);
    public ModeSetting rotate = new ModeSetting("Rotate", "Automatically rotates to the block when mining it.", "Normal", new String[]{"None", "Normal", "Silent"});
    public BooleanSetting switchReset = new BooleanSetting("SwitchReset", "Resets the mining when switching slots.", new ModeSetting.Visibility(this.switchMode, "None", "AltSwap", "AltPickup"), true);
    public BooleanSetting doubleMine = new BooleanSetting("Double", "Allows the mining of 2 blocks at the same time.", false);
    public BooleanSetting shift = new BooleanSetting("Shift", "Selects the block behind according to crosshair as secondary when clicking while holding shift.", new BooleanSetting.Visibility(this.doubleMine, true), true);
    public ModeSetting await = new ModeSetting("Await", "Wait for blocks to break before swapping back. Off: don't wait. Dynamic: wait only while the secondary isn't close to done yet. Always: always wait. Works around real NCP's FastBreak check on some Paper servers (cancels doubleMine's secondary otherwise) -- costs mining throughput while waiting.", new BooleanSetting.Visibility(this.doubleMine, true), "Off", new String[]{"Off", "Dynamic", "Always"});
    public ModeSetting rebreak = new ModeSetting("Rebreak", "Automatically re-mines blocks once they have been replaced.", "None", new String[]{"None", "Fast", "Instant"});
    public NumberSetting instantDelay = new NumberSetting("InstantDelay", "The amount of time that has to pass before instantly mining blocks.", new ModeSetting.Visibility(this.rebreak, "Fast", "Instant"), (Number)0, (Number)0, (Number)20);
    public NumberSetting instantTimeout = new NumberSetting("InstantTimeout", "The amount of time that cancel instantly mine while no block to mine.", new ModeSetting.Visibility(this.rebreak, "Fast", "Instant"), (Number)60, (Number)0, (Number)100);
    public BooleanSetting async = new BooleanSetting("Async", "Keeps instantly re-firing even while the target position is currently air, instead of waiting for it to solidify.", new ModeSetting.Visibility(this.rebreak, "Fast", "Instant"), false);
    public CategorySetting onGroundCheckCategory = new CategorySetting("OnGroundCheck", "Claims onGround on outgoing movement packets while mining, bypassing the not-on-the-ground mining speed penalty.");
    public BooleanSetting ncpCheck = new BooleanSetting("NCP", "Enables the onGround claim below. Turn off for servers that don't penalize air/liquid mining.", new CategorySetting.Visibility(this.onGroundCheckCategory), false);
    public BooleanSetting airCheck = new BooleanSetting("AirCheck", "Claims onGround while airborne and mining.", new CategorySetting.Visibility(this.onGroundCheckCategory), false);
    public BooleanSetting liquidCheck = new BooleanSetting("LiquidCheck", "Claims onGround while in liquid and mining.", new CategorySetting.Visibility(this.onGroundCheckCategory), false);
    public BooleanSetting grim = new BooleanSetting("Grim", "Adds a bypass catered to the Grim anticheat.", false);
    public BooleanSetting whileEating = new BooleanSetting("WhileEating", "Mines blocks while eating.", true);
    public ModeSetting whitelistMode = new ModeSetting("Mode", "All = mine every block. WhiteList = mine only listed blocks. BlackList = mine every block except listed.", "All", new String[]{"All", "WhiteList", "BlackList"});
    public WhitelistSetting whitelist = new WhitelistSetting("List", "Blocks the WhiteList/BlackList mode compares against.", WhitelistSetting.Type.BLOCKS);
    public CategorySetting renderCategory = new CategorySetting("Render", "The category containing all settings related to rendering.");
    public ModeSetting render = new ModeSetting("Render", "Mode", "The rendering that will be applied to the blocks highlighted.", new CategorySetting.Visibility(this.renderCategory), "Both", new String[]{"None", "Fill", "Outline", "Both"});
    public ModeSetting animation = new ModeSetting("Animation", "The animation that will be used when rendering the block mining progress.", new ModeSetting.Visibility(this.render, "Fill", "Outline", "Both"), "Expand", new String[]{"None", "Expand", "Rise"});
    public ModeSetting color = new ModeSetting("Color", "The color that will be used when rendering the block mining.", new ModeSetting.Visibility(this.render, "Fill", "Outline", "Both"), "Smooth", new String[]{"Static", "Smooth", "Custom"});
    public ColorSetting fillColor = new ColorSetting("FillColor", "The color used for the fill rendering.", new ModeSetting.Visibility(this.render, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "The color used for the outline rendering.", new ModeSetting.Visibility(this.render, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    public ModeSetting instantRender = new ModeSetting("InstantRender", "Instant", "The color that will be used for rendering instantly mined blocks.", new CategorySetting.Visibility(this.renderCategory), "None", new String[]{"None", "Default", "Custom"});
    public ColorSetting instantColor = new ColorSetting("InstantColor", "The custom color used for instantly mined blocks.", new ModeSetting.Visibility(this.instantRender, "Custom"), new ColorSetting.Color(new Color(148, 0, 211), false, false));
    private Action primary = null;
    private Secondary secondary = null;
    private static final int SECONDARY_TIMEOUT = 10;
    private int secondaryHoldSlot = -1;
    private int secondaryOriginalSlot = -1;
    private static final int SECONDARY_MAX_TICKS = 60;
    private boolean doubleEngaged = false;
    private boolean primaryPaired = false;
    private boolean handlingSwitchReset = false;
    private final Timer instantTimer = new Timer();
    private static final long INSTANT_ACK_STALL_MS = 200L;
    private final Timer mineTimer = new Timer();
    private double delayBalance = 0.0;
    private long lastStopMs = 0L;
    private static final int STOP_COOLDOWN_TICKS = 6;
    private int stopCooldown = 0;
    private volatile boolean interactPaused = false;
    private volatile long interactPausedAt = 0L;
    private boolean needsRestart = false;
    public final Object interactSyncLock = new Object();
    private static final long INTERACT_PAUSE_TIMEOUT_MS = 750L;
    public volatile BlockPos proxyPrimaryPos = null;
    public volatile float proxyPrimaryProgress = 0.0f;
    public volatile BlockPos proxySecondaryPos = null;
    public volatile float proxySecondaryProgress = 0.0f;
    private volatile float prevProxyPrimaryProgress = 0.0f;
    private volatile long proxyPrimaryUpdateTime = 0L;
    private volatile long proxyPrimaryUpdateInterval = 50L;
    private volatile float prevProxySecondaryProgress = 0.0f;
    private volatile long proxySecondaryUpdateTime = 0L;
    private volatile long proxySecondaryUpdateInterval = 50L;
    private static final int GRIM_DECOY_Y_OFFSET = 2000;

    public Action getPrimary() {
        return this.primary;
    }

    public Secondary getSecondary() {
        return this.secondary;
    }

    public void setDoubleEngaged(boolean engaged) {
        this.doubleEngaged = engaged;
    }

    private boolean hasSecondarySlot() {
        return this.secondary != null;
    }

    private boolean canStartNow() {
        if (!this.whileEating.getValue() && (this.interactPaused || SpeedMineModule.mc.player != null && (SpeedMineModule.mc.player.isUsingItem() || EntityUtils.isEating()))) {
            return false;
        }
        if (!this.farReach.getValue()) {
            return true;
        }
        return this.stopCooldown == 0 && this.canBegin();
    }

    private boolean canBegin() {
        long delay = System.currentTimeMillis() - this.lastStopMs;
        if (delay >= 275L) {
            return true;
        }
        double cost = (300L - delay) * (long)(this.farReach.getValue() ? 2 : 1);
        return this.delayBalance + cost <= 900.0;
    }

    private void trackStarts(int starts) {
        long delay = System.currentTimeMillis() - this.lastStopMs;
        for (int i = 0; i < starts; ++i) {
            if (delay >= 275L) {
                this.delayBalance *= 0.9;
                continue;
            }
            this.delayBalance += (double)(300L - delay);
        }
        this.delayBalance = Mth.clamp((double)this.delayBalance, (double)-1000.0, (double)1000.0);
    }

    private void markStop() {
        this.markStop(false);
    }

    private void markStop(boolean cooldown) {
        this.lastStopMs = System.currentTimeMillis();
        if (cooldown) {
            this.stopCooldown = 6;
        }
    }

    public BlockPos getMiningPosition() {
        return this.primary != null && this.primary.isMining() ? this.primary.getPosition() : null;
    }

    public boolean isInteractPaused() {
        return this.interactPaused;
    }

    public void setInteractPaused(boolean paused) {
        this.interactPaused = paused;
        if (paused) {
            this.interactPausedAt = System.currentTimeMillis();
        } else {
            this.needsRestart = true;
        }
    }

    public void updateProxyMiningState(BlockPos primaryPos, float primaryProgress, BlockPos secondaryPos, float secondaryProgress) {
        long now = System.currentTimeMillis();
        boolean primaryPosChanged = primaryPos == null ? this.proxyPrimaryPos != null : !primaryPos.equals((Object)this.proxyPrimaryPos);
        this.prevProxyPrimaryProgress = primaryPosChanged ? primaryProgress : this.proxyPrimaryProgress;
        this.proxyPrimaryUpdateInterval = Mth.clamp((long)(now - this.proxyPrimaryUpdateTime), (long)1L, (long)500L);
        this.proxyPrimaryUpdateTime = now;
        this.proxyPrimaryPos = primaryPos;
        this.proxyPrimaryProgress = primaryProgress;
        boolean secondaryPosChanged = secondaryPos == null ? this.proxySecondaryPos != null : !secondaryPos.equals((Object)this.proxySecondaryPos);
        this.prevProxySecondaryProgress = secondaryPosChanged ? secondaryProgress : this.proxySecondaryProgress;
        this.proxySecondaryUpdateInterval = Mth.clamp((long)(now - this.proxySecondaryUpdateTime), (long)1L, (long)500L);
        this.proxySecondaryUpdateTime = now;
        this.proxySecondaryPos = secondaryPos;
        this.proxySecondaryProgress = secondaryProgress;
    }

    private float interpolatedProgress(float prev, float current, long updateTime, long interval) {
        float t = Mth.clamp((float)((float)(System.currentTimeMillis() - updateTime) / (float)interval), (float)0.0f, (float)1.0f);
        return prev + (current - prev) * t;
    }

    public boolean handle(BlockPos position, int priority) {
        boolean dual;
        if (!this.canHandle(position)) {
            return false;
        }
        if (this.primary != null && this.primary.getPosition().equals((Object)position) || this.secondary != null && this.secondary.getPosition().equals((Object)position)) {
            return true;
        }
        if (this.primary != null && priority <= 0 && this.sixB.getValue() && this.primary.isBedrockOrigin() && priority <= this.primary.getPriority()) {
            return false;
        }
        boolean bl = dual = this.doubleMine.getValue() && (this.doubleEngaged || priority > 0);
        if (dual) {
            Secondary demoted;
            if (this.secondary != null) {
                if (this.await.getValue().equalsIgnoreCase("Always")) {
                    return false;
                }
                if (this.await.getValue().equalsIgnoreCase("Dynamic") && !this.secondary.isHolding()) {
                    return false;
                }
            }
            Secondary secondary = demoted = this.secondary == null && this.primary != null ? this.primary.demote() : null;
            if (demoted != null) {
                this.secondary = demoted;
            } else if (this.primary != null) {
                this.primary.cancel();
            }
            this.primary = new Action(this, position, priority);
            this.primaryPaired = this.hasSecondarySlot();
        } else {
            if (this.primary != null) {
                this.primary.cancel();
            }
            this.primary = new Action(this, position, priority);
            this.primaryPaired = false;
        }
        return true;
    }

    public boolean fillSecondary(BlockPos position, int priority) {
        if (this.primary != null && this.primary.isTerrainBase()) {
            return false;
        }
        if (!this.canHandle(position)) {
            return false;
        }
        if (this.sixB.getValue() && SpeedMineModule.mc.level.getBlockState(position).getBlock() == Blocks.BEDROCK) {
            return false;
        }
        if (this.secondary != null) {
            return this.secondary.getPosition().equals((Object)position);
        }
        if (this.primary != null && this.primary.getPosition().equals((Object)position)) {
            return true;
        }
        Action fresh = new Action(this, position, priority);
        Secondary demoted = fresh.demote();
        if (demoted == null) {
            return false;
        }
        this.secondary = demoted;
        return true;
    }

    public boolean isMineTimerReady() {
        return this.mineTimer.hasTimeElapsed(50L);
    }

    public void dropPrimaryIfInvalid(Predicate<BlockPos> stillValid) {
        if (this.primary == null || this.primary.getPriority() != 0) {
            return;
        }
        if (!stillValid.test(this.primary.getPosition())) {
            this.dropPrimary();
        }
    }

    public void dropSecondaryIfInvalid(Predicate<BlockPos> stillValid) {
        if (this.secondary == null || this.secondary.getPriority() != 0) {
            return;
        }
        if (!stillValid.test(this.secondary.getPosition())) {
            this.dropSecondary();
        }
    }

    public void dropPrimary() {
        if (this.primary == null) {
            return;
        }
        this.primary.cancel();
        this.primary = null;
        this.primaryPaired = false;
    }

    public void dropSecondary() {
        if (this.secondary == null) {
            return;
        }
        this.secondary.release();
        this.secondary = null;
    }

    public boolean primaryDigging() {
        return this.primary != null && !SpeedMineModule.mc.level.getBlockState(this.primary.getPosition()).canBeReplaced();
    }

    public boolean primaryCamping() {
        if (this.primary == null || !this.primary.isInstantMine()) {
            return false;
        }
        if (!SpeedMineModule.mc.level.getBlockState(this.primary.getPosition()).canBeReplaced() && !this.sixB.getValue()) {
            return false;
        }
        Target t = this.getTarget();
        return t != null && this.isTargetSurroundPosition(this.primary.getPosition(), t.player());
    }

    public boolean slotsFull() {
        if (this.primary != null && this.sixB.getValue() && this.primary.isBedrockOrigin()) {
            return true;
        }
        return !(!this.primaryDigging() && !this.primaryCamping() || this.doubleEngaged && !this.hasSecondarySlot() && !this.primaryPaired);
    }

    public boolean phaseSlotsFull() {
        return this.primaryDigging() && (!this.doubleEngaged || this.hasSecondarySlot() || this.primaryPaired);
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        AutoMineModule autoMine;
        if (this.isDeferringToProxy()) {
            return;
        }
        if (SpeedMineModule.mc.player == null || SpeedMineModule.mc.level == null) {
            return;
        }
        if (this.stopCooldown > 0) {
            --this.stopCooldown;
        }
        if (!this.doubleMine.getValue()) {
            this.doubleEngaged = false;
        }
        if (this.secondary != null && !this.doubleMine.getValue()) {
            this.secondary.release();
            this.secondary = null;
        } else if (this.secondary != null && this.secondary.process()) {
            this.secondary = null;
        }
        if (this.primary != null && this.primary.process()) {
            this.primary = null;
            this.primaryPaired = false;
        }
        if (this.isProxyActive()) {
            this.syncMiningStateToClient();
        }
        AutoMineModule autoMineModule = autoMine = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoMineModule.class) : null;
        if (autoMine != null) {
            autoMine.placePendingTerrain();
        }
        if (this.primary != null && autoMine != null && autoMine.isProtectedTerrainBase(this.primary.getPosition())) {
            this.primary.cancel();
            this.primary = null;
            this.primaryPaired = false;
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (SpeedMineModule.mc.player == null || SpeedMineModule.mc.level == null) {
            return;
        }
        if (this.isDeferringToProxy()) {
            this.renderProxyState(event.getMatrices());
            return;
        }
        if (this.doubleMine.getValue() && this.secondary != null) {
            this.secondary.render(event.getMatrices());
        }
        if (this.primary != null) {
            this.primary.render(event.getMatrices());
        }
    }

    @SubscribeEvent
    public void night$onBlockUpdate(PacketReceiveEvent event) {
        if (SpeedMineModule.mc.player == null || SpeedMineModule.mc.level == null) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (!(packet instanceof ClientboundBlockUpdatePacket)) {
            return;
        }
        ClientboundBlockUpdatePacket blockPacket = (ClientboundBlockUpdatePacket)packet;
        BlockPos pos = blockPacket.getPos();
        if (this.primary != null && pos.equals((Object)this.primary.getPosition())) {
            this.primary.markBroken();
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent.Post event) {
        ServerboundPlayerActionPacket action;
        Packet<?> packet;
        if (this.isDeferringToProxy()) {
            return;
        }
        if (SpeedMineModule.mc.player == null || SpeedMineModule.mc.level == null) {
            return;
        }
        if (this.handlingSwitchReset) {
            return;
        }
        if (event.getPacket() instanceof ServerboundSetCarriedItemPacket && this.switchReset.getValue() && (this.switchMode.getValue().equalsIgnoreCase("AltSwap") || this.switchMode.getValue().equalsIgnoreCase("AltPickup"))) {
            this.handlingSwitchReset = true;
            try {
                if (this.primary != null) {
                    this.primary.cancel();
                    this.primary.tryStart();
                }
            }
            finally {
                this.handlingSwitchReset = false;
            }
        }
        if ((packet = event.getPacket()) instanceof ServerboundPlayerActionPacket && (action = (ServerboundPlayerActionPacket)packet).getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK && action.getPos().getY() > 2000) {
            return;
        }
        packet = event.getPacket();
        if (packet instanceof ServerboundMovePlayerPacket) {
            ServerboundMovePlayerPacket movePacket = (ServerboundMovePlayerPacket)packet;
            if (this.ncpCheck.getValue() && this.primary != null) {
                boolean liquid;
                boolean airborne = this.airCheck.getValue() && !SpeedMineModule.mc.player.onGround() && !SpeedMineModule.mc.player.isInWater();
                boolean bl = liquid = this.liquidCheck.getValue() && SpeedMineModule.mc.player.isInWater();
                if (airborne || liquid) {
                    ((PlayerMoveC2SPacketAccessor)movePacket).setOnGround(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onAttackBlock(AttackBlockEvent event) {
        if (this.isDeferringToProxy()) {
            return;
        }
        if (SpeedMineModule.mc.player == null || SpeedMineModule.mc.level == null) {
            return;
        }
        BlockPos position = event.getPosition();
        if (this.doubleMine.getValue() && this.shift.getValue() && (SpeedMineModule.mc.options.keyShift.isDown() || SpeedMineModule.mc.player.isShiftKeyDown())) {
            Vec3 look = SpeedMineModule.mc.player.getLookAngle();
            double absX = Math.abs(look.x);
            double absY = Math.abs(look.y);
            double absZ = Math.abs(look.z);
            Direction direction = absY > absX && absY > absZ ? (look.y > 0.0 ? Direction.UP : Direction.DOWN) : (absX > absZ ? (look.x > 0.0 ? Direction.EAST : Direction.WEST) : (look.z > 0.0 ? Direction.SOUTH : Direction.NORTH));
            BlockPos behind = position.relative(direction);
            if (this.isValid(behind) && !this.isOutOfRange(behind) && !SpeedMineModule.mc.level.getBlockState(behind).canBeReplaced()) {
                this.handle(behind, 1);
            }
        }
        if (this.handle(position, 1)) {
            event.setCancelled(true);
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientDisconnectEvent event) {
        this.primary = null;
        this.secondary = null;
        this.doubleEngaged = false;
        this.primaryPaired = false;
    }

    @Override
    public void onEnable() {
        this.doubleEngaged = false;
        this.primaryPaired = false;
    }

    @Override
    public void onDisable() {
        this.doubleEngaged = false;
        this.primaryPaired = false;
        if (this.isDeferringToProxy()) {
            return;
        }
        this.restoreIronSwap();
        if (SpeedMineModule.mc.player == null || SpeedMineModule.mc.level == null) {
            this.primary = null;
            this.secondary = null;
            return;
        }
        if (this.secondary != null) {
            this.secondary.release();
        }
        this.secondary = null;
        if (this.primary != null) {
            this.primary.cancel();
            this.primary = null;
        }
        this.setInteractPaused(false);
    }

    public void restoreIronSwap() {
        int hotbarIron;
        if (this.primary != null) {
            this.primary.restoreIronSwap();
        }
        if (SpeedMineModule.mc.player != null && (hotbarIron = InventoryUtils.find(Items.IRON_PICKAXE, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END)) != -1) {
            int invSlot = InventoryUtils.findFastestItem(Blocks.OBSIDIAN.defaultBlockState(), InventoryUtils.HOTBAR_END + 1, InventoryUtils.INVENTORY_END);
            if (invSlot == -1) {
                invSlot = InventoryUtils.findEmptySlot(InventoryUtils.HOTBAR_END + 1, InventoryUtils.INVENTORY_END);
            }
            if (invSlot != -1 && invSlot > InventoryUtils.HOTBAR_END) {
                InventoryUtils.swap("Swap", invSlot, hotbarIron);
            }
        }
    }

    @Override
    public String getMetaData() {
        String primaryProgress = this.primary == null ? "0.0" : new DecimalFormat("0.0").format(this.primary.getProgress() / this.primary.getSpeed());
        String secondaryProgress = this.secondary == null || !this.doubleMine.getValue() ? "" : ", " + new DecimalFormat("0.0").format(this.secondary.getProgress() / this.secondary.getSpeed());
        return primaryProgress + secondaryProgress;
    }

    private boolean canHandle(BlockPos position) {
        boolean allowedByList;
        AutoMineModule autoMine;
        if (SpeedMineModule.mc.gameMode.getPlayerMode() == GameType.CREATIVE || SpeedMineModule.mc.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            return false;
        }
        if (!(SpeedMineModule.mc.level.getBlockState(position).getBlock().defaultDestroyTime() != -1.0f || this.sixB.getValue() && SpeedMineModule.mc.level.getBlockState(position).getBlock() == Blocks.BEDROCK)) {
            return false;
        }
        AutoMineModule autoMineModule = autoMine = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoMineModule.class) : null;
        if (autoMine != null && autoMine.isProtectedTerrainBase(position)) {
            return false;
        }
        boolean listed = this.whitelist.isWhitelistContains(SpeedMineModule.mc.level.getBlockState(position).getBlock());
        allowedByList = switch (this.whitelistMode.getValue()) {
            case "WhiteList" -> listed;
            case "BlackList" -> !listed;
            default -> true;
        };
        if (!allowedByList) {
            return false;
        }
        return !(SpeedMineModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)position)) > Mth.square((double)this.range.getValue().doubleValue()));
    }

    public boolean isInvalid(BlockPos position) {
        if (!this.isValid(position)) {
            return true;
        }
        return this.isMining(position);
    }

    public boolean isValid(BlockPos position) {
        if (position == null) {
            return false;
        }
        if (!(SpeedMineModule.mc.level.getBlockState(position).getBlock().defaultDestroyTime() != -1.0f || this.sixB.getValue() && SpeedMineModule.mc.level.getBlockState(position).getBlock() == Blocks.BEDROCK)) {
            return false;
        }
        return !SpeedMineModule.mc.level.getBlockState(position).getBlock().equals(Blocks.COBWEB);
    }

    public boolean isMining(BlockPos position) {
        if (position == null) {
            return true;
        }
        if (this.primary != null && this.primary.getPosition().equals((Object)position)) {
            return true;
        }
        return this.secondary != null && this.secondary.getPosition().equals((Object)position);
    }

    public boolean isTargetSurroundPosition(BlockPos position, Player target) {
        int yLegs;
        if (position == null || target == null || SpeedMineModule.mc.level == null) {
            return false;
        }
        AABB box = target.getBoundingBox();
        for (int y = yLegs = Mth.floor((double)target.getY()); y <= yLegs + 2; ++y) {
            for (int x = Mth.floor((double)box.minX); x < Mth.ceil((double)box.maxX); ++x) {
                for (int z = Mth.floor((double)box.minZ); z < Mth.ceil((double)box.maxZ); ++z) {
                    BlockPos base = new BlockPos(x, y, z);
                    if (position.equals((Object)base)) {
                        return true;
                    }
                    if (y > yLegs + 1) continue;
                    for (Direction dir : Direction.Plane.HORIZONTAL) {
                        if (!position.equals((Object)base.relative(dir))) continue;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isOutOfRange(BlockPos position) {
        if (position == null) {
            return true;
        }
        return SpeedMineModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)position)) > Mth.square((double)this.range.getValue().doubleValue());
    }

    public Target getTarget() {
        LogoutSpotModule logoutSpot = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(LogoutSpotModule.class) : null;
        PopChamsModule popChams = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(PopChamsModule.class) : null;
        ArrayList<Player> allCandidates = new ArrayList<Player>(SpeedMineModule.mc.level.players());
        if (logoutSpot != null && logoutSpot.isToggled()) {
            for (Player player : logoutSpot.getGhosts()) {
                if (player == null || allCandidates.contains(player)) continue;
                allCandidates.add(player);
            }
        }
        Target optimalTarget = null;
        for (Player player : allCandidates) {
            LogoutSpotModule.Spot spot;
            boolean ghost;
            if (player == SpeedMineModule.mc.player || popChams != null && popChams.isGhost((Entity)player) || !(ghost = EntityUtils.isGhost((Entity)player)) && (!player.isAlive() || player.getHealth() <= 0.0f) || SpeedMineModule.mc.player.distanceToSqr((Entity)player) > Mth.square((double)(this.range.getValue().doubleValue() + 2.0)) || (logoutSpot != null && logoutSpot.isGhost((Entity)player) ? (spot = logoutSpot.getSpot((RemotePlayer)player)) != null && Night.FRIEND_MANAGER.contains(spot.data.name) : Night.FRIEND_MANAGER.contains(player.getName().getString()))) continue;
            List<Position> feetPositions = this.getPositions(player);
            BlockPos position = this.getTargetPosition(feetPositions);
            if (!this.doubleMine.getValue() && (feetPositions.isEmpty() || position == null)) continue;
            if (optimalTarget == null) {
                optimalTarget = new Target(player, feetPositions, position);
                continue;
            }
            boolean bestIsGhost = EntityUtils.isGhost((Entity)optimalTarget.player());
            if (bestIsGhost != ghost) {
                if (!bestIsGhost) continue;
                optimalTarget = new Target(player, feetPositions, position);
                continue;
            }
            if (!(SpeedMineModule.mc.player.distanceToSqr((Entity)player) < SpeedMineModule.mc.player.distanceToSqr((Entity)optimalTarget.player()))) continue;
            optimalTarget = new Target(player, feetPositions, position);
        }
        return optimalTarget;
    }

    private BlockPos getTargetPosition(List<Position> positions) {
        BlockPos optimalPosition = null;
        double optimalScore = 0.0;
        for (Position position : positions) {
            if (this.doubleMine.getValue() && !position.feetPosition() || !this.isValidPosition(position.position()) || HoleUtils.isPlayerInHole((Player)SpeedMineModule.mc.player) && HoleUtils.getFeetPositions((Player)SpeedMineModule.mc.player, true, false, true).contains(position.position())) continue;
            double score = 0.0;
            if (this.sixB.getValue() && SpeedMineModule.mc.level.getBlockState(position.position()).getBlock() == Blocks.BEDROCK) {
                score = position.feetPosition() ? (score += 1000.0) : (score -= 1000.0);
            }
            if (position.feetPosition()) {
                score += 5.0;
                if (SpeedMineModule.mc.level.getBlockState(position.position()).getBlock() == Blocks.ENDER_CHEST) {
                    score += 0.95;
                } else if (WorldUtils.isCrystalPlaceable(position.position().offset(0, 1, 0))) {
                    score += 0.35;
                }
                if (this.hasCityPosition(position.position())) {
                    score += 0.6;
                }
            } else {
                score = SpeedMineModule.mc.level.getBlockState(position.position()).getBlock() == Blocks.ENDER_CHEST ? (score -= 2.0) : (WorldUtils.isCrystalPlaceable(position.position().offset(0, 1, 0)) ? (score += 0.75) : (score -= 2.0));
            }
            if (!(score >= optimalScore)) continue;
            optimalPosition = position.position();
            optimalScore = score;
        }
        return optimalPosition;
    }

    private List<Position> getPositions(Player player) {
        ArrayList<Position> positions = new ArrayList<Position>();
        for (BlockPos position : HoleUtils.getFeetPositions(player, true, false, true)) {
            positions.add(new Position(position, true));
            if (this.doubleMine.getValue()) continue;
            positions.add(new Position(position.offset(0, 1, 0), false));
        }
        if (!this.doubleMine.getValue()) {
            positions.add(new Position(player.blockPosition().offset(0, 2, 0), false));
        }
        return positions;
    }

    private boolean isValidPosition(BlockPos position) {
        if (SpeedMineModule.mc.level.getBlockState(position).canBeReplaced()) {
            return false;
        }
        if (!(SpeedMineModule.mc.level.getBlockState(position).getBlock().defaultDestroyTime() != -1.0f || this.sixB.getValue() && SpeedMineModule.mc.level.getBlockState(position).getBlock() == Blocks.BEDROCK)) {
            return false;
        }
        return !this.isOutOfRange(position);
    }

    private boolean hasCityPosition(BlockPos position) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos offsetPosition = position.relative(dir);
            if (!WorldUtils.isPlaceable(offsetPosition)) continue;
            return true;
        }
        return false;
    }

    private boolean isProxyActive() {
        return PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer() && Night.PROXY_SERVER != null;
    }

    private boolean isDeferringToProxy() {
        return PingBypassFlags.isPingBypassActive();
    }

    private void syncMiningStateToClient() {
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(new S2CMiningStatePacket(this.primary != null ? this.primary.getPosition() : null, this.primary != null ? this.primary.getProgress() / this.primary.getSpeed() : 0.0f, this.secondary != null ? this.secondary.getPosition() : null, this.secondary != null ? this.secondary.getProgress() / this.secondary.getSpeed() : 0.0f)));
        for (Connection conn : Night.PROXY_SERVER.getConnections()) {
            if (!conn.isConnected()) continue;
            conn.send((Packet)packet);
        }
    }

    private void renderProxyState(PoseStack matrices) {
        float progress;
        if (this.proxyPrimaryPos != null) {
            progress = this.interpolatedProgress(this.prevProxyPrimaryProgress, this.proxyPrimaryProgress, this.proxyPrimaryUpdateTime, this.proxyPrimaryUpdateInterval);
            this.renderProxyBlock(matrices, this.proxyPrimaryPos, progress);
        }
        if (this.proxySecondaryPos != null && this.doubleMine.getValue()) {
            progress = this.interpolatedProgress(this.prevProxySecondaryProgress, this.proxySecondaryProgress, this.proxySecondaryUpdateTime, this.proxySecondaryUpdateInterval);
            this.renderProxyBlock(matrices, this.proxySecondaryPos, progress);
        }
    }

    private void renderProxyBlock(PoseStack matrices, BlockPos pos, float progress) {
        if (SpeedMineModule.mc.level.getBlockState(pos).canBeReplaced()) {
            return;
        }
        AABB box = new AABB(pos);
        if (this.animation.getValue().equalsIgnoreCase("Expand")) {
            box = new AABB(pos).deflate(0.5).inflate(Mth.clamp((double)((double)progress / 2.0), (double)0.0, (double)0.5));
        }
        if (this.animation.getValue().equalsIgnoreCase("Rise")) {
            box = new AABB((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)pos.getX() + 1.0, (double)((float)pos.getY() + progress), (double)pos.getZ() + 1.0);
        }
        Color fill = this.fillColor.getColor();
        Color outline = this.outlineColor.getColor();
        if (this.color.getValue().equalsIgnoreCase("Static")) {
            fill = (double)progress >= 0.9 ? new Color(0, 255, 0, this.fillColor.getAlpha()) : new Color(255, 0, 0, this.fillColor.getAlpha());
            outline = (double)progress >= 0.9 ? new Color(0, 255, 0, this.outlineColor.getAlpha()) : new Color(255, 0, 0, this.outlineColor.getAlpha());
        } else if (this.color.getValue().equalsIgnoreCase("Smooth")) {
            fill = new Color(255 - (int)(Mth.clamp((float)progress, (float)0.0f, (float)1.0f) * 255.0f), (int)(Mth.clamp((float)progress, (float)0.0f, (float)1.0f) * 255.0f), 0, this.fillColor.getAlpha());
            outline = new Color(255 - (int)(Mth.clamp((float)progress, (float)0.0f, (float)1.0f) * 255.0f), (int)(Mth.clamp((float)progress, (float)0.0f, (float)1.0f) * 255.0f), 0, this.outlineColor.getAlpha());
        }
        if (this.render.getValue().equalsIgnoreCase("Fill") || this.render.getValue().equalsIgnoreCase("Both")) {
            Renderer3D.renderBox(matrices, box, fill);
        }
        if (this.render.getValue().equalsIgnoreCase("Outline") || this.render.getValue().equalsIgnoreCase("Both")) {
            Renderer3D.renderBoxOutline(matrices, box, outline);
        }
    }

    private void serverSend(Packet<?> packet) {
        Connection serverConn;
        if (this.isProxyActive() && (serverConn = Night.PROXY_SERVER.getServerConnection()) != null && serverConn.isConnected()) {
            ProxyServerTickListener.allowSend(() -> serverConn.send(packet));
            return;
        }
        mc.getConnection().send(packet);
    }

    private <T extends Packet<?>> void serverSendSequenced(IntFunction<T> packetFactory) {
        if (this.isProxyActive()) {
            try (BlockStatePredictionHandler pending = ((ClientWorldAccessor)SpeedMineModule.mc.level).invokeGetPendingUpdateManager().startPredicting();){
                this.serverSend((Packet)packetFactory.apply(pending.currentSequence()));
            }
        } else {
            NetworkUtils.sendSequencedPacket(seq -> {
                Packet p = (Packet)packetFactory.apply(seq);
                return p;
            });
        }
    }

    private void sendRawPlayerAction(ServerboundPlayerActionPacket.Action action, BlockPos target, Direction face) {
        this.sendRawPlayerAction(action, target, face, false);
    }

    private void sendRawPlayerAction(ServerboundPlayerActionPacket.Action action, BlockPos target, Direction face, boolean cooldown) {
        if (action == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
            this.markStop(cooldown);
        }
        try (BlockStatePredictionHandler prediction = ((ClientWorldAccessor)SpeedMineModule.mc.level).invokeGetPendingUpdateManager().startPredicting();){
            this.serverSend((Packet<?>)new ServerboundPlayerActionPacket(action, target, face, prediction.currentSequence()));
        }
    }

    public class Action {
        private final BlockPos position;
        private BlockState state;
        private final int priority;
        private float progress;
        private float prevProgress;
        private int attempts;
        private int ticks;
        private boolean mining;
        private long stallTime;
        private boolean instantMine;
        private int startSlot;
        private boolean started;
        private int brokenCount;
        private int lastBrokenCount;
        private final Timer instantRemineResetTimer;
        private float ghostIronProgress;
        private boolean ironSwapped;
        private int swappedIronInventorySlot;
        private int swappedIronHotbarSlot;
        private boolean terrainBase;
        private boolean terrainSurround;
        private final boolean bedrockOrigin;
        final /* synthetic */ SpeedMineModule this$0;

        public Action(SpeedMineModule this$0, BlockPos position, int priority) {
            SpeedMineModule speedMineModule = this$0;
            Objects.requireNonNull(speedMineModule);
            this.this$0 = speedMineModule;
            this.startSlot = -1;
            this.lastBrokenCount = -1;
            this.instantRemineResetTimer = new Timer();
            this.ghostIronProgress = 0.0f;
            this.ironSwapped = false;
            this.swappedIronInventorySlot = -1;
            this.swappedIronHotbarSlot = -1;
            this.position = position;
            this.state = IMinecraft.mc.level.getBlockState(position);
            this.bedrockOrigin = this.state.getBlock() == Blocks.BEDROCK;
            this.priority = priority;
            this.tryStart();
        }

        public BlockPos getPosition() {
            return this.position;
        }

        public BlockState getState() {
            return this.state;
        }

        public float getProgress() {
            return this.progress;
        }

        public int getTicks() {
            return this.ticks;
        }

        public boolean isMining() {
            return this.mining;
        }

        public boolean isInstantMine() {
            return this.instantMine;
        }

        public int getPriority() {
            return this.priority;
        }

        public boolean isTerrainBase() {
            return this.terrainBase;
        }

        public void setTerrainBase(boolean terrainBase) {
            this.terrainBase = terrainBase;
        }

        public boolean isTerrainSurround() {
            return this.terrainSurround;
        }

        public void setTerrainSurround(boolean terrainSurround) {
            this.terrainSurround = terrainSurround;
        }

        public boolean isStarted() {
            return this.started;
        }

        private int getBreakSlot(BlockState state, int defaultSlot) {
            int ironSlot;
            AutoMineModule autoMine;
            AutoMineModule autoMineModule = autoMine = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoMineModule.class) : null;
            if (autoMine != null && autoMine.isToggled() && autoMine.ironPickaxe.getValue() && this.ironSwapped && (ironSlot = InventoryUtils.find(Items.IRON_PICKAXE, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END)) != -1) {
                return ironSlot;
            }
            return defaultSlot;
        }

        private long asyncFireDelayMs() {
            boolean priorityHead;
            boolean isHeadPosition;
            long baseDelay = this.this$0.instantDelay.getValue().longValue() * 50L;
            if (this.position == null) {
                return baseDelay;
            }
            Target currentTarget = this.this$0.getTarget();
            if (currentTarget == null || currentTarget.player() == null) {
                return baseDelay;
            }
            boolean bl = isHeadPosition = this.position.getY() == Mth.floor((double)currentTarget.player().getY()) + 2 && this.this$0.isTargetSurroundPosition(this.position, currentTarget.player());
            if (!isHeadPosition) {
                return baseDelay;
            }
            AutoTrapModule autoTrap = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoTrapModule.class) : null;
            AutoMineModule autoMine = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoMineModule.class) : null;
            boolean autoTrapActive = autoTrap != null && autoTrap.isToggled();
            boolean bl2 = priorityHead = autoMine != null && autoMine.isToggled() && autoMine.priority.getValue().equalsIgnoreCase("Head");
            if (autoTrapActive && priorityHead) {
                return Math.max(baseDelay, (long)Night.SERVER_MANAGER.getPing());
            }
            return baseDelay;
        }

        private void updateIronGhost() {
            int ironSlot;
            boolean isIronPickMode;
            AutoMineModule autoMine = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoMineModule.class) : null;
            boolean bl = isIronPickMode = autoMine != null && autoMine.isToggled() && autoMine.ironPickaxe.getValue();
            if (isIronPickMode && !this.ironSwapped && (ironSlot = InventoryUtils.find(Items.IRON_PICKAXE, InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END)) != -1 && this.state != null && !this.state.isAir()) {
                float ironDelta = WorldUtils.getMineSpeed(this.state, ironSlot) / Night.WORLD_MANAGER.getTimerMultiplier();
                this.ghostIronProgress = Mth.clamp((float)(this.ghostIronProgress + ironDelta), (float)0.0f, (float)this.getSpeed());
                if (this.ghostIronProgress >= this.getSpeed()) {
                    int fastestHotbarSlot = InventoryUtils.findFastestItem(this.state, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
                    if (fastestHotbarSlot == -1) {
                        fastestHotbarSlot = IMinecraft.mc.player.getInventory().getSelectedSlot();
                    }
                    if (ironSlot > InventoryUtils.HOTBAR_END) {
                        InventoryUtils.swap("Swap", ironSlot, fastestHotbarSlot);
                        this.ironSwapped = true;
                        this.swappedIronInventorySlot = ironSlot;
                        this.swappedIronHotbarSlot = fastestHotbarSlot;
                    } else if (ironSlot != -1) {
                        this.ironSwapped = true;
                        this.swappedIronInventorySlot = -1;
                        this.swappedIronHotbarSlot = ironSlot;
                    }
                }
            }
        }

        public void restoreIronSwap() {
            if (this.ironSwapped) {
                int hotbarIron;
                if (this.swappedIronInventorySlot > InventoryUtils.HOTBAR_END && this.swappedIronHotbarSlot >= 0 && this.swappedIronHotbarSlot <= InventoryUtils.HOTBAR_END) {
                    if (IMinecraft.mc.player != null && IMinecraft.mc.player.getInventory().getItem(this.swappedIronHotbarSlot).is(Items.IRON_PICKAXE)) {
                        InventoryUtils.swap("Swap", this.swappedIronInventorySlot, this.swappedIronHotbarSlot);
                    }
                } else if (IMinecraft.mc.player != null && (hotbarIron = InventoryUtils.find(Items.IRON_PICKAXE, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END)) != -1) {
                    int invSlot;
                    BlockState targetState = this.state != null && !this.state.isAir() ? this.state : Blocks.OBSIDIAN.defaultBlockState();
                    int n = invSlot = this.swappedIronInventorySlot > InventoryUtils.HOTBAR_END ? this.swappedIronInventorySlot : InventoryUtils.findFastestItem(targetState, InventoryUtils.HOTBAR_END + 1, InventoryUtils.INVENTORY_END);
                    if (invSlot == -1) {
                        invSlot = InventoryUtils.findEmptySlot(InventoryUtils.HOTBAR_END + 1, InventoryUtils.INVENTORY_END);
                    }
                    if (invSlot != -1 && invSlot > InventoryUtils.HOTBAR_END) {
                        InventoryUtils.swap("Swap", invSlot, hotbarIron);
                    }
                }
                this.ironSwapped = false;
                this.swappedIronInventorySlot = -1;
                this.swappedIronHotbarSlot = -1;
                this.ghostIronProgress = 0.0f;
            }
        }

        private Secondary demote() {
            this.restoreIronSwap();
            if (!this.started) {
                return null;
            }
            BlockState current = IMinecraft.mc.level.getBlockState(this.position);
            if (current.canBeReplaced()) {
                return null;
            }
            if (this.this$0.sixB.getValue() && current.getBlock() == Blocks.BEDROCK) {
                return null;
            }
            if (!this.instantMine && this.progress < this.getSpeed()) {
                int slot;
                Direction direction = WorldUtils.getClosestDirection(this.position, true);
                slot = this.this$0.switchMode.getValue().equalsIgnoreCase("None") ? -1 : InventoryUtils.findFastestItem(current, InventoryUtils.HOTBAR_START, this.this$0.switchMode.getValue().equalsIgnoreCase("AltSwap") || this.this$0.switchMode.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END);
                if (slot == -1) {
                    slot = IMinecraft.mc.player.getInventory().getSelectedSlot();
                }
                this.fireBreakBurst(direction, slot, true);
            }
            return new Secondary(this.this$0, this.position, this.priority, current, this.progress);
        }

        private void markBroken() {
            ++this.brokenCount;
        }

        private boolean ackStalled() {
            if (this.brokenCount != this.lastBrokenCount) {
                this.instantRemineResetTimer.reset();
                this.lastBrokenCount = this.brokenCount;
            }
            return this.instantRemineResetTimer.hasTimeElapsed(200L);
        }

        private boolean instantAckStalled() {
            return IMinecraft.mc.level.getBlockState(this.position).canBeReplaced() && this.ackStalled();
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void tryStart() {
            Object object = this.this$0.interactSyncLock;
            synchronized (object) {
                if (!this.this$0.canStartNow()) {
                    return;
                }
                this.start();
            }
        }

        public boolean process() {
            boolean clientEating;
            if (this.this$0.isOutOfRange(this.position)) {
                this.cancel();
                return true;
            }
            this.updateIronGhost();
            boolean bl = clientEating = IMinecraft.mc.player != null && (IMinecraft.mc.player.isUsingItem() || EntityUtils.isEating());
            if (this.this$0.interactPaused && !clientEating && System.currentTimeMillis() - this.this$0.interactPausedAt >= 750L) {
                this.this$0.setInteractPaused(false);
            }
            if (this.this$0.needsRestart && this.this$0.isProxyActive()) {
                this.this$0.needsRestart = false;
                this.started = false;
            }
            if (!this.started) {
                if (IMinecraft.mc.level.getBlockState(this.position).canBeReplaced()) {
                    return true;
                }
                this.tryStart();
                return false;
            }
            if (!this.this$0.farReach.getValue()) {
                return this.legacyProcess();
            }
            if (IMinecraft.mc.level.getBlockState(this.position).canBeReplaced()) {
                if (this.brokenCount == 0) {
                    this.markBroken();
                }
                if (this.this$0.sixB.getValue() && this.state.getBlock() == Blocks.BEDROCK) {
                    this.instantMine = true;
                    return false;
                }
                if (this.this$0.rebreak.getValue().equalsIgnoreCase("Fast")) {
                    this.progress = 0.0f;
                    this.prevProgress = 0.0f;
                }
                if (this.instantMine) {
                    boolean timedOut;
                    if (this.this$0.async.getValue()) {
                        if ((this.this$0.whileEating.getValue() || !EntityUtils.isEating()) && !this.isAsyncFireBlockedByCombo() && this.this$0.instantTimer.hasTimeElapsed(this.asyncFireDelayMs())) {
                            int asyncSlot;
                            Direction asyncDirection = WorldUtils.getClosestDirection(this.position, true);
                            asyncSlot = this.this$0.switchMode.getValue().equalsIgnoreCase("None") ? -1 : InventoryUtils.findFastestItem(this.state, InventoryUtils.HOTBAR_START, this.this$0.switchMode.getValue().equalsIgnoreCase("AltSwap") || this.this$0.switchMode.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END);
                            if (asyncSlot == -1) {
                                asyncSlot = IMinecraft.mc.player.getInventory().getSelectedSlot();
                            }
                            this.fireBreakBurst(asyncDirection, this.getBreakSlot(this.state, asyncSlot), false, false);
                            this.this$0.instantTimer.reset();
                            ++this.attempts;
                        }
                        return false;
                    }
                    if (this.instantAckStalled()) {
                        return false;
                    }
                    long timeoutTicks = this.this$0.instantTimeout.getValue().longValue();
                    boolean bl2 = timedOut = timeoutTicks > 0L && this.this$0.instantTimer.hasTimeElapsed(timeoutTicks * 50L);
                    if (!timedOut) {
                        return false;
                    }
                }
                if (this.this$0.isProxyActive()) {
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(IMinecraft.mc.player.getInventory().getSelectedSlot()));
                }
                this.cancel();
                return true;
            }
            Direction direction = WorldUtils.getClosestDirection(this.position, true);
            BlockState state = IMinecraft.mc.level.getBlockState(this.position);
            if (!state.canBeReplaced() && state.getBlock() != this.state.getBlock()) {
                this.state = state;
            }
            if (this.mining) {
                boolean pauseBreak;
                float delta;
                int slot;
                ++this.ticks;
                slot = this.this$0.switchMode.getValue().equalsIgnoreCase("None") ? -1 : InventoryUtils.findFastestItem(this.state, InventoryUtils.HOTBAR_START, this.this$0.switchMode.getValue().equalsIgnoreCase("AltSwap") || this.this$0.switchMode.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END);
                if (slot == -1) {
                    slot = IMinecraft.mc.player.getInventory().getSelectedSlot();
                }
                float f = delta = this.this$0.sixB.getValue() && this.state.getBlock() == Blocks.BEDROCK ? 0.025f : WorldUtils.getMineSpeed(this.state, slot) / Night.WORLD_MANAGER.getTimerMultiplier();
                if (this.instantMine) {
                    this.prevProgress = this.getSpeed();
                    this.progress = this.getSpeed();
                } else {
                    this.prevProgress = this.progress;
                    this.progress = Mth.clamp((float)(this.progress + delta), (float)0.0f, (float)this.getSpeed());
                }
                if (this.this$0.sixB.getValue() && this.state.getBlock() == Blocks.BEDROCK) {
                    if (this.this$0.isProxyActive()) {
                        this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                    } else {
                        IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                    }
                    IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
                }
                if (this.this$0.rotate.getValue().equalsIgnoreCase("Normal") && this.progress + delta * 2.0f >= this.getSpeed()) {
                    float[] rots = RotationUtils.getRotations(WorldUtils.getHitVector(this.position, direction));
                    if (this.this$0.isProxyActive()) {
                        this.this$0.serverSend((Packet<?>)new ServerboundMovePlayerPacket.PosRot(IMinecraft.mc.player.getX(), IMinecraft.mc.player.getY(), IMinecraft.mc.player.getZ(), rots[0], rots[1], IMinecraft.mc.player.onGround(), IMinecraft.mc.player.horizontalCollision));
                    } else {
                        Night.ROTATION_MANAGER.legacyRotate(rots, Night.ROTATION_MANAGER.getLegacyModulePriority(this.this$0));
                    }
                }
                boolean isEatingNow = IMinecraft.mc.player != null && (IMinecraft.mc.player.isUsingItem() || EntityUtils.isEating());
                boolean bl3 = pauseBreak = this.this$0.interactPaused || !this.this$0.whileEating.getValue() && isEatingNow;
                if (this.progress >= this.getSpeed() && !state.canBeReplaced() && !pauseBreak) {
                    if (this.instantMine && this.brokenCount == 0 && this.ackStalled()) {
                        this.start();
                        return false;
                    }
                    if (!this.instantMine || this.this$0.async.getValue() || this.this$0.instantTimer.hasTimeElapsed(this.this$0.instantDelay.getValue().longValue() * 50L)) {
                        this.fireBreakBurst(direction, this.getBreakSlot(this.state, slot), false, !this.instantMine);
                        if (!this.instantMine) {
                            this.this$0.mineTimer.reset();
                        }
                        ++this.attempts;
                        if (this.this$0.rebreak.getValue().equalsIgnoreCase("None") && !this.this$0.sixB.getValue() || this.terrainBase) {
                            this.mining = false;
                            this.stallTime = System.currentTimeMillis();
                        } else {
                            this.instantMine = true;
                            if (this.this$0.rebreak.getValue().equalsIgnoreCase("Fast")) {
                                this.progress = 0.0f;
                                this.prevProgress = 0.0f;
                            }
                            this.this$0.instantTimer.reset();
                            this.instantRemineResetTimer.reset();
                        }
                    }
                    return false;
                }
            } else if (!(IMinecraft.mc.level.getBlockState(this.position).canBeReplaced() || this.attempts != 0 && System.currentTimeMillis() - this.stallTime < 150L)) {
                this.tryStart();
            }
            return false;
        }

        private boolean legacyProcess() {
            this.updateIronGhost();
            if (IMinecraft.mc.level.getBlockState(this.position).canBeReplaced()) {
                if (this.brokenCount == 0) {
                    this.markBroken();
                }
                if (this.this$0.sixB.getValue() && this.state.getBlock() == Blocks.BEDROCK) {
                    this.instantMine = true;
                    return false;
                }
                if (this.instantMine) {
                    boolean timedOut;
                    if (this.this$0.async.getValue()) {
                        if ((this.this$0.whileEating.getValue() || !EntityUtils.isEating()) && !this.isAsyncFireBlockedByCombo() && this.this$0.instantTimer.hasTimeElapsed(this.asyncFireDelayMs())) {
                            int asyncSlot;
                            Direction asyncDirection = WorldUtils.getClosestDirection(this.position, true);
                            asyncSlot = this.this$0.switchMode.getValue().equalsIgnoreCase("None") ? -1 : InventoryUtils.findFastestItem(this.state, InventoryUtils.HOTBAR_START, this.this$0.switchMode.getValue().equalsIgnoreCase("AltSwap") || this.this$0.switchMode.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END);
                            if (asyncSlot == -1) {
                                asyncSlot = IMinecraft.mc.player.getInventory().getSelectedSlot();
                            }
                            this.legacyFireBreakBurst(asyncDirection, this.getBreakSlot(this.state, asyncSlot));
                            this.this$0.instantTimer.reset();
                            ++this.attempts;
                        }
                        return false;
                    }
                    if (this.instantAckStalled()) {
                        return false;
                    }
                    long timeoutTicks = this.this$0.instantTimeout.getValue().longValue();
                    boolean bl = timedOut = timeoutTicks > 0L && this.this$0.instantTimer.hasTimeElapsed(timeoutTicks * 50L);
                    if (!timedOut) {
                        return false;
                    }
                }
                if (this.this$0.isProxyActive()) {
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(IMinecraft.mc.player.getInventory().getSelectedSlot()));
                }
                this.cancel();
                return true;
            }
            Direction direction = WorldUtils.getClosestDirection(this.position, true);
            BlockState state = IMinecraft.mc.level.getBlockState(this.position);
            if (!state.canBeReplaced() && state.getBlock() != this.state.getBlock()) {
                this.state = state;
            }
            if (this.mining) {
                float delta;
                int slot;
                ++this.ticks;
                slot = this.this$0.switchMode.getValue().equalsIgnoreCase("None") ? -1 : InventoryUtils.findFastestItem(this.state, InventoryUtils.HOTBAR_START, this.this$0.switchMode.getValue().equalsIgnoreCase("AltSwap") || this.this$0.switchMode.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END);
                if (slot == -1) {
                    slot = IMinecraft.mc.player.getInventory().getSelectedSlot();
                }
                float f = delta = this.this$0.sixB.getValue() && this.state.getBlock() == Blocks.BEDROCK ? 0.025f : WorldUtils.getMineSpeed(this.state, slot) / Night.WORLD_MANAGER.getTimerMultiplier();
                if (this.instantMine) {
                    this.prevProgress = this.getSpeed();
                    this.progress = this.getSpeed();
                } else {
                    this.prevProgress = this.progress;
                    this.progress = Mth.clamp((float)(this.progress + delta), (float)0.0f, (float)this.getSpeed());
                }
                if (this.this$0.sixB.getValue() && this.state.getBlock() == Blocks.BEDROCK) {
                    if (this.this$0.isProxyActive()) {
                        this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                    } else {
                        IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                    }
                    if (IMinecraft.mc.player == null || !IMinecraft.mc.player.isUsingItem() || IMinecraft.mc.player.getUsedItemHand() != InteractionHand.MAIN_HAND) {
                        IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
                    }
                }
                if (this.this$0.rotate.getValue().equalsIgnoreCase("Normal") && this.progress + delta * 2.0f >= this.getSpeed()) {
                    float[] rots = RotationUtils.getRotations(WorldUtils.getHitVector(this.position, direction));
                    if (this.this$0.isProxyActive()) {
                        this.this$0.serverSend((Packet<?>)new ServerboundMovePlayerPacket.PosRot(IMinecraft.mc.player.getX(), IMinecraft.mc.player.getY(), IMinecraft.mc.player.getZ(), rots[0], rots[1], IMinecraft.mc.player.onGround(), IMinecraft.mc.player.horizontalCollision));
                    } else {
                        Night.ROTATION_MANAGER.legacyRotate(rots, Night.ROTATION_MANAGER.getLegacyModulePriority(this.this$0));
                    }
                }
                if (this.progress >= this.getSpeed() && !state.canBeReplaced() && (this.this$0.whileEating.getValue() || !EntityUtils.isEating())) {
                    if (this.instantMine && this.brokenCount == 0 && this.ackStalled()) {
                        this.start();
                        return false;
                    }
                    if (!this.instantMine || this.this$0.async.getValue() || this.this$0.instantTimer.hasTimeElapsed(this.this$0.instantDelay.getValue().longValue() * 50L)) {
                        this.legacyFireBreakBurst(direction, this.getBreakSlot(this.state, slot));
                        ++this.attempts;
                        if (this.this$0.rebreak.getValue().equalsIgnoreCase("None") && !this.this$0.sixB.getValue() || this.terrainBase) {
                            this.mining = false;
                            this.stallTime = System.currentTimeMillis();
                        } else {
                            this.instantMine = true;
                            if (this.this$0.rebreak.getValue().equalsIgnoreCase("Fast")) {
                                this.progress = 0.0f;
                                this.prevProgress = 0.0f;
                            }
                            this.this$0.instantTimer.reset();
                            this.instantRemineResetTimer.reset();
                        }
                    }
                    return false;
                }
            } else if (!(IMinecraft.mc.level.getBlockState(this.position).canBeReplaced() || this.attempts != 0 && System.currentTimeMillis() - this.stallTime < 150L)) {
                this.start();
            }
            return false;
        }

        private boolean isHeadCrystalComboPending() {
            AutoCrystalModule autoCrystal;
            AutoTrapModule autoTrap;
            AutoTrapModule autoTrapModule = autoTrap = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoTrapModule.class) : null;
            if (autoTrap == null || !autoTrap.isToggled()) {
                return false;
            }
            AutoCrystalModule autoCrystalModule = autoCrystal = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoCrystalModule.class) : null;
            if (autoCrystal == null || !autoCrystal.isToggled()) {
                return false;
            }
            Target currentTarget = this.this$0.getTarget();
            if (currentTarget == null || currentTarget.player() == null) {
                return false;
            }
            int targetFeetY = Mth.floor((double)currentTarget.player().getY());
            return this.position.getY() >= targetFeetY + 2 && Math.abs((double)this.position.getX() + 0.5 - currentTarget.player().getX()) < 1.3 && Math.abs((double)this.position.getZ() + 0.5 - currentTarget.player().getZ()) < 1.3;
        }

        private boolean isCivBreakComboPending() {
            AutoMineModule autoMine = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoMineModule.class) : null;
            return autoMine != null && autoMine.isToggled() && autoMine.isUpComboActive(this.position);
        }

        private boolean isAsyncFireBlockedByCombo() {
            return this.isHeadCrystalComboPending() || this.isCivBreakComboPending();
        }

        private void checkAndPlaceHeadCrystal(BlockPos pos) {
            boolean offhand;
            boolean isHeadPos;
            AutoCrystalModule autoCrystal;
            AutoTrapModule autoTrap;
            if (IMinecraft.mc.player == null || IMinecraft.mc.level == null || IMinecraft.mc.getConnection() == null) {
                return;
            }
            if (!this.instantMine) {
                return;
            }
            AutoTrapModule autoTrapModule = autoTrap = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoTrapModule.class) : null;
            if (autoTrap == null || !autoTrap.isToggled()) {
                return;
            }
            AutoCrystalModule autoCrystalModule = autoCrystal = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoCrystalModule.class) : null;
            if (autoCrystal == null || !autoCrystal.isToggled()) {
                return;
            }
            BlockState blockState = IMinecraft.mc.level.getBlockState(pos);
            if (blockState.getBlock() != Blocks.OBSIDIAN) {
                return;
            }
            Target currentTarget = this.this$0.getTarget();
            if (currentTarget == null || currentTarget.player() == null) {
                return;
            }
            int targetFeetY = Mth.floor((double)currentTarget.player().getY());
            boolean bl = isHeadPos = pos.getY() >= targetFeetY + 2 && Math.abs((double)pos.getX() + 0.5 - currentTarget.player().getX()) < 1.3 && Math.abs((double)pos.getZ() + 0.5 - currentTarget.player().getZ()) < 1.3;
            if (!isHeadPos) {
                return;
            }
            BlockPos crystalPos = pos.above();
            if (!WorldUtils.isCrystalPlaceable(crystalPos)) {
                return;
            }
            int crystalSlot = InventoryUtils.findHotbar(Items.END_CRYSTAL);
            boolean bl2 = offhand = IMinecraft.mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL;
            if (!offhand && crystalSlot == -1) {
                return;
            }
            InteractionHand hand = offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            Vec3 hitVec = new Vec3((double)pos.getX() + 0.5, (double)pos.getY() + 1.0, (double)pos.getZ() + 0.5);
            BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, pos, false);
            if (this.this$0.isProxyActive()) {
                if (offhand) {
                    this.this$0.serverSendSequenced(seq -> new ServerboundUseItemOnPacket(hand, hit, seq));
                    this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(hand));
                } else {
                    int previousSlot = IMinecraft.mc.player.getInventory().getSelectedSlot();
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(crystalSlot));
                    this.this$0.serverSendSequenced(seq -> new ServerboundUseItemOnPacket(hand, hit, seq));
                    this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(hand));
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(previousSlot));
                }
            } else if (offhand) {
                NetworkUtils.sendSequencedPacket(seq -> new ServerboundUseItemOnPacket(hand, hit, seq));
                IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(hand));
            } else {
                int previousSlot = IMinecraft.mc.player.getInventory().getSelectedSlot();
                InventoryUtils.switchSlot("Silent", crystalSlot, previousSlot);
                NetworkUtils.sendSequencedPacket(seq -> new ServerboundUseItemOnPacket(hand, hit, seq));
                IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(hand));
                InventoryUtils.switchBack("Silent", crystalSlot, previousSlot);
            }
        }

        private void legacyFireBreakBurst(Direction direction, int slot) {
            this.checkAndPlaceHeadCrystal(this.position);
            Night.EVENT_HANDLER.post(new DestroyBlockEvent(this.position));
            if (this.this$0.rotate.getValue().equalsIgnoreCase("Silent")) {
                float[] rots = RotationUtils.getRotations(WorldUtils.getHitVector(this.position, direction));
                if (this.this$0.isProxyActive()) {
                    this.this$0.serverSend((Packet<?>)new ServerboundMovePlayerPacket.Rot(rots[0], rots[1], IMinecraft.mc.player.onGround(), IMinecraft.mc.player.horizontalCollision));
                } else {
                    Night.ROTATION_MANAGER.wireRotate("Silent", rots);
                }
            }
            int previousSlot = IMinecraft.mc.player.getInventory().getSelectedSlot();
            if (this.this$0.isProxyActive()) {
                boolean needSwitch;
                int mineSlot = this.this$0.switchMode.getValue().equalsIgnoreCase("None") ? -1 : slot;
                boolean bl = needSwitch = mineSlot != -1 && mineSlot != previousSlot;
                if (needSwitch) {
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(mineSlot));
                }
                this.stopDestroyBlock(this.position, direction, true);
                if (this.this$0.grim.getValue()) {
                    this.this$0.serverSend((Packet<?>)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position.above(500), direction));
                }
                this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                if (needSwitch) {
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(previousSlot));
                }
            } else {
                InventoryUtils.switchSlot(this.this$0.switchMode.getValue(), slot, previousSlot);
                this.stopDestroyBlock(this.position, direction, true);
                if (this.this$0.grim.getValue()) {
                    IMinecraft.mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position.above(500), direction));
                }
                IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                if (IMinecraft.mc.level.getBlockState(this.position).getBlock() == Blocks.BEDROCK) {
                    IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
                }
                InventoryUtils.switchBack(this.this$0.switchMode.getValue(), slot, previousSlot);
            }
        }

        private void fireBreakBurst(Direction direction, int slot, boolean demote) {
            this.fireBreakBurst(direction, slot, demote, !demote);
        }

        private void fireBreakBurst(Direction direction, int slot, boolean demote, boolean armCooldown) {
            if (!demote) {
                this.checkAndPlaceHeadCrystal(this.position);
                Night.EVENT_HANDLER.post(new DestroyBlockEvent(this.position));
            }
            if (this.this$0.rotate.getValue().equalsIgnoreCase("Silent")) {
                float[] rots = RotationUtils.getRotations(WorldUtils.getHitVector(this.position, direction));
                if (this.this$0.isProxyActive()) {
                    this.this$0.serverSend((Packet<?>)new ServerboundMovePlayerPacket.Rot(rots[0], rots[1], IMinecraft.mc.player.onGround(), IMinecraft.mc.player.horizontalCollision));
                } else {
                    Night.ROTATION_MANAGER.wireRotate("Silent", rots);
                }
            }
            int previousSlot = IMinecraft.mc.player.getInventory().getSelectedSlot();
            if (this.this$0.isProxyActive()) {
                int mineSlot;
                int realPreviousSlot = this.startSlot != -1 ? this.startSlot : previousSlot;
                int n = mineSlot = this.this$0.switchMode.getValue().equalsIgnoreCase("None") ? -1 : slot;
                if (mineSlot != -1) {
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(mineSlot));
                    if (this.this$0.switchMode.getValue().equalsIgnoreCase("Normal")) {
                        IMinecraft.mc.player.getInventory().setSelectedSlot(mineSlot);
                    }
                }
                this.stopDestroyBlock(this.position, direction, !demote, armCooldown);
                if (this.this$0.grim.getValue()) {
                    this.this$0.serverSend((Packet<?>)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position.above(500), direction));
                }
                this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                if (IMinecraft.mc.level.getBlockState(this.position).getBlock() == Blocks.BEDROCK) {
                    IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
                }
                if (mineSlot != -1) {
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(realPreviousSlot));
                    if (this.this$0.switchMode.getValue().equalsIgnoreCase("Normal")) {
                        IMinecraft.mc.player.getInventory().setSelectedSlot(realPreviousSlot);
                    }
                }
            } else {
                InventoryUtils.switchSlot(this.this$0.switchMode.getValue(), slot, previousSlot);
                this.stopDestroyBlock(this.position, direction, !demote, armCooldown);
                if (this.this$0.grim.getValue()) {
                    IMinecraft.mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position.above(500), direction));
                }
                IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                if (IMinecraft.mc.level.getBlockState(this.position).getBlock() == Blocks.BEDROCK) {
                    IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
                }
                InventoryUtils.switchBack(this.this$0.switchMode.getValue(), slot, previousSlot);
            }
        }

        private void stopDestroyBlock(BlockPos position, Direction direction, boolean remove) {
            this.stopDestroyBlock(position, direction, remove, false);
        }

        private void stopDestroyBlock(BlockPos position, Direction direction, boolean remove, boolean cooldown) {
            try (BlockStatePredictionHandler prediction = ((ClientWorldAccessor)IMinecraft.mc.level).invokeGetPendingUpdateManager().startPredicting();){
                ServerboundPlayerActionPacket packet = new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, position, direction, prediction.currentSequence());
                this.this$0.markStop(cooldown);
                if (this.this$0.isProxyActive()) {
                    this.this$0.serverSend((Packet<?>)packet);
                } else {
                    IMinecraft.mc.getConnection().send((Packet)packet);
                }
            }
        }

        public void render(PoseStack matrices) {
            double progress;
            if (IMinecraft.mc.level.getBlockState(this.position).canBeReplaced() && !this.instantMine) {
                return;
            }
            AABB box = new AABB(this.position);
            boolean airWaiting = IMinecraft.mc.level.getBlockState(this.position).canBeReplaced() && this.instantMine;
            double d = progress = airWaiting ? (double)(this.progress / this.getSpeed()) : (double)Mth.clamp((float)Mth.lerp((float)IMinecraft.mc.getDeltaTracker().getGameTimeDeltaPartialTick(false), (float)(this.prevProgress / this.getSpeed()), (float)(this.progress / this.getSpeed())), (float)0.0f, (float)1.0f);
            if (this.this$0.animation.getValue().equalsIgnoreCase("Expand")) {
                box = new AABB(this.position).deflate(0.5).inflate(Mth.clamp((double)(progress / 2.0), (double)0.0, (double)0.5));
            }
            if (this.this$0.animation.getValue().equalsIgnoreCase("Rise")) {
                box = new AABB((double)this.position.getX(), (double)this.position.getY(), (double)this.position.getZ(), (double)this.position.getX() + 1.0, (double)this.position.getY() + progress, (double)this.position.getZ() + 1.0);
            }
            Color fill = this.this$0.fillColor.getColor();
            Color outline = this.this$0.outlineColor.getColor();
            if (this.this$0.color.getValue().equalsIgnoreCase("Static")) {
                fill = progress >= 0.9 ? new Color(0, 255, 0, this.this$0.fillColor.getAlpha()) : new Color(255, 0, 0, this.this$0.fillColor.getAlpha());
                outline = progress >= 0.9 ? new Color(0, 255, 0, this.this$0.outlineColor.getAlpha()) : new Color(255, 0, 0, this.this$0.outlineColor.getAlpha());
            } else if (this.this$0.color.getValue().equalsIgnoreCase("Smooth")) {
                fill = new Color(255 - (int)(Mth.clamp((double)progress, (double)0.0, (double)1.0) * 255.0), (int)(Mth.clamp((double)progress, (double)0.0, (double)1.0) * 255.0), 0, this.this$0.fillColor.getAlpha());
                outline = new Color(255 - (int)(Mth.clamp((double)progress, (double)0.0, (double)1.0) * 255.0), (int)(Mth.clamp((double)progress, (double)0.0, (double)1.0) * 255.0), 0, this.this$0.outlineColor.getAlpha());
            }
            if (progress >= (double)this.getSpeed() && this.instantMine && this.this$0.instantRender.getValue().equalsIgnoreCase("Custom")) {
                fill = ColorUtils.getColor(this.this$0.instantColor.getColor(), this.this$0.fillColor.getAlpha());
                outline = ColorUtils.getColor(this.this$0.instantColor.getColor(), this.this$0.outlineColor.getAlpha());
            }
            if (this.this$0.render.getValue().equalsIgnoreCase("Fill") || this.this$0.render.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBox(matrices, box, fill);
            }
            if (this.this$0.render.getValue().equalsIgnoreCase("Outline") || this.this$0.render.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBoxOutline(matrices, box, outline);
            }
        }

        public void start() {
            if (!this.this$0.farReach.getValue()) {
                this.legacyStart();
                return;
            }
            this.this$0.trackStarts(this.this$0.farReach.getValue() ? 2 : 1);
            Direction direction = WorldUtils.getClosestDirection(this.position, true);
            if (this.this$0.isProxyActive()) {
                this.startSlot = IMinecraft.mc.player.getInventory().getSelectedSlot();
                if (this.this$0.farReach.getValue()) {
                    this.this$0.sendRawPlayerAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, direction);
                    BlockPos decoyPos = this.grimDecoyPos();
                    this.this$0.sendRawPlayerAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, decoyPos, WorldUtils.getClosestDirection(decoyPos, true));
                } else {
                    this.this$0.serverSendSequenced(seq -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, direction, seq));
                }
                this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            } else {
                if (this.this$0.farReach.getValue()) {
                    this.this$0.sendRawPlayerAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, direction);
                    BlockPos decoyPos = this.grimDecoyPos();
                    this.this$0.sendRawPlayerAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, decoyPos, WorldUtils.getClosestDirection(decoyPos, true));
                } else {
                    NetworkUtils.sendSequencedPacket(seq -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, direction, seq));
                }
                IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            }
            if (this.state.getBlock() == Blocks.BEDROCK) {
                IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
            }
            this.restoreIronSwap();
            this.progress = 0.0f;
            this.prevProgress = 0.0f;
            this.ghostIronProgress = 0.0f;
            this.ironSwapped = false;
            this.swappedIronInventorySlot = -1;
            this.swappedIronHotbarSlot = -1;
            this.attempts = 0;
            this.ticks = 0;
            this.mining = true;
            this.instantMine = false;
            this.started = true;
            this.brokenCount = 0;
            this.lastBrokenCount = -1;
            this.instantRemineResetTimer.reset();
        }

        private void legacyStart() {
            Direction direction = WorldUtils.getClosestDirection(this.position, true);
            if (this.this$0.isProxyActive()) {
                boolean needSwitch;
                int slot = this.this$0.switchMode.getValue().equalsIgnoreCase("None") ? -1 : InventoryUtils.findFastestItem(this.state, InventoryUtils.HOTBAR_START, this.this$0.switchMode.getValue().equalsIgnoreCase("AltSwap") || this.this$0.switchMode.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END);
                boolean bl = needSwitch = slot != -1 && slot != IMinecraft.mc.player.getInventory().getSelectedSlot();
                if (needSwitch) {
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(slot));
                }
                if (this.this$0.doubleMine.getValue()) {
                    this.this$0.serverSendSequenced(seq -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.position, direction, seq));
                    this.this$0.serverSendSequenced(seq -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, direction, seq));
                    this.this$0.serverSendSequenced(seq -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.position, direction, seq));
                } else {
                    this.this$0.serverSendSequenced(seq -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, direction, seq));
                }
                this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            } else {
                if (this.this$0.doubleMine.getValue()) {
                    NetworkUtils.sendSequencedPacket(seq -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.position, direction, seq));
                    NetworkUtils.sendSequencedPacket(seq -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, direction, seq));
                    NetworkUtils.sendSequencedPacket(seq -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.position, direction, seq));
                } else {
                    NetworkUtils.sendSequencedPacket(seq -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, direction, seq));
                }
                IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            }
            if (this.state.getBlock() == Blocks.BEDROCK) {
                IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
            }
            this.restoreIronSwap();
            this.progress = 0.0f;
            this.prevProgress = 0.0f;
            this.ghostIronProgress = 0.0f;
            this.ironSwapped = false;
            this.swappedIronInventorySlot = -1;
            this.swappedIronHotbarSlot = -1;
            this.attempts = 0;
            this.ticks = 0;
            this.mining = true;
            this.instantMine = false;
            this.started = true;
            this.brokenCount = 0;
            this.lastBrokenCount = -1;
            this.instantRemineResetTimer.reset();
        }

        public void cancel() {
            if (!this.this$0.farReach.getValue()) {
                this.legacyCancel();
                return;
            }
            if (!this.this$0.doubleMine.getValue() && this.started) {
                if (this.this$0.farReach.getValue()) {
                    this.this$0.sendRawPlayerAction(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position, WorldUtils.getClosestDirection(this.position, true));
                    this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                } else if (this.this$0.isProxyActive()) {
                    this.this$0.serverSendSequenced(seq -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position, WorldUtils.getClosestDirection(this.position, true), seq));
                    this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                } else {
                    NetworkUtils.sendSequencedPacket(seq -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position, WorldUtils.getClosestDirection(this.position, true), seq));
                    IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                }
            }
            if (this.this$0.isProxyActive() && this.startSlot != -1) {
                this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(this.startSlot));
                IMinecraft.mc.player.getInventory().setSelectedSlot(this.startSlot);
                this.startSlot = -1;
            }
            this.restoreIronSwap();
            this.progress = 0.0f;
            this.prevProgress = 0.0f;
            this.ghostIronProgress = 0.0f;
            this.ironSwapped = false;
            this.swappedIronInventorySlot = -1;
            this.swappedIronHotbarSlot = -1;
            this.attempts = 0;
            this.mining = false;
            this.started = false;
            this.instantMine = false;
        }

        private void legacyCancel() {
            if (!this.this$0.doubleMine.getValue()) {
                Direction direction = WorldUtils.getClosestDirection(this.position, true);
                if (this.this$0.isProxyActive()) {
                    this.this$0.serverSendSequenced(seq -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position, direction, seq));
                    this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                } else {
                    NetworkUtils.sendSequencedPacket(seq -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position, direction, seq));
                    IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                }
            }
            if (this.this$0.isProxyActive()) {
                this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(IMinecraft.mc.player.getInventory().getSelectedSlot()));
            }
            this.restoreIronSwap();
            this.progress = 0.0f;
            this.prevProgress = 0.0f;
            this.ghostIronProgress = 0.0f;
            this.ironSwapped = false;
            this.swappedIronInventorySlot = -1;
            this.swappedIronHotbarSlot = -1;
            this.attempts = 0;
            this.mining = false;
            this.started = false;
            this.instantMine = false;
        }

        private float getSpeed() {
            return this.this$0.speed.getValue().floatValue();
        }

        private BlockPos grimDecoyPos() {
            return this.position.below(2000);
        }

        public int getTicksRemaining() {
            float delta;
            if (!this.mining) {
                return Integer.MAX_VALUE;
            }
            if (this.this$0.sixB.getValue() && this.state.getBlock() == Blocks.BEDROCK) {
                float delta2 = 0.025f;
                return Math.max(0, Math.round((this.getSpeed() - this.progress) / delta2));
            }
            int bestToolSlot = InventoryUtils.findFastestItem(this.state, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
            if (bestToolSlot == -1) {
                bestToolSlot = IMinecraft.mc.player.getInventory().getSelectedSlot();
            }
            if ((delta = WorldUtils.getMineSpeed(this.state, bestToolSlot) / Night.WORLD_MANAGER.getTimerMultiplier()) <= 0.0f) {
                return Integer.MAX_VALUE;
            }
            return Math.max(0, Math.round((this.getSpeed() - this.progress) / delta));
        }

        @Generated
        public float getPrevProgress() {
            return this.prevProgress;
        }

        @Generated
        public int getAttempts() {
            return this.attempts;
        }

        @Generated
        public long getStallTime() {
            return this.stallTime;
        }

        @Generated
        public int getStartSlot() {
            return this.startSlot;
        }

        @Generated
        public int getBrokenCount() {
            return this.brokenCount;
        }

        @Generated
        public int getLastBrokenCount() {
            return this.lastBrokenCount;
        }

        @Generated
        public Timer getInstantRemineResetTimer() {
            return this.instantRemineResetTimer;
        }

        @Generated
        public float getGhostIronProgress() {
            return this.ghostIronProgress;
        }

        @Generated
        public boolean isIronSwapped() {
            return this.ironSwapped;
        }

        @Generated
        public int getSwappedIronInventorySlot() {
            return this.swappedIronInventorySlot;
        }

        @Generated
        public int getSwappedIronHotbarSlot() {
            return this.swappedIronHotbarSlot;
        }

        @Generated
        public boolean isBedrockOrigin() {
            return this.bedrockOrigin;
        }

        @Generated
        public void setProgress(float progress) {
            this.progress = progress;
        }
    }

    public class Secondary {
        private final BlockPos position;
        private final int priority;
        private BlockState state;
        private float progress;
        private float prevProgress;
        private int ticks;
        private boolean holding;
        private int holdTicks;
        final /* synthetic */ SpeedMineModule this$0;

        private Secondary(SpeedMineModule this$0, BlockPos position, int priority, BlockState state, float progress) {
            SpeedMineModule speedMineModule = this$0;
            Objects.requireNonNull(speedMineModule);
            this.this$0 = speedMineModule;
            this.position = position;
            this.priority = priority;
            this.state = state;
            this.progress = progress;
            this.prevProgress = progress;
        }

        public BlockPos getPosition() {
            return this.position;
        }

        public int getPriority() {
            return this.priority;
        }

        public boolean isMining() {
            return true;
        }

        public boolean isHolding() {
            return this.holding;
        }

        public float getSpeed() {
            return 1.0f;
        }

        public float getProgress() {
            return Mth.clamp((float)this.progress, (float)0.0f, (float)1.0f);
        }

        public boolean process() {
            boolean canHold;
            float delta;
            BlockState current;
            boolean clientEating;
            if (this.this$0.isOutOfRange(this.position)) {
                this.release();
                return true;
            }
            boolean bl = clientEating = IMinecraft.mc.player != null && (IMinecraft.mc.player.isUsingItem() || EntityUtils.isEating());
            if (this.this$0.interactPaused && !clientEating && System.currentTimeMillis() - this.this$0.interactPausedAt >= 750L) {
                this.this$0.setInteractPaused(false);
            }
            if ((current = IMinecraft.mc.level.getBlockState(this.position)).canBeReplaced()) {
                Night.EVENT_HANDLER.post(new DestroyBlockEvent(this.position));
                this.this$0.mineTimer.reset();
                this.release();
                return true;
            }
            this.state = current;
            int bestSlot = InventoryUtils.findFastestItem(this.state, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
            if (bestSlot == -1) {
                bestSlot = IMinecraft.mc.player.getInventory().getSelectedSlot();
            }
            if ((delta = WorldUtils.getMineSpeed(this.state, bestSlot) / Night.WORLD_MANAGER.getTimerMultiplier()) <= 0.0f) {
                this.release();
                return true;
            }
            ++this.ticks;
            this.prevProgress = this.progress;
            this.progress += delta;
            boolean bl2 = canHold = this.this$0.whileEating.getValue() || !clientEating || this.this$0.switchMode.getValue().equalsIgnoreCase("None");
            if (!this.holding && this.progress + delta >= 1.0f && canHold) {
                this.hold(bestSlot);
            }
            if (this.holding && !this.this$0.farReach.getValue() && ++this.holdTicks >= 3) {
                this.release();
                return true;
            }
            if (this.progress >= 1.0f + delta * 10.0f) {
                this.release();
                return true;
            }
            if (this.ticks > 60) {
                this.release();
                return true;
            }
            return false;
        }

        private void hold(int slot) {
            if (this.this$0.switchMode.getValue().equalsIgnoreCase("None")) {
                return;
            }
            int selected = IMinecraft.mc.player.getInventory().getSelectedSlot();
            this.holding = true;
            if (selected == slot) {
                return;
            }
            this.this$0.secondaryOriginalSlot = selected;
            this.this$0.secondaryHoldSlot = slot;
            if (this.this$0.isProxyActive()) {
                this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(slot));
            } else {
                InventoryUtils.switchSlot(this.this$0.switchMode.getValue(), slot, selected);
            }
        }

        private void release() {
            if (!this.holding) {
                return;
            }
            this.holding = false;
            if (this.this$0.secondaryHoldSlot == -1) {
                return;
            }
            int restore = this.this$0.secondaryOriginalSlot;
            int heldSlot = this.this$0.secondaryHoldSlot;
            this.this$0.secondaryHoldSlot = -1;
            this.this$0.secondaryOriginalSlot = -1;
            if (IMinecraft.mc.player == null) {
                return;
            }
            if (this.this$0.switchMode.getValue().equalsIgnoreCase("Normal") && IMinecraft.mc.player.getInventory().getSelectedSlot() == restore) {
                return;
            }
            if (this.this$0.isProxyActive()) {
                this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(restore));
            } else {
                InventoryUtils.switchBack(this.this$0.switchMode.getValue(), heldSlot, restore);
            }
        }

        public void render(PoseStack matrices) {
            if (IMinecraft.mc.level.getBlockState(this.position).canBeReplaced()) {
                return;
            }
            double p = Mth.clamp((float)Mth.lerp((float)IMinecraft.mc.getDeltaTracker().getGameTimeDeltaPartialTick(false), (float)this.prevProgress, (float)this.progress), (float)0.0f, (float)1.0f);
            AABB box = new AABB(this.position);
            if (this.this$0.animation.getValue().equalsIgnoreCase("Expand")) {
                box = new AABB(this.position).deflate(0.5).inflate(Mth.clamp((double)(p / 2.0), (double)0.0, (double)0.5));
            }
            if (this.this$0.animation.getValue().equalsIgnoreCase("Rise")) {
                box = new AABB((double)this.position.getX(), (double)this.position.getY(), (double)this.position.getZ(), (double)this.position.getX() + 1.0, (double)this.position.getY() + p, (double)this.position.getZ() + 1.0);
            }
            Color fill = this.this$0.fillColor.getColor();
            Color outline = this.this$0.outlineColor.getColor();
            if (this.this$0.color.getValue().equalsIgnoreCase("Static")) {
                fill = p >= 0.9 ? new Color(0, 255, 0, this.this$0.fillColor.getAlpha()) : new Color(255, 0, 0, this.this$0.fillColor.getAlpha());
                outline = p >= 0.9 ? new Color(0, 255, 0, this.this$0.outlineColor.getAlpha()) : new Color(255, 0, 0, this.this$0.outlineColor.getAlpha());
            } else if (this.this$0.color.getValue().equalsIgnoreCase("Smooth")) {
                fill = new Color(255 - (int)(p * 255.0), (int)(p * 255.0), 0, this.this$0.fillColor.getAlpha());
                outline = new Color(255 - (int)(p * 255.0), (int)(p * 255.0), 0, this.this$0.outlineColor.getAlpha());
            }
            if (this.this$0.render.getValue().equalsIgnoreCase("Fill") || this.this$0.render.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBox(matrices, box, fill);
            }
            if (this.this$0.render.getValue().equalsIgnoreCase("Outline") || this.this$0.render.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBoxOutline(matrices, box, outline);
            }
        }

        @Generated
        public BlockState getState() {
            return this.state;
        }

        @Generated
        public float getPrevProgress() {
            return this.prevProgress;
        }

        @Generated
        public int getTicks() {
            return this.ticks;
        }

        @Generated
        public int getHoldTicks() {
            return this.holdTicks;
        }
    }

    public record Target(Player player, List<Position> feetPositions, BlockPos position) {
    }

    public record Position(BlockPos position, boolean feetPosition) {
    }
}

