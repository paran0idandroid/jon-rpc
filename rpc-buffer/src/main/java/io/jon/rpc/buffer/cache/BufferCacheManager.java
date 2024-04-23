package io.jon.rpc.buffer.cache;

import io.jon.rpc.common.exception.RpcException;
import io.jon.rpc.constants.RpcConstants;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BufferCacheManager<T> {

    // 缓冲队列
    private BlockingQueue<T> bufferQueue;

    // 缓存管理器单例对象
    private static volatile BufferCacheManager instance;

    // 私有构造方法
    private BufferCacheManager(int bufferSize){
        if(bufferSize <= 0){
            bufferSize = RpcConstants.DEFAULT_BUFFER_SIZE;
        }
        this.bufferQueue = new ArrayBlockingQueue<>(bufferSize);
    }

    // 创建单例
    public static <T> BufferCacheManager<T> getInstance(int bufferSize){

        if(instance == null){
            synchronized (BufferCacheManager.class){
                if(instance == null){
                    instance = new BufferCacheManager(bufferSize);
                }
            }
        }
        return instance;
    }

    // 向缓冲区添加元素
    public void put(T t){
        try{
            bufferQueue.put(t);
        }catch (InterruptedException e){
            throw new RpcException(e);
        }
    }

    // 获取缓冲区元素
    public T take(){
        try{
            return bufferQueue.take();
        }catch (InterruptedException e){
            throw new RpcException(e);
        }
    }
}
