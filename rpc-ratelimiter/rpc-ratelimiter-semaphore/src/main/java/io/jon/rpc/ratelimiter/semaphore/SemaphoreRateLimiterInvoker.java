package io.jon.rpc.ratelimiter.semaphore;

import io.jon.rpc.ratelimiter.base.AbstractRateLimiterInvoker;
import io.jon.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

@SPIClass
public class SemaphoreRateLimiterInvoker extends AbstractRateLimiterInvoker {

    private final Logger logger = LoggerFactory.getLogger(SemaphoreRateLimiterInvoker.class);

    private Semaphore semaphore;
    @Override
    public void init(int permits, int milliSeconds) {
        super.init(permits, milliSeconds);
        this.semaphore = new Semaphore(permits);
    }

    @Override
    public boolean tryAcquire() {
        logger.info("=======>执行Semaphore信号量限流<=======");
        return semaphore.tryAcquire();
    }

    @Override
    public void release() {
        semaphore.release();
    }
}
