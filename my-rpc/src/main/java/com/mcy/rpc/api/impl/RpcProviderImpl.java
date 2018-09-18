package com.mcy.rpc.api.impl;

import com.mcy.rpc.api.RpcProvider;
import com.mcy.rpc.core.model.RpcRequest;
import com.mcy.rpc.core.model.RpcResponse;
import com.mcy.rpc.core.netty.handler.RpcRequestHandler;
import com.mcy.rpc.core.serializer.RpcDecoder;
import com.mcy.rpc.core.serializer.RpcEncoder;
import com.mcy.rpc.util.Configure;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mengchaoyue on 2018/9/8.
 */
public class RpcProviderImpl extends RpcProvider{

    /** 存放接口名与服务对象之间的映射关系 */
    private Map<String, Object> handlerMap = new HashMap<>();

    private Class<?> interfaceClazz;
    private Object classImplement;
    private String version;

    public RpcProviderImpl(Configure configure) {
        super(configure);
    }

    public String getVersion() {
        return version;
    }

    public int getTimeout() {
        return timeout;
    }

    public String getType() {
        return type;
    }

    private int timeout;
    private String type;
    
    @Override
    public RpcProvider serviceInterface(Class<?> serviceInterface) {
        this.interfaceClazz=serviceInterface;
        return this;
    }

    @Override
    public RpcProvider version(String version) {
        this.version=version;
        return this;
    }

    @Override
    public RpcProvider impl(Object serviceInstance) {
        this.classImplement=serviceInstance;
        return this;
    }

    @Override
    public RpcProvider timeout(int timeout) {
        this.timeout=timeout;
        return this;
    }

    @Override
    public RpcProvider serializeType(String serializeType) {
        this.type=serializeType;
        return this;
    }

    /**
     * 发布RPC服务，后面用ZooKeeper，前期用普通的
     */
    @Override
    public void publish() {

        handlerMap.put(interfaceClazz.getName(), classImplement);

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(new RpcEncoder(RpcResponse.class));
                            ch.pipeline().addLast(new RpcDecoder(RpcRequest.class));
//                        	ch.pipeline().addLast(new FSTNettyEncode());
//                          ch.pipeline().addLast(new FSTNettyDecode());
                            ch.pipeline().addLast(new RpcRequestHandler(handlerMap));
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_SNDBUF, 1024)
                    .option(ChannelOption.SO_RCVBUF, 2048);


            ChannelFuture f = serverBootstrap.bind(configure.getListen()).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
