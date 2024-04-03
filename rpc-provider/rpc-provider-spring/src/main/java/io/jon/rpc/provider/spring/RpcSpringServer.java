package io.jon.rpc.provider.spring;

import io.jon.rpc.annotation.RpcService;
import io.jon.rpc.common.helper.RpcServiceHelper;
import io.jon.rpc.constants.RpcConstants;
import io.jon.rpc.protocol.meta.ServiceMeta;
import io.jon.rpc.provider.common.server.base.BaseServer;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;



public class RpcSpringServer extends BaseServer
        implements ApplicationContextAware, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(RpcSpringServer.class);

    public RpcSpringServer(
            String serverAddress,
            String registryAddress,
            String registryType,
            String reflectType,
            String registryLoadBalanceType,
            int heartbeatInterval, int scanNotActiveChannelInterval
    ){

        super(
                serverAddress,
                registryAddress,
                registryType,
                reflectType,
                registryLoadBalanceType,
                heartbeatInterval, scanNotActiveChannelInterval);
    }

    // setApplicationContext 方法通过实现 ApplicationContextAware 接口，获取了Spring容器的上下文对象
    // 并通过 getBeansWithAnnotation 方法获取了所有带有 RpcService 注解的Bean对象
    // 然后遍历这些Bean对象，通过反射获取它们的注解信息，创建对应的 ServiceMeta 对象，并将其注册到 handlerMap 中
    // 同时调用 registryService.register 方法将其注册到注册中心
    @Override
    public void setApplicationContext(ApplicationContext ctx)
            throws BeansException {

        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
        if(MapUtils.isNotEmpty(serviceBeanMap)){
            for (Object serviceBean : serviceBeanMap.values()) {
                RpcService rpcService = serviceBean.getClass().getAnnotation(RpcService.class);
                ServiceMeta serviceMeta = new ServiceMeta(
                        this.getServiceName(rpcService), rpcService.version(),
                        host, port,
                        rpcService.group(), getWeight(rpcService.weight()));

                handlerMap.put(RpcServiceHelper.buildServiceKey(
                        serviceMeta.getServiceName(),
                        serviceMeta.getServiceVersion(),
                        serviceMeta.getServiceGroup()
                ), serviceBean);

                try {
                    registryService.register(serviceMeta);
                }catch (Exception e){
                    logger.error("rpc server init spring exception: {}", e);
                }
            }
        }
    }

    // 获取serviceName
    private String getServiceName(RpcService rpcService) {
        // 优先使用interfaceClass
        Class clazz = rpcService.interfaceClass();
        if (clazz == void.class){
            return rpcService.interfaceClassName();
        }

        String serviceName = clazz.getName();
        if (serviceName == null || serviceName.trim().isEmpty()){
            serviceName = rpcService.interfaceClassName();
        }
        return serviceName;
    }

    private int getWeight(int weight) {
        if (weight < RpcConstants.SERVICE_WEIGHT_MIN){
            weight = RpcConstants.SERVICE_WEIGHT_MIN;
        }
        if (weight > RpcConstants.SERVICE_WEIGHT_MAX){
            weight = RpcConstants.SERVICE_WEIGHT_MAX;
        }
        return weight;

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.startNettyServer();
    }
}
