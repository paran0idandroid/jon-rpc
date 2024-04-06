package io.jon.rpc.demo.spring.annotation.consumer.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(value = {"io.jon.rpc.*"})
public class SpringAnnotationConsumerConfig {
}

