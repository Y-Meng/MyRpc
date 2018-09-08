package com.mcy.rpc.core.netty;

import com.mcy.rpc.core.async.ResponseCallbackListener;
import com.mcy.rpc.core.model.RpcRequest;
import com.mcy.rpc.core.model.RpcResponse;

import java.util.Map;

/**
 * @author zkzc-mcy create at 2018/8/24.
 */
public abstract class RpcConnection {

    private int timeOut;
    private String ip;
    private int port;
    private Map<String, ResponseCallbackListener> asyncMethod;

    public RpcConnection(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public abstract void connect();

    public abstract void close();

    public void setAsyncMethod(Map<String, ResponseCallbackListener> asyncMethod) {
        this.asyncMethod = asyncMethod;
    }

    public abstract RpcResponse Send(RpcRequest request, boolean b);
}
