/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.util.concurrent.ThreadFactoryBuilder
 *  io.netty.bootstrap.ServerBootstrap
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelException
 *  io.netty.channel.ChannelFuture
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelInitializer
 *  io.netty.channel.ChannelOption
 *  io.netty.channel.ChannelPipeline
 *  io.netty.channel.EventLoopGroup
 *  io.netty.channel.epoll.Epoll
 *  io.netty.channel.epoll.EpollEventLoopGroup
 *  io.netty.channel.epoll.EpollServerSocketChannel
 *  io.netty.channel.nio.NioEventLoopGroup
 *  io.netty.channel.socket.nio.NioServerSocketChannel
 *  io.netty.handler.timeout.ReadTimeoutHandler
 *  net.minecraft.network.Connection
 *  net.minecraft.network.PacketListener
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.PacketFlow
 *  net.minecraft.network.protocol.common.ClientboundDisconnectPacket
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package night.pingbypass.server;
import io.netty.channel.ServerChannel;


import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import night.pingbypass.PingBypassConfig;
import night.pingbypass.handler.PbHandshakeHandler;
import night.pingbypass.protocol.PbProtocolHandler;
import night.pingbypass.server.PbSession;
import night.pingbypass.server.RegistryCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyServer.class);
    private static final Lazy<NioEventLoopGroup> SERVER_NIO_EVENTLOOP = new Lazy<NioEventLoopGroup>(() -> new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("PingBypass Server IO #%d").setDaemon(true).build()));
    private static final Lazy<EpollEventLoopGroup> SERVER_EPOLL_EVENTLOOP = new Lazy<EpollEventLoopGroup>(() -> new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("PingBypass Epoll Server IO #%d").setDaemon(true).build()));
    private final List<ChannelFuture> endpoints = Collections.synchronizedList(Lists.newArrayList());
    private final List<Connection> connections = Collections.synchronizedList(Lists.newArrayList());
    private final PingBypassConfig config;
    private final PbProtocolHandler protocolHandler;
    private volatile boolean alive;
    private volatile boolean stayConnected;
    private volatile Connection serverConnection;
    private final RegistryCache registryCache;

    public ProxyServer(PingBypassConfig config) {
        this.config = config;
        this.alive = true;
        this.protocolHandler = new PbProtocolHandler();
        this.registryCache = new RegistryCache();
        this.protocolHandler.registerStayHandler(this);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
   public void bind(InetAddress address, int port) throws IOException {
      LOGGER.info("PingBypass proxy binding to {}:{}", address, port);
      synchronized (this.endpoints) {
         Class<? extends ServerChannel> channelClass;
         EventLoopGroup eventLoopGroup;
         if (Epoll.isAvailable()) {
            channelClass = EpollServerSocketChannel.class;
            eventLoopGroup = SERVER_EPOLL_EVENTLOOP.get();
            LOGGER.info("Using epoll channel type");
         } else {
            channelClass = NioServerSocketChannel.class;
            eventLoopGroup = SERVER_NIO_EVENTLOOP.get();
            LOGGER.info("Using default NIO channel type");
         }

         this.endpoints.add(new ServerBootstrap().channel(channelClass).childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) {
               try {
                  channel.config().setOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
               } catch (ChannelException var4) {
               }

               ChannelPipeline pipeline = channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30));
               Connection.configureSerialization(pipeline, PacketFlow.SERVERBOUND, false, null);
               Connection connection = new PbSession();
               ProxyServer.this.connections.add(connection);
               connection.configurePacketHandler(pipeline);
               connection.setListenerForServerboundHandshake(new PbHandshakeHandler(ProxyServer.this, connection));
            }
         }).group(eventLoopGroup).localAddress(address, port).bind().syncUninterruptibly());
         LOGGER.info("PingBypass proxy is now listening on {}:{}", address, port);
      }
   }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void tick() {
        List<Connection> list = this.connections;
        synchronized (list) {
            Iterator<Connection> iterator = this.connections.iterator();
            while (iterator.hasNext()) {
                Connection connection = iterator.next();
                if (!connection.isConnected()) {
                    iterator.remove();
                    connection.handleDisconnection();
                    continue;
                }
                try {
                    connection.tick();
                }
                catch (Exception e) {
                    LOGGER.warn("Failed to handle packet for {}", (Object)connection.getLoggableAddress(false), (Object)e);
                    connection.send((Packet)new ClientboundDisconnectPacket((Component)Component.literal((String)"Internal server error")));
                    connection.disconnect((Component)Component.literal((String)"Internal server error"));
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void shutdown() {
        this.alive = false;
        LOGGER.info("Shutting down PingBypass proxy server...");
        for (ChannelFuture endpoint : this.endpoints) {
            try {
                endpoint.channel().close().sync();
            }
            catch (InterruptedException e) {
                LOGGER.error("Interrupted whilst closing channel");
            }
        }
        this.endpoints.clear();
        List<Connection> list = this.connections;
        synchronized (list) {
            for (Connection connection : this.connections) {
                connection.disconnect((Component)Component.literal((String)"Proxy server shutting down"));
            }
            this.connections.clear();
        }
        LOGGER.info("PingBypass proxy server shut down.");
    }

    public boolean isAlive() {
        return this.alive;
    }

    public List<Connection> getConnections() {
        return this.connections;
    }

    public List<ChannelFuture> getEndpoints() {
        return this.endpoints;
    }

    public PingBypassConfig getConfig() {
        return this.config;
    }

    public PbProtocolHandler getProtocolHandler() {
        return this.protocolHandler;
    }

    public boolean isStayConnected() {
        return this.stayConnected;
    }

    public void setStayConnected(boolean stayConnected) {
        this.stayConnected = stayConnected;
    }

    public Connection getServerConnection() {
        return this.serverConnection;
    }

    public void setServerConnection(Connection serverConnection) {
        this.serverConnection = serverConnection;
    }

    public RegistryCache getRegistryCache() {
        return this.registryCache;
    }

    private static class Lazy<T> {
        private final Supplier<T> supplier;
        private volatile T value;

        Lazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        T get() {
            if (this.value == null) {
                Lazy lazy = this;
                synchronized (lazy) {
                    if (this.value == null) {
                        this.value = this.supplier.get();
                    }
                }
            }
            return this.value;
        }
    }
}

