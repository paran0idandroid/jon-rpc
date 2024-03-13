
package io.jon.rpc.reflect.asm;


import io.jon.rpc.reflect.api.ReflectInvoker;
import io.jon.rpc.reflect.asm.proxy.ReflectProxy;
import io.jon.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@SPIClass
public class AsmReflectInvoker implements ReflectInvoker {

    private final Logger logger = LoggerFactory.getLogger(AsmReflectInvoker.class);
    private final ThreadLocal<Boolean> exceptionThreadLocal = new ThreadLocal<>();

    @Override
    public Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        logger.info("use asm reflect type to invoke method..."  + Thread.currentThread().getName());
        exceptionThreadLocal.set(false);
        Object result = null;
        try{
            Constructor<?> constructor = serviceClass.getConstructor(new Class[]{});
            Object[] constructorParam = new Object[]{};
            Object instance = ReflectProxy.newProxyInstance(AsmReflectInvoker.class.getClassLoader(), getInvocationHandler(serviceBean), serviceClass, constructor, constructorParam);
            Method method = serviceClass.getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            result = method.invoke(instance, parameters);
            if (exceptionThreadLocal.get()){
                throw new RuntimeException("rpc provider throws exception...");
            }
        }finally {
            exceptionThreadLocal.remove();
        }
        return result;
    }

    private InvocationHandler getInvocationHandler(Object obj){
        return (proxy, method, args) -> {
            logger.info("use proxy invoke method..." + Thread.currentThread().getName());
            method.setAccessible(true);
            Object result = null;
            try{
                result = method.invoke(obj, args);
            }catch (Exception e){
                exceptionThreadLocal.set(true);
            }
            return result;
        };
    }
}
