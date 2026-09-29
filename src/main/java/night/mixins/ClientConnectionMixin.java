/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.ChannelFutureListener
 *  io.netty.channel.ChannelHandlerContext
 *  net.minecraft.network.Connection
 *  net.minecraft.network.ConnectionProtocol
 *  net.minecraft.network.DisconnectionDetails
 *  net.minecraft.network.PacketListener
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundBundlePacket
 *  net.minecraft.network.protocol.handshake.ClientIntentionPacket
 *  org.jetbrains.annotations.Nullable
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import night.Night;
import night.events.impl.ClientDisconnectEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.PacketSendEvent;
import night.modules.impl.miscellaneous.FakePlayerModule;
import night.modules.impl.miscellaneous.NoPacketKickModule;
import night.pingbypass.ProtocolTransitionTracker;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Connection.class})
public class ClientConnectionMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"Night/Disconnect");
    private static final long PROTOCOL_TRANSITION_WINDOW_MS = 1000L;

    @Inject(method={"send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void send$HEAD(Packet<?> packet, @Nullable ChannelFutureListener listener, boolean flush, CallbackInfo info) {
        if (packet instanceof ClientIntentionPacket) {
            boolean genuinelyPastHandshake;
            Connection self = (Connection)(Object)this;
            PacketListener packetListener = self.getPacketListener();
            boolean bl = genuinelyPastHandshake = packetListener != null && packetListener.protocol() != ConnectionProtocol.HANDSHAKING;
            if (genuinelyPastHandshake) {
                LOGGER.error("[PB] Caught an outgoing ClientIntentionPacket on {} -- this connection is already past handshake, this send will crash it", (Object)self.getLoggableAddress(false), (Object)new Throwable("call site"));
            }
        }
        PacketSendEvent event = new PacketSendEvent(packet);
        Night.EVENT_HANDLER.post(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method={"send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V"}, at={@At(value="TAIL")}, cancellable=true)
    private void send$TAIL(Packet<?> packet, @Nullable ChannelFutureListener listener, boolean flush, CallbackInfo info) {
        Night.EVENT_HANDLER.post(new PacketSendEvent.Post(packet));
    }

    @Inject(method={"doSendPacket"}, at={@At(value="HEAD")}, cancellable=true)
    private void doSendPacket$HEAD(Packet<?> packet, @Nullable ChannelFutureListener listener, boolean flush, CallbackInfo info) {
        boolean isPlayOnlyPacket;
        boolean inTransitionWindow = System.currentTimeMillis() - ProtocolTransitionTracker.lastTransitionAt() < 1000L;
        String pkg = packet.getClass().getPackageName();
        boolean bl = isPlayOnlyPacket = pkg.equals("net.minecraft.network.protocol.game") || pkg.equals("net.minecraft.network.protocol.handshake");
        if (inTransitionWindow && isPlayOnlyPacket) {
            LOGGER.warn("[PB] Dropped {} queued right as the connection was mid protocol-transition -- would have crashed the session", (Object)packet.getClass().getSimpleName());
            info.cancel();
            return;
        }
        if (packet instanceof ClientIntentionPacket) {
            boolean genuinelyPastHandshake;
            Connection self = (Connection)(Object)this;
            PacketListener packetListener = self.getPacketListener();
            boolean bl2 = genuinelyPastHandshake = packetListener != null && packetListener.protocol() != ConnectionProtocol.HANDSHAKING;
            if (genuinelyPastHandshake) {
                return;
            }
        }
    }

    @Inject(method={"channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo info) {
        PacketReceiveEvent event = new PacketReceiveEvent(packet, (Connection)(Object)this);
        Night.EVENT_HANDLER.post(event);
        if (packet instanceof ClientboundBundlePacket) {
            ClientboundBundlePacket bundleS2CPacket = (ClientboundBundlePacket)packet;
            for (Packet subPacket : bundleS2CPacket.subPackets()) {
                Night.EVENT_HANDLER.post(new PacketReceiveEvent(subPacket, (Connection)(Object)this));
            }
        }
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method={"disconnect(Lnet/minecraft/network/DisconnectionDetails;)V"}, at={@At(value="HEAD")})
    private void disconnect(DisconnectionDetails disconnectionInfo, CallbackInfo info) {
        boolean isServerLink;
        Connection self = (Connection)(Object)this;
        String reason = disconnectionInfo.reason() != null ? disconnectionInfo.reason().getString() : "unknown";
        boolean bl = isServerLink = Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer() && Night.PROXY_SERVER != null && self == Night.PROXY_SERVER.getServerConnection();
        if (isServerLink) {
            LOGGER.warn("[PB] Proxy's connection to the real server dropped: {}", (Object)reason);
            FakePlayerModule fakePlayer = Night.MODULE_MANAGER.getModule(FakePlayerModule.class);
            if (fakePlayer != null && fakePlayer.isToggled()) {
                fakePlayer.setToggled(false);
            }
        } else {
            LOGGER.info("Connection disconnected ({}): {}", (Object)self.getLoggableAddress(false), (Object)reason);
        }
        Night.EVENT_HANDLER.post(new ClientDisconnectEvent());
    }

    @Inject(method={"exceptionCaught"}, at={@At(value="HEAD")}, cancellable=true)
    private void exceptionCaught$HEAD(ChannelHandlerContext context, Throwable throwable, CallbackInfo ci) {
        NoPacketKickModule noPacketKick;
        NoPacketKickModule noPacketKickModule = noPacketKick = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(NoPacketKickModule.class) : null;
        if (noPacketKick != null && noPacketKick.isToggled() && noPacketKick.shouldSuppress(throwable)) {
            noPacketKick.onExceptionCaught(throwable);
            ci.cancel();
        }
    }
}

