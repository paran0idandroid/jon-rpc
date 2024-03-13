package io.jon.rpc.reflect.javassist;

import io.jon.rpc.reflect.api.ReflectInvoker;
import io.jon.rpc.spi.annotation.SPIClass;
import javassist.util.proxy.ProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

@SPIClass
public class JavassistReflectInvoker implements ReflectInvoker {

    private final Logger logger = LoggerFactory.getLogger(JavassistReflectInvoker.class);

    @Override
    public Object invokeMethod(Object serviceBean,
                               Class<?> serviceClass,
                               String methodName,
                               Class<?>[] parameterTypes,
                               Object[] parameters) throws Throwable {
        logger.info("user javassist reflect type to invoke method...");
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(serviceClass);
        Class<?> childClass = proxyFactory.createClass();
        Method method = childClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(childClass.newInstance(), parameters);
    }
}
