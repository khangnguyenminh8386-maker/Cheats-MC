/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.Pair
 *  it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair
 *  lombok.Generated
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.client.multiplayer.ServerData
 *  net.minecraft.client.multiplayer.resolver.ServerAddress
 *  net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket
 *  net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
 *  net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Action
 *  net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Entry
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  net.minecraft.network.protocol.game.ClientboundSetTimePacket
 */
package night.managers;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import java.util.Arrays;
import java.util.UUID;
import lombok.Generated;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.ClientConnectEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PlayerConnectEvent;
import night.events.impl.PlayerDisconnectEvent;
import night.events.impl.ServerConnectEvent;
import night.modules.impl.miscellaneous.FastLatencyModule;
import night.pingbypass.PingBypassFlags;
import night.utils.IMinecraft;
import night.utils.system.Timer;

public class ServerManager
implements IMinecraft {
    private final Timer setbackTimer = new Timer();
    private final Timer responseTimer = new Timer();
    private final float[] tickRates = new float[20];
    private int nextIndex = 0;
    private long lastUpdate = -1L;
    private long timeJoined;
    private Pair<ServerAddress, ServerData> lastConnection;

    public ServerManager() {
        Night.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        this.responseTimer.reset();
        if (event.getPacket() instanceof ClientboundPlayerPositionPacket) {
            this.setbackTimer.reset();
        }
        if (event.getPacket() instanceof ClientboundSetTimePacket) {
            this.tickRates[this.nextIndex] = Math.clamp(20.0f / ((float)(System.currentTimeMillis() - this.lastUpdate) / 1000.0f), 0.0f, 20.0f);
            this.nextIndex = (this.nextIndex + 1) % this.tickRates.length;
            this.lastUpdate = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public void onClientConnect(ClientConnectEvent event) {
        Arrays.fill(this.tickRates, 0.0f);
        this.nextIndex = 0;
        this.timeJoined = System.currentTimeMillis();
        this.lastUpdate = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void handleConnections(PacketReceiveEvent event) {
        block5: {
            Object object;
            block4: {
                if (ServerManager.mc.level == null) {
                    return;
                }
                object = event.getPacket();
                if (!(object instanceof ClientboundPlayerInfoUpdatePacket)) break block4;
                ClientboundPlayerInfoUpdatePacket packet = (ClientboundPlayerInfoUpdatePacket)object;
                if (!packet.actions().contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER)) break block5;
                for (ClientboundPlayerInfoUpdatePacket.Entry entry : packet.newEntries()) {
                    Night.EVENT_HANDLER.post(new PlayerConnectEvent(entry.profile().id()));
                }
                break block5;
            }
            object = event.getPacket();
            if (object instanceof ClientboundPlayerInfoRemovePacket) {
                ClientboundPlayerInfoRemovePacket packet = (ClientboundPlayerInfoRemovePacket)object;
                for (UUID id : packet.profileIds()) {
                    Night.EVENT_HANDLER.post(new PlayerDisconnectEvent(id));
                }
            }
        }
    }

    @SubscribeEvent
    public void onServerConnect(ServerConnectEvent event) {
        this.lastConnection = new ObjectObjectImmutablePair((Object)event.getAddress(), (Object)event.getInfo());
    }

    public float getTickRate() {
        if (ServerManager.mc.player == null) {
            return 0.0f;
        }
        if (System.currentTimeMillis() - this.timeJoined < 4000L) {
            return 20.0f;
        }
        int ticks = 0;
        float tickRates = 0.0f;
        for (float tickRate : this.tickRates) {
            if (!(tickRate > 0.0f)) continue;
            tickRates += tickRate;
            ++ticks;
        }
        return tickRates / (float)ticks;
    }

    public int getPingDelay() {
        return (int)((float)this.getPing() / 25.0f);
    }

    public int getPing() {
        if (Night.MODULE_MANAGER.getModule(FastLatencyModule.class).isToggled()) {
            return Night.MODULE_MANAGER.getModule(FastLatencyModule.class).getLatency();
        }
        if (mc.getConnection() == null || ServerManager.mc.player == null) {
            return 0;
        }
        PlayerInfo entry = mc.getConnection().getPlayerInfo(ServerManager.mc.player.getUUID());
        if (entry != null) {
            return entry.getLatency();
        }
        if (PingBypassFlags.proxyForwardingActive) {
            for (PlayerInfo e : mc.getConnection().getOnlinePlayers()) {
                if (!e.getProfile().name().equals(ServerManager.mc.player.getName().getString())) continue;
                return e.getLatency();
            }
            for (PlayerInfo e : mc.getConnection().getOnlinePlayers()) {
                if (e.getLatency() <= 0) continue;
                return e.getLatency();
            }
        }
        return 0;
    }

    public String getServerBrand() {
        if (mc.getCurrentServer() == null || mc.getConnection() == null || mc.getConnection().serverBrand() == null) {
            return "Vanilla";
        }
        return mc.getConnection().serverBrand();
    }

    public String getServer() {
        return mc.hasSingleplayerServer() ? "Singleplayer" : ServerAddress.parseString((String)ServerManager.mc.getCurrentServer().ip).getHost();
    }

    @Generated
    public Timer getSetbackTimer() {
        return this.setbackTimer;
    }

    @Generated
    public Timer getResponseTimer() {
        return this.responseTimer;
    }

    @Generated
    public float[] getTickRates() {
        return this.tickRates;
    }

    @Generated
    public int getNextIndex() {
        return this.nextIndex;
    }

    @Generated
    public long getLastUpdate() {
        return this.lastUpdate;
    }

    @Generated
    public long getTimeJoined() {
        return this.timeJoined;
    }

    @Generated
    public Pair<ServerAddress, ServerData> getLastConnection() {
        return this.lastConnection;
    }
}

