/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
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

import com.mojang.authlib.GameProfile;
import java.util.concurrent.ThreadLocalRandom;
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
import night.Night;
import night.pingbypass.handler.PbWaitingHandler;
import night.pingbypass.protocol.PbCustomPayload;
import night.pingbypass.server.LobbyWorldSender;
import night.pingbypass.server.ProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbPasswordHandler
implements ServerGamePacketListener,
TickablePacketListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(PbPasswordHandler.class);
    private static final long KEEP_ALIVE_INTERVAL_MS = 15000L;
    private final ProxyServer proxyServer;
    private final Connection connection;
    private final GameProfile profile;
    private final RegistryAccess registryManager;
    private boolean passwordRequestSent;
    private boolean lobbyWorldSent;
    private boolean clientInPlayState;
    private long lastKeepAliveTime;

    public PbPasswordHandler(ProxyServer proxyServer, Connection connection, GameProfile profile, RegistryAccess registryManager) {
        this.proxyServer = proxyServer;
        this.connection = connection;
        this.profile = profile;
        this.registryManager = registryManager;
        this.lastKeepAliveTime = 0L;
        LOGGER.info("Password handler initialized for {}", (Object)profile.name());
    }

    public void tick() {
        long now = System.currentTimeMillis();
        if (!this.lobbyWorldSent) {
            this.lobbyWorldSent = true;
            LobbyWorldSender.sendLobbyWorld(this.connection, this.registryManager);
        }
        if (now - this.lastKeepAliveTime >= 15000L) {
            this.lastKeepAliveTime = now;
            this.connection.send((Packet)new ClientboundKeepAlivePacket(now));
        }
        if (!this.passwordRequestSent && this.clientInPlayState) {
            this.passwordRequestSent = true;
            this.connection.send((Packet)new ClientboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.passwordRequest()));
            LOGGER.info("Sent password request to {}", (Object)this.profile.name());
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
            if (packetId == 2) {
                this.handlePassword(buf.readUtf());
            } else {
                LOGGER.warn("Unexpected packet ID {} from {} during password verification", (Object)packetId, (Object)this.profile.name());
            }
        }
        finally {
            buf.release();
        }
    }

   private void handlePassword(String password) {
      String expected = Night.PINGBYPASS_CONFIG.getPassword();
      if (expected.equals(password)) {
         LOGGER.info("Password accepted for {}", this.profile.name());
         this.connection
            .setupInboundProtocol(
               GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(RegistryAccess.EMPTY), () -> false),
               new PbWaitingHandler(this.proxyServer, this.connection, this.profile, this.registryManager)
            );
      } else {
         LOGGER.warn("Wrong password from {}", this.profile.name());

         try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(1L, 11L));
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
         }

         this.connection.disconnect(Component.literal("Wrong password"));
      }
   }

    public void onDisconnect(DisconnectionDetails info) {
        LOGGER.info("Client {} disconnected during password verification", (Object)this.profile.name());
    }

    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
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

    public void handleKeepAlive(ServerboundKeepAlivePacket p) {
        this.clientInPlayState = true;
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

