package io.jon.rpc.consumer.common.manager;

import io.jon.rpc.constants.RpcConstants;
import io.jon.rpc.consumer.common.cache.ConsumerChannelCache;
import io.jon.rpc.protocol.RpcProtocol;
import io.jon.rpc.protocol.enumeration.RpcType;
import io.jon.rpc.protocol.header.RpcHeader;
import io.jon.rpc.protocol.header.RpcHeaderFactory;
import io.jon.rpc.protocol.request.RpcRequest;
import io.netty.channel.Channel;
import jdk.nashorn.internal.ir.RuntimeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ConsumerConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerConnectionManager.class);

    // 扫描并移除不活跃的连接
    public static void scanNotActiveChannel(){
        Set<Channel> channelCache = ConsumerChannelCache.getChannelCache();
        if(channelCache == null || channelCache.isEmpty()){
            return;
        }
        channelCache.stream().forEach(
                (channel -> {
                    if(!channel.isOpen() || !channel.isActive()){
                        channel.close();
                        ConsumerChannelCache.remove(channel);
                    }
                })
        );
    }

    // 发送ping消息
    public static void broadcastPingMessageFromConsumer(){
        Set<Channel> channelCache = ConsumerChannelCache.getChannelCache();
        if(channelCache == null || channelCache.isEmpty()) return;

        RpcHeader header = RpcHeaderFactory.getRequestHeader(
                RpcConstants.SERIALIZATION_PROTOSTUFF,
                RpcType.HEARTBEAT_FROM_CONSUMER.getType());

        RpcProtocol<RpcRequest> requestRpcProtocol = new RpcProtocol<>();
        RpcRequest request = new RpcRequest();
        request.setParameters(new Object[]{RpcConstants.HEARTBEAT_PING});
        requestRpcProtocol.setBody(request);
        requestRpcProtocol.setHeader(header);

        channelCache.stream().forEach(channel -> {
            if(channel.isOpen() && channel.isActive()){
                logger.info(
                        "send ===ping ping ping=== heartbeat message from consumer to service provider, " +
                        "the provider is: {}, the heartbeat message is :{}",
                        channel.remoteAddress(),
                        RpcConstants.HEARTBEAT_PING);

                channel.writeAndFlush(requestRpcProtocol);
            }
        });
    }
}
