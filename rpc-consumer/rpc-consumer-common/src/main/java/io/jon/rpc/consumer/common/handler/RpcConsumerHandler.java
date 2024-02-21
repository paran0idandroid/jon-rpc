package io.jon.rpc.consumer.common.handler;

import com.alibaba.fastjson.JSONObject;
import io.jon.rpc.consumer.common.context.RpcContext;
import io.jon.rpc.consumer.common.future.RPCFuture;
import io.jon.rpc.protocol.RpcProtocol;
import io.jon.rpc.protocol.header.RpcHeader;
import io.jon.rpc.protocol.request.RpcRequest;
import io.jon.rpc.protocol.response.RpcResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
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
//    private Map<Long, RpcProtocol<RpcResponse>> pendingResponse = new ConcurrentHashMap<>();
    private Map<Long, RPCFuture> pendingRpc = new ConcurrentHashMap<>();
    public Channel getChannel(){
        return channel;
    }

    public SocketAddress getRemotePeer(){
        return remotePeer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception{

        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcResponse> protocol)
            throws Exception {

        if(protocol == null){
            return;
        }

        logger.info("服务消费者接收到的数据===>>>{}", JSONObject.toJSONString(protocol));

        RpcHeader header = protocol.getHeader();
        long requestId = header.getRequestId();
//        pendingResponse.put(requestId, protocol);

        RPCFuture rpcFuture = pendingRpc.remove(requestId);
        if(rpcFuture != null){
            // 将服务提供者返回来的数据protocol设置到rpcFuture中
            rpcFuture.done(protocol);
        }
    }

    /**
     * 服务消费者向服务提供者发送请求
     */
    public RPCFuture sendRequest(RpcProtocol<RpcRequest> protocol,
                                 boolean async,
                                 boolean oneway){

        logger.info("服务消费者发送的数据===>>>{}", JSONObject.toJSONString(protocol));

        return oneway ?
                this.sendRequestOneway(protocol) :
                async ?
                        this.sendRequestAsync(protocol) : this.sendRequestSync(protocol);
    }

    /**
     * 同步调用
     */
    private RPCFuture sendRequestSync(RpcProtocol<RpcRequest> protocol){
        RPCFuture rpcFuture = this.getRpcFuture(protocol);
        //消费者将消息发送出去之后，channelRead0会将服务提供者返回的数据设置到rpcFuture中
        channel.writeAndFlush(protocol);
        return rpcFuture;
    }

    /**
     * 异步调用
     */
    private RPCFuture sendRequestAsync(RpcProtocol<RpcRequest> protocol){

        RPCFuture rpcFuture = this.getRpcFuture(protocol);
        //异步调用，将RPCFuture放进RpcContext中
        RpcContext.getContext().setRpcFuture(rpcFuture);
        channel.writeAndFlush(protocol);
        return null;
    }

    /**
     * 单向调用
     */
    private RPCFuture sendRequestOneway(RpcProtocol<RpcRequest> protocol){
        channel.writeAndFlush(protocol);
        return null;
    }

    private RPCFuture getRpcFuture(RpcProtocol<RpcRequest> protocol){
        RPCFuture rpcFuture = new RPCFuture(protocol);
        RpcHeader header = protocol.getHeader();
        long requestId = header.getRequestId();
        pendingRpc.put(requestId, rpcFuture);
        return rpcFuture;
    }

    public void close(){
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
}
