package io.jon.rpc.demo.spring.boot.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(value = {"io.jon.rpc"})
public class SpringBootProviderDemoStarter {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootProviderDemoStarter.class, args);
    }
}
