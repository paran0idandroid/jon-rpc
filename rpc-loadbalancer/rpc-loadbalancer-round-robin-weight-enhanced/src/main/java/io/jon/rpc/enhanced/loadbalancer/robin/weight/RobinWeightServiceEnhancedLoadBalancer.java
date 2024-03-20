
package io.jon.rpc.enhanced.loadbalancer.robin.weight;

import io.jon.rpc.loadbalancer.base.BaseEnhancedServiceLoadBalancer;
import io.jon.rpc.protocol.meta.ServiceMeta;
import io.jon.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@SPIClass
public class RobinWeightServiceEnhancedLoadBalancer extends BaseEnhancedServiceLoadBalancer {
    private final Logger logger = LoggerFactory.getLogger(RobinWeightServiceEnhancedLoadBalancer.class);
    private volatile AtomicInteger atomicInteger = new AtomicInteger(0);
    @Override
    public ServiceMeta select(List<ServiceMeta> servers, int hashCode, String ip) {
        logger.info("基于增强型加权轮询算法的负载均衡策略...");
        // 根据服务提供者配置的权重信息重新生成服务元数据列表
        // 权重越大服务实例重复添加次数越多
        servers = this.getWeightServiceMetaList(servers);
        if (servers == null || servers.isEmpty()){
            return null;
        }
        int index = atomicInteger.incrementAndGet();
        if (index >= Integer.MAX_VALUE - 10000){
            atomicInteger.set(0);
        }
        return servers.get(index % servers.size());
    }
}
