package io.jon.rpc.test.scanner.consumer.service.impl;


import io.jon.rpc.annotation.RpcReference;
import io.jon.rpc.test.scanner.consumer.service.ConsumerBusinessService;
import io.jon.rpc.test.scanner.service.DemoService;

/**
 * @description 服务消费者业务逻辑实现类
 */
public class ConsumerBusinessServiceImpl implements ConsumerBusinessService {

    @RpcReference(
            registryType = "zookeeper",
            registryAddress = "127.0.0.1:2181",
            version = "1.0.0", group = "jon")
    private DemoService demoService;

}
