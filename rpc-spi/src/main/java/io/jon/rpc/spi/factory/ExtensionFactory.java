package io.jon.rpc.spi.factory;

import io.jon.rpc.spi.annotation.SPI.SPI;

// 表示扩展类加载器的工厂接口
@SPI("spi")
public interface ExtensionFactory {

    /**
     * 获取扩展对象
     * @param <T> 泛型类型
     * @param key 传入的key值
     * @param clazz Class类型对象
     * @return 扩展类对象
     */
    <T> T getExtension(String key, Class<T> clazz);
}
