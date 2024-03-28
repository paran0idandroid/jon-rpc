package io.jon.rpc.provider.common.cache;

import io.netty.channel.Channel;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ProviderChannelCache {

    // 在服务提供者端缓存活跃的Channel连接
    private static volatile Set<Channel> channelCache =
            new CopyOnWriteArraySet<>();

    public static void add(Channel channel){
        channelCache.add(channel);
    }

    public static void remove(Channel channel){
        channelCache.remove(channel);
    }

    public static Set<Channel> getChannelCache(){
        return channelCache;
    }
}
