package demo;

import com.mcy.rpc.api.RpcProvider;
import com.mcy.rpc.api.impl.RpcProviderImpl;
import com.mcy.rpc.util.Configure;

/**
 * @author zkzc-mcy create at 2018/8/24.
 */
public class RpcServerDemo {

    public static void initServer(){
        Configure configure = new Configure();

        RpcProvider provider = new RpcProviderImpl(configure);
        provider.serviceInterface(IDemoService.class)
                .impl(DemoServiceImpl.class)
                .version("1.0")
                .timeout(1000)
                .publish();
    }


    public static void main(String[] args){

        initServer();
    }
}
