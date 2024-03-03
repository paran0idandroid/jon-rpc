package io.jon.rpc.consumer;

import io.jon.rpc.common.exception.RegistryException;
import io.jon.rpc.consumer.common.RpcConsumer;
import io.jon.rpc.proxy.api.async.IAsyncObjectProxy;
import io.jon.rpc.proxy.api.config.ProxyConfig;
import io.jon.rpc.proxy.api.object.ObjectProxy;
import io.jon.rpc.proxy.jdk.JdkProxyFactory;
import io.jon.rpc.registry.api.RegistryService;
import io.jon.rpc.registry.api.config.RegistryConfig;
import io.jon.rpc.registry.zookeeper.ZookeeperRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class RpcClient {
    public RpcClient(
            String registryAddress, String registryType,
            String serviceVersion, String serviceGroup,
            long timeout, String serializationType,
            int messageType, boolean async, boolean oneway) {
        this.serviceVersion = serviceVersion;
        this.serviceGroup = serviceGroup;
        this.timeout = timeout;
        this.registryService = getRegistryService(registryAddress, registryType);
        this.serializationType = serializationType;
        this.messageType = messageType;
        this.async = async;
        this.oneway = oneway;
    }

    private RegistryService getRegistryService(String registryAddress, String registryType) {

        if(StringUtils.isEmpty(registryType)){
            throw new IllegalArgumentException("registry type is null");
        }

        //TODO SPI扩展
        ZookeeperRegistryService registryService = new ZookeeperRegistryService();
        try{
            registryService.init(new RegistryConfig(registryAddress, registryType));
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

    public <T> T create(Class<T> interfaceClass){

        JdkProxyFactory<T> jdkProxyFactory = new JdkProxyFactory<T>();
        jdkProxyFactory.init(new ProxyConfig<>(
                interfaceClass,
                serviceVersion, serviceGroup,
                timeout, registryService,
                RpcConsumer.getInstance(),
                serializationType, messageType,
                async, oneway));
        return jdkProxyFactory.getProxy(interfaceClass);
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
