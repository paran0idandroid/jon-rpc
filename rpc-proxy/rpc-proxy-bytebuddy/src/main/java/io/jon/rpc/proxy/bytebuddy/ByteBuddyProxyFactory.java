
package io.jon.rpc.proxy.bytebuddy;

import io.jon.rpc.proxy.api.BaseProxyFactory;
import io.jon.rpc.proxy.api.ProxyFactory;
import io.jon.rpc.spi.annotation.SPIClass;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
@SPIClass
public class ByteBuddyProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {
    private final Logger logger = LoggerFactory.getLogger(ByteBuddyProxyFactory.class);
    @Override
    public <T> T getProxy(Class<T> clazz) {
       try{
           logger.info("基于ByteBuddy动态代理...");
           // 创建了一个 ByteBuddy 实例，并调用 subclass() 方法，指定要代理的父类为 Object
           return (T) new ByteBuddy().subclass(Object.class)
                   // 指定要代理的接口为 clazz 参数传入的接口
                   .implement(clazz)
                   //设置代理对象的行为
                   // 在这里，使用 InvocationHandlerAdapter.of(objectProxy)
                   // 将 objectProxy 转换为 InvocationHandler 对象
                   // 并指定为代理对象的处理器
                   .intercept(InvocationHandlerAdapter.of(objectProxy))
                   // 生成代理类的字节码
                   .make()
                   // 加载生成的代理类，并指定加载器为当前类的类加载器
                   .load(ByteBuddyProxyFactory.class.getClassLoader())
                   // 获取代理类的 Class 对象
                   .getLoaded()
                   // 通过反射调用代理类的无参构造函数，创建代理对象的实例
                   .getDeclaredConstructor()
                   .newInstance();
       }catch (Exception e){
           logger.error("bytebuddy proxy throws exception:{}", e);
       }
       return null;
    }
}
