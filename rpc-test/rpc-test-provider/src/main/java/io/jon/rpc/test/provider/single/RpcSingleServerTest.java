package io.jon.rpc.test.provider.single;

import io.jon.rpc.provider.RpcSingleServer;
import org.junit.Test;

public class RpcSingleServerTest {
    @Test
    public void startRpcSingleServer(){

        RpcSingleServer singleServer = new RpcSingleServer("127.0.0.1:27880", "io.jon.rpc.test");
        singleServer.startNettyServer();
    }
}