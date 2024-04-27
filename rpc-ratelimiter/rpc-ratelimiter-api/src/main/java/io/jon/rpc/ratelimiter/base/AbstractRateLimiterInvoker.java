package io.jon.rpc.ratelimiter.base;

import io.jon.rpc.ratelimiter.api.RateLimiterInvoker;

public abstract class AbstractRateLimiterInvoker implements RateLimiterInvoker {

    /**
     * 在milliSeconds毫秒内最多能够通过的请求个数
     */
    protected int permits;
    /**
     * 毫秒数
     */
    protected int milliSeconds;

    @Override
    public void init(int permits, int milliSeconds) {
        this.permits = permits;
        this.milliSeconds = milliSeconds;
    }

}
