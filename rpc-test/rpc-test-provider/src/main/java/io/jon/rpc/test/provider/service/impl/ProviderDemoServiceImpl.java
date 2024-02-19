package io.jon.rpc.test.provider.service.impl;

import io.jon.rpc.annotation.RpcService;
import io.jon.rpc.test.api.DemoService;
import lombok.extern.slf4j.Slf4j;


@RpcService(
        interfaceClass = DemoService.class,
        interfaceClassName = "io.jon.rpc.test.api.DemoService",
        group = "jon"
)
@Slf4j
public class ProviderDemoServiceImpl implements DemoService {


    @Override
    public String hello(String name) {
        log.info("调用hello方法传入的参数为===>>>{}", name);
        return "Hello! " + name;
    }
}
