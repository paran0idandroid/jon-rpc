package io.jon.rpc.demo.consumer;

import io.jon.rpc.consumer.RpcClient;
import io.jon.rpc.demo.api.DemoService;
import io.jon.rpc.protocol.enumeration.RpcType;
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
                "cglib",
                "1.0.0",
                "jon",
                3000,
                "protostuff",
                RpcType.REQUEST.getType(),
                false,
                false,
                "enhanced_leastconnections",
                30000,
                60000);
    }


    @Test
    public void testInterfaceRpc() throws InterruptedException {
        DemoService demoService = rpcClient.create(DemoService.class);
        for (int i = 0; i < 5; i++){
            String result = demoService.hello("kendrick lamar");
            LOGGER.info("返回的结果数据===>>> " + result);
        }
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
