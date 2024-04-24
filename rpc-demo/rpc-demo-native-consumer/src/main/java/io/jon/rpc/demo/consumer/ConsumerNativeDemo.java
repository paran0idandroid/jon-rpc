package io.jon.rpc.demo.consumer;

import io.jon.rpc.consumer.RpcClient;
import io.jon.rpc.demo.api.DemoService;
import io.jon.rpc.proxy.api.async.IAsyncObjectProxy;
import io.jon.rpc.proxy.api.future.RPCFuture;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerNativeDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerNativeDemo.class);

    private RpcClient rpcClient;

    @Before
    public void initRpcClient(){
        rpcClient = new RpcClient(
                "127.0.0.1:2181",
                "zookeeper",
                "enhanced_leastconnections",
                "asm",
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
                false,
                16,
                16,
                "print",
                true,
                2,
                "jdk",
                "io.jon.rpc.demo.consumer.hello.FallbackDemoServiceImpl");
    }
    @Test
    public void testInterfaceRpc() throws InterruptedException {
        DemoService demoService = rpcClient.create(DemoService.class);
        String result = demoService.hello("jon");
        LOGGER.info("返回的结果数据===>>> " + result);
        //rpcClient.shutdown();
        while (true){
            Thread.sleep(1000);
        }
    }

    @Test
    public void testAsyncInterfaceRpc() throws Exception {
        IAsyncObjectProxy demoService = rpcClient.createAsync(DemoService.class);
        RPCFuture future = demoService.call("hello", "jon");
        LOGGER.info("返回的结果数据===>>> " + future.get());
        rpcClient.shutdown();
    }

}
