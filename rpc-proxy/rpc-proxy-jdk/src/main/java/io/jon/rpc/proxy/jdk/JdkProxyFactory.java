package io.jon.rpc.proxy.jdk;

import io.jon.rpc.proxy.api.BaseProxyFactory;
import io.jon.rpc.proxy.api.ProxyFactory;
import io.jon.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
@SuppressWarnings("unchecked")
@SPIClass
public class JdkProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {
    private final Logger logger = LoggerFactory.getLogger(JdkProxyFactory.class);

    @Override
    public <T> T getProxy(Class<T> clazz){
        logger.info("基于JDK动态代理...");

        // 这是创建代理对象的关键代码
        // 使用 Proxy 类的 newProxyInstance() 静态方法来创建一个代理对象
        // clazz.getClassLoader():
        // 指定了代理对象的类加载器 这里使用 clazz 对象的类加载器来加载代理类

        // new Class<?>[]{clazz}:
        // 指定了代理对象要实现的接口数组
        // 这里传入了一个包含 clazz 类型的数组，表示代理对象要实现 clazz 接口

        // objectProxy:
        // 是一个代理处理器（ProxyHandler）它实现了 InvocationHandler 接口
        // 代理对象在调用方法时会委托给这个objectProxy处理器来处理方法调用逻辑
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, objectProxy);
    }
}
