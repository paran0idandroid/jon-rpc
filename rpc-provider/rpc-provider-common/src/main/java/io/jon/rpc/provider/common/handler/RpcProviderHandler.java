package io.jon.rpc.provider.common.handler;

import io.jon.rpc.buffer.cache.BufferCacheManager;
import io.jon.rpc.buffer.object.BufferObject;
import io.jon.rpc.cache.result.CacheResultKey;
import io.jon.rpc.cache.result.CacheResultManager;
import io.jon.rpc.common.helper.RpcServiceHelper;
import io.jon.rpc.common.utils.StringUtils;
import io.jon.rpc.connection.manager.ConnectionManager;
import io.jon.rpc.constants.RpcConstants;
import io.jon.rpc.protocol.RpcProtocol;
import io.jon.rpc.protocol.enumeration.RpcStatus;
import io.jon.rpc.protocol.enumeration.RpcType;
import io.jon.rpc.protocol.header.RpcHeader;
import io.jon.rpc.protocol.request.RpcRequest;
import io.jon.rpc.protocol.response.RpcResponse;
import io.jon.rpc.provider.common.cache.ProviderChannelCache;
import io.jon.rpc.ratelimiter.api.RateLimiterInvoker;
import io.jon.rpc.reflect.api.ReflectInvoker;
import io.jon.rpc.spi.loader.ExtensionLoader;
import io.jon.rpc.threadpool.BufferCacheThreadPool;
import io.jon.rpc.threadpool.ConcurrentThreadPool;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Logger logger = LoggerFactory.getLogger(RpcProviderHandler.class);
    /**
     * 存储服务提供者中被@RpcService注解标注的类的对象
     * key为：serviceName#serviceVersion#group
     * value为：@RpcService注解标注的类的对象
     */
    private final Map<String, Object> handlerMap;

    /**
     * 反射调用真实方法的SPI接口
     */
    private ReflectInvoker reflectInvoker;

    /**
     * 是否启用结果缓存
     */
    private final boolean enableResultCache;

    /**
     * 结果缓存管理器
     */
    private final CacheResultManager<RpcProtocol<RpcResponse>> cacheResultManager;

    /**
     * 线程池
     */
    private final ConcurrentThreadPool concurrentThreadPool;

    /**
     * 连接管理器
     */
    private ConnectionManager connectionManager;

    /**
     * 是否开启缓冲区
     */
    private boolean enableBuffer;

    /**
     * 缓冲区管理器
     */
    private BufferCacheManager<BufferObject<RpcRequest>> bufferCacheManager;

    /**
     * 是否开启限流
     */
    private boolean enableRateLimiter;

    /**
     * 限流SPI接口
     */
    private RateLimiterInvoker rateLimiterInvoker;


    public RpcProviderHandler(String reflectType,
                              boolean enableResultCache,
                              int resultCacheExpire,
                              int corePoolSize,
                              int maximumPoolSize,
                              Map<String, Object> handlerMap,
                              int maxConnections,
                              String disuseStrategyType,
                              boolean enableBuffer,
                              int bufferSize,
                              boolean enableRateLimiter,
                              String rateLimiterType,
                              int permits,
                              int milliSeconds){
        this.handlerMap = handlerMap;
        this.reflectInvoker = ExtensionLoader.getExtension(ReflectInvoker.class, reflectType);
        this.enableResultCache = enableResultCache;
        if (resultCacheExpire <= 0){
            resultCacheExpire = RpcConstants.RPC_SCAN_RESULT_CACHE_EXPIRE;
        }
        this.cacheResultManager = CacheResultManager.getInstance(resultCacheExpire, enableResultCache);
        this.concurrentThreadPool = ConcurrentThreadPool.getInstance(corePoolSize, maximumPoolSize);
        this.connectionManager = ConnectionManager.getInstance(maxConnections, disuseStrategyType);

        this.enableBuffer = enableBuffer;
        this.initBuffer(bufferSize);

        this.enableRateLimiter = enableRateLimiter;
        this.initRateLimiter(rateLimiterType, permits, milliSeconds);
    }

    /**
     * 初始化限流器
     */
    private void initRateLimiter(String rateLimiterType, int permits, int milliSeconds){
        if(enableRateLimiter){
            rateLimiterType = StringUtils.isEmpty(rateLimiterType) ?
                    RpcConstants.DEFAULT_RATELIMITER_INVOKER : rateLimiterType;
            this.rateLimiterInvoker = ExtensionLoader.getExtension(RateLimiterInvoker.class, rateLimiterType);
            this.rateLimiterInvoker.init(permits, milliSeconds);
        }
    }

    /**
     * 初始化缓冲区数据
     */
    private void initBuffer(int bufferSize){
        //开启缓冲
        if (enableBuffer){
            logger.info("开启数据缓冲.......");
            bufferCacheManager = BufferCacheManager.getInstance(bufferSize);
            BufferCacheThreadPool.submit(() -> {
                consumerBufferCache();
            });
        }
    }


    /**
     * 消费缓冲区的数据
     */
    private void consumerBufferCache() {

        while(true){
            BufferObject<RpcRequest> bufferObject = this.bufferCacheManager.take();
            if(bufferObject != null){
                ChannelHandlerContext ctx = bufferObject.getCtx();
                RpcProtocol<RpcRequest> protocol = bufferObject.getProtocol();
                RpcHeader header = protocol.getHeader();
                RpcProtocol<RpcResponse> responseRpcProtocol = handlerRequestMessageWithCache(protocol, header);
                this.writeAndFlush(header.getRequestId(), ctx, responseRpcProtocol);
            }
        }

    }

    private void writeAndFlush(long requestId, ChannelHandlerContext ctx, RpcProtocol<RpcResponse> responseRpcProtocol) {

        ctx.writeAndFlush(responseRpcProtocol).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                log.info("Send response for request:{}", requestId);
            }
        });
    }

    // 未开启数据缓冲执行一下方法
    private void submitRequest(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol){

        log.info("未开启数据缓冲，执行submitRequest方法.......");
        RpcProtocol<RpcResponse> responseRpcProtocol = handlerMessage(protocol, ctx.channel());
        writeAndFlush(protocol.getHeader().getRequestId(), ctx, responseRpcProtocol);
    }

    // 开启数据缓冲执行的方法
    private void bufferRequest(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol){

        RpcHeader header = protocol.getHeader();
        //接收到服务消费者发送的心跳消息
        if (header.getMsgType() == (byte) RpcType.HEARTBEAT_FROM_CONSUMER.getType()){
            RpcProtocol<RpcResponse> responseRpcProtocol = handlerHeartbeatMessageFromConsumer(protocol, header);
            this.writeAndFlush(protocol.getHeader().getRequestId(), ctx, responseRpcProtocol);
        }else if (header.getMsgType() == (byte) RpcType.HEARTBEAT_TO_PROVIDER.getType()){
            //接收到服务消费者响应的心跳消息
            handlerHeartbeatMessageToProvider(protocol, ctx.channel());
        }else if (header.getMsgType() == (byte) RpcType.REQUEST.getType()){ //请求消息
            this.bufferCacheManager.put(new BufferObject<>(ctx, protocol));
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        ProviderChannelCache.remove(ctx.channel());
        connectionManager.remove(ctx.channel());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ProviderChannelCache.add(ctx.channel());
        connectionManager.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ProviderChannelCache.remove(ctx.channel());
        connectionManager.remove(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) throws Exception {
        concurrentThreadPool.submit(() -> {
            connectionManager.update(ctx.channel());
            if (enableBuffer){  //开启队列缓冲
                this.bufferRequest(ctx, protocol);
            }else{  //未开启队列缓冲
                this.submitRequest(ctx, protocol);
            }
        });
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        // 使用finally块确保无论关闭是否成功，都会对通道进行一次写入和刷新
        // （channel.writeAndFlush(Unpooled.EMPTY_BUFFER)）
        // 并且添加一个监听器（ChannelFutureListener），监听通道关闭的事件，以确保通道已经被关闭
        // 最后，调用了父类的userEventTriggered方法，以确保父类中的相应逻辑也能被执行
        if(evt instanceof IdleStateEvent){
            Channel channel = ctx.channel();
            try{
                log.info("IdleStateEvent triggered, close channel " + channel);
                connectionManager.remove(channel);
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
        }else if (header.getMsgType() == (byte) RpcType.HEARTBEAT_TO_PROVIDER.getType()){
            // 接收到服务消费者响应的 pong pong pong 心跳消息
            handlerHeartbeatMessageToProvider(protocol, channel);
        }else if(header.getMsgType() == (byte) RpcType.REQUEST.getType()){
            // 请求类型消息
            responseRpcProtocol = handlerRequestMessageWithCacheAndRateLimiter(protocol, header);
        }
        return responseRpcProtocol;
    }

    private RpcProtocol<RpcResponse> handlerRequestMessage(RpcProtocol<RpcRequest> protocol, RpcHeader header) {

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
     * 带有限流模式提交请求信息
     */
    private RpcProtocol<RpcResponse> handlerRequestMessageWithCacheAndRateLimiter(RpcProtocol<RpcRequest> protocol, RpcHeader header){

        RpcProtocol<RpcResponse> responseRpcProtocol = null;
        if(enableRateLimiter){
            log.info("开启了限流.......");
            if(rateLimiterInvoker.tryAcquire()){
                try{
                    responseRpcProtocol = this.handlerRequestMessageWithCache(protocol, header);
                }finally {
                    rateLimiterInvoker.release();
                }
            }else {
                //TODO 执行各种策略
            }
        }else{
            responseRpcProtocol = this.handlerRequestMessageWithCache(protocol, header);
        }
        return responseRpcProtocol;
    }

    /**
     * 通过缓存处理服务提供者调用真实方法获取到的结果数据
     */
    private RpcProtocol<RpcResponse> handlerRequestMessageCache(RpcProtocol<RpcRequest> protocol, RpcHeader header){

        RpcRequest request = protocol.getBody();
        CacheResultKey cacheKey = new CacheResultKey(
                request.getClassName(), request.getMethodName(),
                request.getParameterTypes(), request.getParameters(),
                request.getVersion(), request.getGroup());

        RpcProtocol<RpcResponse> responseRpcProtocol = cacheResultManager.get(cacheKey);
        if(responseRpcProtocol == null){
            responseRpcProtocol = handlerRequestMessage(protocol, header);
            //设置保存的时间
            cacheKey.setCacheTimeStamp(System.currentTimeMillis());
            cacheResultManager.put(cacheKey, responseRpcProtocol);
        }
        responseRpcProtocol.setHeader(header);
        return responseRpcProtocol;
    }

    private RpcProtocol<RpcResponse> handlerRequestMessageWithCache(RpcProtocol<RpcRequest> protocol, RpcHeader header){

        header.setMsgType((byte)RpcType.RESPONSE.getType());
        if(enableResultCache) return handlerRequestMessageCache(protocol, header);
        return handlerRequestMessage(protocol, header);
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("server caught exception", cause);
        ProviderChannelCache.remove(ctx.channel());
        connectionManager.remove(ctx.channel());
        ctx.close();
    }


}
