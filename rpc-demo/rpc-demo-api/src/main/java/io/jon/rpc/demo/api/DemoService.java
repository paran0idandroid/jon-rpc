package io.jon.rpc.demo.api;

import org.springframework.stereotype.Component;

@Component
public interface DemoService {

    String hello(String name);
}
