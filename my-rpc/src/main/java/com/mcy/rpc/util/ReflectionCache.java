package com.mcy.rpc.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author mengcy
 * @date 2018/9/28
 * 反射对象缓存，可以使用LRU算法
 */
public class ReflectionCache {

//    private static final LRUMap<String, Method> METHOD_CACHE = new LRUMap<String, Method>(1024);

    /** 实际使用方法数量不会太大 */
    private static Map<String, Method> METHOD_CACHE = new HashMap<>(1024);
    /**
     * 获取缓存的Method
     *
     * @param clazz
     * @param methodName
     * @param parameterTypes
     * @return
     * @throws ClassNotFoundException
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    public static Method getMethod(Class<?> clazz, String methodName,
                                   Class<?>[] parameterTypes) throws ClassNotFoundException,
            SecurityException, NoSuchMethodException {

        String key = generateKey(clazz, methodName, parameterTypes);
        Method method = METHOD_CACHE.get(key);
        if (null != method) {
            return method;
        }
        synchronized (METHOD_CACHE) {
            if (null == METHOD_CACHE.get(key)) {
                method = clazz.getMethod(methodName, parameterTypes);
                METHOD_CACHE.put(key, method);
                return method;
            } else {
                return METHOD_CACHE.get(key);
            }
        }
    }

    private static String generateKey(Class clazz, String methodName, Class<?>[] parameterTypes) {

        StringBuffer buffer = new StringBuffer(clazz.getName());
        buffer.append(".").append(methodName);
        if(parameterTypes != null){
            for(Class<?> type : parameterTypes){
                buffer.append(type.getName());
            }
        }
        return buffer.toString();

    }
}
