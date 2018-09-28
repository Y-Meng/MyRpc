package com.mcy.rpc.core.netty;

import com.mcy.rpc.core.async.ResponseCallbackListener;
import com.mcy.rpc.core.async.ResponseFuture;
import com.mcy.rpc.core.model.RpcRequest;
import com.mcy.rpc.core.model.RpcResponse;
import com.mcy.rpc.core.netty.handler.RpcClientHandler;
import com.mcy.rpc.core.serializer.RpcDecoder;
import com.mcy.rpc.core.serializer.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author zkzc-mcy create at 2018/8/24.
 */
public class RpcNettyConnection implements RpcConnection {


    private InetSocketAddress inetAddr;

    private volatile Channel channel;

    private RpcClientHandler handle;

    private static Map<String, InvokeFuture<Object>> futrues = new ConcurrentHashMap<>();
    /** 连接数组 */
    private Map<String, Channel> channels = new ConcurrentHashMap<>();

    private Bootstrap bootstrap;

    /** 异步调用的时候的结果集 */
    private volatile ResultFuture<RpcResponse> resultFuture;

    private long timeout = 3000;

    private boolean connected = false;

    public RpcNettyConnection(String host, int port) {
        inetAddr = new InetSocketAddress(host, port);
        handle = new RpcClientHandler(this);
        init();
    }

    private Channel getChannel(String key) {
        return channels.get(key);
    }

    @Override
    public void init() {
        try {
            EventLoopGroup group = new NioEventLoopGroup();
            bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(new RpcDecoder(RpcResponse.class));
                            channel.pipeline().addLast(new RpcEncoder(RpcRequest.class));
                            channel.pipeline().addLast(handle);
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void connect() {
        try {
            ChannelFuture future = bootstrap.connect(this.inetAddr).sync();
            channels.put(this.inetAddr.toString(), future.channel());
            connected = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connect(String host, int port) {
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture cfuture) throws Exception {
                Channel channel = cfuture.channel();
                //添加进入连接数组
                channels.put(channel.remoteAddress().toString(), channel);
            }
        });
    }

    @Override
    public Object send(RpcRequest request, boolean async) {

        if (channel == null) {
            channel = getChannel(inetAddr.toString());
        }

        if (channel != null) {
            final InvokeFuture<Object> future = new InvokeFuture<>();
            futrues.put(request.getRequestId(), future);
            future.setMethod(request.getMethodName());
            ChannelFuture cfuture = channel.writeAndFlush(request);
            cfuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture rfuture) throws Exception {
                    if (!rfuture.isSuccess()) {
                        future.setCause(rfuture.cause());
                    }
                }
            });
            resultFuture = new ResultFuture<>(timeout);
            resultFuture.setRequestId(request.getRequestId());
            try {
                if (async) {
                    //异步执行的话直接返回
                    ResponseFuture.setFuture(resultFuture);
                    return null;
                }
                Object result = future.getResult(timeout, TimeUnit.MILLISECONDS);
                return result;
            } catch (RuntimeException e) {
                throw e;
            } finally {
                //这个结果已经收到
                if (!async) {
                    futrues.remove(request.getRequestId());
                }
            }
        } else {
            return null;
        }
    }

    @Override
    public void close() {

        if (channel == null) {
            try {
                channel.closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isConnected() {

        return connected;
    }

    @Override
    public boolean isClosed() {

        return (null == channel) || !channel.isOpen()
                || !channel.isWritable() || !channel.isActive();
    }

    @Override
    public boolean containsFuture(String key) {

        return futrues.containsKey(key);
    }

    @Override
    public InvokeFuture<Object> removeFuture(String key) {

        if (containsFuture(key)) {
            return futrues.remove(key);
        }
        return null;
    }

    @Override
    public void setResult(Object ret) {
        RpcResponse response = (RpcResponse) ret;
        if (response.getRequestId().equals(resultFuture.getRequestId())) {
            resultFuture.setResult(response);
        }

    }

    @Override
    public void setTimeOut(long timeout) {

        this.timeout = timeout;
    }

    @Override
    public void setAsyncMethod(Map<String, ResponseCallbackListener> map) {

        handle.setAsynMethod(map);

    }

    @Override
    public List<InvokeFuture<Object>> getFutures(String method) {

        List<InvokeFuture<Object>> list = new ArrayList<>();

        Iterator<Map.Entry<String, InvokeFuture<Object>>> it = futrues.entrySet().iterator();
        String methodName = null;
        InvokeFuture<Object> temp = null;
        while (it.hasNext()) {

            Map.Entry<String, InvokeFuture<Object>> entry = it.next();

            methodName = entry.getValue().getMethod();
            temp = entry.getValue();

            if (methodName != null && methodName.equals(method) && temp != null) {
                list.add(temp);
                methodName = null;
                temp = null;
            }

        }
        return list;
    }
}
