package io.jon.rpc.consumer;

import io.jon.rpc.common.exception.RegistryException;
import io.jon.rpc.consumer.common.RpcConsumer;
import io.jon.rpc.proxy.api.ProxyFactory;
import io.jon.rpc.proxy.api.async.IAsyncObjectProxy;
import io.jon.rpc.proxy.api.config.ProxyConfig;
import io.jon.rpc.proxy.api.object.ObjectProxy;
import io.jon.rpc.registry.api.RegistryService;
import io.jon.rpc.registry.api.config.RegistryConfig;
import io.jon.rpc.registry.zookeeper.ZookeeperRegistryService;
import io.jon.rpc.spi.loader.ExtensionLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class RpcClient {
    public RpcClient(
            String registryAddress, String registryType, String proxy,
            String serviceVersion, String serviceGroup,
            long timeout, String serializationType,
            int messageType, boolean async, boolean oneway,
            String registryLoadBalanceType,
            int hearbeatInterval, int scanNotActiveChannelInterval,
            int retryInterval, int retryTimes) {
        this.serviceVersion = serviceVersion;
        this.proxy = proxy;
        this.serviceGroup = serviceGroup;
        this.timeout = timeout;
        this.registryService = getRegistryService(registryAddress, registryType, registryLoadBalanceType);
        this.serializationType = serializationType;
        this.messageType = messageType;
        this.async = async;
        this.oneway = oneway;
        this.hearbeatInterval = hearbeatInterval;
        this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        this.retryInterval = retryInterval;
        this.retryTimes = retryTimes;
    }

    private RegistryService getRegistryService(String registryAddress, String registryType, String registryLoadBalanceType) {

        if(StringUtils.isEmpty(registryType)){
            throw new IllegalArgumentException("registry type is null");
        }

        //TODO SPI扩展
        ZookeeperRegistryService registryService = new ZookeeperRegistryService();
        try{
            registryService.init(new RegistryConfig(registryAddress, registryType, registryLoadBalanceType));
        }catch (Exception e){
            logger.error("RpcClient init registry service throws exception:{}", e);
            throw new RegistryException(e.getMessage(), e);
        }
        return registryService;
    }

    private final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    // 服务版本号
    private String serviceVersion;

    // 服务分组
    private String serviceGroup;

    // 超时时间，默认15s
    private long timeout = 15000;

    private RegistryService registryService;

    // 序列化类型
    private String serializationType;

    // 消息类型
    private int messageType;

    // 是否异步调用
    private boolean async;

    // 是否单向调用
    private boolean oneway;

    // 动态代理的方式
    private String proxy;

    // 心跳间隔时间 默认30秒
    private int hearbeatInterval = 30000;

    // 扫描并移除空闲连接时间 默认60秒
    private int scanNotActiveChannelInterval = 60000;

    //重试间隔时间
    private int retryInterval = 1000;

    //重试次数
    private int retryTimes = 3;


    public <T> T create(Class<T> interfaceClass){

        ProxyFactory proxyFactory = ExtensionLoader.getExtension(ProxyFactory.class, proxy);
        proxyFactory.init(new ProxyConfig<>(
                interfaceClass,
                serviceVersion, serviceGroup,
                timeout, registryService,
                RpcConsumer.getInstance(
                        hearbeatInterval, scanNotActiveChannelInterval,
                        retryInterval, retryTimes),
                serializationType, messageType,
                async, oneway));
        return proxyFactory.getProxy(interfaceClass);
    }

    public void shutdown(){
        RpcConsumer.getInstance(
                hearbeatInterval, scanNotActiveChannelInterval,
                retryInterval, retryTimes).close();
    }

    public <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass){
        return new ObjectProxy<T>(
                interfaceClass,
                serviceVersion, serviceGroup,
                timeout, registryService,
                RpcConsumer.getInstance(
                        hearbeatInterval, scanNotActiveChannelInterval,
                        retryInterval, retryTimes),
                serializationType, messageType,
                async, oneway);
    }
}
