package io.jon.rpc.spi.annotation.SPI;

import java.lang.annotation.*;

/**
 * 标注到加入SPI机制的接口上
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SPI {

    String value() default "";
}
