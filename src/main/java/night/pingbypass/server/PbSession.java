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
 *  net.minecraft.network.protocol.PacketFlow
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package night.pingbypass.server;

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
import net.minecraft.network.protocol.PacketFlow;
import night.mixins.accessors.ConnectionAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbSession
extends Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(PbSession.class);

    public PbSession() {
        super(PacketFlow.SERVERBOUND);
    }

    public <T extends PacketListener> void setupInboundProtocol(ProtocolInfo<T> protocol, T listener) {
        if (protocol.flow() != this.getReceiving()) {
            throw new IllegalStateException("Invalid inbound protocol: " + String.valueOf(protocol.id()));
        }
        ConnectionAccessor self = (ConnectionAccessor)((Object)this);
        self.setPacketListener(listener);
        Channel channel = self.getChannel();
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
                LOGGER.error("[PbSession] Failed inbound transition to {}", (Object)protocol.id(), (Object)e);
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
    }

    public void setupOutboundProtocol(ProtocolInfo<?> protocol) {
        if (protocol.flow() != this.getSending()) {
            throw new IllegalStateException("Invalid outbound protocol: " + String.valueOf(protocol.id()));
        }
        ConnectionAccessor self = (ConnectionAccessor)((Object)this);
        Channel channel = self.getChannel();
        self.setSendLoginDisconnect(protocol.id() == ConnectionProtocol.LOGIN);
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
                LOGGER.error("[PbSession] Failed outbound transition to {}", (Object)protocol.id(), (Object)e);
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
    }
}

