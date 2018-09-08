package com.mcy.rpc.core.async;

/**
 * @author zkzc-mcy create at 2018/8/24.
 */
public interface ResponseCallbackListener {
    void onResponse(Object response);
    void onTimeout();
    void onException(Exception e);
}
