package io.jon.rpc.registry.zookeeper;

import io.jon.rpc.common.helper.RpcServiceHelper;
import io.jon.rpc.constants.RpcConstants;
import io.jon.rpc.loadbalancer.api.ServiceLoadBalancer;
import io.jon.rpc.loadbalancer.helper.ServiceLoadBalancerHelper;
import io.jon.rpc.protocol.meta.ServiceMeta;
import io.jon.rpc.registry.api.RegistryService;
import io.jon.rpc.registry.api.config.RegistryConfig;
import io.jon.rpc.spi.annotation.SPIClass;
import io.jon.rpc.spi.loader.ExtensionLoader;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@SPIClass
public class ZookeeperRegistryService implements RegistryService {

    public static final int BASE_SLEEP_TIME_MS = 1000;
    public static final int MAX_RETRIES = 3;
    public static final String ZK_BASE_PATH = "/jon_rpc";

    private ServiceDiscovery serviceDiscovery;

    private ServiceLoadBalancer<ServiceInstance<ServiceMeta>> serviceLoadBalancer;
    private ServiceLoadBalancer<ServiceMeta> serviceEnhancedLoadBalancer;

    private final Logger logger = LoggerFactory.getLogger(ZookeeperRegistryService.class);
    @Override
    public void register(ServiceMeta serviceMeta) throws Exception {

        logger.info("=======注册实例到zookeeper注册中心=======");
        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
                .<ServiceMeta>builder()
                .name(RpcServiceHelper.buildServiceKey(
                        serviceMeta.getServiceName(),
                        serviceMeta.getServiceVersion(),
                        serviceMeta.getServiceGroup()
                ))
                .address(serviceMeta.getServiceAddr())
                .port(serviceMeta.getServicePort())
                .payload(serviceMeta)
                .build();

        serviceDiscovery.registerService(serviceInstance);
    }

    @Override
    public void unRegister(ServiceMeta serviceMeta) throws Exception {

        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
                .<ServiceMeta>builder()
                .name(serviceMeta.getServiceName())
                .address(serviceMeta.getServiceAddr())
                .port(serviceMeta.getServicePort())
                .payload(serviceMeta)
                .build();

        serviceDiscovery.unregisterService(serviceInstance);

    }

    @Override
    public ServiceMeta discovery(String serviceName, int invokerHashCode, String sourceIp) throws Exception {

        logger.info("=======从zookeeper注册中心发现实例=======");
        Collection<ServiceInstance<ServiceMeta>> serviceInstances =
                serviceDiscovery.queryForInstances(serviceName);

        if(serviceLoadBalancer != null){
            return getServiceMetaInstance(invokerHashCode, sourceIp,
                    (List<ServiceInstance<ServiceMeta>>) serviceInstances);
        }
        return this.serviceEnhancedLoadBalancer.select(
                ServiceLoadBalancerHelper.getServiceMetaList(
                        (List<ServiceInstance<ServiceMeta>>) serviceInstances), invokerHashCode, sourceIp);
    }

    @Override
    public void destroy() throws IOException {

        serviceDiscovery.close();
    }

    @Override
    public void init(RegistryConfig registryConfig) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(
                registryConfig.getRegistryAddr(),
                new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES));

        client.start();
        JsonInstanceSerializer<ServiceMeta> serializer = new JsonInstanceSerializer<>(ServiceMeta.class);

        this.serviceDiscovery = ServiceDiscoveryBuilder
                .builder(ServiceMeta.class)
                .client(client)
                .serializer(serializer)
                .basePath(ZK_BASE_PATH)
                .build();
        this.serviceDiscovery.start();


        //增强型负载均衡策略
        if(registryConfig
                .getRegistryLoadBalanceType()
                .toLowerCase()
                .contains(RpcConstants.SERVICE_ENHANCED_LOAD_BALANCER_PREFIX)){
            this.serviceEnhancedLoadBalancer =
                    ExtensionLoader.getExtension(
                            ServiceLoadBalancer.class,
                            registryConfig.getRegistryLoadBalanceType());
        }else{

            this.serviceLoadBalancer = ExtensionLoader.getExtension(
                    ServiceLoadBalancer.class,
                    registryConfig.getRegistryLoadBalanceType()
            );
        }
    }

    private ServiceMeta getServiceMetaInstance(int invokerHashCode, String sourceIp, List<ServiceInstance<ServiceMeta>> serviceInstances){

        ServiceInstance<ServiceMeta> instance = this.serviceLoadBalancer.select(serviceInstances, invokerHashCode, sourceIp);
        if(instance != null){
            return instance.getPayload();
        }
        return null;
    }
}
