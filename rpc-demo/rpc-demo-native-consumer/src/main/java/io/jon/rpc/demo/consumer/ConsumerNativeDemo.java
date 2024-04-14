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
                "127.0.0.1:27880,127.0.0.1:27880,127.0.0.1:27880");
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
                "127.0.0.1:27880,127.0.0.1:27880,127.0.0.1:27880");
        // 这里一开始就传错了
        DemoService demoService = rpcClient.create(DemoService.class);

        String result = demoService.hello("kevin durant");
        LOGGER.info("result: " + result);
    }


    @Test
    public void testInterfaceRpc() throws InterruptedException {
        DemoService demoService = rpcClient.create(DemoService.class);
        Thread.sleep(5000);
        String result = demoService.hello("kdot");
        LOGGER.info("返回的结果数据===>>> " + result);
        //rpcClient.shutdown();
        while (true){
            Thread.sleep(1000);
        }
    }

    @Test
    public void testAsyncInterfaceRpc() throws Exception {
        IAsyncObjectProxy demoService = rpcClient.createAsync(DemoService.class);
        RPCFuture future = demoService.call("hello", "kdot");
        LOGGER.info("返回的结果数据===>>> " + future.get());
        rpcClient.shutdown();
    }
}
