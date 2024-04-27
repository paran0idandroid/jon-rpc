package io.jon.rpc.demo.spring.boot.consumer;

import io.jon.rpc.demo.spring.boot.consumer.service.ConsumerDemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"io.jon.rpc"})
public class SpringBootConsumerDemoStarter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringBootConsumerDemoStarter.class);
    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(SpringBootConsumerDemoStarter.class, args);
        ConsumerDemoService consumerDemoService = context.getBean(ConsumerDemoService.class);

        for (int i = 0; i < 100; i++) {
            String result = consumerDemoService.hello("kobe");
            LOGGER.info("返回的结果数据===>>> " + result);
        }
        while (true){
            Thread.sleep(1000);
        }
    }
}

