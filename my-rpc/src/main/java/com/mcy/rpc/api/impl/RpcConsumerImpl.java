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
import com.mcy.rpc.util.Tool;

import javax.security.auth.login.Configuration;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zkzc-mcy create at 2018/8/24.
 */
public class RpcConsumerImpl implements RpcConsumer,InvocationHandler {

    private static AtomicLong callTimes = new AtomicLong(0L);
    private RpcConnection connection;
    private List<RpcConnection> connection_list;
    private Map<String, ResponseCallbackListener> asyncMethods;
    private Class<?> interfaceClass;
    private String version;
    private int timeout;
    private ConsumerHook hook;

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public String getVersion() {
        return version;
    }

    public int getTimeout() {
        this.connection.setTimeOut(timeout);
        return timeout;
    }

    public ConsumerHook getHook() {
        return hook;
    }

    RpcConnection select() {
        Random rd = new Random(System.currentTimeMillis());
        int d = (int) (callTimes.getAndIncrement() % (connection_list.size() + 1));
        if (d == 0) {
            return connection;
        } else {
            return connection_list.get(d - 1);
        }
    }

    public RpcConsumerImpl() {
        String ip = Configure.getInstance().getIp();
        this.asyncMethods = new HashMap<String, ResponseCallbackListener>();
        this.connection = new RpcNettyConnection(ip, 8888);
        this.connection.connect();
        connection_list = new ArrayList<RpcConnection>();
        int num = Runtime.getRuntime().availableProcessors() / 3 - 2;
        for (int i = 0; i < num; i++) {
            connection_list.add(new RpcNettyConnection(ip, 8888));
        }
        for (RpcConnection conn : connection_list) {
            conn.connect();
        }
    }

    public void destroy() throws Throwable {
        if (null != connection) {
            connection.close();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T proxy(Class<T> interfaceClass) throws Throwable {
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException(interfaceClass.getName() + " is not an interface");
        }
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, this);
    }

    @Override
    public RpcConsumer interfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
        return this;
    }

    @Override
    public RpcConsumer version(String version) {
        this.version = version;
        return this;
    }

    @Override
    public RpcConsumer clientTimeout(int clientTimeout) {
        this.timeout = clientTimeout;
        return this;
    }

    @Override
    public RpcConsumer hook(ConsumerHook hook) {
        this.hook = hook;
        return this;
    }

    @Override
    public Object instance() {
        try {
            return proxy(this.interfaceClass);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void asynCall(String methodName) {
        asynCall(methodName, null);
    }

    @Override
    public <T extends ResponseCallbackListener> void asynCall(String methodName, T callbackListener) {
        this.asyncMethods.put(methodName, callbackListener);
        this.connection.setAsyncMethod(asyncMethods);
        for (RpcConnection conn : connection_list) {
            conn.setAsyncMethod(asyncMethods);
        }
    }

    @Override
    public void cancelAsyn(String methodName) {
        this.asyncMethods.remove(methodName);
        this.connection.setAsyncMethod(asyncMethods);
        for (RpcConnection conn : connection_list) {
            conn.setAsyncMethod(asyncMethods);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        List<String> parameterTypes = new LinkedList<String>();
        for (Class<?> parameterType : method.getParameterTypes()) {
            parameterTypes.add(parameterType.getName());
        }
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        if (hook != null) hook.before(request);
        RpcResponse response = null;
        try {
            request.setContext(RpcContext.props);
            response = (RpcResponse) select().Send(request, asyncMethods.containsKey(request.getMethodName()));
            if (hook != null) hook.after(request);
            if (!asyncMethods.containsKey(request.getMethodName()) && response.getExption() != null) {
                Throwable e = (Throwable) Tool.deserialize(response.getExption(), response.getClazz());
                throw e.getCause();
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        } finally {
            if (asyncMethods.containsKey(request.getMethodName()) && asyncMethods.get(request.getMethodName()) != null) {
                cancelAsyn(request.getMethodName());
            }
        } if (response == null) {
            return null;
        } else if (response.getErrorMsg() != null) {
            throw response.getErrorMsg();
        } else {
            return response.getAppResponse();
        }
    }
}

