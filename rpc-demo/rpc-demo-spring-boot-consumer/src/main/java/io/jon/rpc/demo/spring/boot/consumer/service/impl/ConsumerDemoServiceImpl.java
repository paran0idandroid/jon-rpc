package io.jon.rpc.demo.spring.boot.consumer.service.impl;

import io.jon.rpc.annotation.RpcReference;
import io.jon.rpc.demo.api.DemoService;
import io.jon.rpc.demo.spring.boot.consumer.service.ConsumerDemoService;
import org.springframework.stereotype.Service;

@Service
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
