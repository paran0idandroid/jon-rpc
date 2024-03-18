package io.jon.rpc.loadbalancer.robin;

import io.jon.rpc.loadbalancer.api.ServiceLoadBalancer;
import io.jon.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@SPIClass
public class RobinServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {

    private final Logger logger = LoggerFactory.getLogger(RobinServiceLoadBalancer.class);

    private volatile AtomicInteger atomicInteger = new AtomicInteger(0);
    @Override
    public T select(List<T> servers, int hashCode) {

        logger.info("基于轮询算法的负载均衡策略...");
        if(servers == null || servers.isEmpty()){
            return null;
        }

        int count = servers.size();
        int index = atomicInteger.getAndIncrement();
        if(index >= (Integer.MAX_VALUE - 10000)){
            atomicInteger.set(0);
        }
        return servers.get(index % count);

    }
}
