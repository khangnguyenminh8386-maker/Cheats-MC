/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundEntityEventPacket
 *  net.minecraft.resources.Identifier
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityTypes
 *  net.minecraft.world.entity.LightningBolt
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Vector3f
 */
package night.modules.impl.visuals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.RenderWorldEvent;
import night.events.impl.TickEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.miscellaneous.FakePlayerModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.graphics.KillEffectMemeRenderer;
import night.utils.minecraft.EntityUtils;
import org.joml.Vector3f;

@RegisterModule(name="DeathEffects", description="Renders certain effects on players when they die or disconnect.", category=Module.Category.VISUALS)
public class DeathEffectsModule
extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "The effect that will be rendered.", "Both", new String[]{"Lightning", "Overlay", "Meme", "Both"});
    public BooleanSetting lightningOnKill = new BooleanSetting("LightningOnKill", "Spawn lightning bolt when a player dies near you.", new ModeSetting.Visibility(this.mode, "Lightning", "Both"), true);
    public BooleanSetting lightningOnDisconnect = new BooleanSetting("LightningOnDisconnect", "Spawn lightning bolt when a player disconnects near you.", new ModeSetting.Visibility(this.mode, "Lightning", "Both"), true);
    public NumberSetting lightningAmount = new NumberSetting("LightningAmount", "The amount of lightning bolts to spawn.", new ModeSetting.Visibility(this.mode, "Lightning", "Both"), (Number)1, (Number)1, (Number)10);
    public NumberSetting disconnectRange = new NumberSetting("DisconnectRange", "Range to detect player disconnects.", 64.0, 1.0, 256.0);
    public BooleanSetting overlayOnKill = new BooleanSetting("OverlayOnKill", "Show freeze overlay when a player dies near you.", new ModeSetting.Visibility(this.mode, "Overlay"), true);
    public BooleanSetting overlayOnDisconnect = new BooleanSetting("OverlayOnDisconnect", "Show freeze overlay when a player disconnects near you.", new ModeSetting.Visibility(this.mode, "Overlay"), true);
    public NumberSetting overlayDuration = new NumberSetting("OverlayDuration", "Duration of the freeze overlay in milliseconds.", new ModeSetting.Visibility(this.mode, "Overlay"), (Number)3000, (Number)100, (Number)10000);
    public NumberSetting overlayFadeDuration = new NumberSetting("OverlayFadeDuration", "Fade duration of the freeze overlay in milliseconds.", new ModeSetting.Visibility(this.mode, "Overlay"), (Number)2000, (Number)100, (Number)5000);
    public NumberSetting overlayAlpha = new NumberSetting("OverlayAlpha", "Transparency of the freeze overlay (0-255, higher = more transparent).", new ModeSetting.Visibility(this.mode, "Overlay"), (Number)200, (Number)0, (Number)255);
    public BooleanSetting memeOnKill = new BooleanSetting("MemeOnKill", "Show the meme effect when a player dies near you.", new ModeSetting.Visibility(this.mode, "Meme", "Both"), true);
    public BooleanSetting memeOnDisconnect = new BooleanSetting("MemeOnDisconnect", "Show the meme effect when a player disconnects near you.", new ModeSetting.Visibility(this.mode, "Meme", "Both"), true);
    public NumberSetting memeSize = new NumberSetting("MemeSize", "Half-size of the circle/arrow, in blocks.", new ModeSetting.Visibility(this.mode, "Meme", "Both"), (Number)1.0, (Number)0.3, (Number)3.0);
    private static final Identifier VINE_SOUND_ID = Identifier.fromNamespaceAndPath((String)"night", (String)"vine");
    private static final SoundEvent VINE_SOUND = SoundEvent.createVariableRangeEvent((Identifier)VINE_SOUND_ID);
    private final Map<UUID, Vec3> lastPlayerPositions = new HashMap<UUID, Vec3>();
    private final Set<UUID> trackedPlayers = new HashSet<UUID>();
    private long freezeEndTime = 0L;
    private boolean frostActive = false;
    private int nextLightningId = -90000;

    @Override
    public void onEnable() {
        this.lastPlayerPositions.clear();
        this.trackedPlayers.clear();
        this.freezeEndTime = 0L;
        this.frostActive = false;
    }

    @Override
    public void onDisable() {
        if (DeathEffectsModule.mc.player != null) {
            DeathEffectsModule.mc.player.setTicksFrozen(0);
        }
        this.frostActive = false;
        this.freezeEndTime = 0L;
        this.lastPlayerPositions.clear();
        this.trackedPlayers.clear();
    }

    private boolean isInvalidTarget(Player player) {
        FakePlayerModule fakePlayer;
        if (player == null || player == DeathEffectsModule.mc.player) {
            return true;
        }
        if (player.getId() < 0) {
            return true;
        }
        if (EntityUtils.isGhost((Entity)player)) {
            return true;
        }
        FakePlayerModule fakePlayerModule = fakePlayer = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FakePlayerModule.class) : null;
        return fakePlayer != null && fakePlayer.isToggled() && player == fakePlayer.getPlayer();
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        ClientboundEntityEventPacket packet;
        if (DeathEffectsModule.mc.level == null || DeathEffectsModule.mc.player == null) {
            return;
        }
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof ClientboundEntityEventPacket && (packet = (ClientboundEntityEventPacket)packet2).getEventId() == 3) {
            mc.execute(() -> {
                if (DeathEffectsModule.mc.level == null || DeathEffectsModule.mc.player == null) {
                    return;
                }
                try {
                    double distance;
                    Player player;
                    Entity entity = packet.getEntity((Level)DeathEffectsModule.mc.level);
                    if (entity instanceof Player && !this.isInvalidTarget(player = (Player)entity) && (distance = DeathEffectsModule.mc.player.distanceToSqr((Entity)player)) <= 100.0) {
                        if ((this.mode.getValue().equals("Lightning") || this.mode.getValue().equals("Both")) && this.lightningOnKill.getValue()) {
                            this.spawnLightning(new Vec3(player.getX(), player.getY(), player.getZ()));
                        }
                        if (this.mode.getValue().equals("Overlay") && this.overlayOnKill.getValue()) {
                            this.startOverlay();
                        }
                        if ((this.mode.getValue().equals("Meme") || this.mode.getValue().equals("Both")) && this.memeOnKill.getValue()) {
                            this.spawnMeme(new Vec3(player.getX(), player.getY() + (double)player.getBbHeight() * 0.5, player.getZ()));
                        }
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            });
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (DeathEffectsModule.mc.player == null || DeathEffectsModule.mc.level == null) {
            return;
        }
        this.updateFrostEffect();
        HashSet<UUID> currentPlayerUuids = new HashSet<UUID>();
        for (Player player : DeathEffectsModule.mc.level.players()) {
            if (this.isInvalidTarget(player)) continue;
            currentPlayerUuids.add(player.getUUID());
            this.lastPlayerPositions.put(player.getUUID(), new Vec3(player.getX(), player.getY(), player.getZ()));
        }
        for (UUID uuid : new HashSet<UUID>(this.trackedPlayers)) {
            if (currentPlayerUuids.contains(uuid) || !this.lastPlayerPositions.containsKey(uuid)) continue;
            Vec3 lastPos = this.lastPlayerPositions.get(uuid);
            double distance = Math.sqrt(Math.pow(DeathEffectsModule.mc.player.getX() - lastPos.x, 2.0) + Math.pow(DeathEffectsModule.mc.player.getY() - lastPos.y, 2.0) + Math.pow(DeathEffectsModule.mc.player.getZ() - lastPos.z, 2.0));
            if (distance <= this.disconnectRange.getValue().doubleValue()) {
                if ((this.mode.getValue().equals("Meme") || this.mode.getValue().equals("Both")) && this.memeOnDisconnect.getValue()) {
                    this.spawnMeme(lastPos.add(0.0, 0.9, 0.0));
                }
                if ((this.mode.getValue().equals("Lightning") || this.mode.getValue().equals("Both")) && this.lightningOnDisconnect.getValue()) {
                    this.spawnLightning(lastPos);
                }
                if (this.mode.getValue().equals("Overlay") && this.overlayOnDisconnect.getValue()) {
                    this.startOverlay();
                }
            }
            this.lastPlayerPositions.remove(uuid);
        }
        this.trackedPlayers.clear();
        this.trackedPlayers.addAll(currentPlayerUuids);
    }

    private void updateFrostEffect() {
        if (this.frostActive && DeathEffectsModule.mc.player != null) {
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - (this.freezeEndTime - this.overlayDuration.getValue().longValue() - this.overlayFadeDuration.getValue().longValue());
            long totalDuration = this.overlayDuration.getValue().longValue() + this.overlayFadeDuration.getValue().longValue();
            if (elapsed < this.overlayDuration.getValue().longValue()) {
                int frozenTicks = (int)(140.0 * (this.overlayAlpha.getValue().doubleValue() / 255.0));
                DeathEffectsModule.mc.player.setTicksFrozen(frozenTicks);
            } else if (elapsed < totalDuration) {
                long fadeElapsed = elapsed - this.overlayDuration.getValue().longValue();
                double fadeProgress = 1.0 - (double)fadeElapsed / (double)this.overlayFadeDuration.getValue().longValue();
                int frozenTicks = (int)(140.0 * (this.overlayAlpha.getValue().doubleValue() / 255.0) * fadeProgress);
                DeathEffectsModule.mc.player.setTicksFrozen(frozenTicks);
            } else {
                this.frostActive = false;
                this.freezeEndTime = 0L;
                DeathEffectsModule.mc.player.setTicksFrozen(0);
            }
        } else if (DeathEffectsModule.mc.player != null && DeathEffectsModule.mc.player.getTicksFrozen() > 0 && DeathEffectsModule.mc.level != null && !DeathEffectsModule.mc.level.getBlockState(DeathEffectsModule.mc.player.blockPosition()).is(Blocks.POWDER_SNOW)) {
            DeathEffectsModule.mc.player.setTicksFrozen(0);
        }
    }

    private void startOverlay() {
        this.frostActive = true;
        this.freezeEndTime = System.currentTimeMillis();
    }

    private void spawnLightning(Vec3 pos) {
        if (DeathEffectsModule.mc.level == null) {
            return;
        }
        if (!mc.isSameThread()) {
            mc.execute(() -> this.spawnLightning(pos));
            return;
        }
        int count = this.lightningAmount.getValue().intValue();
        for (int i = 0; i < count; ++i) {
            LightningBolt lightning = new LightningBolt(EntityTypes.LIGHTNING_BOLT, (Level)DeathEffectsModule.mc.level);
            lightning.setPos(pos.x, pos.y, pos.z);
            lightning.setId(this.nextLightningId--);
            DeathEffectsModule.mc.level.addEntity((Entity)lightning);
        }
    }

    private void spawnMeme(Vec3 pos) {
        KillEffectMemeRenderer.INSTANCE.spawn(new Vector3f((float)pos.x, (float)pos.y, (float)pos.z), this.memeSize.getValue().floatValue());
        if (mc.getSoundManager() != null) {
            mc.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)VINE_SOUND, (float)1.0f, (float)1.0f));
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (!this.mode.getValue().equals("Meme") && !this.mode.getValue().equals("Both")) {
            return;
        }
        KillEffectMemeRenderer.INSTANCE.render(event.getMatrices());
    }
}

