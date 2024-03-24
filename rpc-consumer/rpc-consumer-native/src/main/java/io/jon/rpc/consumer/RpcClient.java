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
            String registryLoadBalanceType) {
        this.serviceVersion = serviceVersion;
        this.proxy = proxy;
        this.serviceGroup = serviceGroup;
        this.timeout = timeout;
        this.registryService = getRegistryService(registryAddress, registryType, registryLoadBalanceType);
        this.serializationType = serializationType;
        this.messageType = messageType;
        this.async = async;
        this.oneway = oneway;
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

    public <T> T create(Class<T> interfaceClass){

        ProxyFactory proxyFactory = ExtensionLoader.getExtension(ProxyFactory.class, proxy);
        proxyFactory.init(new ProxyConfig<>(
                interfaceClass,
                serviceVersion, serviceGroup,
                timeout, registryService,
                RpcConsumer.getInstance(),
                serializationType, messageType,
                async, oneway));
        return proxyFactory.getProxy(interfaceClass);
    }

    public void shutdown(){
        RpcConsumer.getInstance().close();
    }

    public <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass){
        return new ObjectProxy<T>(
                interfaceClass,
                serviceVersion, serviceGroup,
                timeout, registryService,
                RpcConsumer.getInstance(),
                serializationType, messageType,
                async, oneway);
    }
}
