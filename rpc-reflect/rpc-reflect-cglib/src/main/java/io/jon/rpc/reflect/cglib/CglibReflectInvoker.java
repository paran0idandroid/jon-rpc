package io.jon.rpc.reflect.cglib;

import io.jon.rpc.reflect.api.ReflectInvoker;
import io.jon.rpc.spi.annotation.SPIClass;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SPIClass
public class CglibReflectInvoker implements ReflectInvoker {

    private final Logger logger = LoggerFactory.getLogger(CglibReflectInvoker.class);


    @Override
    public Object invokeMethod(Object serviceBean,
                               Class<?> serviceClass,
                               String methodName,
                               Class<?>[] parameterTypes,
                               Object[] parameters) throws Throwable {
        logger.info("use cglib reflect type to invoke method...");
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        return serviceFastMethod.invoke(serviceBean, parameters);
    }
}
