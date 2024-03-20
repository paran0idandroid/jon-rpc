package io.jon.rpc.loadbalancer.base;

import io.jon.rpc.loadbalancer.api.ServiceLoadBalancer;
import io.jon.rpc.protocol.meta.ServiceMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public abstract class BaseEnhancedServiceLoadBalancer
        implements ServiceLoadBalancer<ServiceMeta> {

    /**
     * 根据权重重新生成服务元数据列表，权重越高的元数据，会在最终的列表中出现的次数越多
     * 例如，权重为1，最终出现1次，权重为2，最终出现2次，权重为3，最终出现3次，依此类推...
     */
    protected List<ServiceMeta> getWeightServiceMetaList(List<ServiceMeta> servers){

        if(servers == null || servers.isEmpty()){
            return null;
        }

        List<ServiceMeta> serviceMetaList = new ArrayList<>();
        servers.stream()
                .forEach((server) -> {
                    // 这个范围表示需要重复添加当前服务实例的次数
                    IntStream.range(0, server.getWeight())
                            .forEach((i) -> {
                                serviceMetaList.add(server);
                            });
                });
        return serviceMetaList;
    }

}
