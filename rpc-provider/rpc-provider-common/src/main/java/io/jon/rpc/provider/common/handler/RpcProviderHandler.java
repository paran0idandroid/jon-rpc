package io.jon.rpc.provider.common.handler;

import io.jon.rpc.common.helper.RpcServiceHelper;
import io.jon.rpc.common.threadpool.ServerThreadPool;
import io.jon.rpc.constants.RpcConstants;
import io.jon.rpc.protocol.RpcProtocol;
import io.jon.rpc.protocol.enumeration.RpcStatus;
import io.jon.rpc.protocol.enumeration.RpcType;
import io.jon.rpc.protocol.header.RpcHeader;
import io.jon.rpc.protocol.request.RpcRequest;
import io.jon.rpc.protocol.response.RpcResponse;
import io.jon.rpc.provider.common.cache.ProviderChannelCache;
import io.jon.rpc.reflect.api.ReflectInvoker;
import io.jon.rpc.spi.loader.ExtensionLoader;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
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

    private ReflectInvoker reflectInvoker;

    public RpcProviderHandler(Map<String, Object> handlerMap, String reflectType){

        this.handlerMap = handlerMap;
        this.reflectInvoker = ExtensionLoader.getExtension(ReflectInvoker.class, reflectType);
    }


    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        ProviderChannelCache.remove(ctx.channel());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ProviderChannelCache.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ProviderChannelCache.remove(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) throws Exception {
        ServerThreadPool.submit(() -> {
            RpcProtocol<RpcResponse> responseRpcProtocol = handlerMessage(protocol, ctx.channel());
            ctx.writeAndFlush(responseRpcProtocol).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    log.debug("Send response for request " + protocol.getHeader().getRequestId());
                }
            });
        });
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if(evt instanceof IdleStateEvent){
            Channel channel = ctx.channel();
            try{
                log.info("IdleStateEvent triggered, close channel " + channel);
                channel.close();
            }finally {
                channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }

        super.userEventTriggered(ctx, evt);
    }

    private RpcProtocol<RpcResponse> handlerMessage(RpcProtocol<RpcRequest> protocol, Channel channel){

        RpcProtocol<RpcResponse> responseRpcProtocol = null;
        RpcHeader header = protocol.getHeader();

        // consumer发送给provider ping ping ping 心跳类型消息
        if(header.getMsgType() == (byte) RpcType.HEARTBEAT_FROM_CONSUMER.getType()){
            // provider需要返回pong pong pong 心跳类型消息
            responseRpcProtocol = handlerHeartbeatMessageFromConsumer(protocol, header);
        }else if(header.getMsgType() == (byte) RpcType.REQUEST.getType()){
            // 请求类型消息
            responseRpcProtocol = handlerRequestMessage(protocol, header);
        }else if (header.getMsgType() == (byte) RpcType.HEARTBEAT_TO_PROVIDER.getType()){
            // 接收到服务消费者响应的 pong pong pong 心跳消息
            handlerHeartbeatMessageToProvider(protocol, channel);
        }
        return responseRpcProtocol;
    }

    private RpcProtocol<RpcResponse> handlerRequestMessage(RpcProtocol<RpcRequest> protocol, RpcHeader header) {

        header.setMsgType((byte) RpcType.RESPONSE.getType());
        RpcRequest request = protocol.getBody();
        log.debug("Receive request " + header.getRequestId());
        RpcProtocol<RpcResponse> responseRpcProtocol = new RpcProtocol<>();
        RpcResponse response = new RpcResponse();
        try{
            Object result = handle(request);
            response.setResult(result);
            response.setOneway(request.isOneway());
            response.setAsync(request.isAsync());
            header.setStatus((byte) RpcStatus.SUCCESS.getCode());
        }catch (Throwable t){
            response.setError(t.toString());
            header.setStatus((byte) RpcStatus.FAIL.getCode());
            log.error("RPC Server handler request error: ", t);
        }

        responseRpcProtocol.setHeader(header);
        responseRpcProtocol.setBody(response);
        return responseRpcProtocol;
    }

    private RpcProtocol<RpcResponse> handlerHeartbeatMessageFromConsumer(RpcProtocol<RpcRequest> protocol, RpcHeader header) {

        // 处理发送给consumer的pong pong pong心跳信息
        header.setMsgType((byte) RpcType.HEARTBEAT_TO_CONSUMER.getType());
        RpcRequest request = protocol.getBody();
        RpcProtocol<RpcResponse> responseRpcProtocol = new RpcProtocol<>();
        RpcResponse response = new RpcResponse();
        response.setResult(RpcConstants.HEARTBEAT_PONG);
        response.setOneway(request.isOneway());
        response.setAsync(request.isAsync());
        header.setStatus((byte)RpcStatus.SUCCESS.getCode());
        responseRpcProtocol.setBody(response);
        responseRpcProtocol.setHeader(header);
        return responseRpcProtocol;
    }

    /**
     * 处理服务消费者响应的心跳消息
     */
    private void handlerHeartbeatMessageToProvider(RpcProtocol<RpcRequest> protocol, Channel channel) {
        log.info("receive service consumer's ===pong pong pong=== heartbeat message, " +
                "the consumer is: {}, the heartbeat message is: {}",
                channel.remoteAddress(), protocol.getBody().getParameters()[0]);
    }

    private Object handle(RpcRequest request) throws Throwable{
        String serviceKey = RpcServiceHelper.buildServiceKey(
                request.getClassName(),
                request.getVersion(),
                request.getGroup());

        Object serviceBean = handlerMap.get(serviceKey);
        if(serviceBean == null){
            throw new RuntimeException(String.format(
                    "service not exist: %s:%s",
                    request.getClassName(),
                    request.getMethodName()
            ));
        }

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        log.debug(serviceClass.getName());
        log.debug(methodName);

        if(parameterTypes != null && parameterTypes.length > 0){
            for (int i = 0; i < parameterTypes.length; ++i) {
                log.info(parameterTypes[i].getName());
            }
        }

        if(parameters != null && parameters.length > 0){
            for (int i = 0; i < parameters.length; ++i) {
                log.info(parameters[i].toString());
            }
        }

        return this.reflectInvoker.invokeMethod(serviceBean, serviceClass, methodName, parameterTypes, parameters);
    }

}
