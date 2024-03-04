package io.jon.rpc.provider;

import io.jon.rpc.provider.common.scanner.RpcServiceScanner;
import io.jon.rpc.provider.common.server.base.BaseServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcSingleServer extends BaseServer {
    public RpcSingleServer(String serverAddress, String registryAddress,
                           String registryType, String scanPackage, String reflectType) {

        super(serverAddress,registryAddress,registryType,reflectType);

        try{
            this.handlerMap = RpcServiceScanner
                    .doScannerWithRpcServiceAnnotationFilterAndRegistryService(
                            host, port, scanPackage, registryService);
        }catch (Exception e){
            log.error("RPC Server init error", e);
        }
    }
}
