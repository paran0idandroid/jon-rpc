package io.jon.rpc.test.provider.single;

import io.jon.rpc.provider.RpcSingleServer;
import org.junit.Test;

public class RpcSingleServerTest {
    @Test
    public void startRpcSingleServer(){

        RpcSingleServer singleServer = new RpcSingleServer(
                "127.0.0.1:27880",
                "127.0.0.1:2181",
                "zookeeper",
                "io.jon.rpc.test",
                "asm",
                "randomweight",
                3000,
                6000);
        singleServer.startNettyServer();
    }
}
