/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.client.player.RemotePlayer
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  net.minecraft.network.protocol.game.ClientboundRespawnPacket
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionfc
 */
package night.modules.impl.visuals;
import java.util.Map.Entry;
import net.minecraft.world.entity.Entity.RemovalReason;


import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.ClientConnectEvent;
import night.events.impl.ClientDisconnectEvent;
import night.events.impl.EntitySpawnEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PlayerConnectEvent;
import night.events.impl.PlayerDeathEvent;
import night.events.impl.PlayerDisconnectEvent;
import night.events.impl.RenderWorldEvent;
import night.events.impl.ServerConnectEvent;
import night.events.impl.TickEvent;
import night.mixins.accessors.LimbAnimatorAccessor;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.core.IgnoreNakedModule;
import night.modules.impl.miscellaneous.FakePlayerModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.utils.color.ColorUtils;
import night.utils.graphics.Renderer3D;
import night.utils.minecraft.EntityUtils;
import org.joml.Quaternionfc;

@RegisterModule(name="LogoutSpot", description="Renders a frozen chams model and nametag where players logged out.", category=Module.Category.VISUALS)
public class LogoutSpotModule
extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "The rendering that will be applied to the logout chams.", "Both", new String[]{"Fill", "Outline", "Both"});
    public ColorSetting fillColor = new ColorSetting("FillColor", "The color used for the fill rendering.", new ModeSetting.Visibility(this.mode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "The color used for the outline rendering.", new ModeSetting.Visibility(this.mode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    public BooleanSetting nametag = new BooleanSetting("Nametag", "Renders player info above the logout spot.", true);
    public BooleanSetting health = new BooleanSetting("Health", "Renders the health of the logged out player.", true);
    public BooleanSetting totemPops = new BooleanSetting("TotemPops", "Renders the popped totems count of the logged out player.", true);
    public NumberSetting scale = new NumberSetting("Scale", "The scaling applied to the nametag rendering.", 30, 10, 100);
    private static final EquipmentSlot[] COPIED_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final Map<UUID, Spot> spots = new ConcurrentHashMap<UUID, Spot>();
    private final Map<RemotePlayer, Spot> ghosts = new ConcurrentHashMap<RemotePlayer, Spot>();
    private final Map<UUID, TrackedPlayer> trackedPlayers = new ConcurrentHashMap<UUID, TrackedPlayer>();
    private int nextId = -80000;
    private static final long DISCONNECT_DEBOUNCE_MS = 1500L;
    private final Map<UUID, Long> pendingDisconnects = new ConcurrentHashMap<UUID, Long>();
    private final Map<UUID, Long> recentPearlThrowers = new ConcurrentHashMap<UUID, Long>();
    private final Map<Integer, UUID> activePearls = new ConcurrentHashMap<Integer, UUID>();
    private long lastLocalTeleportTime = 0L;
    private Vec3 lastLocalPos = null;

    private boolean hasRecentlyPearled(UUID id) {
        if (id == null) {
            return false;
        }
        Long throwTime = this.recentPearlThrowers.get(id);
        if (throwTime != null && System.currentTimeMillis() - throwTime < 12000L) {
            return true;
        }
        for (UUID thrower : this.activePearls.values()) {
            if (!id.equals(thrower)) continue;
            return true;
        }
        return false;
    }

    private void onLocalPlayerTeleport() {
        this.lastLocalTeleportTime = System.currentTimeMillis();
        this.pendingDisconnects.clear();
        this.trackedPlayers.clear();
    }

    public boolean isPlayerInServer(UUID id, String name) {
        if (id == null && name == null) {
            return false;
        }
        if (LogoutSpotModule.mc.level != null) {
            if (id != null && LogoutSpotModule.mc.level.getPlayerByUUID(id) != null) {
                return true;
            }
            if (name != null) {
                for (Player player : LogoutSpotModule.mc.level.players()) {
                    if (this.isGhost((Entity)player) || !name.equalsIgnoreCase(player.getName().getString())) continue;
                    return true;
                }
            }
        }
        if (mc.getConnection() != null) {
            if (id != null && mc.getConnection().getPlayerInfo(id) != null) {
                return true;
            }
            if (name != null && mc.getConnection().getPlayerInfo(name) != null) {
                return true;
            }
            for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
                if (info == null || info.getProfile() == null) continue;
                if (id != null && id.equals(info.getProfile().id())) {
                    return true;
                }
                if (name == null || !name.equalsIgnoreCase(info.getProfile().name())) continue;
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
   public void onTick(TickEvent event) {
      if (mc.level != null && mc.player != null) {
         if (this.lastLocalPos != null && mc.player.position().distanceToSqr(this.lastLocalPos) > 100.0) {
            this.onLocalPlayerTeleport();
         }

         this.lastLocalPos = mc.player.position();
         long now = System.currentTimeMillis();
         this.recentPearlThrowers.entrySet().removeIf(entryx -> now - (Long)entryx.getValue() > 15000L);
         this.activePearls.entrySet().removeIf(entryx -> mc.level.getEntity((Integer)entryx.getKey()) == null);
         FakePlayerModule fakePlayer = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FakePlayerModule.class) : null;
         Player fake = fakePlayer != null && fakePlayer.isToggled() ? fakePlayer.getPlayer() : null;
         boolean ignoreNaked = Night.MODULE_MANAGER.getModule(IgnoreNakedModule.class).isToggled();

         for (Player player : new ArrayList<>(mc.level.players())) {
            if (player != mc.player && player != fake && !this.isGhost(player) && player.isAlive() && (!ignoreNaked || !EntityUtils.isNaked(player))) {
               UUID uuid = player.getUUID();
               this.pendingDisconnects.remove(uuid);
               if (this.spots.containsKey(uuid)) {
                  this.removeSpot(uuid);
               }

               boolean holdingPearl = player.getMainHandItem().getItem() == Items.ENDER_PEARL || player.getOffhandItem().getItem() == Items.ENDER_PEARL;
               if (holdingPearl && (player.swinging || player.isUsingItem())) {
                  this.recentPearlThrowers.put(uuid, now);
               }

               int pops = Night.WORLD_MANAGER != null ? Night.WORLD_MANAGER.getPoppedTotems().getOrDefault(uuid, 0) : 0;
               this.trackedPlayers.put(uuid, new LogoutSpotModule.TrackedPlayer(player, pops));
            }
         }

         if (!this.pendingDisconnects.isEmpty()) {
            for (Entry<UUID, Long> entry : this.pendingDisconnects.entrySet()) {
               UUID id = entry.getKey();
               LogoutSpotModule.TrackedPlayer tracked = this.trackedPlayers.get(id);
               String name = tracked != null ? tracked.name : null;
               if (this.isPlayerInServer(id, name)) {
                  this.pendingDisconnects.remove(id);
               } else if (this.hasRecentlyPearled(id)) {
                  this.pendingDisconnects.remove(id);
               } else if (now - entry.getValue() >= 1500L) {
                  this.pendingDisconnects.remove(id);
                  this.spawnGhost(id);
               }
            }
         }

         if (!this.spots.isEmpty()) {
            for (Entry<UUID, LogoutSpotModule.Spot> entry : this.spots.entrySet()) {
               UUID id = entry.getKey();
               LogoutSpotModule.Spot spot = entry.getValue();
               String name = spot.data != null ? spot.data.name : null;
               if (this.isPlayerInServer(id, name)) {
                  this.removeSpot(id);
               }
            }
         }
      }
   }

    @SubscribeEvent
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        FakePlayerModule fakePlayer;
        if (!this.isToggled() || LogoutSpotModule.mc.level == null || LogoutSpotModule.mc.player == null) {
            return;
        }
        UUID id = event.getId();
        if (id == null || id.equals(LogoutSpotModule.mc.player.getUUID())) {
            return;
        }
        if (System.currentTimeMillis() - this.lastLocalTeleportTime < 3000L) {
            return;
        }
        FakePlayerModule fakePlayerModule = fakePlayer = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FakePlayerModule.class) : null;
        if (fakePlayer != null && fakePlayer.isToggled() && fakePlayer.getPlayer() != null && id.equals(fakePlayer.getPlayer().getUUID())) {
            return;
        }
        if (!this.trackedPlayers.containsKey(id)) {
            return;
        }
        if (this.hasRecentlyPearled(id)) {
            this.trackedPlayers.remove(id);
            return;
        }
        this.pendingDisconnects.put(id, System.currentTimeMillis());
    }

    private void spawnGhost(UUID id) {
        TrackedPlayer tracked = this.trackedPlayers.get(id);
        if (tracked == null) {
            return;
        }
        mc.execute(() -> {
            if (LogoutSpotModule.mc.level == null || this.spots.containsKey(id)) {
                return;
            }
            if (this.isPlayerInServer(id, tracked.name)) {
                return;
            }
            if (this.hasRecentlyPearled(id)) {
                return;
            }
            GameProfile profile = new GameProfile(UUID.randomUUID(), tracked.name);
            RemotePlayer ghost = new RemotePlayer(LogoutSpotModule.mc.level, profile);
            ghost.setId(this.nextId--);
            ghost.setPos(tracked.x, tracked.y, tracked.z);
            ghost.xo = tracked.xo;
            ghost.yo = tracked.yo;
            ghost.zo = tracked.zo;
            ghost.xOld = tracked.x;
            ghost.yOld = tracked.y;
            ghost.zOld = tracked.z;
            ghost.setYRot(tracked.yRot);
            ghost.setXRot(tracked.xRot);
            ghost.yRotO = tracked.yRot;
            ghost.xRotO = tracked.xRot;
            ghost.setYHeadRot(tracked.yHeadRot);
            ghost.yHeadRotO = tracked.yHeadRot;
            ghost.yBodyRot = tracked.yBodyRot;
            ghost.yBodyRotO = tracked.yBodyRot;
            ghost.setPose(tracked.pose);
            ghost.setShiftKeyDown(tracked.shift);
            ghost.setSwimming(tracked.swim);
            ghost.setSprinting(tracked.sprint);
            ghost.refreshDimensions();
            ghost.walkAnimation.setSpeed(tracked.walkSpeed);
            ((LimbAnimatorAccessor)ghost.walkAnimation).setPos(tracked.walkPos);
            ghost.swinging = tracked.swinging;
            ghost.swingTime = tracked.swingTime;
            ghost.swingingArm = tracked.swingingArm;
            ghost.attackAnim = tracked.attackAnim;
            ghost.oAttackAnim = tracked.oAttackAnim;
            ghost.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            ghost.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            for (EquipmentSlot slot : COPIED_SLOTS) {
                ghost.setItemSlot(slot, tracked.armor.getOrDefault(slot, ItemStack.EMPTY).copy());
            }
            LogoutSpotModule.mc.level.addEntity((Entity)ghost);
            Spot spot = new Spot(ghost, tracked);
            this.spots.put(id, spot);
            this.ghosts.put(ghost, spot);
        });
    }

    @SubscribeEvent
    public void onPlayerConnect(PlayerConnectEvent event) {
        UUID id = event.getId();
        if (id == null) {
            return;
        }
        this.pendingDisconnects.remove(id);
        if (this.spots.containsKey(id)) {
            mc.execute(() -> this.removeSpot(id));
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getPlayer() != LogoutSpotModule.mc.player) {
            return;
        }
        this.clearAll();
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientDisconnectEvent event) {
        this.clearAll();
    }

    @SubscribeEvent
    public void onClientConnect(ClientConnectEvent event) {
        this.clearAll();
    }

    @SubscribeEvent
    public void onServerConnect(ServerConnectEvent event) {
        this.clearAll();
    }

    @SubscribeEvent
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (LogoutSpotModule.mc.level == null) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof ThrownEnderpearl) {
            ThrownEnderpearl pearl = (ThrownEnderpearl)entity;
            UUID throwerId = null;
            Entity entity2 = pearl.getOwner();
            if (entity2 instanceof Player) {
                Player player = (Player)entity2;
                if (player != LogoutSpotModule.mc.player && !this.isGhost((Entity)player)) {
                    throwerId = player.getUUID();
                }
            } else {
                double minDistanceSq = 36.0;
                for (Player player : LogoutSpotModule.mc.level.players()) {
                    double distSq;
                    if (player == LogoutSpotModule.mc.player || this.isGhost((Entity)player) || !((distSq = player.distanceToSqr(pearl.position())) < minDistanceSq)) continue;
                    minDistanceSq = distSq;
                    throwerId = player.getUUID();
                }
            }
            if (throwerId != null) {
                this.recentPearlThrowers.put(throwerId, System.currentTimeMillis());
                this.activePearls.put(pearl.getId(), throwerId);
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacket() instanceof ClientboundRespawnPacket) {
            mc.execute(this::clearAll);
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof ClientboundPlayerPositionPacket) {
            double pz;
            double py;
            double px;
            Vec3 current;
            ClientboundPlayerPositionPacket packet2 = (ClientboundPlayerPositionPacket)packet;
            if (LogoutSpotModule.mc.player != null && (current = LogoutSpotModule.mc.player.position()).distanceToSqr(px = packet2.change().position().x(), py = packet2.change().position().y(), pz = packet2.change().position().z()) > 100.0) {
                mc.execute(this::onLocalPlayerTeleport);
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent.Post event) {
        if (!this.isToggled() || LogoutSpotModule.mc.level == null || LogoutSpotModule.mc.player == null) {
            return;
        }
        if (!this.nametag.getValue() || this.spots.isEmpty()) {
            return;
        }
        PoseStack matrices = event.getMatrices();
        for (Spot spot : this.spots.values()) {
            RemotePlayer ghost = spot.ghost;
            if (ghost == null) continue;
            double x = ghost.getX();
            double y = ghost.getY() + (double)(ghost.isShiftKeyDown() ? 1.9f : 2.1f);
            double z = ghost.getZ();
            Vec3 vec3d = new Vec3(x - LogoutSpotModule.mc.gameRenderer.mainCamera().position().x, y - LogoutSpotModule.mc.gameRenderer.mainCamera().position().y, z - LogoutSpotModule.mc.gameRenderer.mainCamera().position().z);
            float distance = (float)Math.sqrt(LogoutSpotModule.mc.gameRenderer.mainCamera().position().distanceToSqr(x, y, z));
            float scaling = 0.0018f + (float)this.scale.getValue().intValue() / 10000.0f * distance;
            if (distance <= 8.0f) {
                scaling = 0.0245f;
            }
            matrices.pushPose();
            matrices.translate(vec3d.x, vec3d.y, vec3d.z);
            matrices.mulPose((Quaternionfc)LogoutSpotModule.mc.gameRenderer.mainCamera().rotation());
            matrices.scale(scaling, -scaling, scaling);
            StringBuilder text = new StringBuilder(spot.data.name);
            if (this.health.getValue()) {
                float totalHp = spot.data.health + spot.data.absorption;
                text.append(" HP: ").append(new DecimalFormat("0.0").format(totalHp));
            }
            if (this.totemPops.getValue() && spot.data.totemPops > 0) {
                text.append(" -").append(spot.data.totemPops);
            }
            String fullText = text.toString();
            int width = Night.FONT_MANAGER.getWidth(fullText);
            int fontHeight = Night.FONT_MANAGER.getHeight();
            Renderer3D.renderQuad(matrices, (float)(-width) / 2.0f - 2.0f, -fontHeight - 1, (float)width / 2.0f + 2.0f, 1.0f, new Color(0, 0, 0, 120));
            Renderer3D.renderOutline(matrices, (float)(-width) / 2.0f - 2.0f, -fontHeight - 1, (float)width / 2.0f + 2.0f, 1.0f, new Color(0, 0, 0, 160));
            Renderer3D.draw(Renderer3D.QUADS, Renderer3D.DEBUG_LINES, false);
            Renderer3D.QUADS.clear();
            Renderer3D.DEBUG_LINES.clear();
            Night.FONT_MANAGER.drawText(matrices, fullText, -width / 2, -fontHeight, ColorUtils.getColor(this.fillColor.getColor(), 255));
            matrices.popPose();
        }
    }

    @Override
    public void onDisable() {
        this.clearAll();
    }

    private void removeSpot(UUID id) {
        Spot spot = this.spots.remove(id);
        if (spot != null) {
            this.ghosts.remove(spot.ghost);
            this.despawn(spot.ghost);
        }
    }

    private void clearAll() {
        this.spots.values().forEach(s -> this.despawn(s.ghost));
        this.spots.clear();
        this.ghosts.clear();
        this.trackedPlayers.clear();
        this.pendingDisconnects.clear();
        this.recentPearlThrowers.clear();
        this.activePearls.clear();
        this.lastLocalPos = null;
    }

    private void despawn(RemotePlayer ghost) {
        if (ghost == null) {
            return;
        }
        Runnable action = () -> {
            if (LogoutSpotModule.mc.level != null) {
                LogoutSpotModule.mc.level.removeEntity(ghost.getId(), Entity.RemovalReason.DISCARDED);
            }
        };
        if (mc.isSameThread()) {
            action.run();
        } else {
            mc.execute(action);
        }
    }

    public boolean isGhost(Entity entity) {
        return entity instanceof RemotePlayer && this.ghosts.containsKey(entity);
    }

    public Collection<RemotePlayer> getGhosts() {
        return this.ghosts.keySet();
    }

    public Spot getSpot(RemotePlayer ghost) {
        return this.ghosts.get(ghost);
    }

    public boolean shouldFill() {
        return this.mode.getValue().equals("Fill") || this.mode.getValue().equals("Both");
    }

    public boolean shouldOutline() {
        return this.mode.getValue().equals("Outline") || this.mode.getValue().equals("Both");
    }

    public Color getFillColor(RemotePlayer ghost) {
        return this.fillColor.getColor();
    }

    public Color getOutlineColor(RemotePlayer ghost) {
        return this.outlineColor.getColor();
    }

    public static class TrackedPlayer {
        public final String name;
        public final double x;
        public final double y;
        public final double z;
        public final double xo;
        public final double yo;
        public final double zo;
        public final float yRot;
        public final float xRot;
        public final float yHeadRot;
        public final float yBodyRot;
        public final Pose pose;
        public final boolean shift;
        public final boolean swim;
        public final boolean sprint;
        public final float walkSpeed;
        public final float walkPos;
        public final boolean swinging;
        public final int swingTime;
        public final InteractionHand swingingArm;
        public final float attackAnim;
        public final float oAttackAnim;
        public final ItemStack mainHand;
        public final ItemStack offHand;
        public final Map<EquipmentSlot, ItemStack> armor = new ConcurrentHashMap<EquipmentSlot, ItemStack>();
        public final float health;
        public final float absorption;
        public final int totemPops;

        public TrackedPlayer(Player player, int totemPops) {
            this.name = player.getName().getString();
            this.x = player.getX();
            this.y = player.getY();
            this.z = player.getZ();
            this.xo = player.xo;
            this.yo = player.yo;
            this.zo = player.zo;
            this.yRot = player.getYRot();
            this.xRot = player.getXRot();
            this.yHeadRot = player.getYHeadRot();
            this.yBodyRot = player.yBodyRot;
            this.pose = player.getPose();
            this.shift = player.isShiftKeyDown();
            this.swim = player.isSwimming();
            this.sprint = player.isSprinting();
            this.walkSpeed = player.walkAnimation.speed();
            this.walkPos = player.walkAnimation.position();
            this.swinging = player.swinging;
            this.swingTime = player.swingTime;
            this.swingingArm = player.swingingArm;
            this.attackAnim = player.attackAnim;
            this.oAttackAnim = player.oAttackAnim;
            this.mainHand = player.getMainHandItem().copy();
            this.offHand = player.getOffhandItem().copy();
            for (EquipmentSlot slot : COPIED_SLOTS) {
                this.armor.put(slot, player.getItemBySlot(slot).copy());
            }
            this.health = player.getHealth();
            this.absorption = player.getAbsorptionAmount();
            this.totemPops = totemPops;
        }
    }

    public static class Spot {
        public final RemotePlayer ghost;
        public final TrackedPlayer data;
        public final long timestamp;

        public Spot(RemotePlayer ghost, TrackedPlayer data) {
            this.ghost = ghost;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
    }
}

