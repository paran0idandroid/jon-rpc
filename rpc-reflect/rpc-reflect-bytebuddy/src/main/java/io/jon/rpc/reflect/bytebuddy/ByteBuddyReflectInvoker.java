package io.jon.rpc.reflect.bytebuddy;

import io.jon.rpc.reflect.api.ReflectInvoker;
import io.jon.rpc.spi.annotation.SPIClass;
import net.bytebuddy.ByteBuddy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

@SPIClass
public class ByteBuddyReflectInvoker implements ReflectInvoker {

    private final Logger logger = LoggerFactory.getLogger(ByteBuddyReflectInvoker.class);
    @Override
    public Object invokeMethod(Object serviceBean,
                               Class<?> serviceClass,
                               String methodName,
                               Class<?>[] parameterTypes,
                               Object[] parameters) throws Throwable {
        logger.info("use bytebuddy reflect type to invoke method...");
        Class<?> childClass = new ByteBuddy()
                .subclass(serviceClass)
                .make()
                .load(ByteBuddyReflectInvoker.class.getClassLoader())
                .getLoaded();
        Object instance = childClass.getDeclaredConstructor().newInstance();
        Method method = childClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(instance, parameters);
    }
}
