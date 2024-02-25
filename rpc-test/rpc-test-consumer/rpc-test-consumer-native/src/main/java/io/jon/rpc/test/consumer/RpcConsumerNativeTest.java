package io.jon.rpc.test.consumer;

import io.jon.rpc.consumer.RpcClient;
import io.jon.rpc.protocol.enumeration.RpcType;
import io.jon.rpc.test.api.DemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcConsumerNativeTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcConsumerNativeTest.class);

    public static void main(String[] args) {
        RpcClient rpcClient = new RpcClient(
                "1.0.0", "jon", 3000,
                "jdk", RpcType.HEARTBEAT.getType(),
                false, false);

        DemoService demoService = rpcClient.create(DemoService.class);
        String result = demoService.hello("kevin durant");
        LOGGER.info("result: " + result);
        rpcClient.shutdown();
    }
}
