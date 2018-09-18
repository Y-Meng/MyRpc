package com.mcy.rpc.core.async;

import com.mcy.rpc.core.model.RpcResponse;
import com.mcy.rpc.core.netty.ResultFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author  by mengchaoyue on 2018/9/8.
 */
public class ResponseFuture {

    public static ThreadLocal<ResultFuture<RpcResponse>> futureThreadLocal = new ThreadLocal();

    public static Object getResponse(long timeout) throws InterruptedException {

        if (null == futureThreadLocal.get()) {
            throw new RuntimeException("Thread [" + Thread.currentThread() + "] have not set the response future!");
        }

        try {
            RpcResponse response = futureThreadLocal.get().get(timeout, TimeUnit.MILLISECONDS);
            if (response.isError()) {
                throw new RuntimeException(response.getErrorMsg());
            }
            return response.getAppResponse();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException("Time out", e);
        }
    }

    public static void setFuture(ResultFuture<RpcResponse> future){
        futureThreadLocal.set(future);
    }
}
