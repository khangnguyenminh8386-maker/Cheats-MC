/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.ConnectScreen
 *  net.minecraft.client.gui.screens.DisconnectedScreen
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.TitleScreen
 *  net.minecraft.client.multiplayer.ServerData
 *  net.minecraft.client.multiplayer.ServerData$Type
 *  net.minecraft.client.multiplayer.resolver.ServerAddress
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.network.Connection
 *  net.minecraft.network.DisconnectionDetails
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.PacketListener
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.TickablePacketListener
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.ClientboundKeepAlivePacket
 *  net.minecraft.network.protocol.common.ServerboundClientInformationPacket
 *  net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket
 *  net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.ServerboundKeepAlivePacket
 *  net.minecraft.network.protocol.common.ServerboundPongPacket
 *  net.minecraft.network.protocol.common.ServerboundResourcePackPacket
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket
 *  net.minecraft.network.protocol.game.ClientboundSystemChatPacket
 *  net.minecraft.network.protocol.game.GameProtocols
 *  net.minecraft.network.protocol.game.ServerGamePacketListener
 *  net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket
 *  net.minecraft.network.protocol.game.ServerboundAttackPacket
 *  net.minecraft.network.protocol.game.ServerboundBlockEntityTagQueryPacket
 *  net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket
 *  net.minecraft.network.protocol.game.ServerboundChangeGameModePacket
 *  net.minecraft.network.protocol.game.ServerboundChatAckPacket
 *  net.minecraft.network.protocol.game.ServerboundChatCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket
 *  net.minecraft.network.protocol.game.ServerboundChatPacket
 *  net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket
 *  net.minecraft.network.protocol.game.ServerboundChunkBatchReceivedPacket
 *  net.minecraft.network.protocol.game.ServerboundClientCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundClientTickEndPacket
 *  net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket
 *  net.minecraft.network.protocol.game.ServerboundConfigurationAcknowledgedPacket
 *  net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket
 *  net.minecraft.network.protocol.game.ServerboundContainerClickPacket
 *  net.minecraft.network.protocol.game.ServerboundContainerClosePacket
 *  net.minecraft.network.protocol.game.ServerboundContainerSlotStateChangedPacket
 *  net.minecraft.network.protocol.game.ServerboundDebugSubscriptionRequestPacket
 *  net.minecraft.network.protocol.game.ServerboundEditBookPacket
 *  net.minecraft.network.protocol.game.ServerboundEntityTagQueryPacket
 *  net.minecraft.network.protocol.game.ServerboundInteractPacket
 *  net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket
 *  net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
 *  net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket
 *  net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket
 *  net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket
 *  net.minecraft.network.protocol.game.ServerboundPickItemFromEntityPacket
 *  net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket
 *  net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket
 *  net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket
 *  net.minecraft.network.protocol.game.ServerboundRenameItemPacket
 *  net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket
 *  net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket
 *  net.minecraft.network.protocol.game.ServerboundSelectTradePacket
 *  net.minecraft.network.protocol.game.ServerboundSetBeaconPacket
 *  net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket
 *  net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket
 *  net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket
 *  net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket
 *  net.minecraft.network.protocol.game.ServerboundSetGameRulePacket
 *  net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket
 *  net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket
 *  net.minecraft.network.protocol.game.ServerboundSetTestBlockPacket
 *  net.minecraft.network.protocol.game.ServerboundSignUpdatePacket
 *  net.minecraft.network.protocol.game.ServerboundSpectatorActionPacket
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket
 *  net.minecraft.network.protocol.game.ServerboundTestInstanceBlockActionPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemPacket
 *  net.minecraft.network.protocol.ping.ServerboundPingRequestPacket
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package night.pingbypass.handler;
import net.minecraft.client.multiplayer.ServerData.Type;


import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQueryPacket;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundChunkBatchReceivedPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundConfigurationAcknowledgedPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundContainerSlotStateChangedPacket;
import net.minecraft.network.protocol.game.ServerboundDebugSubscriptionRequestPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundEntityTagQueryPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromEntityPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetGameRulePacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetTestBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSpectatorActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.network.protocol.game.ServerboundTestInstanceBlockActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import night.mixins.accessors.DisconnectedScreenAccessor;
import night.pingbypass.handler.PbPlayHandler;
import night.pingbypass.protocol.PbCustomPayload;
import night.pingbypass.protocol.packets.S2CErrorPacket;
import night.pingbypass.protocol.packets.S2CServerNamePacket;
import night.pingbypass.server.LobbyWorldSender;
import night.pingbypass.server.ProxyServer;
import night.pingbypass.server.S2CForwarder;
import night.pingbypass.server.WorldStateReplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbWaitingHandler
implements ServerGamePacketListener,
TickablePacketListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(PbWaitingHandler.class);
    private static final long KEEP_ALIVE_TIMEOUT_MS = 30000L;
    private static final long KEEP_ALIVE_INTERVAL_MS = 15000L;
    private final ProxyServer proxyServer;
    private final Connection connection;
    private final GameProfile profile;
    private final RegistryAccess registryManager;
    private long lastKeepAliveTime;
    private long lastKeepAliveSentTime;
    private boolean lobbyWorldSent;
    private String pendingServerIp;
    private int pendingServerPort;
    private volatile boolean connectingToServer;
    private int connectionAttemptTicks;

    public PbWaitingHandler(ProxyServer proxyServer, Connection connection, GameProfile profile, RegistryAccess registryManager) {
        this.proxyServer = proxyServer;
        this.connection = connection;
        this.profile = profile;
        this.registryManager = registryManager;
        this.lastKeepAliveTime = System.currentTimeMillis();
        this.lastKeepAliveSentTime = 0L;
        LOGGER.info("Waiting handler initialized for {}", (Object)profile.name());
    }

    public void tick() {
        long now = System.currentTimeMillis();
        if (!this.lobbyWorldSent) {
            this.lobbyWorldSent = true;
            LobbyWorldSender.sendLobbyWorld(this.connection, this.registryManager);
        }
        if (now - this.lastKeepAliveTime > 30000L) {
            LOGGER.warn("Client {} timed out (no keep-alive for 30s)", (Object)this.profile.name());
            this.connection.disconnect((Component)Component.literal((String)"Timed out"));
        }
        if (now - this.lastKeepAliveSentTime >= 15000L) {
            this.lastKeepAliveSentTime = now;
            this.connection.send((Packet)new ClientboundKeepAlivePacket(now));
        }
        if (this.connectingToServer) {
            ++this.connectionAttemptTicks;
            Minecraft mc = Minecraft.getInstance();
            if (this.connectionAttemptTicks == 20) {
                this.sendChat("\u00a77[PingBypass] Resolving address...");
            } else if (this.connectionAttemptTicks == 40 && mc.getConnection() == null) {
                this.sendChat("\u00a77[PingBypass] Establishing connection...");
            } else if (this.connectionAttemptTicks == 60 && mc.getConnection() == null) {
                this.sendChat("\u00a77[PingBypass] Encrypting...");
            }
            if (mc.getConnection() != null && mc.player == null) {
                if (this.connectionAttemptTicks % 40 == 0) {
                    this.sendChat("\u00a77[PingBypass] Configuring...");
                }
            } else if (mc.getConnection() != null && mc.player != null && mc.level != null) {
                if (mc.getConnection().registryAccess() != null) {
                    this.connectingToServer = false;
                    this.sendChat("\u00a7a[PingBypass] Connected! Loading world...");
                    this.onServerConnectionEstablished(mc, this.pendingServerIp, this.pendingServerPort);
                } else if (this.connectionAttemptTicks % 40 == 0) {
                    this.sendChat("\u00a77[PingBypass] Loading terrain...");
                }
            } else {
                Screen screen = mc.gui.screen();
                if (screen instanceof DisconnectedScreen) {
                    DisconnectedScreen disconnectedScreen = (DisconnectedScreen)screen;
                    this.connectingToServer = false;
                    String reason = ((DisconnectedScreenAccessor)disconnectedScreen).night$getDetails().reason().getString();
                    LOGGER.warn("Connection to {}:{} failed for {}: {}", new Object[]{this.pendingServerIp, this.pendingServerPort, this.profile.name(), reason});
                    this.sendError("Failed to connect to " + this.pendingServerIp + ":" + this.pendingServerPort + ": " + reason);
                    this.sendChat("\u00a7c[PingBypass] Failed to connect to " + this.pendingServerIp + ":" + this.pendingServerPort + " (" + reason + ")");
                } else if (this.connectionAttemptTicks > 600) {
                    this.connectingToServer = false;
                    LOGGER.warn("Connection to {}:{} timed out for {}", new Object[]{this.pendingServerIp, this.pendingServerPort, this.profile.name()});
                    this.sendError("Connection to " + this.pendingServerIp + ":" + this.pendingServerPort + " timed out");
                    this.sendChat("\u00a7c[PingBypass] Connection timed out");
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void handleCustomPayload(ServerboundCustomPayloadPacket packet) {
        CustomPacketPayload payload = packet.payload();
        if (!PbCustomPayload.CHANNEL.equals((Object)payload.type().id())) {
            return;
        }
        if (!(payload instanceof PbCustomPayload)) {
            return;
        }
        PbCustomPayload pbPayload = (PbCustomPayload)payload;
        FriendlyByteBuf buf = pbPayload.toBuf();
        try {
            int packetId = buf.readVarInt();
            if (packetId == 0) {
                String serverIp = buf.readUtf();
                int serverPort = buf.readVarInt();
                this.handleJoinRequest(serverIp, serverPort);
            } else {
                LOGGER.warn("Unexpected packet ID {} from {} during waiting state", (Object)packetId, (Object)this.profile.name());
            }
        }
        catch (Exception e) {
            LOGGER.warn("Malformed custom payload from {}", (Object)this.profile.name(), (Object)e);
        }
        finally {
            buf.release();
        }
    }

    void handleJoinRequest(String serverIp, int serverPort) {
        LOGGER.info("Client {} requested join to {}:{}", new Object[]{this.profile.name(), serverIp, serverPort});
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null && mc.player != null && mc.level != null && mc.getConnection().registryAccess() != null) {
            LOGGER.info("Proxy already connected to a server, replaying world state for {}", (Object)this.profile.name());
            this.sendChat("\u00a7a[PingBypass] Proxy already connected! Resuming session...");
            this.onServerConnectionEstablished(mc, serverIp, serverPort);
            return;
        }
        this.sendChat("\u00a7d[PingBypass] \u00a7fConnecting to \u00a7a" + serverIp + ":" + serverPort + "\u00a7f...");
        String address = serverIp + ":" + serverPort;
        ServerAddress serverAddress = ServerAddress.parseString((String)address);
        ServerData serverInfo = new ServerData("PingBypass Target", address, ServerData.Type.OTHER);
        this.pendingServerIp = serverIp;
        this.pendingServerPort = serverPort;
        this.connectingToServer = true;
        this.connectionAttemptTicks = 0;
        mc.execute(() -> {
            try {
                LOGGER.info("Initiating connection to {} for {}", (Object)address, (Object)this.profile.name());
                ConnectScreen.startConnecting((Screen)new TitleScreen(), (Minecraft)mc, (ServerAddress)serverAddress, (ServerData)serverInfo, (boolean)false, null);
            }
            catch (Exception e) {
                LOGGER.error("Failed to initiate connection to {} for {}", new Object[]{address, this.profile.name(), e});
                this.sendError("Failed to connect to " + address + ": " + e.getMessage());
                this.connectingToServer = false;
            }
        });
    }

   private void onServerConnectionEstablished(Minecraft mc, String serverIp, int serverPort) {
      LOGGER.info("Server connection established to {}:{} for {}", serverIp, serverPort, this.profile.name());

      try {
         Connection serverConnection = mc.getConnection().getConnection();
         this.proxyServer.setServerConnection(serverConnection);
         RegistryAccess serverRegistry = mc.level.registryAccess();
         this.connection.setupOutboundProtocol(GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(serverRegistry)));
         LOGGER.info("Re-transitioned outbound encoder to real server registry for {}", this.profile.name());
         RegistryAccess finalRegistry = serverRegistry;
         String address = serverIp + ":" + serverPort;
         this.connection.send(new ClientboundCustomPayloadPacket(PbCustomPayload.fromPacket(new S2CServerNamePacket(address))));
         S2CForwarder s2cForwarder = new S2CForwarder(this.connection);
         s2cForwarder.start();
         int initialTeleportId = WorldStateReplay.replay(this.connection, serverRegistry);
         PbPlayHandler playHandler = new PbPlayHandler(this.proxyServer, this.connection, this.profile, s2cForwarder, 0);
         this.connection
            .setupInboundProtocol(GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(finalRegistry), () -> false), playHandler);
         LOGGER.info("Proxy fully active for {} on {}:{} (awaiting teleport confirm)", this.profile.name(), serverIp, serverPort);
      } catch (Exception e) {
         LOGGER.error("Failed to set up forwarding for {}", this.profile.name(), e);
         this.sendError("Failed to set up forwarding: " + e.getMessage());
      }
   }

    private void sendError(String message) {
        if (this.connection.isConnected()) {
            this.connection.send((Packet)new ClientboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(new S2CErrorPacket(message))));
        }
    }

    private void sendChat(String message) {
        if (this.connection.isConnected()) {
            this.connection.send((Packet)new ClientboundSystemChatPacket((Component)Component.literal((String)message), false));
        }
    }

    public void handleKeepAlive(ServerboundKeepAlivePacket packet) {
        this.lastKeepAliveTime = System.currentTimeMillis();
    }

    public void onDisconnect(DisconnectionDetails info) {
        LOGGER.info("Client {} disconnected during waiting state", (Object)this.profile.name());
    }

    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    public GameProfile getProfile() {
        return this.profile;
    }

    public ProxyServer getProxyServer() {
        return this.proxyServer;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void handleAnimate(ServerboundSwingPacket p) {
    }

    public void handleChat(ServerboundChatPacket p) {
    }

    public void handleChatCommand(ServerboundChatCommandPacket p) {
    }

    public void handleSignedChatCommand(ServerboundChatCommandSignedPacket p) {
    }

    public void handleChatAck(ServerboundChatAckPacket p) {
    }

    public void handleClientCommand(ServerboundClientCommandPacket p) {
    }

    public void handleContainerButtonClick(ServerboundContainerButtonClickPacket p) {
    }

    public void handleContainerClick(ServerboundContainerClickPacket p) {
    }

    public void handlePlaceRecipe(ServerboundPlaceRecipePacket p) {
    }

    public void handleContainerClose(ServerboundContainerClosePacket p) {
    }

    public void handleAttack(ServerboundAttackPacket p) {
    }

    public void handleInteract(ServerboundInteractPacket p) {
    }

    public void handleSpectatorAction(ServerboundSpectatorActionPacket p) {
    }

    public void handleMovePlayer(ServerboundMovePlayerPacket p) {
    }

    public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket p) {
    }

    public void handlePlayerAction(ServerboundPlayerActionPacket p) {
    }

    public void handlePlayerCommand(ServerboundPlayerCommandPacket p) {
    }

    public void handlePlayerInput(ServerboundPlayerInputPacket p) {
    }

    public void handleSetCarriedItem(ServerboundSetCarriedItemPacket p) {
    }

    public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket p) {
    }

    public void handleSignUpdate(ServerboundSignUpdatePacket p) {
    }

    public void handleUseItemOn(ServerboundUseItemOnPacket p) {
    }

    public void handleUseItem(ServerboundUseItemPacket p) {
    }

    public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket p) {
    }

    public void handlePaddleBoat(ServerboundPaddleBoatPacket p) {
    }

    public void handleMoveVehicle(ServerboundMoveVehiclePacket p) {
    }

    public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket p) {
    }

    public void handleAcceptPlayerLoad(ServerboundPlayerLoadedPacket p) {
    }

    public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket p) {
    }

    public void handleBundleItemSelectedPacket(ServerboundSelectBundleItemPacket p) {
    }

    public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket p) {
    }

    public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket p) {
    }

    public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket p) {
    }

    public void handleSetCommandBlock(ServerboundSetCommandBlockPacket p) {
    }

    public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket p) {
    }

    public void handlePickItemFromBlock(ServerboundPickItemFromBlockPacket p) {
    }

    public void handlePickItemFromEntity(ServerboundPickItemFromEntityPacket p) {
    }

    public void handleRenameItem(ServerboundRenameItemPacket p) {
    }

    public void handleSetBeaconPacket(ServerboundSetBeaconPacket p) {
    }

    public void handleSetGameRule(ServerboundSetGameRulePacket p) {
    }

    public void handleSetStructureBlock(ServerboundSetStructureBlockPacket p) {
    }

    public void handleSetTestBlock(ServerboundSetTestBlockPacket p) {
    }

    public void handleTestInstanceBlockAction(ServerboundTestInstanceBlockActionPacket p) {
    }

    public void handleSelectTrade(ServerboundSelectTradePacket p) {
    }

    public void handleEditBook(ServerboundEditBookPacket p) {
    }

    public void handleEntityTagQuery(ServerboundEntityTagQueryPacket p) {
    }

    public void handleContainerSlotStateChanged(ServerboundContainerSlotStateChangedPacket p) {
    }

    public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQueryPacket p) {
    }

    public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket p) {
    }

    public void handleJigsawGenerate(ServerboundJigsawGeneratePacket p) {
    }

    public void handleChangeDifficulty(ServerboundChangeDifficultyPacket p) {
    }

    public void handleChangeGameMode(ServerboundChangeGameModePacket p) {
    }

    public void handleLockDifficulty(ServerboundLockDifficultyPacket p) {
    }

    public void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket p) {
    }

    public void handleConfigurationAcknowledged(ServerboundConfigurationAcknowledgedPacket p) {
    }

    public void handleChunkBatchReceived(ServerboundChunkBatchReceivedPacket p) {
    }

    public void handleDebugSubscriptionRequest(ServerboundDebugSubscriptionRequestPacket p) {
    }

    public void handleClientTickEnd(ServerboundClientTickEndPacket p) {
    }

    public void handleClientInformation(ServerboundClientInformationPacket p) {
    }

    public void handlePong(ServerboundPongPacket p) {
    }

    public void handleResourcePackResponse(ServerboundResourcePackPacket p) {
    }

    public void handleCookieResponse(ServerboundCookieResponsePacket p) {
    }

    public void handleCustomClickAction(ServerboundCustomClickActionPacket p) {
    }

    public void handlePingRequest(ServerboundPingRequestPacket p) {
    }
}

