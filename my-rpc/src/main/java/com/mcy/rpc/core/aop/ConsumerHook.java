package com.mcy.rpc.core.aop;

import com.mcy.rpc.core.model.RpcRequest;

/**
 * @author zkzc-mcy create at 2018/8/24.
 * 为 rpc 接口提供AOP钩子
 */
public interface ConsumerHook {
    void before(RpcRequest request);

    void after(RpcRequest request);
}
