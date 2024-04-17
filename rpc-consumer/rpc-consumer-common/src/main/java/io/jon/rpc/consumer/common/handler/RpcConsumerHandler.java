package io.jon.rpc.consumer.common.handler;

import com.alibaba.fastjson.JSONObject;
import io.jon.rpc.constants.RpcConstants;
import io.jon.rpc.consumer.common.cache.ConsumerChannelCache;
import io.jon.rpc.consumer.common.context.RpcContext;
import io.jon.rpc.protocol.RpcProtocol;
import io.jon.rpc.protocol.enumeration.RpcStatus;
import io.jon.rpc.protocol.enumeration.RpcType;
import io.jon.rpc.protocol.header.RpcHeader;
import io.jon.rpc.protocol.header.RpcHeaderFactory;
import io.jon.rpc.protocol.request.RpcRequest;
import io.jon.rpc.protocol.response.RpcResponse;
import io.jon.rpc.proxy.api.future.RPCFuture;
import io.jon.rpc.threadpool.ConcurrentThreadPool;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcConsumerHandler extends
        SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {

    private final Logger logger = LoggerFactory.getLogger(RpcConsumerHandler.class);
    private volatile Channel channel;
    private SocketAddress remotePeer;

    //存储请求ID与RpcResponse协议的映射关系
    private Map<Long, RPCFuture> pendingRpc = new ConcurrentHashMap<>();

    private ConcurrentThreadPool concurrentThreadPool;

    public RpcConsumerHandler(ConcurrentThreadPool concurrentThreadPool){
        this.concurrentThreadPool = concurrentThreadPool;
    }

    public Channel getChannel() {
        return channel;
    }

    public SocketAddress getRemotePeer() {
        return remotePeer;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
        ConsumerChannelCache.add(channel);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception{

        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception{
        super.channelUnregistered(ctx);
        ConsumerChannelCache.remove(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception{

        super.channelInactive(ctx);
        ConsumerChannelCache.remove(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcResponse> protocol) throws Exception {

        if(protocol == null){
            return;
        }

        logger.info("服务消费者接收到的数据===>>>{}", JSONObject.toJSONString(protocol));

//        this.handlerMessage(protocol);
        concurrentThreadPool.submit(()->{
            this.handlerMessage(protocol, ctx.channel());
        });
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            //发送一次心跳数据
            RpcHeader header = RpcHeaderFactory.getRequestHeader(RpcConstants.SERIALIZATION_PROTOSTUFF,
                    RpcType.HEARTBEAT_FROM_CONSUMER.getType());
            RpcProtocol<RpcRequest> requestRpcProtocol = new RpcProtocol<RpcRequest>();
            RpcRequest rpcRequest = new RpcRequest();
            rpcRequest.setParameters(new Object[]{RpcConstants.HEARTBEAT_PING});
            requestRpcProtocol.setHeader(header);
            requestRpcProtocol.setBody(rpcRequest);
            ctx.writeAndFlush(requestRpcProtocol);
        }else {
            super.userEventTriggered(ctx, evt);
        }
    }


    private void handlerMessage(RpcProtocol<RpcResponse> protocol, Channel channel) {

        RpcHeader header = protocol.getHeader();

        //消费者接收到服务提供者的 响应心跳消息 pong
        if(header.getMsgType() == (byte) RpcType.HEARTBEAT_TO_CONSUMER.getType()){
            this.handlerHeartbeatMessageToConsumer(protocol, channel);
        }else if (header.getMsgType() == (byte) RpcType.HEARTBEAT_FROM_PROVIDER.getType()){
            // 消费者接收到ping 所以需要返回pong
            this.handlerHeartbeatMessageFromProvider(protocol, channel);
        }else if(header.getMsgType() == (byte) RpcType.RESPONSE.getType()){
            // 响应消息
            this.handlerResponseMessage(protocol, header);
        }
    }

    private void handlerResponseMessage(RpcProtocol<RpcResponse> protocol, RpcHeader header) {

        long requestId = header.getRequestId();
        RPCFuture rpcFuture = pendingRpc.remove(requestId);
        if(rpcFuture != null){
            rpcFuture.done(protocol);
        }
    }

    private void handlerHeartbeatMessageToConsumer(RpcProtocol<RpcResponse> protocol, Channel channel) {
        // 服务提供者响应的心跳消息 收到pong
        logger.info(
                "receive service provider ===pong pong pong=== heartbeat message" +
                ", the provider is:{}, the heart beat message is:{}",
                channel.remoteAddress(),
                protocol.getBody().getResult());
    }

    /**
     * 处理从服务提供者发送过来的心跳消息
     */
    private void handlerHeartbeatMessageFromProvider(RpcProtocol<RpcResponse> protocol, Channel channel) {
        RpcHeader header = protocol.getHeader();
        header.setMsgType((byte) RpcType.HEARTBEAT_TO_PROVIDER.getType());
        RpcProtocol<RpcRequest> requestRpcProtocol = new RpcProtocol<RpcRequest>();
        RpcRequest request = new RpcRequest();
        request.setParameters(new Object[]{RpcConstants.HEARTBEAT_PONG});
        header.setStatus((byte) RpcStatus.SUCCESS.getCode());
        requestRpcProtocol.setHeader(header);
        requestRpcProtocol.setBody(request);
        channel.writeAndFlush(requestRpcProtocol);
    }

    /**
     * 服务消费者向服务提供者发送请求
     */
    public RPCFuture sendRequest(RpcProtocol<RpcRequest> protocol, boolean async, boolean oneway){
        logger.info("服务消费者发送的数据===>>>{}", JSONObject.toJSONString(protocol));
        return concurrentThreadPool.submit(() -> {
            return oneway ? this.sendRequestOneway(protocol) : async ? sendRequestAsync(protocol) : this.sendRequestSync(protocol);
        });
    }

    private RPCFuture sendRequestSync(RpcProtocol<RpcRequest> protocol) {
        RPCFuture rpcFuture = this.getRpcFuture(protocol);
        channel.writeAndFlush(protocol);
        return rpcFuture;
    }


    private RPCFuture sendRequestAsync(RpcProtocol<RpcRequest> protocol) {
        RPCFuture rpcFuture = this.getRpcFuture(protocol);
        //如果是异步调用，则将RPCFuture放入RpcContext
        RpcContext.getContext().setRPCFuture(rpcFuture);
        channel.writeAndFlush(protocol);
        return null;
    }

    private RPCFuture sendRequestOneway(RpcProtocol<RpcRequest> protocol) {
        channel.writeAndFlush(protocol);
        return null;
    }

    private RPCFuture getRpcFuture(RpcProtocol<RpcRequest> protocol){
        RPCFuture rpcFuture = new RPCFuture(protocol, concurrentThreadPool);
        RpcHeader header = protocol.getHeader();
        long requestId = header.getRequestId();
        pendingRpc.put(requestId, rpcFuture);
        return rpcFuture;
    }

    public void close(){
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
}
