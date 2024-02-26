package io.jon.rpc.test.consumer;

import io.jon.rpc.consumer.RpcClient;
import io.jon.rpc.protocol.enumeration.RpcType;
import io.jon.rpc.proxy.api.async.IAsyncObjectProxy;
import io.jon.rpc.proxy.api.future.RPCFuture;
import io.jon.rpc.test.api.DemoService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcConsumerNativeTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcConsumerNativeTest.class);

    @Test
    public void testSyncInterfaceRpc() throws Exception{
        RpcClient rpcClient = new RpcClient(
                "1.0.0", "jon", 3000,
                "jdk", RpcType.REQUEST.getType(),
                false, false);

        DemoService demoService = rpcClient.create(DemoService.class);
        String result = demoService.hello("kevin durant");
        LOGGER.info("result: " + result);
        rpcClient.shutdown();
    }

    @Test
    public void testAsyncInterfaceRpc() throws Exception{

        RpcClient rpcClient = new RpcClient("1.0.0", "jon", 3000,
                "jdk", RpcType.REQUEST.getType(),
                false, false);

        IAsyncObjectProxy demoService = rpcClient.createAsync(DemoService.class);
        RPCFuture future = demoService.call("hello", "westbrook");
        LOGGER.info("result: " + future.get());
        rpcClient.shutdown();
    }
}
