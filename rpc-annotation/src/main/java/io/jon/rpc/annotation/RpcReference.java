package io.jon.rpc.annotation;


import io.jon.rpc.constants.RpcConstants;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * rpc服务消费者
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Autowired
public @interface RpcReference {

    /**
     * 版本号
     */
    String version() default "1.0.0";

    /**
     * 注册中心类型，目前包含：zookeeper、nacos、etcd、consul
     */
    String registryType() default "zookeeper";

    /**
     * 注册地址
     */
    String registryAddress() default "127.0.0.1:2181";

    /**
     * 负载均衡类型，默认基于zk的一致性hash
     */
    String loadBalanceType() default "zkconsistenthash";

    /**
     * 序列化类型：protostuff、kryo、json、jdk、hessian2、fst
     */
    String serializationType() default "protostuff";

    /**
     * 超时时间，默认5s
     */
    long timeout() default 5000;

    /**
     * 是否异步执行
     */
    boolean async() default false;

    /**
     * 是否单向调用
     */
    boolean oneway() default false;

    /**
     * 代理类型：jdk、javassist、cglib
     */
    String proxy() default "jdk";

    /**
     * 服务分组，默认为空
     */
    String group() default "";

    /**
     * 心跳间隔时间，默认30秒
     */
    int heartbeatInterval() default RpcConstants.RPC_COMMON_DEFAULT_HEARTBEATINTERVAL;

    /**
     * 扫描空闲连接间隔时间，默认60秒
     */
    int scanNotActiveChannelInterval() default RpcConstants.RPC_COMMON_DEFAULT_SCANNOTACTIVECHANNELINTERVAL;

    /**
     * 重试间隔时间
     */
    int retryInterval() default 1000;

    /**
     * 重试间隔时间
     */
    int retryTimes() default 3;

    /**
     * 是否开启结果缓存
     */
    boolean enableResultCache() default false;

    /**
     * 缓存结果的时长，单位是毫秒
     */
    int resultCacheExpire() default RpcConstants.RPC_SCAN_RESULT_CACHE_EXPIRE;

    /**
     * 是否开启直连服务
     */
    boolean enableDirectServer() default false;

    /**
     * 直连服务的地址
     */
    String directServerUrl() default RpcConstants.RPC_COMMON_DEFAULT_DIRECT_SERVER;

    /**
     * 是否开启延迟连接
     */
    boolean enableDelayConnection() default false;

    /**
     * 默认并发线程池核心线程数
     */
    int corePoolSize() default RpcConstants.DEFAULT_CORE_POOL_SIZE;

    /**
     * 默认并发线程池最大线程数
     */
    int maximumPoolSize() default RpcConstants.DEFAULT_MAXI_NUM_POOL_SIZE;

    /**
     * 流控分析类型
     */
    String flowType() default RpcConstants.FLOW_POST_PROCESSOR_PRINT;

    /**
     * 是否开启缓冲区
     */
    boolean enableBuffer() default false;

    /**
     * 缓冲区大小
     */
    int bufferSize() default RpcConstants.DEFAULT_BUFFER_SIZE;


}
