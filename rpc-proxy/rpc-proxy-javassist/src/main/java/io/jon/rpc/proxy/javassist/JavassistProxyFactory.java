package io.jon.rpc.proxy.javassist;


import io.jon.rpc.proxy.api.BaseProxyFactory;
import io.jon.rpc.proxy.api.ProxyFactory;
import io.jon.rpc.spi.annotation.SPIClass;
import javassist.util.proxy.MethodHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

@SPIClass
public class JavassistProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {
    private final Logger logger = LoggerFactory.getLogger(JavassistProxyFactory.class);
    private javassist.util.proxy.ProxyFactory proxyFactory = new javassist.util.proxy.ProxyFactory();
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProxy(Class<T> clazz) {
        try {
            logger.info("基于Javassist动态代理...");
            //设置代理类的父类
            //设置要创建代理对象的接口，传入了一个包含 clazz 的类对象的数组，因为动态代理通常基于接口来创建
            proxyFactory.setInterfaces(new Class[]{clazz});

            // 设置代理对象的方法处理器
            // MethodHandler 是一个接口，实现了代理对象的方法执行逻辑
            // 在这里，使用了匿名内部类实现了 MethodHandler 接口
            // 并在 invoke() 方法中定义了方法的执行逻辑
            proxyFactory.setHandler(new MethodHandler() {
                @Override
                public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                    return objectProxy.invoke(self, thisMethod, args);
                }
            });
            // 通过字节码技术动态创建子类实例
            // 使用 Javassist 动态创建了一个代理类的字节码，并实例化了一个代理对象
            return (T) proxyFactory.createClass().newInstance();
        }catch (Exception e){
            logger.error("javassist proxy throws exception:{}", e);
        }
        return null;
    }
}
