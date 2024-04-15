package io.jon.rpc.provider;

import io.jon.rpc.provider.common.scanner.RpcServiceScanner;
import io.jon.rpc.provider.common.server.base.BaseServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcSingleServer extends BaseServer {
    public RpcSingleServer(String serverAddress, String registryAddress,
                           String registryType, String scanPackage, String reflectType,
                           String registryLoadBalanceType,
                           int heartbeatInterval,
                           int scanNotActiveChannel,
                           boolean enableResultCache,
                           int resultCacheExpire,
                           int corePoolSize,
                           int maximumPoolSize) {

        super(
                serverAddress, registryAddress,
                registryType,reflectType,registryLoadBalanceType,
                heartbeatInterval, scanNotActiveChannel,
                enableResultCache, resultCacheExpire,
                corePoolSize, maximumPoolSize);

        try{
            this.handlerMap = RpcServiceScanner
                    .doScannerWithRpcServiceAnnotationFilterAndRegistryService(
                            host, port, scanPackage, registryService);
        }catch (Exception e){
            log.error("RPC Server init error", e);
        }
    }
}
