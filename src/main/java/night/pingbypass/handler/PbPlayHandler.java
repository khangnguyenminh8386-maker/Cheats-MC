/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMaps
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.network.Connection
 *  net.minecraft.network.DisconnectionDetails
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.HashedStack
 *  net.minecraft.network.TickablePacketListener
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ClientboundKeepAlivePacket
 *  net.minecraft.network.protocol.common.ServerboundClientInformationPacket
 *  net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket
 *  net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.ServerboundKeepAlivePacket
 *  net.minecraft.network.protocol.common.ServerboundPongPacket
 *  net.minecraft.network.protocol.common.ServerboundResourcePackPacket
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket
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
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action
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
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.ItemStack
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package night.pingbypass.handler;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.HashedStack;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import night.Night;
import night.events.impl.AttackBlockEvent;
import night.events.impl.PlayerJumpEvent;
import night.mixins.accessors.ClientPlayerEntityAccessor;
import night.modules.Module;
import night.modules.impl.player.SpeedMineModule;
import night.pingbypass.PingBypassFlags;
import night.pingbypass.modules.PbModule;
import night.pingbypass.modules.SyncModule;
import night.pingbypass.protocol.PbCustomPayload;
import night.pingbypass.protocol.packets.C2SFriendSyncPacket;
import night.pingbypass.protocol.packets.C2SModuleTogglePacket;
import night.pingbypass.protocol.packets.C2SOpenInventoryPacket;
import night.pingbypass.protocol.packets.C2SSettingChangePacket;
import night.pingbypass.server.ProxyServer;
import night.pingbypass.server.ProxyServerTickListener;
import night.pingbypass.server.S2CForwarder;
import night.settings.Setting;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.settings.impl.StringSetting;
import night.utils.minecraft.InventoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbPlayHandler
implements ServerGamePacketListener,
TickablePacketListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(PbPlayHandler.class);
    private static final long KEEP_ALIVE_INTERVAL_MS = 15000L;
    private final ProxyServer proxyServer;
    private final Connection clientConnection;
    private final GameProfile profile;
    private final S2CForwarder s2cForwarder;
    private boolean wasOnGround = true;
    private long lastKeepAliveSent;

    public PbPlayHandler(ProxyServer proxyServer, Connection clientConnection, GameProfile profile, S2CForwarder s2cForwarder, int initialTeleportId) {
        this.proxyServer = proxyServer;
        this.clientConnection = clientConnection;
        this.profile = profile;
        this.s2cForwarder = s2cForwarder;
        LOGGER.info("Play handler initialized for {} \u2014 dumb pipe mode", (Object)profile.name());
    }

    public GameProfile getProfile() {
        return this.profile;
    }

    public void tick() {
        long now = System.currentTimeMillis();
        if (now - this.lastKeepAliveSent >= 15000L) {
            this.lastKeepAliveSent = now;
            this.clientConnection.send((Packet)new ClientboundKeepAlivePacket(now));
        }
    }

    public void onDisconnect(DisconnectionDetails info) {
        LOGGER.info("Client {} disconnected: {}", (Object)this.profile.name(), (Object)info.reason());
        this.s2cForwarder.stop();
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.level != null) {
                mc.level.disconnect((Component)Component.literal((String)"Client disconnected"));
            }
            if (mc.getConnection() != null) {
                mc.getConnection().getConnection().disconnect((Component)Component.literal((String)"Client disconnected"));
            }
        });
    }

    public boolean isAcceptingMessages() {
        return this.clientConnection.isConnected();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void syncSlotForInteract() {
        Minecraft mc;
        InventoryUtils.flushPendingRestores();
        SpeedMineModule speedMine = Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
        if (speedMine != null && speedMine.isToggled() && speedMine.isRunningOnProxy()) {
            mc = Minecraft.getInstance();
            if (mc.player != null) {
                Object object = speedMine.interactSyncLock;
                synchronized (object) {
                    int clientSlot = mc.player.getInventory().getSelectedSlot();
                    this.forward((Packet<?>)new ServerboundSetCarriedItemPacket(clientSlot));
                    speedMine.setInteractPaused(true);
                    return;
                }
            }
        }
        mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        int clientSlot = mc.player.getInventory().getSelectedSlot();
        if (clientSlot != Night.POSITION_MANAGER.getServerSlot()) {
            this.forward((Packet<?>)new ServerboundSetCarriedItemPacket(clientSlot));
        }
    }

    private void forward(Packet<?> packet) {
        Connection serverConn = this.proxyServer.getServerConnection();
        if (serverConn != null && serverConn.isConnected()) {
            ProxyServerTickListener.allowSend(() -> serverConn.send(packet));
        }
    }

    public void handleMovePlayer(ServerboundMovePlayerPacket p) {
        Minecraft.getInstance().execute(() -> this.handleMovePlayer0(p));
    }

    private void handleMovePlayer0(ServerboundMovePlayerPacket p) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            double prevY = mc.player.getY();
            if (p.hasPosition()) {
                mc.player.setPos(p.getX(mc.player.getX()), p.getY(mc.player.getY()), p.getZ(mc.player.getZ()));
                double newY = mc.player.getY();
                if (newY < prevY && !p.isOnGround()) {
                    mc.player.fallDistance += (double)((float)(prevY - newY));
                } else if (p.isOnGround()) {
                    mc.player.fallDistance = 0.0;
                }
                if (newY > prevY && !p.isOnGround() && this.wasOnGround) {
                    Night.EVENT_HANDLER.post(new PlayerJumpEvent());
                }
                this.wasOnGround = p.isOnGround();
            }
            if (p.hasRotation()) {
                mc.player.setYRot(p.getYRot(mc.player.getYRot()));
                mc.player.setXRot(p.getXRot(mc.player.getXRot()));
            }
            mc.player.setOnGround(p.isOnGround());
            mc.player.horizontalCollision = p.horizontalCollision();
            ProxyServerTickListener.allowSend(() -> ((ClientPlayerEntityAccessor)mc.player).invokeSendMovementPackets());
        }
        PingBypassFlags.clientOnGround = p.isOnGround();
        PingBypassFlags.clientHorizontalCollision = p.horizontalCollision();
    }

    public void handleUseItemOn(ServerboundUseItemOnPacket p) {
        this.syncSlotForInteract();
        this.mirrorStartUsingItem(p.getHand());
        this.forward((Packet<?>)p);
    }

    public void handleUseItem(ServerboundUseItemPacket p) {
        this.syncSlotForInteract();
        this.mirrorStartUsingItem(p.getHand());
        this.forward((Packet<?>)p);
    }

    private void mirrorStartUsingItem(InteractionHand hand) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        ItemStack stack = mc.player.getItemInHand(hand);
        if (stack.getUseDuration((LivingEntity)mc.player) > 0) {
            mc.player.startUsingItem(hand);
        }
    }

    public void handleInteract(ServerboundInteractPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handlePlayerAction(ServerboundPlayerActionPacket p) {
        SpeedMineModule speedMine;
        if (p.getAction() == ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM) {
            SpeedMineModule speedMine2;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.stopUsingItem();
            }
            if ((speedMine2 = Night.MODULE_MANAGER.getModule(SpeedMineModule.class)) != null && speedMine2.isInteractPaused()) {
                speedMine2.setInteractPaused(false);
            }
            this.forward((Packet<?>)p);
            return;
        }
        if (p.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK && (speedMine = Night.MODULE_MANAGER.getModule(SpeedMineModule.class)) != null && speedMine.isToggled()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.execute(() -> {
                    AttackBlockEvent event = new AttackBlockEvent(p.getPos(), p.getDirection());
                    Night.EVENT_HANDLER.post(event);
                    if (!event.isCancelled()) {
                        this.forward((Packet<?>)p);
                    }
                });
                return;
            }
        }
        this.forward((Packet<?>)p);
    }

    public void handleAnimate(ServerboundSwingPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleContainerClick(ServerboundContainerClickPacket p) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.execute(() -> {
                try {
                    AbstractContainerMenu handler = p.containerId() == mc.player.containerMenu.containerId ? mc.player.containerMenu : mc.player.inventoryMenu;
                    handler.clicked((int)p.slotNum(), (int)p.buttonNum(), p.containerInput(), (Player)mc.player);
                    ServerboundContainerClickPacket freshPacket = new ServerboundContainerClickPacket(p.containerId(), handler.getStateId(), p.slotNum(), p.buttonNum(), p.containerInput(), Int2ObjectMaps.emptyMap(), HashedStack.EMPTY);
                    this.forward((Packet<?>)freshPacket);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            });
        }
    }

    public void handleContainerClose(ServerboundContainerClosePacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleSetCarriedItem(ServerboundSetCarriedItemPacket p) {
        boolean speedMineOwnsSlot;
        Minecraft mc = Minecraft.getInstance();
        SpeedMineModule speedMine = Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
        boolean bl = speedMineOwnsSlot = speedMine != null && speedMine.isToggled() && speedMine.isRunningOnProxy() && (speedMine.getPrimary() != null || speedMine.getSecondary() != null);
        if (mc.player != null) {
            mc.player.getInventory().setSelectedSlot(p.getSlot());
        }
        if (speedMineOwnsSlot || InventoryUtils.hasActiveSilentSwitch()) {
            return;
        }
        this.forward((Packet<?>)p);
    }

    public void handleClientCommand(ServerboundClientCommandPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleChat(ServerboundChatPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleChatCommand(ServerboundChatCommandPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleSignedChatCommand(ServerboundChatCommandSignedPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleChatAck(ServerboundChatAckPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleSpectatorAction(ServerboundSpectatorActionPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleResourcePackResponse(ServerboundResourcePackPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleCookieResponse(ServerboundCookieResponsePacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleChunkBatchReceived(ServerboundChunkBatchReceivedPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleClientTickEnd(ServerboundClientTickEndPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleConfigurationAcknowledged(ServerboundConfigurationAcknowledgedPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleClientInformation(ServerboundClientInformationPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQueryPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleChangeDifficulty(ServerboundChangeDifficultyPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleContainerSlotStateChanged(ServerboundContainerSlotStateChangedPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleDebugSubscriptionRequest(ServerboundDebugSubscriptionRequestPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleEditBook(ServerboundEditBookPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleEntityTagQuery(ServerboundEntityTagQueryPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleJigsawGenerate(ServerboundJigsawGeneratePacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleLockDifficulty(ServerboundLockDifficultyPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleMoveVehicle(ServerboundMoveVehiclePacket p) {
        this.forward((Packet<?>)p);
    }

    public void handlePaddleBoat(ServerboundPaddleBoatPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handlePickItemFromBlock(ServerboundPickItemFromBlockPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handlePickItemFromEntity(ServerboundPickItemFromEntityPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handlePlaceRecipe(ServerboundPlaceRecipePacket p) {
        this.forward((Packet<?>)p);
    }

    public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handlePlayerCommand(ServerboundPlayerCommandPacket p) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            switch (p.getAction()) {
                case START_SPRINTING: {
                    mc.player.setSprinting(true);
                    break;
                }
                case STOP_SPRINTING: {
                    mc.player.setSprinting(false);
                    break;
                }
            }
        }
        this.forward((Packet<?>)p);
    }

    public void handlePlayerInput(ServerboundPlayerInputPacket p) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.setShiftKeyDown(p.input().shift());
        }
        this.forward((Packet<?>)p);
    }

    public void handleAcceptPlayerLoad(ServerboundPlayerLoadedPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleRenameItem(ServerboundRenameItemPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleBundleItemSelectedPacket(ServerboundSelectBundleItemPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleSelectTrade(ServerboundSelectTradePacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleSetBeaconPacket(ServerboundSetBeaconPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleSetCommandBlock(ServerboundSetCommandBlockPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleSetGameRule(ServerboundSetGameRulePacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleSetStructureBlock(ServerboundSetStructureBlockPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleSetTestBlock(ServerboundSetTestBlockPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleSignUpdate(ServerboundSignUpdatePacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleTestInstanceBlockAction(ServerboundTestInstanceBlockActionPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleChangeGameMode(ServerboundChangeGameModePacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleAttack(ServerboundAttackPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleContainerButtonClick(ServerboundContainerButtonClickPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleCustomClickAction(ServerboundCustomClickActionPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handleKeepAlive(ServerboundKeepAlivePacket p) {
    }

    public void handlePong(ServerboundPongPacket p) {
        this.forward((Packet<?>)p);
    }

    public void handlePingRequest(ServerboundPingRequestPacket p) {
    }

    public void handleCustomPayload(ServerboundCustomPayloadPacket p) {
        CustomPacketPayload customPacketPayload = p.payload();
        if (customPacketPayload instanceof PbCustomPayload) {
            PbCustomPayload pbPayload = (PbCustomPayload)customPacketPayload;
            this.handlePbPayload(pbPayload);
            return;
        }
        this.forward((Packet<?>)p);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void handlePbPayload(PbCustomPayload pbPayload) {
        FriendlyByteBuf buf = pbPayload.toBuf();
        try {
            int packetId = buf.readVarInt();
            switch (packetId) {
                case 3: {
                    C2SModuleTogglePacket pkt = new C2SModuleTogglePacket(buf);
                    PbModule pbModule = Night.PB_MODULE_MANAGER.getModule(pkt.getModuleName());
                    if (pbModule != null) {
                        Minecraft.getInstance().execute(() -> {
                            SyncModule.applyToggle(pbModule, pkt.isEnabled());
                            LOGGER.info("[PB] PbModule {} toggled to {}", (Object)pkt.getModuleName(), (Object)pkt.isEnabled());
                        });
                        return;
                    } else {
                        Module module = Night.MODULE_MANAGER.getModule(pkt.getModuleName());
                        if (module == null) return;
                        Minecraft.getInstance().execute(() -> {
                            module.setToggled(pkt.isEnabled(), false);
                            LOGGER.info("[PB] Module {} toggled to {}", (Object)pkt.getModuleName(), (Object)pkt.isEnabled());
                        });
                        return;
                    }
                }
                case 4: {
                    C2SSettingChangePacket pkt = new C2SSettingChangePacket(buf);
                    PbModule pbModule = Night.PB_MODULE_MANAGER.getModule(pkt.getModuleName());
                    if (pbModule != null) {
                        Minecraft.getInstance().execute(() -> SyncModule.applySetting(pbModule, pkt.getSettingName(), pkt.getValue()));
                        return;
                    } else {
                        this.handleSettingChange(pkt);
                        return;
                    }
                }
                case 15: {
                    C2SOpenInventoryPacket pkt = new C2SOpenInventoryPacket(buf);
                    Minecraft.getInstance().execute(() -> {
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.player == null) {
                            return;
                        }
                        if (pkt.isOpen()) {
                            mc.gui.setScreen((Screen)new InventoryScreen((Player)mc.player));
                        } else {
                            InventoryScreen inv;
                            Screen patt0$temp = mc.gui.screen();
                            if (patt0$temp instanceof InventoryScreen && (inv = (InventoryScreen)patt0$temp).getMenu() == mc.player.inventoryMenu) {
                                mc.gui.setScreen(null);
                            }
                        }
                    });
                    return;
                }
                case 14: {
                    C2SFriendSyncPacket pkt = new C2SFriendSyncPacket(buf);
                    Minecraft.getInstance().execute(() -> {
                        Night.FRIEND_MANAGER.clear();
                        for (String friend : pkt.getFriends()) {
                            Night.FRIEND_MANAGER.add(friend);
                        }
                        LOGGER.info("[PB] Synced {} friend(s) from client", (Object)pkt.getFriends().size());
                    });
                    return;
                }
                default: {
                    LOGGER.debug("[PB] Unknown packet ID: {}", (Object)packetId);
                    return;
                }
            }
        }
        catch (Exception e) {
            LOGGER.warn("[PB] Failed to handle payload", (Throwable)e);
            return;
        }
        finally {
            buf.release();
        }
    }

    private void handleSettingChange(C2SSettingChangePacket pkt) {
        Module module = Night.MODULE_MANAGER.getModule(pkt.getModuleName());
        if (module == null) {
            return;
        }
        Setting setting = module.getSetting(pkt.getSettingName());
        if (setting == null) {
            return;
        }
        Minecraft.getInstance().execute(() -> {
            try {
                String value = pkt.getValue();
                if (setting instanceof BooleanSetting) {
                    BooleanSetting s = (BooleanSetting)setting;
                    s.setValue(Boolean.parseBoolean(value));
                } else if (setting instanceof NumberSetting) {
                    NumberSetting s = (NumberSetting)setting;
                    switch (s.getType()) {
                        case INTEGER: {
                            s.setValue(Integer.parseInt(value));
                            break;
                        }
                        case LONG: {
                            s.setValue(Long.parseLong(value));
                            break;
                        }
                        case FLOAT: {
                            s.setValue(Float.valueOf(Float.parseFloat(value)));
                            break;
                        }
                        case DOUBLE: {
                            s.setValue(Double.parseDouble(value));
                        }
                    }
                } else if (setting instanceof ModeSetting) {
                    ModeSetting s = (ModeSetting)setting;
                    s.setValue(value);
                } else if (setting instanceof StringSetting) {
                    StringSetting s = (StringSetting)setting;
                    s.setValue(value);
                } else if (setting instanceof ColorSetting) {
                    ColorSetting s = (ColorSetting)setting;
                    String[] parts = value.split(",");
                    if (parts.length >= 4) {
                        s.setColor(new Color(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3])));
                        if (parts.length >= 5) {
                            s.setSync(Boolean.parseBoolean(parts[4]));
                        }
                        if (parts.length >= 6) {
                            s.setRainbow(Boolean.parseBoolean(parts[5]));
                        }
                    }
                }
                LOGGER.info("[PB] Setting {}.{} = {}", new Object[]{pkt.getModuleName(), pkt.getSettingName(), value});
            }
            catch (Exception e) {
                LOGGER.warn("[PB] Failed to apply setting {}.{}", new Object[]{pkt.getModuleName(), pkt.getSettingName(), e});
            }
        });
    }
}

