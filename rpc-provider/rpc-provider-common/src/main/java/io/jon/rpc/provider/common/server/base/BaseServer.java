package io.jon.rpc.provider.common.server.base;

import io.jon.rpc.codec.RpcDecoder;
import io.jon.rpc.codec.RpcEncoder;
import io.jon.rpc.provider.common.handler.RpcProviderHandler;
import io.jon.rpc.provider.common.server.api.Server;
import io.jon.rpc.registry.api.RegistryService;
import io.jon.rpc.registry.api.config.RegistryConfig;
import io.jon.rpc.registry.zookeeper.ZookeeperRegistryService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class BaseServer implements Server {

    protected String host = "127.0.0.1";

    protected int port = 27110;

    protected Map<String, Object> handlerMap = new HashMap<>();

    private final String reflectType;

    protected RegistryService registryService;

    public BaseServer(String serverAddress,
                      String registryAddress,
                      String registryType,
                      String reflectType){
        if(!StringUtils.isEmpty(serverAddress)){
            String[] serverArray = serverAddress.split(":");
            this.host = serverArray[0];
            this.port = Integer.parseInt(serverArray[1]);
        }

        this.reflectType = reflectType;
        this.registryService = this.getRegistryService(registryAddress, registryType);
    }

    private RegistryService getRegistryService(String registryAddress, String registryType) {

        //TODO 后续扩展支持SPI
        RegistryService registryService = null;
        try{
            registryService = new ZookeeperRegistryService();
            registryService.init(new RegistryConfig(registryAddress, registryType));
        }catch (Exception e){
            log.error("RPC Server init error: ", e);
        }

        return registryService;
    }


    @Override
    public void startNettyServer() {
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
//                                    .addLast(new StringDecoder()) //Netty自带
//                                    .addLast(new StringEncoder()) //Netty自带
                                    .addLast(new RpcDecoder())
                                    .addLast(new RpcEncoder())
                                    .addLast(new RpcProviderHandler(handlerMap, reflectType));
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
}
