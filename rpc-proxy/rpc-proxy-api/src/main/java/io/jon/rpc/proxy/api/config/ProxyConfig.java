package io.jon.rpc.proxy.api.config;

import io.jon.rpc.proxy.api.consumer.Consumer;
import io.jon.rpc.registry.api.RegistryService;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class ProxyConfig<T> implements Serializable {

    private static final long serialVersionUID = 6648940252795742398L;

    // 接口的Class对象
    private Class<T> clazz;

    // 服务版本号
    private String serviceVersion;

    // 服务分组
    private String serviceGroup;

    // 超时时间
    private long timeout;

    private RegistryService registryService;

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
}
