package io.jon.rpc.proxy.jdk;

import io.jon.rpc.proxy.api.consumer.Consumer;
import io.jon.rpc.proxy.api.object.ObjectProxy;

import java.lang.reflect.Proxy;

public class JdkProxyFactory<T> {

    // 服务版本号
    private String serviceVersion;

    // 服务分组
    private String serviceGroup;

    // 超时时间，默认15s
    private long timeout = 15000;

    // 服务消费者
    private Consumer consumer;

    // 序列化类型
    private String serializationType;

    // 消息类型
    private int messageType;

    // 是否异步调用
    private boolean async;

    // 是否单向调用
    private boolean oneway;

    public JdkProxyFactory(String serviceVersion, String serviceGroup,
                           long timeout, Consumer consumer,
                           String serializationType, int messageType,
                           boolean async, boolean oneway) {
        this.serviceVersion = serviceVersion;
        this.serviceGroup = serviceGroup;
        this.timeout = timeout;
        this.consumer = consumer;
        this.serializationType = serializationType;
        this.messageType = messageType;
        this.async = async;
        this.oneway = oneway;
    }

    public <T> T getProxy(Class<T> clazz){
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                new ObjectProxy<T>(
                        clazz, serviceVersion, serviceGroup,
                        timeout, consumer, serializationType,
                        messageType, async, oneway)
        );
    }
}
