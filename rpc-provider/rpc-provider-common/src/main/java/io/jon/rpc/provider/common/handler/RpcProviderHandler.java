package io.jon.rpc.provider.common.handler;

import io.jon.rpc.common.helper.RpcServiceHelper;
import io.jon.rpc.common.threadpool.ServerThreadPool;
import io.jon.rpc.protocol.RpcProtocol;
import io.jon.rpc.protocol.enumeration.RpcStatus;
import io.jon.rpc.protocol.enumeration.RpcType;
import io.jon.rpc.protocol.header.RpcHeader;
import io.jon.rpc.protocol.request.RpcRequest;
import io.jon.rpc.protocol.response.RpcResponse;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import io.jon.rpc.constants.RpcConstants;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import java.lang.reflect.Method;
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

    private final String reflectType;

    public RpcProviderHandler(Map<String, Object> handlerMap, String reflectType){

        this.handlerMap = handlerMap;
        this.reflectType = reflectType;
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

        return invokeMethod(serviceBean, serviceClass, methodName, parameterTypes, parameters);

    }

    private Object invokeMethod(Object serviceBean,
                                Class<?> serviceClass,
                                String methodName,
                                Class<?>[] parameterTypes,
                                Object[] parameters) throws Throwable{

        switch (this.reflectType) {

            case RpcConstants.REFLECT_TYPE_JDK:
                return this.invokeJDKMethod(
                        serviceBean,
                        serviceClass,
                        methodName,
                        parameterTypes,
                        parameters);
            case RpcConstants.REFLECT_TYPE_CGLIB:
                return this.invokeCGLibMethod(
                        serviceBean,
                        serviceClass,
                        methodName,
                        parameterTypes,
                        parameters);
            default:
                throw new IllegalArgumentException("not support reflect type");
        }
    }

    private Object invokeCGLibMethod(Object serviceBean,
                                     Class<?> serviceClass,
                                     String methodName,
                                     Class<?>[] parameterTypes,
                                     Object[] parameters) throws Throwable{
        log.info("use cglib reflect type to invoke method...");
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        return serviceFastMethod.invoke(serviceBean, parameters);
    }

    private Object invokeJDKMethod(Object serviceBean,
                                   Class<?> serviceClass,
                                   String methodName,
                                   Class<?>[] parameterTypes,
                                   Object[] parameters) throws Throwable{

        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);
    }

//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol)
//            throws Exception {
//
//        log.info("RPC提供者收到的数据为====>>> " + JSONObject.toJSONString(protocol));
//        log.info("handlerMap中存放的数据如下所示：");
//        for (Map.Entry<String, Object> entry : handlerMap.entrySet()){
//            log.info(entry.getKey() + "===" + entry.getValue());
//        }
//
//        RpcHeader header = protocol.getHeader();
//        RpcRequest request = protocol.getBody();
//        //将header中的消息类型设置为响应类型的消息
//        header.setMsgType((byte) RpcType.RESPONSE.getType());
//        //构建响应协议数据
//        RpcProtocol<RpcResponse> responseRpcProtocol = new RpcProtocol<>();
//
//        RpcResponse response = new RpcResponse();
//        response.setResult("数据交互成功");
//        response.setAsync(request.isAsync());
//        response.setOneway(request.isOneway());
//        responseRpcProtocol.setHeader(header);
//        responseRpcProtocol.setBody(response);
//        ctx.writeAndFlush(responseRpcProtocol);
//    }
}
