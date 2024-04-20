package io.jon.rpc.test.consumer;

import io.jon.rpc.consumer.RpcClient;
import io.jon.rpc.demo.api.DemoService;
import io.jon.rpc.proxy.api.async.IAsyncObjectProxy;
import io.jon.rpc.proxy.api.future.RPCFuture;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcConsumerNativeTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcConsumerNativeTest.class);

    private RpcClient rpcClient;

    @Before
    public void initRpcClient(){
        rpcClient = new RpcClient(
                "127.0.0.1:2181",
                "zookeeper",
                "enhanced_leastconnections",
                "cglib",
                "1.0.0",
                "jon",
                "protostuff",
                3000,
                false,
                false,
                3000,
                6000,
                1000,
                3,
                true,
                10000,
                true,
                "127.0.0.1:27880,127.0.0.1:27880,127.0.0.1:27880",
                true,
                16,
                16,
                "print");
    }

    public static void main(String[] args) {

        RpcClient rpcClient = new RpcClient(
                "127.0.0.1:2181",
                "zookeeper",
                "enhanced_leastconnections",
                "cglib",
                "1.0.0",
                "jon",
                "protostuff",
                3000,
                false,
                false,
                3000,
                6000,
                1000,
                3,
                true,
                10000,
                true,
                "127.0.0.1:27880,127.0.0.1:27880,127.0.0.1:27880",
                true,
                16,
                16,
                "print");
        // 这里一开始就传错了
        DemoService demoService = rpcClient.create(DemoService.class);

        String result = demoService.hello("kevin durant");
        LOGGER.info("result: " + result);
    }
    @Test
    public void testSyncInterfaceRpc() throws Exception{

        DemoService demoService = rpcClient.create(DemoService.class);

        String result = demoService.hello("kevin durant");
        LOGGER.info("result: " + result);
//        rpcClient.shutdown();
    }

    @Test
    public void testAsyncInterfaceRpc() throws Exception{

        IAsyncObjectProxy demoService = rpcClient.createAsync(DemoService.class);
        RPCFuture future = demoService.call("hello", "westbrook");
        LOGGER.info("result: " + future.get());
        rpcClient.shutdown();
    }
}
