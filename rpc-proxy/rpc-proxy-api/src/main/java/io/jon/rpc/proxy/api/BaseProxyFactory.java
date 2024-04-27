package io.jon.rpc.proxy.api;

import io.jon.rpc.proxy.api.config.ProxyConfig;
import io.jon.rpc.proxy.api.object.ObjectProxy;

public abstract class BaseProxyFactory<T> implements ProxyFactory{

    protected ObjectProxy<T> objectProxy;

    @Override
    public <T> void init(ProxyConfig<T> proxyConfig) {
        this.objectProxy = new ObjectProxy(
                proxyConfig.getClazz(),
                proxyConfig.getServiceVersion(),
                proxyConfig.getServiceGroup(),
                proxyConfig.getTimeout(),
                proxyConfig.getRegistryService(),
                proxyConfig.getConsumer(),
                proxyConfig.getSerializationType(),
                proxyConfig.isAsync(),
                proxyConfig.isOneway(),
                proxyConfig.isEnableResultCache(),
                proxyConfig.getResultCacheExpire(),
                proxyConfig.getReflectType(),
                proxyConfig.getFallbackClassName(),
                proxyConfig.getFallbackClass(),
                proxyConfig.isEnableRateLimiter(),
                proxyConfig.getRateLimiterType(),
                proxyConfig.getPermits(),
                proxyConfig.getMilliSeconds()

        );
    }
}
