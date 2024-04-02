package io.jon.rpc.demo.spring.xml.provider;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringXmlProviderStarter {

    public static void main(String[] args) {
        // 启动一个Spring应用程序，加载名为 server-spring.xml 的配置文件
        // 创建了一个Spring的IOC容器
        new ClassPathXmlApplicationContext("server-spring.xml");
    }
}
