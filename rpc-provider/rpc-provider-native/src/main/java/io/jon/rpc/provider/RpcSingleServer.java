package io.jon.rpc.provider;

import io.jon.rpc.common.scanner.server.RpcServiceScanner;
import io.jon.rpc.provider.common.server.base.BaseServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcSingleServer extends BaseServer {
    public RpcSingleServer(String serverAddress, String scanPackage) {
        super(serverAddress);

        try{
            this.handlerMap = RpcServiceScanner
                    .doScannerWithRpcServiceAnnotationFilterAndRegistryService(scanPackage);
        }catch (Exception e){
            log.error("RPC Server init error", e);
        }
    }
}
