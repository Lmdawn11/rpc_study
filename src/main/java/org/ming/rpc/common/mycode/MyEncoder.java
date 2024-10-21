package org.ming.rpc.common.mycode;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ming.rpc.common.Message.RpcRequest;
import org.ming.rpc.common.Message.MessageType;
import org.ming.rpc.common.Message.RpcResponse;
import org.ming.rpc.common.mySerializer.Serializer;

import static org.ming.rpc.utils.ByteBufferUtil.debugAll;


@Slf4j
@AllArgsConstructor
public class MyEncoder extends MessageToByteEncoder {
    private Serializer serializer;
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        //1
        if (msg instanceof RpcRequest){
            out.writeShort(MessageType.REQUEST.getCode());
        }else if(msg instanceof RpcResponse){
            out.writeShort(MessageType.RESPONSE.getCode());
        }
        //2 1
        out.writeShort(serializer.getType());


        byte[] serializeBytes = serializer.serialize(msg);

        //3.写入长度
        out.writeInt(serializeBytes.length);
        //4.写入序列化数组
        out.writeBytes(serializeBytes);
        log.debug("encode data .....");
        debugAll(out.nioBuffer());
    }
}
