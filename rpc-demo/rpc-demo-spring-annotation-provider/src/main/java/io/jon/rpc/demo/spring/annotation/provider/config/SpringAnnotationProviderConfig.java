package io.jon.rpc.demo.spring.annotation.provider.config;

import io.jon.rpc.provider.spring.RpcSpringServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

// Spring配置类
@Configuration
// 扫描ProviderDemoServiceImpl 带RpcService注解
// RpcService注解带Component注解
@ComponentScan(value = {"io.jon.rpc.demo"})
@PropertySource(value = {"classpath:rpc.properties"})
public class SpringAnnotationProviderConfig {

    @Value("${registry.address}")
    private String registryAddress;

    @Value("${registry.type}")
    private String registryType;

    @Value("${registry.loadbalance.type}")
    private String registryLoadBalanceType;

    @Value("${server.address}")
    private String serverAddress;

    @Value("${reflect.type}")
    private String reflectType;

    @Value("${server.heartbeatInterval}")
    private int heartbeatInterval;

    @Value("${server.scanNotActiveChannelInterval}")
    private int scanNotActiveChannelInterval;

    @Bean
    public RpcSpringServer rpcSpringServer(){
        return new RpcSpringServer(
                serverAddress,
                registryAddress,
                registryType,
                reflectType,
                registryLoadBalanceType,
                heartbeatInterval, scanNotActiveChannelInterval
        );
    }
}
