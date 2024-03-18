package io.jon.rpc.loadbalancer.random;

import io.jon.rpc.loadbalancer.api.ServiceLoadBalancer;
import io.jon.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

@SPIClass
public class RandomServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {

    private final Logger logger = LoggerFactory.getLogger(RandomServiceLoadBalancer.class);
    @Override
    public T select(List<T> servers, int hashCode, String sourceIp) {
        logger.info("基于随机算法的负载均衡策略");

        if(servers == null || servers.isEmpty()){
            return null;
        }

        Random random = new Random();
        int index = random.nextInt(servers.size());
        return servers.get(index);
    }
}
