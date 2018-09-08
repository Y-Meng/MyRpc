package com.mcy.rpc.core.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zkzc-mcy create at 2018/8/24.
 */
public class RpcContext {

    //TODO how can I get props as a provider? tip:ThreadLocal
    public static Map<String,Object> props = new HashMap<String, Object>();

    public static void addProp(String key ,Object value){
        props.put(key,value);
    }

    public static Object getProp(String key){
        return props.get(key);
    }

    public static Map<String,Object> getProps(){
        return Collections.unmodifiableMap(props);
    }
}
