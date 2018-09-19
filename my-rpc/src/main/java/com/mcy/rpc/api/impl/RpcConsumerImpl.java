package com.mcy.rpc.api.impl;

import com.mcy.rpc.api.RpcConsumer;
import com.mcy.rpc.core.aop.ConsumerHook;
import com.mcy.rpc.core.async.ResponseCallbackListener;
import com.mcy.rpc.core.context.RpcContext;
import com.mcy.rpc.core.model.RpcRequest;
import com.mcy.rpc.core.model.RpcResponse;
import com.mcy.rpc.core.netty.RpcConnection;
import com.mcy.rpc.core.netty.RpcNettyConnection;
import com.mcy.rpc.util.Configure;
import com.mcy.rpc.util.SerializeTool;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zkzc-mcy create at 2018/8/24.
 */
public class RpcConsumerImpl extends RpcConsumer implements InvocationHandler {

    /** 被代理接口 */
    private Class<?> interfaceClazz;

    /** 版本号 */
    private String version;

    /** 请求超时时间单位ms */
    private int timeout = 3000;

    /** AOP 回调钩子*/
    private ConsumerHook hook;

    private static AtomicLong callTimes = new AtomicLong(0L);

    private RpcConnection connection;
    private List<RpcConnection> connectionList;
    private Map<String, ResponseCallbackListener> asyncMethods;

    public RpcConsumerImpl() {
        super();
        initConnection();
    }

    public RpcConsumerImpl(Configure configure){
        super(configure);
        initConnection();
    }

    private void initConnection(){

        this.asyncMethods = new HashMap();
        this.connection = new RpcNettyConnection(configure.getRemoteIp(), configure.getRemotePort());
        this.connection.connect();

        this.connectionList = new ArrayList();
        int num = Runtime.getRuntime().availableProcessors() / 3 - 2;

        for (int i = 0; i < num; i++) {
            connectionList.add(new RpcNettyConnection(configure.getRemoteIp(), configure.getRemotePort()));
        }
        for (RpcConnection conn : connectionList) {
            conn.connect();
        }
    }

    @Override
    public RpcConsumer interfaceClass(Class<?> interfaceClass){
        this.interfaceClazz = interfaceClass;
        return this;
    }

    @Override
    public RpcConsumer version(String version){
        this.version = version;
        return this;
    }

    @Override
    public RpcConsumer clientTimeout(int clientTimeout) {
        this.timeout = clientTimeout;
        return this;
    }

    /** 将请求分配到不同的连接处理 */
    private RpcConnection select() {
        int d = (int) (callTimes.getAndIncrement() % (connectionList.size() + 1));
        if (d == 0) {
            return connection;
        } else {
            return connectionList.get(d - 1);
        }
    }

    public void destroy() throws Throwable {
        if (null != connection) {
            connection.close();
        }
    }

    public <T> T proxy(Class<T> interfaceClass) throws Exception {

        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException(interfaceClass.getName() + " is not an interface");
        }

        Object proxy = Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, this);
        return (T) proxy;
    }

    @Override
    public RpcConsumer hook(ConsumerHook hook) {
        this.hook = hook;
        return this;
    }

    @Override
    public Object instance() {
        try {
            return proxy(this.interfaceClazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void asyncCall(String methodName) {
        asyncCall(methodName, null);
    }

    @Override
    public <T extends ResponseCallbackListener> void asyncCall(String methodName, T callbackListener) {

        this.asyncMethods.put(methodName, callbackListener);
        this.connection.setAsyncMethod(asyncMethods);
        for (RpcConnection conn : connectionList) {
            conn.setAsyncMethod(asyncMethods);
        }
    }

    @Override
    public void cancelAsync(String methodName) {

        this.asyncMethods.remove(methodName);
        this.connection.setAsyncMethod(asyncMethods);
        for (RpcConnection conn : connectionList) {
            conn.setAsyncMethod(asyncMethods);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        System.out.println("service invoke");

        List<String> parameterTypes = new LinkedList<>();
        for (Class<?> parameterType : method.getParameterTypes()) {
            parameterTypes.add(parameterType.getName());
        }
        RpcRequest request = new RpcRequest();

        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameters(method.getParameterTypes());
        request.setParameters(args);

        // 前置钩子回调
        if (hook != null) {hook.before(request);}

        RpcResponse response = null;
        try {
            request.setContext(RpcContext.getProps());
            response = (RpcResponse) select().Send(request, asyncMethods.containsKey(request.getMethodName()));

            // 后置钩子回调
            if (hook != null) {hook.after(request);}
            if (!asyncMethods.containsKey(request.getMethodName()) && response.getException() != null) {
                Throwable e = SerializeTool.deserialize(response.getException(), response.getClazz());
                throw e.getCause();
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        } finally {
            if (asyncMethods.containsKey(request.getMethodName()) && asyncMethods.get(request.getMethodName()) != null) {
                cancelAsync(request.getMethodName());
            }
        }

        if (response == null) {
            return null;
        } else if (response.getErrorMsg() != null) {
            throw response.getErrorMsg();
        } else {
            return response.getAppResponse();
        }
    }
}

