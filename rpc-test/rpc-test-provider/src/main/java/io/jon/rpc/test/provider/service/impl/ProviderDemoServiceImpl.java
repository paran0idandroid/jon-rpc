package io.jon.rpc.test.provider.service.impl;

import io.jon.rpc.annotation.RpcService;
import io.jon.rpc.test.provider.service.DemoService;


@RpcService(
        interfaceClass = DemoService.class,
        interfaceClassName = "io.jon.rpc.test.scanner.service.DemoService",
        group = "jon"
)
public class ProviderDemoServiceImpl implements DemoService {


}
