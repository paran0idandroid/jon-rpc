package io.jon.rpc.threadpool;

import io.jon.rpc.constants.RpcConstants;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BufferCacheThreadPool {

    private static ThreadPoolExecutor threadPoolExecutor;
    static {
        threadPoolExecutor = new ThreadPoolExecutor(
                8,
                8,
                RpcConstants.DEFAULT_KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(RpcConstants.DEFAULT_QUEUE_CAPACITY));
    }

    public static void submit(Runnable task){
        threadPoolExecutor.submit(task);
    }

    public static void shutdown(){
        threadPoolExecutor.shutdown();
    }
}
