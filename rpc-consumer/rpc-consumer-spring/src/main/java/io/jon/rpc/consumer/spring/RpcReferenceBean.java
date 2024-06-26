package io.jon.rpc.consumer.spring;

import io.jon.rpc.consumer.RpcClient;
import lombok.Data;
import org.springframework.beans.factory.FactoryBean;

@Data
public class RpcReferenceBean implements FactoryBean<Object> {

    // 接口类型
    private Class<?> interfaceClass;

    // 版本号
    private String version;

    // 注册中心类型
    private String registryType;

    // 负载均衡类型
    private String loadBalanceType;

    // 序列化类型
    private String serializationType;

    // 消息类型
    private int messageType;

    /**
     * 注册中心地址
     */
    private String registryAddress;

    /**
     * 超时时间
     */
    private long timeout;

    /**
     * 服务分组
     */
    private String group;
    /**
     * 是否异步
     */
    private boolean async;

    /**
     * 是否单向调用
     */
    private boolean oneway;
    /**
     * 代理方式
     */
    private String proxy;
    /**
     * 生成的代理对象
     */
    private Object object;

    /**
     * 扫描空闲连接时间，默认60秒
     */
    private int scanNotActiveChannelInterval;

    /**
     * 心跳检测时间
     */
    private int heartbeatInterval;

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

    private RpcClient rpcClient;

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
     * 容错类
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
     * 异常处理类型
     */
    private String exceptionPostProcessorType;


    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    @Override
    public Object getObject() throws RuntimeException{
        return object;
    }

    @SuppressWarnings("unchecked")
    public void init() throws RuntimeException{

        rpcClient = new RpcClient(
                registryAddress, registryType,
                loadBalanceType, proxy,
                version, group,
                serializationType, timeout,
                async, oneway,
                heartbeatInterval, scanNotActiveChannelInterval,
                retryInterval, retryTimes,
                enableResultCache, resultCacheExpire,
                enableDirectServer, directServerUrl,
                enableDelayConnection,
                corePoolSize, maximumPoolSize,
                flowType,
                enableBuffer, bufferSize,
                reflectType, fallbackClassName,
                enableRateLimiter, rateLimiterType, permits, milliSeconds,
                rateLimiterFailStrategy,
                enableFusing, fusingType,
                totalFailure, fusingMilliSeconds,
                exceptionPostProcessorType
        );

        rpcClient.setFallbackClass(fallbackClass);
        this.object = rpcClient.create(interfaceClass);
    }
}
