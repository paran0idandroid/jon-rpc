package io.jon.rpc.consumer.common.cache;

import io.netty.channel.Channel;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ConsumerChannelCache {

    // 在服务消费者端 缓存 成功连接 服务提供者 的Channel

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
