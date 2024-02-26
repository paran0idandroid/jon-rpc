package io.jon.rpc.consumer;

import io.jon.rpc.consumer.common.RpcConsumer;
import io.jon.rpc.proxy.api.async.IAsyncObjectProxy;
import io.jon.rpc.proxy.api.object.ObjectProxy;
import io.jon.rpc.proxy.jdk.JdkProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcClient {
    public RpcClient(String serviceVersion, String serviceGroup,
                     long timeout, String serializationType,
                     int messageType, boolean async, boolean oneway) {
        this.serviceVersion = serviceVersion;
        this.serviceGroup = serviceGroup;
        this.timeout = timeout;
        this.serializationType = serializationType;
        this.messageType = messageType;
        this.async = async;
        this.oneway = oneway;
    }

    private final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    // 服务版本号
    private String serviceVersion;

    // 服务分组
    private String serviceGroup;

    // 超时时间，默认15s
    private long timeout = 15000;

    // 序列化类型
    private String serializationType;

    // 消息类型
    private int messageType;

    // 是否异步调用
    private boolean async;

    // 是否单向调用
    private boolean oneway;

    public <T> T create(Class<T> interfaceClass){

        JdkProxyFactory<T> jdkProxyFactory = new JdkProxyFactory<T>(
                serviceVersion, serviceGroup,
                timeout, RpcConsumer.getInstance(),
                serializationType, messageType,
                async, oneway);

        return jdkProxyFactory.getProxy(interfaceClass);
    }

    public void shutdown(){
        RpcConsumer.getInstance().close();
    }

    public <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass){
        return new ObjectProxy<T>(
                interfaceClass,
                serviceVersion, serviceGroup,
                timeout, RpcConsumer.getInstance(),
                serializationType, messageType,
                async, oneway);
    }
}
