package io.jon.rpc.spi.annotation;

import java.lang.annotation.*;

/**
 * 标注到加入SPI机制的接口实现类上
 */

/**
 * @Documented: 这个注解表明该注解会被包含在Java文档中
 * 也就是说，如果某个类或方法使用了这个注解
 * 那么在生成Javadoc文档时，这个注解及其信息会被包含在文档中，方便开发者查阅
 *
 * @Retention(RetentionPolicy.RUNTIME): 这个注解表明该注解的生命周期
 * 为运行时（Runtime）。这意味着编译器将保留这个注解，并在运行时通过反射机制来读取它。
 *
 * @Target({ElementType.TYPE}): 这个注解表明该注解可以
 * 被应用于类、接口（包括注解类型）或枚举。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SPIClass {
}
