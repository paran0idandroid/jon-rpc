package io.jon.rpc.proxy.api.future;

import io.jon.rpc.protocol.RpcProtocol;
import io.jon.rpc.protocol.request.RpcRequest;
import io.jon.rpc.protocol.response.RpcResponse;
import io.jon.rpc.proxy.api.callback.AsyncRPCCallback;
import io.jon.rpc.threadpool.ClientThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

public class RPCFuture extends CompletableFuture<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RPCFuture.class);

    private Sync sync;
    private RpcProtocol<RpcRequest> requestRpcProtocol;
    private RpcProtocol<RpcResponse> responseRpcProtocol;
    private long startTime;

    private long responseTimeThreshold = 5000;

    // 存放回调接口
    private List<AsyncRPCCallback> pendingCallbacks = new ArrayList<>();

    // 添加和执行回调方法时，进行加锁和解锁操作
    private ReentrantLock lock = new ReentrantLock();

    public RPCFuture(RpcProtocol<RpcRequest> requestRpcProtocol){
        this.sync = new Sync();
        this.requestRpcProtocol = requestRpcProtocol;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public boolean isDone(){
        return sync.isDone();
    }

    /**
     * 阻塞获取responseRpcProtocol协议中对象的实际结果数据
     */
    @Override
    public Object get() throws InterruptedException, ExecutionException{
        // sync.acquire(-1)用于获取同步状态
        // -1 通常表示无限等待，即当前线程会一直等待直到获取到同步状态done或被中断
        sync.acquire(-1);
        if(this.responseRpcProtocol != null){
            return this.responseRpcProtocol.getBody().getResult();
        }else {
            return null;
        }
    }

    /**
     * 超时阻塞获取responseRpcProtocol协议中对象的实际结果数据
     */
    @Override
    public Object get(long timeout, TimeUnit unit) throws
            InterruptedException, ExecutionException, TimeoutException{
        /**
         * sync.tryAcquireNanos(-1, unit.toNanos(timeout))用于尝试在给定的时间内获取同步状态
         * 参数 nanosTimeout 表示等待的超时时间，以纳秒为单位
         * 通常情况下，如果参数为负数，表示无限等待
         * 在这个特定的情境下，-1 被转换为纳秒时间，并且被传递给 tryAcquireNanos 方法
         * 这意味着程序会尝试在无限期间内等待获取同步状态
         * 如果成功获取到了同步状态，则返回 true，否则在等待超时后返回 false
         */
        boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));
        if(success){
            if(this.responseRpcProtocol != null){
                return this.responseRpcProtocol.getBody().getResult();
            }else{
                return null;
            }
        }else{
            throw new RuntimeException("Timeout Exception. Request id: " +
                    this.requestRpcProtocol.getHeader().getRequestId() +
                    ". Request class name: " + this.requestRpcProtocol.getBody().getClassName() +
                    ". Request method: " + this.requestRpcProtocol.getBody().getMethodName());
        }
    }

    @Override
    public boolean isCancelled(){
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning){
        throw new UnsupportedOperationException();
    }

    /**
     * 当服务消费者接收到服务提供者响应的结果数据时，就会调用done方法
     * 传入RpcResponse类型的协议对象，唤醒阻塞的线程获取响应的结果数据
     * @param responseRpcProtocol
     */
    public void done(RpcProtocol<RpcResponse> responseRpcProtocol){

        this.responseRpcProtocol = responseRpcProtocol;
        /**
         * release(1) 表示释放一个同步状态单元
         * 这意味着，如果当前同步状态是 pending（即等待状态）0，则释放后会将状态更新为 done（完成状态）1
         * 如果同步状态已经是 done 1，则调用释放方法也不会改变其状态
         */
        sync.release(1);

        //调用回调方法
        invokeCallbacks();

        //Threshold
        long responseTime = System.currentTimeMillis() - startTime;
        if(responseTime > this.responseTimeThreshold){
            LOGGER.warn("Service response time is too long. Request id = " +
                    responseRpcProtocol.getHeader().getRequestId() + ". Response Time = " +
                    responseTime + "ms");
        }
    }

    //异步执行回调方法
    private void runCallback(final AsyncRPCCallback callback){

        final RpcResponse res = this.responseRpcProtocol.getBody();
        ClientThreadPool.submit(()->{
            if(!res.isError()){
                callback.onSuccess(res.getResult());
            }else{
                callback.onException(new RuntimeException("Response error",
                        new Throwable(res.getError())));
            }
        });
    }

    //外部服务添加回调接口实例对象到pendingCallbacks集合中
    public RPCFuture addCallback(AsyncRPCCallback callback){

        lock.lock();
        try{
            if(isDone()){
                runCallback(callback);
            }else{
                this.pendingCallbacks.add(callback);
            }
        }finally {
            lock.unlock();
        }
        return this;
    }

    //依次执行pendingCallbacks集合中回调接口的方法
    private void invokeCallbacks(){
        lock.lock();

        try{
            for(final AsyncRPCCallback callback : pendingCallbacks){
                runCallback(callback);
            }
        }finally {
            lock.unlock();
        }
    }

    static class Sync extends AbstractQueuedSynchronizer{

        private static final long serialVersionUID = 1L;

        //future status
        private final int done = 1;
        private final int pending = 0;

        /**
         * tryAcquire(int acquires) 方法尝试获取同步状态，它会检查当前状态是否为 done
         * 如果是，则返回 true，表示获取成功；否则返回 false，表示获取失败
         */
        protected boolean tryAcquire(int acquires){
            return getState() == done;
        }

        /**
         * tryRelease(int releases) 方法尝试释放同步状态，它会检查当前状态是否为 pending
         * 如果是，则尝试将状态更新为 done，并返回 true 表示释放成功；否则返回 false，表示释放失败
         */
        protected boolean tryRelease(int releases){
            if(getState() == pending){
                if(compareAndSetState(pending, done)){
                    return true;
                }
            }
            return false;
        }

        public boolean isDone(){
            getState();
            return getState() == done;
        }
    }



}
