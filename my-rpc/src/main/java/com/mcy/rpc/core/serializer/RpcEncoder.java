package com.mcy.rpc.core.serializer;

import com.mcy.rpc.core.model.RpcRequest;
import com.mcy.rpc.core.model.RpcResponse;
import com.mcy.rpc.util.SerializeTool;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by mengchaoyue on 2018/9/8.
 */
public class RpcEncoder extends MessageToByteEncoder {

    private static Object responseCacheName = null;
    private static byte[] responseCacheValue = null;
    private static Object requestCacheName = null;
    private static byte[] requestCacheValue = null;
    private Class<?> genericClass;

    public RpcEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {

        //如果是对RpcResponse进行编码，首先把request id指定为""，然后到缓存中找相等的对象的缓存值
        if (genericClass.equals(RpcResponse.class)) {

            RpcResponse response = (RpcResponse) msg;
            String requestId = response.getRequestId();
            response.setRequestId("");
            byte[] requestIdByte = requestId.getBytes();

            byte[] body = null;
            if (responseCacheName != null && responseCacheName.equals(response)) {
                body = responseCacheValue;
            } else {
                body = SerializeTool.serialize(msg);
                //缓存这个值
                responseCacheName = response;
                responseCacheValue = body;
            }

            //总长度 = 记录requestid的byte长度的int长度 + requestid的byte长度 + 真实数据的byte长度
            int totalLen = 4 + requestIdByte.length + body.length;

            out.writeInt(totalLen);
            out.writeInt(requestIdByte.length);
            out.writeBytes(requestIdByte);
            out.writeBytes(body);

        } else if (genericClass.equals(RpcRequest.class)) {

            RpcRequest request = (RpcRequest) msg;
            String requestId = request.getRequestId();
            request.setRequestId("");
            byte[] requestIdByte = requestId.getBytes();

            byte[] body = null;
            if (requestCacheName != null && requestCacheName.equals(request)) {
                body = requestCacheValue;
            } else {
                body = SerializeTool.serialize(msg);
                //缓存这个值
                requestCacheName = request;
                requestCacheValue = body;
            }

            //总长度为 一个表示 requestid的int 一个 requestid的byte长度 和真实数据的byte长度
            int totalLen = requestIdByte.length + 4 + body.length;

            out.writeInt(totalLen);
            out.writeInt(requestIdByte.length);
            out.writeBytes(requestIdByte);
            out.writeBytes(body);
        } else {
            byte[] body = SerializeTool.serialize(msg);
            out.writeInt(body.length);
            out.writeBytes(body);
        }
    }
}
