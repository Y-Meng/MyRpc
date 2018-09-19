package demo;

import com.mcy.rpc.api.RpcConsumer;
import com.mcy.rpc.api.impl.RpcConsumerImpl;
import com.mcy.rpc.util.Configure;

/**
 * @author mengcy
 * @date 2018/9/18
 */
public class RpcClientDemo {


    public static void initClient(){
        Configure configure = new Configure();
        RpcConsumer consumer = new RpcConsumerImpl();
        consumer.interfaceClass(IDemoService.class)
                .version("1.0")
                .clientTimeout(1000);

        IDemoService service = (IDemoService) consumer.instance();
        for(int i = 0; i < 10; i++) {
            service.hello("你好" + i);
        }
    }

    public static void main(String[] args){

        initClient();
    }
}
