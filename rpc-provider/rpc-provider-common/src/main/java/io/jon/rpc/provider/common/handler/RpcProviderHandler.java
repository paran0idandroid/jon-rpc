package io.jon.rpc.provider.common.handler;

import com.alibaba.fastjson.JSONObject;
import io.jon.rpc.protocol.RpcProtocol;
import io.jon.rpc.protocol.enumeration.RpcType;
import io.jon.rpc.protocol.header.RpcHeader;
import io.jon.rpc.protocol.request.RpcRequest;
import io.jon.rpc.protocol.response.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * SimpleChannelInboundHandler是Netty中的一个特殊类型的ChannelInboundHandler
 * 它用于处理入站事件（incoming events），例如接收到的数据或连接建立事件。
 * 它是一个泛型类，可以指定它要处理的消息类型。
 * 与ChannelInboundHandlerAdapter不同
 * SimpleChannelInboundHandler提供了一种更简单的方式来处理入站消息
 * 因为它自动释放资源以避免内存泄漏。当处理完消息后，它会自动释放对该消息的引用。
 * SimpleChannelInboundHandler主要用于处理入站消息并生成响应。
 * 它的一个常见用例是在服务器端，处理客户端发送的请求消息，并发送响应消息。
 * 在处理完消息后，您通常不需要手动释放消息
 * 因为SimpleChannelInboundHandler会在处理完毕后负责释放消息。
 */
@Slf4j
public class RpcProviderHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {

    private final Map<String, Object> handlerMap;

    public RpcProviderHandler(Map<String, Object> handlerMap){
        this.handlerMap = handlerMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol)
            throws Exception {

        log.info("RPC提供者收到的数据为====>>> " + JSONObject.toJSONString(protocol));
        log.info("handlerMap中存放的数据如下所示：");
        for (Map.Entry<String, Object> entry : handlerMap.entrySet()){
            log.info(entry.getKey() + "===" + entry.getValue());
        }

        RpcHeader header = protocol.getHeader();
        RpcRequest request = protocol.getBody();
        //将header中的消息类型设置为响应类型的消息
        header.setMsgType((byte) RpcType.RESPONSE.getType());
        //构建响应协议数据
        RpcProtocol<RpcResponse> responseRpcProtocol = new RpcProtocol<>();

        RpcResponse response = new RpcResponse();
        response.setResult("数据交互成功");
        response.setAsync(request.isAsync());
        response.setOneway(request.isOneway());
        responseRpcProtocol.setHeader(header);
        responseRpcProtocol.setBody(response);
        ctx.writeAndFlush(responseRpcProtocol);
    }
}
