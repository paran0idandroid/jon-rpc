package io.jon.rpc.demo.consumer.hello;

import io.jon.rpc.demo.api.DemoService;

public class FallbackDemoServiceImpl implements DemoService {
    @Override
    public String hello(String name) {
        return "发生熔断！！！返回熔断结果: " + name;
    }
}
