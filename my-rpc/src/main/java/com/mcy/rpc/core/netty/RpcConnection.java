package com.mcy.rpc.core.netty;

import com.mcy.rpc.core.async.ResponseCallbackListener;
import com.mcy.rpc.core.model.RpcRequest;
import java.util.List;
import java.util.Map;

/**
 * @author zkzc-mcy create at 2018/8/24.
 */
public interface RpcConnection {

    void init();
    void connect();
    void connect(String host,int port);
    Object send(RpcRequest request,boolean async);
    void close();
    boolean isConnected();
    boolean isClosed();
    boolean containsFuture(String key);
    InvokeFuture<Object> removeFuture(String key);
    void setResult(Object ret);
    void setTimeOut(long timeout);
    void setAsyncMethod(Map<String,ResponseCallbackListener> map);
    List<InvokeFuture<Object>> getFutures(String method);
}
