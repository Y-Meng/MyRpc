package com.mcy.rpc.core.model;

/**
 * @author zkzc-mcy create at 2018/8/24.
 */
public class RpcResponse {
    private Object exption;
    private Object clazz;
    private Throwable errorMsg;
    private Object appResponse;

    public Object getExption() {
        return exption;
    }

    public Object getClazz() {
        return clazz;
    }

    public Throwable getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(Throwable errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Object getAppResponse() {
        return appResponse;
    }

    public void setAppResponse(Object appResponse) {
        this.appResponse = appResponse;
    }
}
