package com.mcy.rpc.api;

import com.mcy.rpc.core.aop.ConsumerHook;
import com.mcy.rpc.core.async.ResponseCallbackListener;

/**
 * @author zkzc-mcy create at 2018/8/24.
 */
public interface RpcConsumer {

    RpcConsumer interfaceClass(Class<?> interfaceClass);

    RpcConsumer version(String version);

    RpcConsumer clientTimeout(int clientTimeout);

    RpcConsumer hook(ConsumerHook hook);

    Object instance();

    void asynCall(String methodName);

    <T extends ResponseCallbackListener> void asynCall(String methodName, T callbackListener);

    void cancelAsyn(String methodName);
}
