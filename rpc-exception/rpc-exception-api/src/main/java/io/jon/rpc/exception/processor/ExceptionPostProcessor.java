package io.jon.rpc.exception.processor;

import io.jon.rpc.constants.RpcConstants;
import io.jon.rpc.spi.annotation.SPI.SPI;

@SPI(RpcConstants.EXCEPTION_POST_PROCESSOR_PRINT)
public interface ExceptionPostProcessor {

    /**
     * 处理异常信息，进行统计等
     */
    void postExceptionProcessor(Throwable e);
}
