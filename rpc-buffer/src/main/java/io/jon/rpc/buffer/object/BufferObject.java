package io.jon.rpc.buffer.object;

import io.jon.rpc.protocol.RpcProtocol;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;

import java.io.Serializable;

@Data
public class BufferObject<T> implements Serializable {

    private static final long serialVersionUID = -5465112244213170405L;

    // Netty读写数据的ChannelHandlerContext
    private ChannelHandlerContext ctx;
    // 网络传输协议对象
    private RpcProtocol<T> protocol;

    public BufferObject(){}

    public BufferObject(ChannelHandlerContext ctx, RpcProtocol<T> protocol){
        this.ctx = ctx;
        this.protocol = protocol;
    }
}
