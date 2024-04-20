package io.jon.rpc.codec;

import io.jon.rpc.common.utils.SerializationUtils;
import io.jon.rpc.flow.processor.FlowPostProcessor;
import io.jon.rpc.protocol.RpcProtocol;
import io.jon.rpc.protocol.header.RpcHeader;
import io.jon.rpc.serialization.api.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

public class RpcEncoder extends MessageToByteEncoder<RpcProtocol<Object>> implements RpcCodec{

    private FlowPostProcessor flowPostProcessor;

    public RpcEncoder(FlowPostProcessor flowPostProcessor){
        this.flowPostProcessor = flowPostProcessor;
    }

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

        byteBuf.writeBytes(SerializationUtils.paddingString(serializationType).getBytes(StandardCharsets.UTF_8));
        byte[] data = serialization.serialize(msg.getBody());
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);

        header.setMsgLen(data.length);
        // 异步调用流控分析后置处理器
        // postFlowProcessor属于RpcCodec的接口方法
        // 里面的入参是接口FlowPostProcessor
        this.postFlowProcessor(flowPostProcessor, header);
    }
}
