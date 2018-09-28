package com.mcy.rpc.core.serializer;

import com.mcy.rpc.core.model.RpcRequest;
import com.mcy.rpc.core.model.RpcResponse;
import com.mcy.rpc.util.SerializeTool;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by mengchaoyue on 2018/9/8.
 */
public class RpcDecoder extends ByteToMessageDecoder {

    private static byte[] requestCacheName = null;
    private static RpcRequest requestCacheValue = null;
    private static byte[] responseCacheName = null;
    private static RpcResponse responseCacheValue = null;
    private Class<?> genericClass;

    public RpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        int HEAD_LENGTH = 4;
        if (in.readableBytes() < HEAD_LENGTH) {
            return;
        }

        in.markReaderIndex();
        int dataLength = in.readInt();
        if (dataLength < 0) {
            ctx.close();
        }
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }

        if (genericClass.equals(RpcResponse.class)) {

            // 获取到requestId的长度
            int requestIdLength = in.readInt();

            byte[] requestIdBytes = new byte[requestIdLength];
            in.readBytes(requestIdBytes);

            int bodyLength = dataLength - 4 - requestIdLength;

            byte[] body = new byte[bodyLength];
            in.readBytes(body);
            String requestId = new String(requestIdBytes);

            if (responseCacheName != null && cacheEqual(responseCacheName, body)) {

                RpcResponse response = new RpcResponse();
                response.setRequestId(requestId);
                response.setAppResponse(responseCacheValue.getAppResponse());
                response.setClazz(responseCacheValue.getClazz());
                response.setException(responseCacheValue.getException());

                out.add(response);
            } else {
                RpcResponse response = SerializeTool.deserialize(body, genericClass);
                // 设置requestId
                response.setRequestId(requestId);
                out.add(response);

                responseCacheName = body;
                responseCacheValue = new RpcResponse();
                responseCacheValue.setAppResponse(response.getAppResponse());
                responseCacheValue.setClazz(response.getClazz());
                responseCacheValue.setException(response.getException());

            }
        } else if (genericClass.equals(RpcRequest.class)) {

            //获取到requestId的长度
            int requestIdLength = in.readInt();

            byte[] requestIdBytes = new byte[requestIdLength];
            in.readBytes(requestIdBytes);

            int bodyLength = dataLength - 4 - requestIdLength;

            byte[] body = new byte[bodyLength];
            in.readBytes(body);
            String requestId = new String(requestIdBytes);

            if (requestCacheName != null && cacheEqual(requestCacheName, body)) {

                RpcRequest request = new RpcRequest();
                request.setClassName(requestCacheValue.getClassName());
                request.setContext(requestCacheValue.getContext());
                request.setMethodName(requestCacheValue.getMethodName());
                request.setParameters(requestCacheValue.getParameters());
                request.setParameterTypes(requestCacheValue.getParameterTypes());
                request.setRequestId(requestId);

                out.add(request);

            } else {
                RpcRequest request = SerializeTool.deserialize(body, genericClass);
                request.setRequestId(requestId);
                out.add(request);

                requestCacheName = body;
                requestCacheValue = new RpcRequest();
                requestCacheValue.setClassName(request.getClassName());
                requestCacheValue.setContext(request.getContext());
                requestCacheValue.setMethodName(request.getMethodName());
                requestCacheValue.setParameters(request.getParameters());
                requestCacheValue.setParameterTypes(request.getParameterTypes());
            }
        } else {
            byte[] body = new byte[dataLength];
            in.readBytes(body);
            Object obj = SerializeTool.deserialize(body, genericClass);
            out.add(obj);
        }
    }

    private static boolean cacheEqual(byte[] data1, byte[] data2) {

        if (data1 == null) {
            if (data2 != null) {
                return false;
            }
        } else {
            if (data2 == null) {
                return false;
            }

            if (data1.length != data2.length) {
                return false;
            }

            for (int i = 0; i < data1.length; i++) {
                if (data1[i] != data2[i]) {
                    return false;
                }
            }
        }
        return true;
    }

}
