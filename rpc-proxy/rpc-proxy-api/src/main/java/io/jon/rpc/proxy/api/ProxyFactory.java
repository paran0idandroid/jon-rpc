package io.jon.rpc.proxy.api;

import io.jon.rpc.proxy.api.config.ProxyConfig;

public interface ProxyFactory {

    //获取代理对象
    <T> T getProxy(Class<T> clazz);

    //默认初始化方法
    default <T> void init(ProxyConfig<T> proxyConfig) {

    }
}
