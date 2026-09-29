/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelPipeline
 *  net.minecraft.network.Connection
 *  net.minecraft.network.ConnectionProtocol
 *  net.minecraft.network.PacketBundlePacker
 *  net.minecraft.network.PacketBundleUnpacker
 *  net.minecraft.network.PacketDecoder
 *  net.minecraft.network.PacketEncoder
 *  net.minecraft.network.PacketListener
 *  net.minecraft.network.ProtocolInfo
 *  net.minecraft.network.protocol.BundlerInfo
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketBundlePacker;
import net.minecraft.network.PacketBundleUnpacker;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.BundlerInfo;
import night.mixins.accessors.ConnectionAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Connection.class})
public class ConnectionSafeReconfigureMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"Night/SafeReconfigure");

    @Inject(method={"setupOutboundProtocol"}, at={@At(value="HEAD")}, cancellable=true)
    private void setupOutboundProtocol$safe(ProtocolInfo<?> protocol, CallbackInfo info) {
        Connection self = (Connection)(Object)this;
        if (protocol.flow() != self.getSending()) {
            throw new IllegalStateException("Invalid outbound protocol: " + String.valueOf(protocol.id()));
        }
        ConnectionAccessor accessor = (ConnectionAccessor)((Object)this);
        Channel channel = accessor.getChannel();
        accessor.setSendLoginDisconnect(protocol.id() == ConnectionProtocol.LOGIN);
        Runnable swap = () -> {
            try {
                ChannelPipeline p = channel.pipeline();
                if (p.get("fabric:splitter") != null) {
                    p.remove("fabric:splitter");
                }
                PacketEncoder enc = new PacketEncoder(protocol);
                if (p.get("encoder") != null) {
                    p.replace("encoder", "encoder", (ChannelHandler)enc);
                } else if (p.get("outbound_config") != null) {
                    p.replace("outbound_config", "encoder", (ChannelHandler)enc);
                } else {
                    p.addAfter("prepender", "encoder", (ChannelHandler)enc);
                }
                BundlerInfo bh = protocol.bundlerInfo();
                if (bh != null) {
                    PacketBundleUnpacker u = new PacketBundleUnpacker(bh);
                    if (p.get("unbundler") != null) {
                        p.replace("unbundler", "unbundler", (ChannelHandler)u);
                    } else {
                        p.addAfter("encoder", "unbundler", (ChannelHandler)u);
                    }
                }
            }
            catch (Exception e) {
                LOGGER.error("[PB] Failed safe outbound transition to {}", (Object)protocol.id(), (Object)e);
            }
        };
        if (channel.eventLoop().inEventLoop()) {
            swap.run();
        } else {
            try {
                channel.eventLoop().submit(swap).sync();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        info.cancel();
    }

    @Inject(method={"setupInboundProtocol"}, at={@At(value="HEAD")}, cancellable=true)
    private <T extends PacketListener> void setupInboundProtocol$safe(ProtocolInfo<T> protocol, T listener, CallbackInfo info) {
        Connection self = (Connection)(Object)this;
        if (protocol.flow() != self.getReceiving()) {
            throw new IllegalStateException("Invalid inbound protocol: " + String.valueOf(protocol.id()));
        }
        ConnectionAccessor accessor = (ConnectionAccessor)((Object)this);
        accessor.setPacketListener(listener);
        Channel channel = accessor.getChannel();
        Runnable swap = () -> {
            try {
                ChannelPipeline p = channel.pipeline();
                if (p.get("fabric:merger") != null) {
                    p.remove("fabric:merger");
                }
                PacketDecoder dec = new PacketDecoder(protocol);
                if (p.get("decoder") != null) {
                    p.replace("decoder", "decoder", (ChannelHandler)dec);
                } else if (p.get("inbound_config") != null) {
                    p.replace("inbound_config", "decoder", (ChannelHandler)dec);
                }
                channel.config().setAutoRead(true);
                BundlerInfo bh = protocol.bundlerInfo();
                if (bh != null) {
                    PacketBundlePacker b = new PacketBundlePacker(bh);
                    if (p.get("bundler") != null) {
                        p.replace("bundler", "bundler", (ChannelHandler)b);
                    } else {
                        p.addAfter("decoder", "bundler", (ChannelHandler)b);
                    }
                }
            }
            catch (Exception e) {
                LOGGER.error("[PB] Failed safe inbound transition to {}", (Object)protocol.id(), (Object)e);
            }
        };
        if (channel.eventLoop().inEventLoop()) {
            swap.run();
        } else {
            try {
                channel.eventLoop().submit(swap).sync();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        info.cancel();
    }
}

