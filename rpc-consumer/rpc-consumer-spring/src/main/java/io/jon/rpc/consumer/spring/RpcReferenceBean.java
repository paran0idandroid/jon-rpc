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

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    @Override
    public Object getObject() throws Exception{
        return object;
    }

    public void init() throws Exception{

        RpcClient rpcClient = new RpcClient(
                registryAddress, registryType, proxy,
                version, group,
                timeout, serializationType,
                messageType, async, oneway,
                loadBalanceType,
                heartbeatInterval, scanNotActiveChannelInterval,
                retryInterval, retryTimes
        );

        this.object = rpcClient.create(interfaceClass);
    }
}
