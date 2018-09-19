package com.mcy.rpc.api;

import com.mcy.rpc.core.aop.ConsumerHook;
import com.mcy.rpc.core.async.ResponseCallbackListener;
import com.mcy.rpc.util.Configure;

/**
 * @author zkzc-mcy create at 2018/8/24.
 */
public abstract class RpcConsumer{

    /** 配置信息 */
    protected Configure configure;


    public RpcConsumer(){
        this.configure = new Configure();
        configure.printClientConfig();
    }

    public RpcConsumer interfaceClass(Class<?> interfaceClass){
        return this;
    }

    public RpcConsumer version(String version){
        return this;
    }

    public RpcConsumer clientTimeout(int clientTimeout) {
        return this;
    }

    public RpcConsumer hook(ConsumerHook hook){
        return this;
    }

    /** 获取接口实例 */
    public abstract Object instance();

    public void asyncCall(String methodName) {
        asyncCall(methodName, null);
    }

    public abstract <T extends ResponseCallbackListener> void asyncCall(String methodName, T callbackListener);

    public abstract void cancelAsync(String methodName);
}
