package com.mcy.rpc.core.netty;

import com.mcy.rpc.core.model.RpcRequest;
import com.mcy.rpc.core.model.RpcResponse;

/**
 * @author zkzc-mcy create at 2018/8/24.
 */
public class RpcNettyConnection extends RpcConnection {


    public RpcNettyConnection(String ip, int port) {
        super(ip, port);
    }

    @Override
    public void connect() {

    }

    @Override
    public void close() {

    }

    @Override
    public RpcResponse Send(RpcRequest request, boolean b) {
        return null;
    }
}
