package com.mcy.rpc.core.netty.handler;

import com.mcy.rpc.core.context.RpcContext;
import com.mcy.rpc.core.model.RpcRequest;
import com.mcy.rpc.core.model.RpcResponse;
import com.mcy.rpc.core.serializer.KryoSerialization;
import com.mcy.rpc.util.SerializeTool;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mengchaoyue on 2018/9/8.
 *
 * 处理服务器收到的RPC请求并返回结果
 */
public class RpcRequestHandler extends ChannelInboundHandlerAdapter {

    /**
     * 对应每个请求ID和端口号 对应一个RpcContext的Map;
     */
    private static Map<String, Map<String, Object>> ThreadLocalMap = new HashMap<String, Map<String, Object>>();

    /**
     * 服务端接口-实现类的映射表
     */
    private final Map<String, Object> handlerMap;

    public RpcRequestHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("active");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("disconnected");
    }

    /**
     * 更新RpcContext的类容
     */
    private void UpdateRpcContext(String host, Map<String, Object> map) {

        if (ThreadLocalMap.containsKey(host)) {
            Map<String, Object> local = ThreadLocalMap.get(host);
            local.putAll(map);//把客户端的加进来
            ThreadLocalMap.put(host, local);//放回去
            for (Map.Entry<String, Object> entry : map.entrySet()) { //更新变量
                RpcContext.addProp(entry.getKey(), entry.getValue());
            }
        } else {
            ThreadLocalMap.put(host, map);
            //把对应线程的Context更新
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                RpcContext.addProp(entry.getKey(), entry.getValue());
            }
        }

    }

    /** 用来缓存住需要序列化的结果 */
    private static Object cacheName = null;
    private static Object cacheValue = null;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        RpcRequest request = (RpcRequest) msg;
        String host = ctx.channel().remoteAddress().toString();
        //更新上下文
        UpdateRpcContext(host, request.getContext());

        //TODO 获取接口名 函数名 参数    找到实现类   反射实现
        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());
        try {
            Object result = handle(request);
            if (cacheName != null && cacheName.equals(result)) {
                response.setAppResponse(cacheValue);
            } else {
                response.setAppResponse(SerializeTool.ObjectToByte(result));
                cacheName = result;
                cacheValue = SerializeTool.ObjectToByte(result);
            }
        } catch (Throwable t) {
            response.setException(SerializeTool.serialize(t));
            response.setClazz(t.getClass());
        }
        ctx.writeAndFlush(response);
    }

    /**
     * 运行调用的函数返回结果
     *
     * @param request
     * @return
     * @throws Throwable
     */
    private static RpcRequest methodCacheName = null;
    private static Object methodCacheValue = null;

    private Object handle(RpcRequest request) throws Throwable {

        String className = request.getClassName();

        Object classimpl = handlerMap.get(className);//通过类名找到实现的类

        Class<?> clazz = classimpl.getClass();

        String methodName = request.getMethodName();

        Class<?>[] parameterTypes = request.getParameterTypes();

        Object[] parameters = request.getParameters();

//		 Method method = ReflectionCache.getMethod(clazz.getName(),methodName, parameterTypes);
//		 method.setAccessible(true);

        //System.out.println(className+":"+methodName+":"+parameters.length);
        if (methodCacheName != null && methodCacheName.equals(request)) {
            return methodCacheValue;
        } else {
            try {
                methodCacheName = request;
                if (methodMap.containsKey(methodName)) {
                    methodCacheValue = methodMap.get(methodName).invoke(classimpl, parameters);
                    return methodCacheValue;
                } else {
                    FastClass serviceFastClass = FastClass.create(clazz);
                    FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
                    methodMap.put(methodName, serviceFastMethod);
                    methodCacheValue = serviceFastMethod.invoke(classimpl, parameters);
                    return methodCacheValue;
                }
                //return method.invoke(classimpl, parameters);
            } catch (Throwable e) {
                throw e.getCause();
            }
        }
    }

    private Map<String, FastMethod> methodMap = new HashMap<String, FastMethod>();

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

}
