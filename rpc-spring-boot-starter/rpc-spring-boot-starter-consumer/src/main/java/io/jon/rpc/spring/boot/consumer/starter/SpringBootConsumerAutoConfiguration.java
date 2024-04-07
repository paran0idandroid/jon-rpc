package io.jon.rpc.spring.boot.consumer.starter;

import io.jon.rpc.consumer.RpcClient;
import io.jon.rpc.spring.boot.consumer.config.SpringBootConsumerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
// @EnableConfigurationProperties 是一个 Spring Boot 注解
// 它的作用是启用对@ConfigurationProperties注解类的支持
// 在 Spring Boot 应用中，@ConfigurationProperties 用于绑定和验证属性文件中的属性值
// 通过将 @EnableConfigurationProperties 注解应用于一个 @Configuration 类
// 可以告诉 Spring Boot 去寻找标记了 @ConfigurationProperties 注解的类
// 并且自动将属性文件中的属性值绑定到这些类的实例上
@EnableConfigurationProperties
public class SpringBootConsumerAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "rpc.jon.consumer")
    public SpringBootConsumerConfig springBootConsumerConfig(){
        return new SpringBootConsumerConfig();
    }

    @Bean
    public RpcClient rpcClient(final SpringBootConsumerConfig springBootConsumerConfig){
        return new RpcClient(
                springBootConsumerConfig.getRegistryAddress(),
                springBootConsumerConfig.getRegistryType(),
                springBootConsumerConfig.getProxy(),
                springBootConsumerConfig.getVersion(),
                springBootConsumerConfig.getGroup(),
                springBootConsumerConfig.getTimeout(),
                springBootConsumerConfig.getSerializationType(),
                springBootConsumerConfig.isAsync(),
                springBootConsumerConfig.isOneway(),
                springBootConsumerConfig.getLoadBalanceType(),
                springBootConsumerConfig.getHeartbeatInterval(),
                springBootConsumerConfig.getScanNotActiveChannelInterval(),
                springBootConsumerConfig.getRetryInterval(),
                springBootConsumerConfig.getRetryTimes());
    }

}
