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
                "randomweight");
        singleServer.startNettyServer();
    }
}
