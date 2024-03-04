package io.jon.rpc.codec;

import io.jon.rpc.common.utils.SerializationUtils;
import io.jon.rpc.protocol.RpcProtocol;
import io.jon.rpc.protocol.header.RpcHeader;
import io.jon.rpc.serialization.api.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder<RpcProtocol<Object>> implements RpcCodec{

    @Override
    protected void encode(ChannelHandlerContext ctx,
                          RpcProtocol<Object> msg,
                          ByteBuf byteBuf) throws Exception {
        RpcHeader header = msg.getHeader();
        byteBuf.writeShort(header.getMagic());
        byteBuf.writeByte(header.getMsgType());
        byteBuf.writeByte(header.getStatus());
        byteBuf.writeLong(header.getRequestId());
        String serializationType = header.getSerializationType();
        Serialization serialization = getSerialization(serializationType);

        byteBuf.writeBytes(SerializationUtils.paddingString(serializationType).getBytes("UTF-8"));
        byte[] data = serialization.serialize(msg.getBody());
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
    }
}
