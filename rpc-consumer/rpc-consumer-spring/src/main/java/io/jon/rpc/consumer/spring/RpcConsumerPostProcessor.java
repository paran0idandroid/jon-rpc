package io.jon.rpc.consumer.spring;

import io.jon.rpc.annotation.RpcReference;
import io.jon.rpc.constants.RpcConstants;
import io.jon.rpc.consumer.spring.context.RpcConsumerSpringContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class RpcConsumerPostProcessor
        implements ApplicationContextAware, BeanClassLoaderAware, BeanFactoryPostProcessor {

    // ApplicationContextAware 获取Spring应用上下文对象
    // BeanClassLoaderAware 获取加载当前bean类的类加载器
    // BeanFactoryPostProcessor 在Spring应用上下文加载bean的定义后，但是在实例化bean之前修改bean的定义

    // 解析RpcReference注解，创建RpcReferenceBean类的BeanDefinition对象
    // 将BeanDefinition对象注册到IOC容器中
    private final Logger logger = LoggerFactory.getLogger(RpcConsumerPostProcessor.class);

    private ApplicationContext context;

    private ClassLoader classLoader;

    private final Map<String, BeanDefinition> rpcRefBeanDefinitions = new LinkedHashMap<>();

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
        RpcConsumerSpringContext.getInstance().setContext(applicationContext);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

        // 一个关键的回调方法，在该方法中，它遍历了所有的 bean 定义
        // 解析带有 @RpcReference 注解的字段，并根据这些字段创建 RpcReferenceBean 的 BeanDefinition 对象
        // 最后将这些 BeanDefinition 注册到 Spring 的容器中
        for(String beanDefinitionName : configurableListableBeanFactory.getBeanDefinitionNames()){
            BeanDefinition beanDefinition = configurableListableBeanFactory.getBeanDefinition(beanDefinitionName);

            String beanClassName = beanDefinition.getBeanClassName();
            if(beanClassName != null){
                Class<?> clazz = ClassUtils.resolveClassName(beanClassName, this.classLoader);
                ReflectionUtils.doWithFields(clazz, this::parseRpcReference);
            }
        }

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) configurableListableBeanFactory;
        this.rpcRefBeanDefinitions.forEach((beanName, beanDefinition) -> {
            if(context.containsBean(beanName)){
                throw new IllegalArgumentException("spring context already has a bean named " + beanName);
            }
            registry.registerBeanDefinition(beanName, rpcRefBeanDefinitions.get(beanName));
            logger.info("registered RpcReferenceBean {} success.", beanName);
        });
    }

    // 解析 @RpcReference 注解，将注解中的属性值设置到 RpcReferenceBean 的 BeanDefinition 中
    // 并将其存储在一个映射中，最后注册到 Spring 容器中
    private void parseRpcReference(Field field) {
        RpcReference annotation = AnnotationUtils.getAnnotation(field, RpcReference.class);
        if (annotation != null) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RpcReferenceBean.class);
            builder.setInitMethodName(RpcConstants.INIT_METHOD_NAME);
            builder.addPropertyValue("interfaceClass", field.getType());
            builder.addPropertyValue("version", annotation.version());
            builder.addPropertyValue("registryType", annotation.registryType());
            builder.addPropertyValue("registryAddress", annotation.registryAddress());
            builder.addPropertyValue("loadBalanceType", annotation.loadBalanceType());
            builder.addPropertyValue("serializationType", annotation.serializationType());
            builder.addPropertyValue("timeout", annotation.timeout());
            builder.addPropertyValue("async", annotation.async());
            builder.addPropertyValue("oneway", annotation.oneway());
            builder.addPropertyValue("proxy", annotation.proxy());
            builder.addPropertyValue("group", annotation.group());
            builder.addPropertyValue("scanNotActiveChannelInterval", annotation.scanNotActiveChannelInterval());
            builder.addPropertyValue("heartbeatInterval", annotation.heartbeatInterval());
            builder.addPropertyValue("retryInterval", annotation.retryInterval());
            builder.addPropertyValue("retryTimes", annotation.retryTimes());
            builder.addPropertyValue("enableResultCache", annotation.enableResultCache());
            builder.addPropertyValue("resultCacheExpire", annotation.resultCacheExpire());
            builder.addPropertyValue("enableDirectServer", annotation.enableDirectServer());
            builder.addPropertyValue("directServerUrl", annotation.directServerUrl());
            builder.addPropertyValue("enableDelayConnection", annotation.enableDelayConnection());
            builder.addPropertyValue("corePoolSize", annotation.corePoolSize());
            builder.addPropertyValue("maximumPoolSize", annotation.maximumPoolSize());
            builder.addPropertyValue("flowType", annotation.flowType());
            builder.addPropertyValue("enableBuffer", annotation.enableBuffer());
            builder.addPropertyValue("bufferSize", annotation.bufferSize());
            builder.addPropertyValue("reflectType", annotation.reflectType());
            builder.addPropertyValue("fallbackClass", annotation.fallbackClass());
            builder.addPropertyValue("fallbackClassName", annotation.fallbackClassName());
            builder.addPropertyValue("enableRateLimiter", annotation.enableRateLimiter());
            builder.addPropertyValue("rateLimiterType", annotation.rateLimiterType());
            builder.addPropertyValue("permits", annotation.permits());
            builder.addPropertyValue("milliSeconds", annotation.milliSeconds());
            builder.addPropertyValue("rateLimiterFailStrategy", annotation.rateLimiterFailStrategy());

            BeanDefinition beanDefinition = builder.getBeanDefinition();
            rpcRefBeanDefinitions.put(field.getName(), beanDefinition);
        }
    }

}
