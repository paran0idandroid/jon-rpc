package io.jon.rpc.provider.common.manager;

import io.jon.rpc.constants.RpcConstants;
import io.jon.rpc.protocol.RpcProtocol;
import io.jon.rpc.protocol.enumeration.RpcType;
import io.jon.rpc.protocol.header.RpcHeader;
import io.jon.rpc.protocol.header.RpcHeaderFactory;
import io.jon.rpc.protocol.response.RpcResponse;
import io.jon.rpc.provider.common.cache.ProviderChannelCache;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ProviderConnectionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderConnectionManager.class);
    /**
     * 扫描并移除不活跃的连接
     */
    public static void scanNotActiveChannel(){
        Set<Channel> channelCache = ProviderChannelCache.getChannelCache();
        if (channelCache == null || channelCache.isEmpty()) return;
        channelCache.stream().forEach((channel) -> {
            if (!channel.isOpen() || !channel.isActive()){
                channel.close();
                ProviderChannelCache.remove(channel);
            }
        });
    }

    /**
     * 发送ping消息
     */
    public static void broadcastPingMessageFromProvider(){
        Set<Channel> channelCache = ProviderChannelCache.getChannelCache();
        if (channelCache == null || channelCache.isEmpty()) return;
        RpcHeader header = RpcHeaderFactory.getRequestHeader(RpcConstants.SERIALIZATION_PROTOSTUFF, RpcType.HEARTBEAT_FROM_PROVIDER.getType());
        RpcProtocol<RpcResponse> responseRpcProtocol = new RpcProtocol<RpcResponse>();
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setResult(RpcConstants.HEARTBEAT_PING);
        responseRpcProtocol.setHeader(header);
        responseRpcProtocol.setBody(rpcResponse);
        channelCache.stream().forEach((channel) -> {
            if (channel.isOpen() && channel.isActive()){
                LOGGER.info("send ===ping ping ping=== heartbeat message to service consumer, " +
                        "the consumer is: {}, the heartbeat message is: {}",
                        channel.remoteAddress(), rpcResponse.getResult());
                channel.writeAndFlush(responseRpcProtocol);
            }
        });
    }
}
