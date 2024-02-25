package io.jon.rpc.proxy.jdk;

import io.jon.rpc.proxy.api.consumer.Consumer;
import io.jon.rpc.proxy.api.object.ObjectProxy;
import lombok.AllArgsConstructor;

import java.lang.reflect.Proxy;

@AllArgsConstructor
public class JdkProxyFactory {

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

//    public JdkProxyFactory(String serviceVersion, String serviceGroup,
//                           String serializationType, int messageType,
//                           long timeout, Consumer consumer,
//                           boolean async, boolean oneway){
//
//
//    }

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
