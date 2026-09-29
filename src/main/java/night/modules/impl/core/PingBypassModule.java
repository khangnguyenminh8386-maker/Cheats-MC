/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.ConnectScreen
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.TitleScreen
 *  net.minecraft.client.multiplayer.ServerData
 *  net.minecraft.client.multiplayer.ServerData$Type
 *  net.minecraft.client.multiplayer.resolver.ServerAddress
 *  net.minecraft.network.Connection
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.world.entity.player.Player
 */
package night.modules.impl.core;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.ClientDisconnectEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.SettingChangeEvent;
import night.events.impl.TickEvent;
import night.gui.ClickGuiScreen;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.player.SpeedMineModule;
import night.pingbypass.PingBypassFlags;
import night.pingbypass.input.ClientInputForwarder;
import night.pingbypass.protocol.PbCustomPayload;
import night.pingbypass.protocol.PbPacket;
import night.pingbypass.protocol.packets.C2SFriendSyncPacket;
import night.pingbypass.protocol.packets.C2SJoinPacket;
import night.pingbypass.protocol.packets.C2SModuleTogglePacket;
import night.pingbypass.protocol.packets.C2SPasswordPacket;
import night.pingbypass.protocol.packets.C2SSettingChangePacket;
import night.pingbypass.protocol.packets.S2CBlockRenderPacket;
import night.pingbypass.protocol.packets.S2CErrorPacket;
import night.pingbypass.protocol.packets.S2CMiningStatePacket;
import night.pingbypass.protocol.packets.S2CModuleStatePacket;
import night.pingbypass.protocol.packets.S2CRenderPositionPacket;
import night.pingbypass.protocol.packets.S2CServerNamePacket;
import night.pingbypass.protocol.packets.S2CSettingStatePacket;
import night.pingbypass.protocol.packets.S2CSlotSyncPacket;
import night.pingbypass.protocol.packets.S2CWindowClickPacket;
import night.settings.Setting;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.settings.impl.StringSetting;
import night.utils.miscellaneous.RenderPosition;

@RegisterModule(name="PingBypass", description="Connects to a PingBypass proxy server for low-latency combat.", category=Module.Category.CORE)
public class PingBypassModule
extends Module {
    public final StringSetting ip = new StringSetting("IP", "The IP address of the PingBypass proxy server.", "127.0.0.1");
    public final NumberSetting port = new NumberSetting("Port", "The port of the PingBypass proxy server.", 25565, 1, 65535);
    public final StringSetting password = new StringSetting("Password", "The password for the PingBypass proxy server.", "");
    public final StringSetting server = new StringSetting("Server", "The target Minecraft server to join through the proxy (e.g. mc.hypixel.net or mc.hypixel.net:25565).", "");
    private volatile boolean joinSent;
    public volatile int proxyPing;
    private volatile String serverName;
    private final Map<String, Boolean> proxyModuleStates = new ConcurrentHashMap<String, Boolean>();
    private final Map<String, String> proxySettingValues = new ConcurrentHashMap<String, String>();
    private volatile Connection proxyConnection;
    private final List<PendingPayload> pendingPayloads = Collections.synchronizedList(new ArrayList());
    private int pendingDelay = 10;
    private ClientInputForwarder inputForwarder;

    @Override
    public void onEnable() {
        if (Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer()) {
            Night.CHAT_MANAGER.error("Cannot enable PingBypass on a PingBypass server!");
            this.setToggled(false, false);
            return;
        }
        if (PingBypassModule.mc.gui.overlay() != null) {
            Night.LOGGER.info("[PingBypass] Skipping auto-connect during startup");
            return;
        }
        if (Night.CONFIG_MANAGER != null && Night.CONFIG_MANAGER.isLoadingConfig()) {
            Night.LOGGER.info("[PingBypass] Skipping auto-connect during config load");
            return;
        }
        if (PingBypassModule.mc.gui.screen() instanceof ClickGuiScreen) {
            PingBypassModule.mc.gui.setScreen(null);
        }
        this.proxyModuleStates.clear();
        this.proxySettingValues.clear();
        this.serverName = null;
        this.joinSent = false;
        if (PingBypassModule.mc.level != null) {
            PingBypassModule.mc.level.disconnect((Component)Component.literal((String)"PingBypass enabled."));
        }
        if (mc.getConnection() != null) {
            mc.getConnection().getConnection().disconnect((Component)Component.literal((String)"PingBypass enabled."));
        }
        String proxyIp = this.ip.getValue();
        int proxyPort = this.port.getValue().intValue();
        ServerAddress address = new ServerAddress(proxyIp, proxyPort);
        ServerData serverData = new ServerData("PingBypass Proxy", address.getHost() + ":" + address.getPort(), ServerData.Type.OTHER);
        ConnectScreen.startConnecting((Screen)new TitleScreen(), (Minecraft)mc, (ServerAddress)address, (ServerData)serverData, (boolean)false, null);
    }

    @Override
    public void onDisable() {
        if (PingBypassModule.mc.level != null) {
            PingBypassModule.mc.level.disconnect((Component)Component.literal((String)"PingBypass disabled."));
        }
        if (mc.getConnection() != null) {
            mc.getConnection().getConnection().disconnect((Component)Component.literal((String)"PingBypass disabled."));
        }
        this.proxyPing = 0;
        this.serverName = null;
        this.proxyConnection = null;
        PingBypassFlags.proxyForwardingActive = false;
        if (this.inputForwarder != null) {
            this.inputForwarder.stop();
            this.inputForwarder = null;
        }
        this.proxyModuleStates.clear();
        this.proxySettingValues.clear();
    }

    @Override
    public String getMetaData() {
        return this.proxyPing + "ms";
    }

    @SubscribeEvent
    public void onDisconnect(ClientDisconnectEvent event) {
        if (this.isToggled() && PingBypassFlags.proxyForwardingActive) {
            mc.execute(() -> this.setToggled(false, false));
        }
    }

    @SubscribeEvent
    public void onSettingChange(SettingChangeEvent event) {
        if (!this.isToggled() || !PingBypassFlags.proxyForwardingActive) {
            return;
        }
        if (Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer()) {
            return;
        }
        Setting setting = event.getSetting();
        for (Module module : Night.MODULE_MANAGER.getModules()) {
            if (!module.isProxyEnhanced() || !module.getSettings().contains(setting)) continue;
            String value = this.serializeSetting(setting);
            if (value == null) {
                return;
            }
            Connection connection = this.proxyConnection;
            if (connection == null && mc.getConnection() != null) {
                connection = mc.getConnection().getConnection();
            }
            if (connection != null && connection.isConnected()) {
                connection.send((Packet)new ServerboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(new C2SSettingChangePacket(module.getName(), setting.getName(), value))));
            }
            return;
        }
    }

    private String serializeSetting(Setting setting) {
        if (setting instanceof BooleanSetting) {
            BooleanSetting s = (BooleanSetting)setting;
            return String.valueOf(s.getValue());
        }
        if (setting instanceof NumberSetting) {
            NumberSetting s = (NumberSetting)setting;
            return s.getValue().toString();
        }
        if (setting instanceof ModeSetting) {
            ModeSetting s = (ModeSetting)setting;
            return s.getValue();
        }
        if (setting instanceof StringSetting) {
            StringSetting s = (StringSetting)setting;
            return s.getValue();
        }
        if (setting instanceof ColorSetting) {
            ColorSetting s = (ColorSetting)setting;
            Color c = s.getValue().getColor();
            return c.getRed() + "," + c.getGreen() + "," + c.getBlue() + "," + c.getAlpha() + "," + s.isSync() + "," + s.isRainbow();
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if (!(packet instanceof ClientboundCustomPayloadPacket)) return;
        ClientboundCustomPayloadPacket s2cPacket = (ClientboundCustomPayloadPacket)packet;
        CustomPacketPayload payload = s2cPacket.payload();
        if (!(payload instanceof PbCustomPayload)) return;
        PbCustomPayload pbPayload = (PbCustomPayload)payload;
        if (event.getConnection() != null) {
            this.proxyConnection = event.getConnection();
        }
        FriendlyByteBuf buf = pbPayload.toBuf();
        try {
            int packetId = buf.readVarInt();
            switch (packetId) {
                case 1: {
                    this.handlePasswordRequest();
                    return;
                }
                case 5: {
                    this.handleModuleState(new S2CModuleStatePacket(buf));
                    return;
                }
                case 6: {
                    this.handleSettingState(new S2CSettingStatePacket(buf));
                    return;
                }
                case 7: {
                    this.handleError(new S2CErrorPacket(buf));
                    return;
                }
                case 8: {
                    this.handleServerName(new S2CServerNamePacket(buf));
                    return;
                }
                case 10: {
                    this.handleRenderPosition(new S2CRenderPositionPacket(buf));
                    return;
                }
                case 11: {
                    this.handleBlockRender(new S2CBlockRenderPacket(buf));
                    return;
                }
                case 12: {
                    this.handleMiningState(new S2CMiningStatePacket(buf));
                    return;
                }
                case 13: {
                    this.handleSlotSync(new S2CSlotSyncPacket(buf));
                    return;
                }
                case 16: {
                    this.handleWindowClick(new S2CWindowClickPacket(buf));
                    return;
                }
            }
            return;
        }
        catch (Exception e) {
            Night.LOGGER.warn("[PingBypass] Failed to handle S2C packet", (Throwable)e);
            return;
        }
        finally {
            buf.release();
        }
    }

    private void handlePasswordRequest() {
        Connection connection = this.proxyConnection;
        if (connection == null && mc.getConnection() != null) {
            connection = mc.getConnection().getConnection();
        }
        if (connection == null || !connection.isConnected()) {
            Night.LOGGER.warn("[PingBypass] Cannot send password response \u2014 no active connection");
            return;
        }
        this.sendRawCustomPayload(connection, new C2SPasswordPacket(this.password.getValue()));
        Night.LOGGER.info("[PingBypass] Sent password response to proxy");
        this.sendJoinIfReady(connection);
    }

    private void sendJoinIfReady(Connection connection) {
        String host;
        if (this.joinSent) {
            return;
        }
        String targetServer = this.server.getValue();
        if (targetServer == null || targetServer.isBlank()) {
            return;
        }
        int targetPort = 25565;
        if (targetServer.contains(":")) {
            String[] parts = targetServer.split(":", 2);
            host = parts[0];
            try {
                targetPort = Integer.parseInt(parts[1]);
            }
            catch (NumberFormatException e) {
                Night.LOGGER.warn("[PingBypass] Invalid port in server setting: {}", (Object)targetServer);
                return;
            }
        } else {
            host = targetServer;
        }
        this.joinSent = true;
        this.sendRawCustomPayload(connection, new C2SJoinPacket(host, targetPort));
        Night.LOGGER.info("[PingBypass] Sent join request to proxy: {}:{}", (Object)host, (Object)targetPort);
    }

    private void sendRawCustomPayload(Connection connection, PbPacket packet) {
        PbCustomPayload payload = PbCustomPayload.fromPacket(packet);
        this.pendingPayloads.add(new PendingPayload(payload, connection));
        this.pendingDelay = 10;
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (this.pendingPayloads.isEmpty()) {
            return;
        }
        if (this.pendingDelay > 0) {
            --this.pendingDelay;
            return;
        }
        Iterator<PendingPayload> iterator = this.pendingPayloads.iterator();
        while (iterator.hasNext()) {
            PendingPayload pending = iterator.next();
            if (!pending.connection.isConnected()) {
                iterator.remove();
                continue;
            }
            try {
                pending.connection.send((Packet)new ServerboundCustomPayloadPacket((CustomPacketPayload)pending.payload));
                iterator.remove();
                Night.LOGGER.info("[PingBypass] Sent deferred payload via vanilla networking");
            }
            catch (IllegalStateException e) {
                this.pendingDelay = 5;
            }
        }
    }

    private void handleModuleState(S2CModuleStatePacket packet) {
        this.proxyModuleStates.put(packet.getModuleName(), packet.isEnabled());
        Module module = Night.MODULE_MANAGER.getModule(packet.getModuleName());
        if (module != null && module.isToggled() != packet.isEnabled()) {
            mc.execute(() -> module.setToggled(packet.isEnabled(), false));
        }
        Night.LOGGER.debug("[PingBypass] Module {} state synced to {}", (Object)packet.getModuleName(), (Object)packet.isEnabled());
    }

    private void handleSettingState(S2CSettingStatePacket packet) {
        String key = packet.getModuleName() + "." + packet.getSettingName();
        this.proxySettingValues.put(key, packet.getValue());
        Night.LOGGER.debug("[PingBypass] Setting {}.{} updated to {}", new Object[]{packet.getModuleName(), packet.getSettingName(), packet.getValue()});
    }

    private void handleError(S2CErrorPacket packet) {
        Night.CHAT_MANAGER.error("[PingBypass] " + packet.getMessage());
    }

    private void handleServerName(S2CServerNamePacket packet) {
        this.serverName = packet.getServerIp();
        PingBypassFlags.proxyForwardingActive = true;
        if (this.inputForwarder == null) {
            this.inputForwarder = new ClientInputForwarder();
            this.inputForwarder.start();
        }
        this.syncAllSettingsToProxy();
        Night.CHAT_MANAGER.message("[PingBypass] Proxy connected to: " + packet.getServerIp());
    }

    public void syncAllSettingsToProxy() {
        Connection connection = this.proxyConnection;
        if (connection == null && mc.getConnection() != null) {
            connection = mc.getConnection().getConnection();
        }
        if (connection == null || !connection.isConnected()) {
            return;
        }
        int settingCount = 0;
        for (Module module : Night.MODULE_MANAGER.getModules()) {
            if (!module.isProxyEnhanced()) continue;
            connection.send((Packet)new ServerboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(new C2SModuleTogglePacket(module.getName(), module.isToggled()))));
            for (Setting setting : module.getSettings()) {
                String value = this.serializeSetting(setting);
                if (value == null) continue;
                connection.send((Packet)new ServerboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(new C2SSettingChangePacket(module.getName(), setting.getName(), value))));
                ++settingCount;
            }
        }
        Night.LOGGER.info("[PingBypass] Synced {} settings to proxy", (Object)settingCount);
        this.syncFriendsToProxy();
    }

    public void syncFriendsToProxy() {
        Connection connection = this.proxyConnection;
        if (connection == null && mc.getConnection() != null) {
            connection = mc.getConnection().getConnection();
        }
        if (connection == null || !connection.isConnected()) {
            return;
        }
        connection.send((Packet)new ServerboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(new C2SFriendSyncPacket(new ArrayList<String>(Night.FRIEND_MANAGER.getFriends())))));
    }

    private void handleRenderPosition(S2CRenderPositionPacket packet) {
        Night.RENDER_MANAGER.setRenderPosition(packet.getPosition());
    }

    private void handleBlockRender(S2CBlockRenderPacket packet) {
        RenderPosition renderPosition = new RenderPosition(packet.getPosition());
        if (!Night.RENDER_MANAGER.renderPositions.contains(renderPosition)) {
            Night.RENDER_MANAGER.renderPositions.add(renderPosition);
        }
    }

    private void handleMiningState(S2CMiningStatePacket packet) {
        SpeedMineModule speedMine = Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
        if (speedMine != null) {
            speedMine.updateProxyMiningState(packet.getPrimaryPos(), packet.getPrimaryProgress(), packet.getSecondaryPos(), packet.getSecondaryProgress());
        }
    }

    private void handleSlotSync(S2CSlotSyncPacket packet) {
        if (PingBypassModule.mc.player != null) {
            PingBypassModule.mc.player.getInventory().setSelectedSlot(packet.getSlot());
        }
    }

    private void handleWindowClick(S2CWindowClickPacket packet) {
        mc.execute(() -> {
            if (PingBypassModule.mc.player == null || PingBypassModule.mc.player.containerMenu.containerId != packet.getContainerId()) {
                return;
            }
            try {
                PingBypassModule.mc.player.containerMenu.clicked(packet.getSlotNum(), packet.getButtonNum(), packet.getContainerInput(), (Player)PingBypassModule.mc.player);
            }
            catch (Exception e) {
                Night.LOGGER.warn("[PingBypass] Failed to replay proxy window click", (Throwable)e);
            }
        });
    }

    public String getServerName() {
        return this.serverName;
    }

    public Map<String, Boolean> getProxyModuleStates() {
        return this.proxyModuleStates;
    }

    public Map<String, String> getProxySettingValues() {
        return this.proxySettingValues;
    }

    public boolean isProxyModuleEnabled(String moduleName) {
        return this.proxyModuleStates.getOrDefault(moduleName, false);
    }

    public String getProxySettingValue(String moduleName, String settingName) {
        return this.proxySettingValues.get(moduleName + "." + settingName);
    }

    private record PendingPayload(PbCustomPayload payload, Connection connection) {
    }
}

