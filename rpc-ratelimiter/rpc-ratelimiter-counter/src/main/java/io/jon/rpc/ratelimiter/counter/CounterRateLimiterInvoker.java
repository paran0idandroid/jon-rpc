package io.jon.rpc.ratelimiter.counter;

import io.jon.rpc.ratelimiter.base.AbstractRateLimiterInvoker;
import io.jon.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

// 计数器限流
@SPIClass
public class CounterRateLimiterInvoker extends AbstractRateLimiterInvoker {

    private final Logger logger = LoggerFactory.getLogger(CounterRateLimiterInvoker.class);

    private final AtomicInteger currentCounter = new AtomicInteger(0);
    private volatile long lastTimeStamp = System.currentTimeMillis();
    private final ThreadLocal<Boolean> threadLocal = new ThreadLocal<>();

    @Override
    public boolean tryAcquire() {

        logger.info("=======>执行计数器限流<=======");
        long currentTimeStamp = System.currentTimeMillis();
        // 计数器统计的是在milliSeconds毫秒内最多能够通过的请求个数
        // 每经过一个执行周期（milliSeconds毫秒），计数器重置
        if(currentTimeStamp - lastTimeStamp >= milliSeconds){
            lastTimeStamp = currentTimeStamp;
            currentCounter.set(0);
            return true;
        }

        // 当前请求数小于配置的数量
        if(currentCounter.incrementAndGet() <= permits){
            threadLocal.set(true);
            return true;
        }

        return false;
    }

    @Override
    public void release() {
        if (threadLocal.get()){
            try {
                currentCounter.decrementAndGet();
            }finally {
                threadLocal.remove();
            }
        }
    }
}
