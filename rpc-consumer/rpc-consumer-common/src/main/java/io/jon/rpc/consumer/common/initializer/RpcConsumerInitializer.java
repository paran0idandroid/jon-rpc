package io.jon.rpc.consumer.common.initializer;

import io.jon.rpc.codec.RpcDecoder;
import io.jon.rpc.codec.RpcEncoder;
import io.jon.rpc.constants.RpcConstants;
import io.jon.rpc.consumer.common.handler.RpcConsumerHandler;
import io.jon.rpc.flow.processor.FlowPostProcessor;
import io.jon.rpc.threadpool.ConcurrentThreadPool;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class RpcConsumerInitializer extends ChannelInitializer<SocketChannel> {

    private int heartbeatInterval;

    private ConcurrentThreadPool concurrentThreadPool;

    private FlowPostProcessor flowPostProcessor;
    public RpcConsumerInitializer(int heartbeatInterval,
                                  ConcurrentThreadPool concurrentThreadPool,
                                  FlowPostProcessor flowPostProcessor){
        if(heartbeatInterval > 0){
            this.heartbeatInterval = heartbeatInterval;
        }
        this.concurrentThreadPool = concurrentThreadPool;

        this.flowPostProcessor = flowPostProcessor;
    }


    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline cp = channel.pipeline();
        cp.addLast(RpcConstants.CODEC_ENCODER, new RpcEncoder(flowPostProcessor));
        cp.addLast(RpcConstants.CODEC_DECODER, new RpcDecoder(flowPostProcessor));
        //是的，您理解得很正确。当IdleStateHandler的配置中将readerIdleTime和writerIdleTime都设置为0
        // 而heartbeatInterval设置为一个非零值时，IdleStateHandler将在两次心跳之间检测是否有读写事件发生
        // 如果在两次心跳之间没有发生读写事件，即通道在心跳间隔时间内没有发生任何读写操作
        // 那么IdleStateHandler将触发相应的空闲状态事件，表示通道处于空闲状态
        cp.addLast(RpcConstants.CODEC_CLIENT_IDLE_HANDLER,
                new IdleStateHandler(
                        heartbeatInterval, 0, 0, TimeUnit.MILLISECONDS));
        cp.addLast(new RpcConsumerHandler(concurrentThreadPool));
    }
}
