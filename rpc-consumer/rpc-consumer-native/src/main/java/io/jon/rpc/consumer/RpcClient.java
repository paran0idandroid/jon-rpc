package io.jon.rpc.consumer;

import io.jon.rpc.common.exception.RegistryException;
import io.jon.rpc.consumer.common.RpcConsumer;
import io.jon.rpc.proxy.api.ProxyFactory;
import io.jon.rpc.proxy.api.async.IAsyncObjectProxy;
import io.jon.rpc.proxy.api.config.ProxyConfig;
import io.jon.rpc.proxy.api.object.ObjectProxy;
import io.jon.rpc.registry.api.RegistryService;
import io.jon.rpc.registry.api.config.RegistryConfig;
import io.jon.rpc.spi.loader.ExtensionLoader;
import io.jon.rpc.threadpool.ConcurrentThreadPool;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

@Data
public class RpcClient {

    private final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    /**
     * 注册服务
     */
    private RegistryService registryService;

    /**
     * 服务版本
     */
    private String serviceVersion;
    /**
     * 服务分组
     */
    private String serviceGroup;
    /**
     * 序列化类型
     */
    private String serializationType;
    /**
     * 超时时间
     */
    private long timeout;

    /**
     * 代理
     */
    private String proxy;

    /**
     * 是否异步调用
     */
    private boolean async;

    /**
     * 是否单向调用
     */
    private boolean oneway;

    //心跳间隔时间，默认30秒
    private int heartbeatInterval;

    //扫描空闲连接时间，默认60秒
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
    private boolean enableDelayConnection = false;

    /**
     * 并发线程池
     */
    private ConcurrentThreadPool concurrentThreadPool;

    /**
     * 流控分析类型
     */
    private String flowType;

    /**
     * 是否开启数据缓冲
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




    public RpcClient(String registryAddress, String registryType,
                     String registryLoadBalanceType, String proxy,
                     String serviceVersion, String serviceGroup,
                     String serializationType, long timeout, boolean async,
                     boolean oneway, int heartbeatInterval,
                     int scanNotActiveChannelInterval, int retryInterval,
                     int retryTimes, boolean enableResultCache,
                     int resultCacheExpire, boolean enableDirectServer, String directServerUrl,
                     boolean enableDelayConnection,
                     int corePoolSize, int maximumPoolSize,
                     String flowType, boolean enableBuffer, int bufferSize,
                     String reflectType, String fallbackClassName,
                     boolean enableRateLimiter, String rateLimiterType, int permits, int milliSeconds,
                     String rateLimiterFailStrategy

    ) {
        this.serviceVersion = serviceVersion;
        this.proxy = proxy;
        this.timeout = timeout;
        this.serviceGroup = serviceGroup;
        this.serializationType = serializationType;
        this.async = async;
        this.oneway = oneway;
        this.heartbeatInterval = heartbeatInterval;
        this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        this.retryInterval = retryInterval;
        this.retryTimes = retryTimes;
        this.enableResultCache = enableResultCache;
        this.resultCacheExpire = resultCacheExpire;
        this.enableDirectServer = enableDirectServer;
        this.directServerUrl = directServerUrl;
        this.registryService = this.getRegistryService(registryAddress, registryType, registryLoadBalanceType);
        this.enableDelayConnection = enableDelayConnection;
        this.concurrentThreadPool = ConcurrentThreadPool.getInstance(corePoolSize, maximumPoolSize);
        this.flowType = flowType;
        this.enableBuffer = enableBuffer;
        this.bufferSize = bufferSize;
        this.reflectType = reflectType;
        this.fallbackClassName = fallbackClassName;
        this.enableRateLimiter = enableRateLimiter;
        this.rateLimiterType = rateLimiterType;
        this.permits = permits;
        this.milliSeconds = milliSeconds;
        this.rateLimiterFailStrategy = rateLimiterFailStrategy;

    }

    private RegistryService getRegistryService(String registryAddress, String registryType, String registryLoadBalanceType) {
        if (StringUtils.isEmpty(registryType)){
            throw new IllegalArgumentException("registry type is null");
        }
        RegistryService registryService = ExtensionLoader.getExtension(RegistryService.class, registryType);
        try {
            registryService.init(new RegistryConfig(registryAddress, registryType, registryLoadBalanceType));
        } catch (Exception e) {
            logger.error("RpcClient init registry service throws exception:{}", e);
            throw new RegistryException(e.getMessage(), e);
        }
        return registryService;
    }

    public <T> T create(Class<T> interfaceClass) {
        ProxyFactory proxyFactory = ExtensionLoader.getExtension(ProxyFactory.class, proxy);
        proxyFactory.init(new ProxyConfig(
                interfaceClass, serviceVersion,
                serviceGroup, timeout, registryService,
                RpcConsumer.getInstance()
                        .setHeartbeatInterval(heartbeatInterval)
                        .setRetryInterval(retryInterval)
                        .setDirectServerUrl(directServerUrl)
                        .setEnableDirectServer(enableDirectServer)
                        .setRetryTimes(retryTimes)
                        .setScanNotActiveChannelInterval(scanNotActiveChannelInterval)
                        .setEnableDelayConnection(enableDelayConnection)
                        .setConcurrentThreadPool(concurrentThreadPool)
                        .setFlowPostProcessor(flowType)
                        .setEnableBuffer(enableBuffer)
                        .setBufferSize(bufferSize)
                        .buildNettyGroup()
                        .buildConnection(registryService),
                serializationType,
                async, oneway,
                enableResultCache, resultCacheExpire,
                reflectType, fallbackClassName, fallbackClass,
                enableRateLimiter, rateLimiterType, permits, milliSeconds,
                rateLimiterFailStrategy));
        return proxyFactory.getProxy(interfaceClass);
    }

    public <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass) {
        return new ObjectProxy<T>(
                interfaceClass, serviceVersion,
                serviceGroup, timeout, registryService,
                RpcConsumer.getInstance()
                        .setHeartbeatInterval(heartbeatInterval)
                        .setRetryInterval(retryInterval)
                        .setDirectServerUrl(directServerUrl)
                        .setEnableDirectServer(enableDirectServer)
                        .setRetryTimes(retryTimes)
                        .setScanNotActiveChannelInterval(scanNotActiveChannelInterval)
                        .setEnableDelayConnection(enableDelayConnection)
                        .setConcurrentThreadPool(concurrentThreadPool)
                        .setFlowPostProcessor(flowType)
                        .buildNettyGroup()
                        .buildConnection(registryService),
                serializationType,
                async, oneway,
                enableResultCache, resultCacheExpire,
                reflectType, fallbackClassName, fallbackClass,
                enableRateLimiter, rateLimiterType, permits, milliSeconds,
                rateLimiterFailStrategy);
    }

    public void shutdown() {
        RpcConsumer.getInstance().close();
    }
}

