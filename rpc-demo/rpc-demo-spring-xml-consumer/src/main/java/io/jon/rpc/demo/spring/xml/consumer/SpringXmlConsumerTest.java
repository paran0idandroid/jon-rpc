package io.jon.rpc.demo.spring.xml.consumer;

import io.jon.rpc.consumer.RpcClient;
import io.jon.rpc.demo.api.DemoService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class) // 使得此测试类可以基于Spring的测试模块启动
@ContextConfiguration(locations = "classpath:client-spring.xml") // 读取此路径中的xml文件创建IOC容器
public class SpringXmlConsumerTest {

    private static Logger logger = LoggerFactory.getLogger(SpringXmlConsumerTest.class);

    @Autowired
    private RpcClient rpcClient;

    @Test
    public void testInterfaceRpc() throws InterruptedException {
        DemoService demoService = rpcClient.create(DemoService.class);
        for (int i = 0; i < 100; i++) {

            String result = demoService.hello("kdot");
            logger.info("返回的结果数据===>>> " + result);
        }
        while (true){
            Thread.sleep(1000);
        }
    }
}

