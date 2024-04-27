package io.jon.rpc.ratelimiter.guava;

import com.google.common.util.concurrent.RateLimiter;
import io.jon.rpc.ratelimiter.base.AbstractRateLimiterInvoker;
import io.jon.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SPIClass
public class GuavaRateLimiterInvoker extends AbstractRateLimiterInvoker {

    private final Logger logger = LoggerFactory.getLogger(GuavaRateLimiterInvoker.class);

    private RateLimiter rateLimiter;

    @Override
    public void init(int permits, int milliSeconds) {
        super.init(permits, milliSeconds);
        // 转换成每秒最多允许的个数
        double permitsPerSecond = ((double) permits) / milliSeconds * 1000;
        this.rateLimiter = RateLimiter.create(permitsPerSecond);
    }

    @Override
    public boolean tryAcquire() {
        logger.info("=======>执行Guava限流<=======");
        return this.rateLimiter.tryAcquire();
    }

    @Override
    public void release() {

        //Guava不需要手动释放资源
    }
}
