package io.jon.rpc.test.consumer.codec.handler;

import com.alibaba.fastjson.JSONObject;
import io.jon.rpc.protocol.RpcProtocol;
import io.jon.rpc.protocol.enumeration.RpcType;
import io.jon.rpc.protocol.header.RpcHeaderFactory;
import io.jon.rpc.protocol.request.RpcRequest;
import io.jon.rpc.protocol.response.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcTestConsumerHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                RpcProtocol<RpcResponse> protocol) throws Exception {

        log.info("服务消费者接收到的数据===>>>{}", JSONObject.toJSONString(protocol));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        log.info("发送数据开始...");

        //模拟消费者发送数据
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        protocol.setHeader(RpcHeaderFactory.getRequestHeader("jdk", RpcType.REQUEST.getType()));
        RpcRequest request = new RpcRequest();
//        request.setClassName("io.jon.rpc.test.DemoService");
        request.setClassName("io.jon.rpc.test.api.DemoService");
        request.setGroup("jon");
        request.setMethodName("hello");
        request.setParameters(new Object[]{"jon"});
        request.setParameterTypes(new Class[]{String.class});
        request.setVersion("1.0.0");
        request.setAsync(false);
        request.setOneway(false);
        protocol.setBody(request);
        log.info("服务消费者发送的数据===>>>{}", JSONObject.toJSONString(protocol));

        ctx.writeAndFlush(protocol);
        log.info("发送数据完毕...");

    }



}
