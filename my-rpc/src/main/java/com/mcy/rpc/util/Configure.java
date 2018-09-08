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

    // server 配置
    private int listen;

    // client 配置
    private String serverIp;
    private int serverPort;

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
        System.out.println(properties);

    }

    public int getListen() {
        return listen;
    }

    public void setListen(int listen) {
        this.listen = listen;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}
