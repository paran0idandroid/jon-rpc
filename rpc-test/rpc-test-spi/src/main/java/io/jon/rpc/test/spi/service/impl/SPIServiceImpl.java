package io.jon.rpc.test.spi.service.impl;

import io.jon.rpc.spi.annotation.SPIClass;
import io.jon.rpc.test.spi.service.SPIService;

@SPIClass
public class SPIServiceImpl implements SPIService {
    @Override
    public String hello(String name) {
        return "hello " + name;
    }
}
