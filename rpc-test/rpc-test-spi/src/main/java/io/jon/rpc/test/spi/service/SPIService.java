package io.jon.rpc.test.spi.service;

import io.jon.rpc.spi.annotation.SPI.SPI;

@SPI("spiService")
public interface SPIService {

    String hello(String name);
}
