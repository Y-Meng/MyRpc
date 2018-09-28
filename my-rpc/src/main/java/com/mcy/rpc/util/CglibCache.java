package com.mcy.rpc.util;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mengcy
 * @date 2018/9/28
 */
public class CglibCache {

    private static Map<String, FastMethod> methodMap = new HashMap<>();

    public static FastMethod getMethod(Class clazz, String methodName, Class<?>[] parameterTypes) {

        String key = generateKey(clazz, methodName, parameterTypes);
        FastMethod method = methodMap.get(key);

        if(method == null) {
            FastClass serviceFastClass = FastClass.create(clazz);
            method = serviceFastClass.getMethod(methodName, parameterTypes);
            methodMap.put(key, method);
        }
        return method;
    }

    /** 生成缓存key */
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
