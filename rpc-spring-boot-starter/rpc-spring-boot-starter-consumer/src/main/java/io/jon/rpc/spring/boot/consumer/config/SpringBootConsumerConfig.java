package io.jon.rpc.spring.boot.consumer.config;

import lombok.Data;

@Data
public final class SpringBootConsumerConfig {

    /**
     * 注册地址
     */
    private String registryAddress;
    /**
     * 注册类型
     */
    private String registryType;
    /**
     * 负载均衡类型
     */
    private String loadBalanceType;
    /**
     * 代理
     */
    private String proxy;
    /**
     * 版本号
     */
    private String version;
    /**
     * 分组
     */
    private String group;
    /**
     * 序列化类型
     */
    private String serializationType;
    /**
     * 超时时间
     */
    private long timeout;
    /**
     * 是否异步
     */
    private boolean async;

    /**
     * 是否单向调用
     */
    private boolean oneway;

    /**
     * 心跳检测
     */
    private int heartbeatInterval;

    /**
     * 扫描并移除不活跃连接的时间间隔
     */
    private int scanNotActiveChannelInterval;

    //重试间隔时间
    private int retryInterval = 1000;

    //重试次数
    private int retryTimes = 3;

    /**
     * 是否开启结果缓存
     */
    private boolean enableResultCache;

    /**
     * 缓存结果的时长，单位是毫秒
     */
    private int resultCacheExpire;

    /**
     * 是否开启直连服务
     */
    private boolean enableDirectServer;

    /**
     * 直连服务的地址
     */
    private String directServerUrl;

    /**
     * 是否开启延迟连接
     */
    private boolean enableDelayConnection;

    /**
     * 并发线程池核心线程数
     */
    private int corePoolSize;

    /**
     * 并发线程池最大线程数
     */
    private int maximumPoolSize;

    /**
     * 流控分析类型
     */
    private String flowType;

    /**
     * 是否开启缓冲区
     */
    private boolean enableBuffer;

    /**
     * 缓冲区大小
     */
    private int bufferSize;

    /**
     * 反射类型
     */
    private String reflectType;

    /**
     * 容错类Class名称
     */
    private String fallbackClassName;

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



    public SpringBootConsumerConfig() {
    }

    public SpringBootConsumerConfig(
            final String registryAddress, final String registryType, final String loadBalanceType,
            final String proxy, final String version, final String group, final String serializationType,
            final long timeout, final boolean async, final boolean oneway, final int heartbeatInterval,
            final int scanNotActiveChannelInterval, final int retryInterval, final int retryTimes,
            final boolean enableDirectServer, final String directServerUrl,
            final boolean enableDelayConnection,
            final int corePoolSize, final int maximumPoolSize,
            String flowType,
            final boolean enableBuffer, final int bufferSize,
            final String reflectType, final String fallbackClassName,
            final boolean enableRateLimiter, final String rateLimiterType, final int permits, final int milliSeconds,
            final String rateLimiterFailStrategy, final boolean enableFusing, final String fusingType,
            final double totalFailure, final int fusingMilliSeconds, final String exceptionPostProcessorType


    ) {
        this.registryAddress = registryAddress;
        this.registryType = registryType;
        this.loadBalanceType = loadBalanceType;
        this.proxy = proxy;
        this.version = version;
        this.group = group;
        this.serializationType = serializationType;
        this.timeout = timeout;
        this.async = async;
        this.oneway = oneway;
        if (heartbeatInterval > 0){
            this.heartbeatInterval = heartbeatInterval;
        }
        this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        this.retryInterval = retryInterval;
        this.retryTimes = retryTimes;
        this.enableDirectServer = enableDirectServer;
        this.directServerUrl = directServerUrl;
        this.enableDelayConnection = enableDelayConnection;
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.flowType = flowType;
        this.enableBuffer = enableBuffer;
        this.bufferSize = bufferSize;
        this.fallbackClassName = fallbackClassName;
        this.reflectType = reflectType;
        this.enableRateLimiter = enableRateLimiter;
        this.rateLimiterType = rateLimiterType;
        this.permits = permits;
        this.milliSeconds = milliSeconds;
        this.rateLimiterFailStrategy = rateLimiterFailStrategy;
        this.enableFusing = enableFusing;
        this.fusingType = fusingType;
        this.totalFailure = totalFailure;
        this.fusingMilliSeconds = fusingMilliSeconds;
        this.exceptionPostProcessorType = exceptionPostProcessorType;

    }
}

