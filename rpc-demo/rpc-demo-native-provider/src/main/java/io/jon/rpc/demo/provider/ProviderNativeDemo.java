package io.jon.rpc.demo.provider;

import io.jon.rpc.provider.RpcSingleServer;
import org.junit.Test;

public class ProviderNativeDemo {

    @Test
    public void startRpcSingleServer(){
        RpcSingleServer singleServer = new RpcSingleServer(
                "127.0.0.1:27880",
                "127.0.0.1:2181",
                "zookeeper",
                "io.jon.rpc.demo",
                "asm",
                "randomweight",
                3000,
                6000,
                true,
                30000,
                16,
                16);
        singleServer.startNettyServer();
    }
}
