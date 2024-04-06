package io.jon.rpc.demo.spring.annotation.provider;

import io.jon.rpc.demo.spring.annotation.provider.config.SpringAnnotationProviderConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringAnnotationProviderStarter {
    public static void main(String[] args) {
        // Spring 将会加载并初始化这个配置类中定义的 bean，并将它们放入应用程序的上下文中进行管理
        new AnnotationConfigApplicationContext(SpringAnnotationProviderConfig.class);
    }
}
