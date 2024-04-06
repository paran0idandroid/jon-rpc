package io.jon.rpc.demo.spring.annotation.consumer.service.impl;

import io.jon.rpc.annotation.RpcReference;
import io.jon.rpc.demo.api.DemoService;
import io.jon.rpc.demo.spring.annotation.consumer.service.ConsumerDemoService;
import org.springframework.stereotype.Service;

@Service
// 当Spring IOC容器启动的时候会扫描service注解，将标注的类定义信息注册到IOC容器
// 并生成Bean对象注入IOC容器中
public class ConsumerDemoServiceImpl implements ConsumerDemoService {

    @RpcReference(
            registryType = "zookeeper",
            registryAddress = "127.0.0.1:2181",
            loadBalanceType = "zkconsistenthash",
            version = "1.0.0", group = "jon",
            serializationType = "protostuff",
            proxy = "cglib", timeout = 30000,
            async = false, oneway = false)
    private DemoService demoService;

    @Override
    public String hello(String name) {
        return demoService.hello(name);
    }
}

