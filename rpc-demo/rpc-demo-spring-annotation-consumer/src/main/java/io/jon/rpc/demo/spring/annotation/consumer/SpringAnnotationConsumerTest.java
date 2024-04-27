package io.jon.rpc.demo.spring.annotation.consumer;

import io.jon.rpc.demo.spring.annotation.consumer.config.SpringAnnotationConsumerConfig;
import io.jon.rpc.demo.spring.annotation.consumer.service.ConsumerDemoService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringAnnotationConsumerTest {

    private static Logger logger = LoggerFactory.getLogger(SpringAnnotationConsumerTest.class);

    @Test
    public void testInterfaceRpc() throws InterruptedException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                SpringAnnotationConsumerConfig.class);
        ConsumerDemoService consumerDemoService = context.getBean(ConsumerDemoService.class);
        for (int i = 0; i < 100; i++) {
            String result = consumerDemoService.hello("kdot");
            logger.info("返回的结果数据===>>> " + result);
        }
        //rpcClient.shutdown();
        while (true){
            Thread.sleep(1000);
        }
    }
}
