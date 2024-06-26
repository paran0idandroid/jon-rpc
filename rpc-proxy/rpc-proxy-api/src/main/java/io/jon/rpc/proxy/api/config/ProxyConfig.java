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

    // 是否异步调用
    private boolean async;

    // 是否单向调用
    private boolean oneway;

    /**
     * 是否开启结果缓存
     */
    private boolean enableResultCache;

    /**
     * 缓存结果的时长，单位是毫秒
     */
    private int resultCacheExpire;

    /**
     * 反射类型
     */
    private String reflectType;

    /**
     * 容错class名称
     */
    private String fallbackClassName;

    /**
     * 容错class
     */
    private Class<?> fallbackClass;

    /**
     * 是否开启限流
     */
    private boolean enableRateLimiter;
    /**
     * 限流类型
     */
    private String rateLimiterType;
    /**
     * 在milliSeconds毫秒内最多能够通过的请求个数
     */
    private int permits;
    /**
     * 毫秒数
     */
    private int milliSeconds;

    /**
     * 当限流失败时的处理策略
     */
    private String rateLimiterFailStrategy;

    /**
     * 是否开启熔断策略
     */
    private boolean enableFusing;

    /**
     * 熔断规则标识
     */
    private String fusingType;

    /**
     * 在fusingMilliSeconds毫秒内触发熔断操作的上限值
     */
    private double totalFailure;

    /**
     * 熔断的毫秒时长
     */
    private int fusingMilliSeconds;

    /**
     * 异常监控类型
     */
    private String exceptionPostProcessorType;


}
