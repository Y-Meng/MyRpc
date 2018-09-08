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

    static final String DEFAULT_CONFIG = "rpc-consumer.properties";

    Properties properties;
    private static Configure instance;
    private String ip;

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

    public static Configure getInstance() {
        return instance;
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

    public String getIp() {
        return ip;
    }
}
