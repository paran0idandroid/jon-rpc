package io.jon.rpc.consumer.common;

import io.jon.rpc.common.helper.RpcServiceHelper;
import io.jon.rpc.consumer.common.handler.RpcConsumerHandler;
import io.jon.rpc.consumer.common.helper.RpcConsumerHandlerHelper;
import io.jon.rpc.consumer.common.initializer.RpcConsumerInitializer;
import io.jon.rpc.protocol.RpcProtocol;
import io.jon.rpc.protocol.meta.ServiceMeta;
import io.jon.rpc.protocol.request.RpcRequest;
import io.jon.rpc.proxy.api.consumer.Consumer;
import io.jon.rpc.proxy.api.future.RPCFuture;
import io.jon.rpc.registry.api.RegistryService;
import io.jon.rpc.threadpool.ClientThreadPool;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class RpcConsumer implements Consumer {

    private final Logger logger = LoggerFactory.getLogger(RpcConsumer.class);
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;
    private static volatile RpcConsumer instance;

    private static Map<String, RpcConsumerHandler> handlerMap = new ConcurrentHashMap<>();

    private RpcConsumer() {

        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new RpcConsumerInitializer());
    }

    public static RpcConsumer getInstance(){
        if(instance == null){
            synchronized (RpcConsumer.class){
                if(instance == null){
                    instance = new RpcConsumer();
                }
            }
        }
        return instance;
    }

    public void close(){
        RpcConsumerHandlerHelper.closeRpcClientHandler();
        eventLoopGroup.shutdownGracefully();
        ClientThreadPool.shutdown();
    }

    @Override
    public RPCFuture sendRequest(
            RpcProtocol<RpcRequest> protocol,
            RegistryService registryService)
            throws Exception{

        RpcRequest request = protocol.getBody();
        String serviceKey = RpcServiceHelper.buildServiceKey(
                request.getClassName(),
                request.getVersion(),
                request.getGroup()
        );
        Object[] parameters = request.getParameters();
        int invokerHashCode = (parameters == null || parameters.length <= 0)
                ? serviceKey.hashCode() : parameters[0].hashCode();

        ServiceMeta serviceMeta = registryService.discovery(serviceKey, invokerHashCode);
        if(serviceMeta != null){
            RpcConsumerHandler handler = RpcConsumerHandlerHelper.get(serviceMeta);

            //缓存中没有handler
            if(handler == null){
                handler = getRpcConsumerHandler(serviceMeta.getServiceAddr(), serviceMeta.getServicePort());
                RpcConsumerHandlerHelper.put(serviceMeta, handler);
            }else if(!handler.getChannel().isActive()){
                //缓存中存在但不活跃
                handler.close();
                handler = getRpcConsumerHandler(serviceMeta.getServiceAddr(), serviceMeta.getServicePort());
                RpcConsumerHandlerHelper.put(serviceMeta, handler);
            }
            return handler.sendRequest(protocol, request.isAsync(), request.isOneway());
        }

        return null;
    }

    private RpcConsumerHandler getRpcConsumerHandler(String serviceAddress, int port) throws InterruptedException{

        ChannelFuture channelFuture = bootstrap.connect(serviceAddress, port).sync();
        channelFuture.addListener((ChannelFutureListener) listener ->{
            if(channelFuture.isSuccess()){
                logger.info("connect rpc server {} on port {} success.", serviceAddress, port);
            }else{
                logger.error("connect rpc server {} on port {} failed.", serviceAddress, port);
                channelFuture.cause().printStackTrace();
                eventLoopGroup.shutdownGracefully();
            }
        });

        return channelFuture.channel().pipeline().get(RpcConsumerHandler.class);
    }
}
