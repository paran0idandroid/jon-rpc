package io.jon.rpc.loadbalancer.least.connections;

import io.jon.rpc.loadbalancer.api.ServiceLoadBalancer;
import io.jon.rpc.loadbalancer.context.ConnectionsContext;
import io.jon.rpc.protocol.meta.ServiceMeta;
import io.jon.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@SPIClass
public class LeastConnectionsServiceLoadBalancer implements ServiceLoadBalancer<ServiceMeta> {

    private final Logger logger = LoggerFactory.getLogger(LeastConnectionsServiceLoadBalancer.class);

    @Override
    public ServiceMeta select(List<ServiceMeta> servers, int hashCode, String sourceIp) {
        logger.info("基于最少连接数的负载均衡策略...");

        if (servers == null || servers.isEmpty()){
            return null;
        }

        // 先看看有没有 从未被连接过的服务实例 有则返回
        // 如果没有则返回连接数最少的服务实例
        ServiceMeta serviceMeta = this.getNullServiceMeta(servers);
        if (serviceMeta == null){
            serviceMeta = this.getServiceMeta(servers);
        }
        return serviceMeta;
    }

    private ServiceMeta getServiceMeta(List<ServiceMeta> servers) {

        ServiceMeta leastConnectedServiceMeta = servers.get(0);
        Integer leastConnectedServiceMetaCount = ConnectionsContext.getValue(leastConnectedServiceMeta);
        for (int i = 1; i < servers.size(); i++) {

            ServiceMeta meta = servers.get(i);
            Integer metaCount = ConnectionsContext.getValue(meta);
            if(leastConnectedServiceMetaCount > metaCount){
                leastConnectedServiceMetaCount = metaCount;
                leastConnectedServiceMeta = meta;
            }
        }
        return leastConnectedServiceMeta;
    }

    private ServiceMeta getNullServiceMeta(List<ServiceMeta> servers) {

        // 获取服务元数据列表中连接数为空的元数据
        for (int i = 0; i < servers.size(); i++) {
            ServiceMeta serviceMeta = servers.get(i);
            if(ConnectionsContext.getValue(serviceMeta) == null){
                return serviceMeta;
            }
        }

        return null;
    }
}
