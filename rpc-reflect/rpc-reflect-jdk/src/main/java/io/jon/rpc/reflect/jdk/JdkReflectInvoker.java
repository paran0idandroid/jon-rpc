package io.jon.rpc.reflect.jdk;

import io.jon.rpc.reflect.api.ReflectInvoker;
import io.jon.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

@SPIClass
public class JdkReflectInvoker implements ReflectInvoker {

    private final Logger logger = LoggerFactory.getLogger(JdkReflectInvoker.class);
    @Override
    public Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        logger.info("use jdk reflect type to invoke method...");
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);
    }
}
