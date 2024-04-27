package io.jon.rpc.ratelimiter.api;

import io.jon.rpc.constants.RpcConstants;
import io.jon.rpc.spi.annotation.SPI.SPI;

@SPI(RpcConstants.DEFAULT_RATELIMITER_INVOKER)
public interface RateLimiterInvoker {

    // 限流方法
    boolean tryAcquire();

    // 释放资源
    void release();

    /**
     * 在milliSeconds毫秒内最多允许通过permits个请求
     * @param permits 在milliSeconds毫秒内最多能够通过的请求个数
     * @param milliSeconds 毫秒数
     */
    default void init(int permits, int milliSeconds){}

}
