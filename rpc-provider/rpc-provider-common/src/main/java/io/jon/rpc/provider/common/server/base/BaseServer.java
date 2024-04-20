package io.jon.rpc.provider.common.server.base;

import io.jon.rpc.codec.RpcDecoder;
import io.jon.rpc.codec.RpcEncoder;
import io.jon.rpc.constants.RpcConstants;
import io.jon.rpc.flow.processor.FlowPostProcessor;
import io.jon.rpc.provider.common.handler.RpcProviderHandler;
import io.jon.rpc.provider.common.manager.ProviderConnectionManager;
import io.jon.rpc.provider.common.server.api.Server;
import io.jon.rpc.registry.api.RegistryService;
import io.jon.rpc.registry.api.config.RegistryConfig;
import io.jon.rpc.spi.loader.ExtensionLoader;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BaseServer implements Server {

    protected String host = "127.0.0.1";

    protected int port = 27110;

    //心跳间隔时间，默认30秒
    private int heartbeatInterval = 30000;

    //扫描并移除空闲连接时间，默认60秒
    private int scanNotActiveChannelInterval = 60000;

    protected Map<String, Object> handlerMap = new HashMap<>();

    private final String reflectType;

    protected RegistryService registryService;

    private ScheduledExecutorService executorService;

    //是否开启结果缓存
    private boolean enableResultCache;

    //结果缓存过期时长，默认5秒
    private int resultCacheExpire = 5000;

    //核心线程数
    private int corePoolSize;
    //最大线程数
    private int maximumPoolSize;
    //流控分析后置处理器
    private FlowPostProcessor flowPostProcessor;


    public BaseServer(String serverAddress,
                      String registryAddress,
                      String registryType,
                      String reflectType,
                      String registryLoadBalanceType,
                      int heartbeatInterval,
                      int scanNotActiveChannelInterval,
                      boolean enableResultCache,
                      int resultCacheExpire,
                      int corePoolSize,
                      int maximumPoolSize,
                      String flowType){

        if(heartbeatInterval > 0){
            this.heartbeatInterval = heartbeatInterval;
        }
        if(scanNotActiveChannelInterval > 0){
            this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        }
        if(!StringUtils.isEmpty(serverAddress)){
            String[] serverArray = serverAddress.split(":");
            this.host = serverArray[0];
            this.port = Integer.parseInt(serverArray[1]);
        }

        this.reflectType = reflectType;
        this.registryService = this.getRegistryService(registryAddress, registryType, registryLoadBalanceType);

        this.enableResultCache = enableResultCache;
        if(resultCacheExpire > 0){
            this.resultCacheExpire = resultCacheExpire;
        }
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;

        // 通过SPI技术为flowPostProcessor成员变量赋值
        this.flowPostProcessor = ExtensionLoader.getExtension(FlowPostProcessor.class, flowType);
    }

    private RegistryService getRegistryService(String registryAddress, String registryType, String registryLoadBalanceType) {

        RegistryService registryService = null;
        try{
            registryService = ExtensionLoader.getExtension(RegistryService.class, registryType);
            registryService.init(new RegistryConfig(registryAddress, registryType, registryLoadBalanceType));
        }catch (Exception e){
            log.error("RPC Server init error: ", e);
        }

        return registryService;
    }


    @Override
    public void startNettyServer() {

        this.startHeartbeat();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(RpcConstants.CODEC_DECODER, new RpcDecoder(flowPostProcessor))
                                    .addLast(RpcConstants.CODEC_ENCODER, new RpcEncoder(flowPostProcessor))
                                    .addLast(
                                            RpcConstants.CODEC_SERVER_IDLE_HANDLER,
                                            // readerIdleTime 读空闲超时检测定时任务在每readerIdleTime启动一次
                                            // 如果在该时间内没有发生过读事件，则触发读超时事件READER_IDLE_STATE_EVENT
                                            // 并将超时事件交给NettyClientHandler处理，如果为0则不创建定时任务
                                            // writerIdleTime同理
                                            new IdleStateHandler(
                                                    0,
                                                    0,
                                                    heartbeatInterval, TimeUnit.MILLISECONDS))
                                    // 当Netty的IdleStateHandler触发超时机制，会将事件传递到下一个handler
                                    // 就是下面这个RpcProviderHandler
                                    // 接收超时事件的方法是userEventTriggered
                                    .addLast(
                                            RpcConstants.CODEC_HANDLER,
                                            new RpcProviderHandler(
                                                    reflectType, enableResultCache, resultCacheExpire,
                                                    corePoolSize, maximumPoolSize, handlerMap));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = bootstrap.bind(host, port).sync();
            log.info("Server started on {}:{}", host, port);
            future.channel().closeFuture().sync();
        }catch (Exception e){
            log.error("RPC Server start error", e);
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private void startHeartbeat() {
        executorService = Executors.newScheduledThreadPool(2);
        //扫描并处理所有不活跃的连接
        executorService.scheduleAtFixedRate(() -> {
            log.info("=============scanNotActiveChannel============");
            ProviderConnectionManager.scanNotActiveChannel();
        }, 10, scanNotActiveChannelInterval, TimeUnit.MILLISECONDS);

        executorService.scheduleAtFixedRate(()->{
            log.info("=============broadcastPingMessageFromProvider============");
            ProviderConnectionManager.broadcastPingMessageFromProvider();
        }, 3, heartbeatInterval, TimeUnit.MILLISECONDS);
    }
}
