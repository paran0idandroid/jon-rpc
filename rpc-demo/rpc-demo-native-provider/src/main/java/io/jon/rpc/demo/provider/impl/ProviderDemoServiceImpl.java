package io.jon.rpc.demo.provider.impl;

import io.jon.rpc.annotation.RpcService;
import io.jon.rpc.common.exception.RpcException;
import io.jon.rpc.demo.api.DemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RpcService(
        interfaceClass = DemoService.class,
        interfaceClassName = "io.jon.rpc.demo.api.DemoService",
        group = "jon")
public class ProviderDemoServiceImpl implements DemoService {
    private final Logger logger = LoggerFactory.getLogger(ProviderDemoServiceImpl.class);
    @Override
    public String hello(String name) {
        logger.info("调用hello方法传入的参数为===>>>{}", name);
        if ("jon".equals(name)){
            throw new RpcException("rpc provider throws exception");
        }
        return "hello " + name;
    }
}
