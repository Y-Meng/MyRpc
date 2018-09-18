package demo;

/**
 * @author mengcy
 * @date 2018/9/18
 */
public class DemoServiceImpl implements IDemoService{

    @Override
    public String hello(String name) {
        return  name + ", hello! ---from server";
    }
}
