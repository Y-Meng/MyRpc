package com.mcy.rpc.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * @author zkzc-mcy create at 2018/8/24.
 */
public class Configure {

    static final String DEFAULT_CONFIG = "my-rpc.properties";

    private Properties properties;

    /** server 配置 */
    private int listen;

    /** client 配置 */
    private String remoteIp;
    private int remotePort;

    public Configure() {
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Configure(Properties properties){
        this.properties = properties;
        initValues(properties);
    }

    private void init() throws IOException {
        URL url = getClass().getResource("/");
        File file = new File(url.getPath() + DEFAULT_CONFIG);
        properties = new Properties();
        properties.load(new FileReader(file));
        initValues(properties);
    }

    private void initValues(Properties properties) {

        this.listen = Integer.valueOf(properties.getProperty("rpc.server.listen", "7000"));

        this.remoteIp = properties.getProperty("rpc.client.remote.ip", "127.0.0.1");
        this.remotePort = Integer.valueOf(properties.getProperty("rpc.client.remote.port", "7000"));
    }

    public void printServerConfig(){
        StringBuilder serverConfig = new StringBuilder("{");
        serverConfig.append("listen=");
        serverConfig.append(listen);
        serverConfig.append("}");
        System.out.println(serverConfig.toString());
    }

    public void printClientConfig(){
        StringBuilder clientConfig = new StringBuilder("{");
        clientConfig.append("remoteIp=");
        clientConfig.append(remoteIp);
        clientConfig.append(",remotePort=");
        clientConfig.append(remotePort);
        clientConfig.append("}");
        System.out.println(clientConfig);
    }

    public int getListen() {
        return listen;
    }

    public void setListen(int listen) {
        this.listen = listen;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }
}
