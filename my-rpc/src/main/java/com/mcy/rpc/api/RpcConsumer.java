package com.mcy.rpc.api;

import com.mcy.rpc.core.aop.ConsumerHook;
import com.mcy.rpc.core.async.ResponseCallbackListener;
import com.mcy.rpc.util.Configure;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author zkzc-mcy create at 2018/8/24.
 */
public abstract class RpcConsumer  implements InvocationHandler{

    /** 配置信息 */
    protected Configure configure;

    /** 被代理接口 */
    protected Class<?> interfaceClazz;

    /** 版本号 */
    protected String version;

    /** 请求超时时间单位ms */
    protected int timeout = 3000;


    public RpcConsumer(Configure configure){
        this.configure = configure;
        configure.printClientConfig();
    }

    public RpcConsumer interfaceClass(Class<?> interfaceClass){
        this.interfaceClazz = interfaceClass;
        return this;
    }

    public RpcConsumer version(String version){
        this.version = version;
        return this;
    }

    public RpcConsumer clientTimeout(int clientTimeout) {
        this.timeout = clientTimeout;
        return this;
    }

    public Object instance() {

        // 返回服务接口代理对象
        return Proxy.newProxyInstance(this.getClass().getClassLoader(),new Class[]{this.interfaceClazz},this);
    }

    public void asyncCall(String methodName) {
        asyncCall(methodName, null);
    }

    public abstract RpcConsumer hook(ConsumerHook hook);

    public abstract <T extends ResponseCallbackListener> void asyncCall(String methodName, T callbackListener);

    public abstract void cancelAsync(String methodName);
}
