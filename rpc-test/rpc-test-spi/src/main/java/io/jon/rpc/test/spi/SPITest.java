package io.jon.rpc.test.spi;

import io.jon.rpc.spi.annotation.SPIClass;
import io.jon.rpc.spi.loader.ExtensionLoader;
import io.jon.rpc.test.spi.service.SPIService;
import org.junit.Test;

public class SPITest {

    @Test
    public void testSpiLoader(){

        SPIService spiService = ExtensionLoader.getExtension(SPIService.class, "spiService");
        String result = spiService.hello("kd");
        System.out.println(result);
    }
}
