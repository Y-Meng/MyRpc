package demo;

import com.mcy.rpc.api.RpcConsumer;
import com.mcy.rpc.api.impl.RpcConsumerImpl;

/**
 * @author mengcy
 * @date 2018/9/18
 */
public class RpcClientDemo {


    public static void initClient(){

        RpcConsumer consumer = new RpcConsumerImpl();
        consumer.interfaceClass(IDemoService.class)
                .version("1.0")
                .clientTimeout(1000);

        IDemoService service = (IDemoService) consumer.instance();

        for(int n = 0; n < 5; n++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 10000; i++) {
                String result = service.hello("你好" + i);
//            System.out.println(result);
            }
            long end = System.currentTimeMillis();
            System.out.println("耗时：" + (end - start) + "ms");
        }

        System.out.println("-----------------------------------------");

        for(int n = 0; n < 5; n++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 10000; i++) {
                int result = service.sum(i, 1);
//            System.out.println(result);
            }
            long end = System.currentTimeMillis();
            System.out.println("耗时：" + (end - start) + "ms");
        }
    }

    public static void main(String[] args){

        initClient();
    }
}
