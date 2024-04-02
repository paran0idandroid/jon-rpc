package io.jon.rpc.consumer.common;

import io.jon.rpc.common.helper.RpcServiceHelper;
import io.jon.rpc.common.ip.IpUtils;
import io.jon.rpc.constants.RpcConstants;
import io.jon.rpc.consumer.common.handler.RpcConsumerHandler;
import io.jon.rpc.consumer.common.helper.RpcConsumerHandlerHelper;
import io.jon.rpc.consumer.common.initializer.RpcConsumerInitializer;
import io.jon.rpc.consumer.common.manager.ConsumerConnectionManager;
import io.jon.rpc.loadbalancer.context.ConnectionsContext;
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

import java.net.ConnectException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class RpcConsumer implements Consumer {

    private final Logger logger = LoggerFactory.getLogger(RpcConsumer.class);
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;
    private static volatile RpcConsumer instance;
    private final String localIp;
    private static Map<String, RpcConsumerHandler> handlerMap = new ConcurrentHashMap<>();

    // 使用定时任务线程池向服务提供者定时发送心跳数据
    private ScheduledExecutorService executorService;

    // 心跳间隔时间 默认30秒
    private int hearbeatInterval = 30000;

    // 扫描并移除空闲连接时间 默认60秒
    private int scanNotActiveChannelInterval = 60000;

    //重试间隔时间
    private int retryInterval = 1000;

    //重试次数
    private int retryTimes = 3;

    //当前重试次数
    private volatile int currentConnectRetryTimes = 0;


    private RpcConsumer(
            int hearbeatInterval,
            int scanNotActiveChannelInterval,
            int retryInterval,
            int retryTimes
    ) {

        this.retryInterval = retryInterval <= 0 ? RpcConstants.DEFAULT_RETRY_INTERVAL : retryInterval;
        this.retryTimes = retryTimes <= 0 ? RpcConstants.DEFAULT_RETRY_TIMES : retryTimes;
        if(hearbeatInterval > 0){
            this.hearbeatInterval = hearbeatInterval;
        }
        if(scanNotActiveChannelInterval > 0){
            this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        }
        localIp = IpUtils.getLocalHostIp();
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new RpcConsumerInitializer(hearbeatInterval));
        this.startHeartbeat();
    }

    public static RpcConsumer getInstance(int hearbeatInterval, int scanNotActiveChannelInterval, int retryInterval, int retryTimes){
        if(instance == null){
            synchronized (RpcConsumer.class){
                if(instance == null){
                    instance = new RpcConsumer(hearbeatInterval, scanNotActiveChannelInterval, retryInterval, retryTimes);
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

        ServiceMeta serviceMeta = this.getServiceMetaWithRetry(registryService, serviceKey, invokerHashCode);
        RpcConsumerHandler handler = null;
        if(serviceMeta != null){

            handler = getRpcConsumerHandlerWithRetry(serviceMeta);
        }
        RPCFuture rpcFuture = null;
        if(handler != null){
            rpcFuture = handler.sendRequest(protocol, request.isAsync(), request.isOneway());
        }

        return rpcFuture;
    }

    private RpcConsumerHandler getRpcConsumerHandler(ServiceMeta serviceMeta) throws InterruptedException{

        // 连接服务提供者
        ChannelFuture channelFuture = bootstrap.connect(serviceMeta.getServiceAddr(), serviceMeta.getServicePort()).sync();
        channelFuture.addListener((ChannelFutureListener) listener ->{
            if(channelFuture.isSuccess()){
                logger.info("connect rpc server {} on port {} success.", serviceMeta.getServiceAddr(), serviceMeta.getServicePort());
                // 添加连接信息 在服务消费者端记录每个服务提供者实例的连接次数
                ConnectionsContext.add(serviceMeta);
                currentConnectRetryTimes = 0;
            }else{
                logger.error("connect rpc server {} on port {} failed.", serviceMeta.getServiceAddr(), serviceMeta.getServicePort());
                channelFuture.cause().printStackTrace();
                eventLoopGroup.shutdownGracefully();
            }
        });

        return channelFuture.channel().pipeline().get(RpcConsumerHandler.class);
    }

    private void startHeartbeat(){

        executorService = Executors.newScheduledThreadPool(2);

        // 扫描并处理所有不活跃的连接
        executorService.scheduleAtFixedRate(() -> {
            logger.info("=======scanNotActiveChannel=======");
            ConsumerConnectionManager.scanNotActiveChannel();
        }, 10, scanNotActiveChannelInterval, TimeUnit.MILLISECONDS);

        executorService.scheduleAtFixedRate(()->{
            logger.info("=============broadcastPingMessageFromConsumer============");
            ConsumerConnectionManager.broadcastPingMessageFromConsumer();
        }, 3, hearbeatInterval, TimeUnit.MILLISECONDS);
    }

    private ServiceMeta getServiceMetaWithRetry(RegistryService registryService, String serviceKey, int invokeHashCode) throws Exception{

        // 首次获取服务元数据信息，如果获取到直接返回，否则进行重试
        logger.info("获取服务提供者元数据...");

        ServiceMeta serviceMeta = registryService.discovery(serviceKey, invokeHashCode, localIp);

        //启动重试机制
        if(serviceMeta == null){
            for (int i = 1; i <= retryTimes; i++) {
                logger.info("获取服务提供者元数据第【{}】次重试...", i);
                serviceMeta = registryService.discovery(serviceKey, invokeHashCode, localIp);
                if(serviceMeta != null){
                    break;
                }
                Thread.sleep(retryInterval);
            }
        }

        return serviceMeta;
    }

    // 从缓存中获取RpcConsumerHandler，缓存中没有再创建
    private RpcConsumerHandler getRpcConsumerHandlerWithCache(ServiceMeta serviceMeta) throws InterruptedException{

        RpcConsumerHandler handler = RpcConsumerHandlerHelper.get(serviceMeta);
        if(handler == null){
            handler = getRpcConsumerHandler(serviceMeta);
            RpcConsumerHandlerHelper.put(serviceMeta, handler);
        }else if(!handler.getChannel().isActive()){
            handler.close();
            handler = getRpcConsumerHandler(serviceMeta);
            RpcConsumerHandlerHelper.put(serviceMeta, handler);
        }

        return handler;
    }

    private RpcConsumerHandler getRpcConsumerHandlerWithRetry(ServiceMeta serviceMeta) throws InterruptedException{

        logger.info("服务消费者连接服务提供者...");
        RpcConsumerHandler handler = null;

        try{
            handler = this.getRpcConsumerHandlerWithCache(serviceMeta);
        }catch (Exception e){

            if(e instanceof ConnectException){
                if(handler == null){
                    if(currentConnectRetryTimes < retryTimes){
                        currentConnectRetryTimes++;
                        logger.info("服务消费者连接服务提供者第【{}】次重试...", currentConnectRetryTimes);
                        handler = this.getRpcConsumerHandlerWithRetry(serviceMeta);
                        Thread.sleep(retryInterval);
                    }
                }
            }
        }
        return handler;
    }
}
