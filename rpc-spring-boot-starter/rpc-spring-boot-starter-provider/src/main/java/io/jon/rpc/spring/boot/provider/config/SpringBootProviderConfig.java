package io.jon.rpc.spring.boot.provider.config;

import lombok.Data;

@Data
public class SpringBootProviderConfig {

    /**
     * 服务地址
     */
    private String serverAddress;
    /**
     * 注册中心地址
     */
    private String registryAddress;
    /**
     * 注册类型
     */
    private String registryType;
    /**
     * 负载均衡类型
     */
    private String registryLoadBalanceType;
    /**
     * 反射类型
     */
    private String reflectType;

    /**
     * 心跳时间间隔
     */
    private int heartbeatInterval;

    /**
     * 扫描并清理不活跃连接的时间间隔
     */
    private int scanNotActiveChannelInterval;

    boolean enableResultCache;

    int resultCacheExpire;

    /**
     * 核心线程数
     */
    private int corePoolSize;

    /**
     * 最大线程数
     */
    private int maximumPoolSize;

    /**
     * 流控类型
     */
    private String flowType;

    /**
     * 最大连接数
     */
    private int maxConnections;

    /**
     * 拒绝策略类型
     */
    private String disuseStrategyType;

    /**
     * 是否开启数据缓冲
     */
    private boolean enableBuffer;

    /**
     * 缓冲区大小
     */
    private int bufferSize;


    public SpringBootProviderConfig() {
    }

    public SpringBootProviderConfig(final String serverAddress,
                                    final String registryAddress,
                                    final String registryType,
                                    final String registryLoadBalanceType,
                                    final String reflectType,
                                    final int heartbeatInterval,
                                    int scanNotActiveChannelInterval,
                                    final boolean enableResultCache,
                                    final int resultCacheExpire,
                                    final int corePoolSize,
                                    final int maximumPoolSize,
                                    final String flowType,
                                    final int maxConnections,
                                    final String disuseStrategyType,
                                    final boolean enableBuffer,
                                    final int bufferSize) {
        this.serverAddress = serverAddress;
        this.registryAddress = registryAddress;
        this.registryType = registryType;
        this.registryLoadBalanceType = registryLoadBalanceType;
        this.reflectType = reflectType;
        if (heartbeatInterval > 0){
            this.heartbeatInterval = heartbeatInterval;
        }
        this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        this.enableResultCache = enableResultCache;
        this.resultCacheExpire = resultCacheExpire;
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.flowType = flowType;
        this.maxConnections = maxConnections;
        this.disuseStrategyType = disuseStrategyType;
        this.enableBuffer = enableBuffer;
        this.bufferSize = bufferSize;

    }

}
