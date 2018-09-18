package com.mcy.rpc.api;

import com.mcy.rpc.util.Configure;

/**
 * @author zkzc-mcy create at 2018/8/24.
 */
public abstract class RpcProvider {

    /** 配置信息 */
    protected Configure configure;

    public RpcProvider(Configure configure) {
        this.configure = configure;
        configure.printServerConfig();
    }

    /**
     * init Provider
     */
    private void init(){
        publish();
    }

    /**
     * set the interface which this provider want to expose as a service
     * @param serviceInterface
     */
    public RpcProvider serviceInterface(Class<?> serviceInterface){
        //TODO
        return this;
    }

    /**
     * set the version of the service
     * @param version
     */
    public RpcProvider version(String version){
        //TODO
        return this;
    }

    /**
     * set the instance which implements the service's interface
     * @param serviceInstance
     */
    public RpcProvider impl(Object serviceInstance){
        //TODO
        return this;
    }

    /**
     * set the timeout of the service
     * @param timeout
     */
    public RpcProvider timeout(int timeout){
        //TODO
        return this;
    }

    /**
     * set serialize type of this service
     * @param serializeType
     */
    public RpcProvider serializeType(String serializeType){
        //TODO
        return this;
    }

    /**
     * publish this service
     * if you want to publish your service , you need a registry server.
     * after all , you cannot write servers' ips in config file when you have 1 million server.
     * you can use ZooKeeper as your registry server to make your services found by your consumers.
     */
    public void publish() {
        //TODO
    }
}
