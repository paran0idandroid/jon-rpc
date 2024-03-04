package io.jon.rpc.spi.factory;

import io.jon.rpc.spi.annotation.SPI.SPI;
import io.jon.rpc.spi.annotation.SPIClass;
import io.jon.rpc.spi.loader.ExtensionLoader;

import java.util.Optional;

@SPIClass
public class SpiExtensionFactory implements ExtensionFactory{
    @Override
    public <T> T getExtension(final String key, final Class<T> clazz) {
        return Optional
                .ofNullable(clazz)
                .filter(Class::isInterface)
                .filter(cls->cls.isAnnotationPresent(SPI.class))
                .map(ExtensionLoader::getExtensionLoader)
                .map(ExtensionLoader::getDefaultSpiClassInstance)
                .orElse(null);
    }
}
