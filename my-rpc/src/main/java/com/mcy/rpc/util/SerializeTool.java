package com.mcy.rpc.util;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zkzc-mcy create at 2018/8/24.
 *
 * 序列化工具，使用protostuff
 */
public class SerializeTool {

    private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

    private SerializeTool() {
    }

    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            if (schema != null) {
                cachedSchema.put(cls, schema);
            }
        }
        return schema;
    }

    /**
     * 序列化（对象 -> 字节数组）
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] serialize(T obj) {

        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化（字节数组 -> 对象）
     */
    public static <T> T deserialize(byte[] data, Class<?> cls) {
        try {
            T message = (T) cls.newInstance();
            Schema<T> schema = getSchema((Class<T>) cls);
            ProtostuffIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }


    public static Object ByteToObject(byte[] bytes) {
        return ByteObjConverter.ByteToObject(bytes);
    }

    public static byte[] ObjectToByte(Object obj) {
        return ByteObjConverter.ObjectToByte(obj);
    }
}
