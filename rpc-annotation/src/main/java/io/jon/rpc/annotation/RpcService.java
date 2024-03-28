package io.jon.rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * rpc服务提供者注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcService {

    /**
     * 接口的class
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 接口的className
     */
    String interfaceClassName() default "";

    /**
     * 版本号
     */
    String version() default "1.0.0";

    /**
     * 服务分组，默认为空
     */
    String group() default "";

    /**
     * 服务权重，默认为0
     */
    int weight() default 0;

    /**
     * 心跳间隔时间，默认30秒
     */
    int heartbeatInterval() default 30000;

    /**
     * 扫描空闲连接间隔时间，默认60秒
     */
    int scanNotActiveChannelInterval() default 60000;

}
