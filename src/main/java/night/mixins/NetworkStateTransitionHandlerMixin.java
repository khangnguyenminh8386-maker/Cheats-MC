/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelHandlerContext
 *  net.minecraft.network.Connection
 *  net.minecraft.network.ProtocolSwapHandler
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.PacketFlow
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.ProtocolSwapHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import night.pingbypass.PingBypassFlags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ProtocolSwapHandler.class})
public interface NetworkStateTransitionHandlerMixin {
    @Inject(method={"handleInboundTerminalPacket"}, at={@At(value="HEAD")}, cancellable=true)
    private static void handleInboundTerminalPacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo info) {
        Connection cc;
        if (!PingBypassFlags.suppressEncoderErrors) {
            return;
        }
        ChannelHandler channelHandler = context.pipeline().get("packet_handler");
        if (channelHandler instanceof Connection && (cc = (Connection)channelHandler).getReceiving() == PacketFlow.CLIENTBOUND) {
            info.cancel();
        }
    }

    @Inject(method={"handleOutboundTerminalPacket"}, at={@At(value="HEAD")}, cancellable=true)
    private static void handleOutboundTerminalPacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo info) {
        Connection cc;
        if (!PingBypassFlags.suppressEncoderErrors) {
            return;
        }
        ChannelHandler channelHandler = context.pipeline().get("packet_handler");
        if (channelHandler instanceof Connection && (cc = (Connection)channelHandler).getReceiving() == PacketFlow.CLIENTBOUND) {
            info.cancel();
        }
    }
}

