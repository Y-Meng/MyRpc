package com.mcy.rpc.core.model;

import java.io.Serializable;

/**
 * @author zkzc-mcy create at 2018/8/24.
 *         rpc返回封装
 */
public class RpcResponse implements Serializable {

    static private final long serialVersionUID = -4364536436151723421L;

    private Class<?> clazz;

    /**
     * @return the clazz
     */
    public Class<?> getClazz() {
        return clazz;
    }

    /**
     * @param clazz the clazz to set
     */
    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    /**
     * @return the exption
     */
    public byte[] getException() {
        return exception;
    }

    /**
     * @param exption the exption to set
     */
    public void setException(byte[] exption) {
        this.exception = exption;
    }

    private byte[] exception;
    private String requestId;

    /**
     * @return the requestID
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * @param requestId the requestID to set
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    private Throwable errorMsg;

    private Object appResponse;

    /**
     * @param errorMsg the errorMsg to set
     */
    public void setErrorMsg(Throwable errorMsg) {
        this.errorMsg = errorMsg;
    }

    /**
     * @param appResponse the appResponse to set
     */
    public void setAppResponse(Object appResponse) {
        this.appResponse = appResponse;
    }

    public Object getAppResponse() {
        return appResponse;
    }

    public Throwable getErrorMsg() {
        return errorMsg;
    }

    public boolean isError() {
        return errorMsg == null ? false : true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        RpcResponse other = (RpcResponse) obj;

        if (clazz == null) {
            if (other.getClazz() != null) {
                return false;
            }
        } else {
            if (other.getClazz() == null) {
                return false;
            } else if (!clazz.equals(other.getClazz())) {
                return false;
            }
        }

        if (exception == null) {
            if (other.getException() != null) {
                return false;
            }
        } else {
            if (other.getException() == null) {return false;}
            if (exception.length != other.getException().length) {
                return false;
            } else {
                for (int i = 0; i < exception.length; i++) {
                    if (exception[i] != other.getException()[i]) {return false;}
                }
            }
        }
        if (appResponse == null) {
            if (other.getAppResponse() != null) {
                return false;
            }
        } else {
            if (other.getAppResponse() == null) {
                return false;
            } else if (!appResponse.equals(other.getAppResponse())){
                return false;
            }
        }
        return true;
    }
}
