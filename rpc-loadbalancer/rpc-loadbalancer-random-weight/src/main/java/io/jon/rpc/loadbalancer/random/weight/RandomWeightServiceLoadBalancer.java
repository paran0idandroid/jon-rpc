package io.jon.rpc.loadbalancer.random.weight;

import io.jon.rpc.loadbalancer.api.ServiceLoadBalancer;
import io.jon.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

@SPIClass
public class RandomWeightServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {

    private final Logger logger = LoggerFactory.getLogger(RandomWeightServiceLoadBalancer.class);

    @Override
    public T select(List<T> servers, int hashCode, String sourceIp) {
        logger.info("基于加权随机算法的负载均衡策略...");
        if(servers == null || servers.isEmpty()){
            return null;
        }

        hashCode = Math.abs(hashCode);
        // 获取到传入的服务提供者实例列表的前 (hashCode % servers.size()) 个服务提供者
        // 从中随机选一个，缩小随即范围
        int count = hashCode % servers.size();
        if(count <= 1){
            count = servers.size();
        }
        Random random = new Random();
        int index = random.nextInt(count);
        return servers.get(index);
    }
}
