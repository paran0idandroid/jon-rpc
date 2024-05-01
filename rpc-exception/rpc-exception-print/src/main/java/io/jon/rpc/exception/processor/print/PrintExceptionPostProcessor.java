package io.jon.rpc.exception.processor.print;

import io.jon.rpc.exception.processor.ExceptionPostProcessor;
import io.jon.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SPIClass
public class PrintExceptionPostProcessor implements ExceptionPostProcessor {

    private final Logger logger = LoggerFactory.getLogger(PrintExceptionPostProcessor.class);

    @Override
    public void postExceptionProcessor(Throwable e) {
        logger.info("=======>>>流控分析之程序抛出异常: {}<<<=======", e.getMessage());
    }
}
