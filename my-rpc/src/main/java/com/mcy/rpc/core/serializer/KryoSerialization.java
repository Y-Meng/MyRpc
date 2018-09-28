package com.mcy.rpc.core.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * @author mengchaoyue on 2018/9/8.
 * Kryo 序列化工具类
 */
public class KryoSerialization {

    private Kryo kryo;
    private Registration registration = null;
    private Class<?> t;
    public KryoSerialization() {
        kryo = new Kryo();
        kryo.setReferences(true);
    }

    public void register(Class<?> T) {
        //注册类
        t = T;
        registration = kryo.register(t);
    }
    public byte[] Serialize(Object object) {
        Output output =  new Output(1, 4096);
        kryo.writeClassAndObject(output, object);
        byte[] bb = output.toBytes();
        output.flush();

        return bb;
    }

    public <T> T Deserialize(byte[] bb) {
        Input input = new Input(bb);
        @SuppressWarnings("unchecked")
        T res = (T) kryo.readClassAndObject(input);
        input.close();
        return res;
    }
}
