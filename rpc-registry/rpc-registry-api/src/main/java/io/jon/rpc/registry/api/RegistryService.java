package io.jon.rpc.registry.api;

import io.jon.rpc.protocol.meta.ServiceMeta;
import io.jon.rpc.registry.api.config.RegistryConfig;
import io.jon.rpc.spi.annotation.SPI.SPI;

import java.io.IOException;
import java.util.List;

@SPI
public interface RegistryService {

    /**
     * 服务注册
     * @param serviceMeta 服务元数据
     * @throws Exception
     */
    void register(ServiceMeta serviceMeta) throws Exception;

    /**
     * 服务取消注册
     * @param serviceMeta 服务元数据
     * @throws Exception
     */
    void unRegister(ServiceMeta serviceMeta) throws Exception;

    /**
     * 服务发现
     * @param serviceName 服务名称
     * @param invokerHashCode HashCode值
     * @param sourceIp 源IP地址
     * @return 服务元数据
     * @throws Exception 抛出异常
     */
    ServiceMeta discovery(String serviceName, int invokerHashCode, String sourceIp) throws Exception;

    /**
     * 服务销毁
     * @throws java.io.IOException 抛出异常
     */
    void destroy() throws IOException;

    /**
     * 默认初始化方法
     */
    default void init(RegistryConfig registryConfig) throws Exception{}

    /**
     * 从多个元数据列表中根据一定的规则获取一个元数据
     * @param serviceMetaList 元数据列表
     * @return 某个特定的元数据
     */
    ServiceMeta select(List<ServiceMeta> serviceMetaList, int invokerHashCode, String sourceIp);

    /**
     * 获取所有的数据
     */
    List<ServiceMeta> discoveryAll() throws Exception;

}
