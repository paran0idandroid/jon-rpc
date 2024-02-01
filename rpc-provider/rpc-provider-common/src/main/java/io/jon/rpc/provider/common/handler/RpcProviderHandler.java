package io.jon.rpc.provider.common.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class RpcProviderHandler extends SimpleChannelInboundHandler<Object> {

    private final Map<String, Object> handlerMap;

    public RpcProviderHandler(Map<String, Object> handlerMap){
        this.handlerMap = handlerMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object o) throws Exception {

        log.info("RPC提供者收到的数据为====>>> " + o.toString());
        log.info("handlerMap中存放的数据如下所示：");
        for (Map.Entry<String, Object> entry : handlerMap.entrySet()){
            log.info(entry.getKey() + "===" + entry.getValue());
        }
        //直接返回数据
        ctx.writeAndFlush(o);
    }
}
