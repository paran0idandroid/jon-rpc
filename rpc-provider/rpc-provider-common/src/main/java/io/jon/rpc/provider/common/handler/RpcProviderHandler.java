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
import io.jon.rpc.reflect.api.ReflectInvoker;
import io.jon.rpc.spi.loader.ExtensionLoader;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
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

    private ReflectInvoker reflectInvoker;

    public RpcProviderHandler(Map<String, Object> handlerMap, String reflectType){

        this.handlerMap = handlerMap;
        this.reflectInvoker = ExtensionLoader.getExtension(ReflectInvoker.class, reflectType);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol)
            throws Exception {
        /**
         * 通过调用submit方法使任务异步执行
         */
        ServerThreadPool.submit(()->{
            RpcHeader header = protocol.getHeader();
            header.setMsgType((byte)RpcType.RESPONSE.getType());
            RpcRequest request = protocol.getBody();
            log.debug("Receive request " + header.getRequestId());
            RpcProtocol<RpcResponse> responseRpcProtocol = new RpcProtocol<>();
            RpcResponse response = new RpcResponse();

            try{
                Object result = handle(request);
                response.setResult(result);
                response.setAsync(request.isAsync());
                response.setOneway(request.isOneway());
                header.setStatus((byte) RpcStatus.SUCCESS.getCode());
            }catch (Throwable t){
                response.setError(t.toString());
                header.setStatus((byte)RpcStatus.FAIL.getCode());
                log.error("RPC Server handle request error: ", t);
            }
            responseRpcProtocol.setHeader(header);
            responseRpcProtocol.setBody(response);
            ctx.writeAndFlush(responseRpcProtocol).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    log.debug("Send response for request: " + header.getRequestId());
                }
            });
        });
    }

    private RpcProtocol<RpcResponse> handlerMessage(RpcProtocol<RpcRequest> protocol){

        RpcProtocol<RpcResponse> responseRpcProtocol = null;
        RpcHeader header = protocol.getHeader();

        // 处理consumer发送到provider的心跳类型消息
        if(header.getMsgType() == (byte) RpcType.HEARTBEAT_FROM_CONSUMER.getType()){
            responseRpcProtocol = handlerHeartbeatMessage(protocol, header);
        }else if(header.getMsgType() == (byte) RpcType.REQUEST.getType()){
            // 请求类型消息
            responseRpcProtocol = handlerRequestMessage(protocol, header);
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

    private RpcProtocol<RpcResponse> handlerHeartbeatMessage(RpcProtocol<RpcRequest> protocol, RpcHeader header) {

        // 处理发送给consumer的心跳信息
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
