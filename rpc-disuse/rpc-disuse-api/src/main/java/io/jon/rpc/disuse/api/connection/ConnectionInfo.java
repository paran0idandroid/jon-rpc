package io.jon.rpc.disuse.api.connection;

import io.netty.channel.Channel;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class ConnectionInfo implements Serializable {

    private static final long serialVersionUID = -9165095996736033806L;

    /**
     * Channel连接
     */
    private Channel channel;

    /**
     * 连接的时间
     */
    private long connectionTime;

    /**
     * 最后使用时间
     */
    private long lastUseTime;

    /**
     * 使用次数
     */
    private AtomicInteger useCount = new AtomicInteger(0);


    public ConnectionInfo(){}

    public ConnectionInfo(Channel channel){
        this.channel = channel;
        long currentTimeStamp = System.currentTimeMillis();
        this.connectionTime = currentTimeStamp;
        this.lastUseTime = currentTimeStamp;
    }

    /**
     * equals() 方法:
     * 首先，检查传入的对象是否是当前对象的引用
     * 如果是则返回 true，因为两个引用相同,它们肯定是相等的。
     * 接着，检查传入的对象是否为 null 或者是否与当前对象属于不同的类。
     * 如果是，则返回 false，因为不同类的对象肯定不相等。
     * 最后，将传入对象强制转换为 ConnectionInfo 类型
     * 然后比较它们的 channel 字段是否相等。如果相等，则返回 true
     * 表示这两个对象相等；如果不相等，则返回 false。
     * hashCode() 方法:
     * hashCode() 方法通过调用 Objects.hashCode() 方法计算哈希值
     * 这个方法会返回给定对象的哈希码，如果对象为 null，则返回 0。
     * 在这里，hashCode() 方法计算的是 channel 字段的哈希码。
     * 由于 channel 是一个引用类型，因此其哈希码将是其内存地址的哈希码。
     * 如果两个 ConnectionInfo 对象的 channel 引用相同，则它们的哈希码也会相同。
     * 总的来说，equals() 方法用于判断两个对象是否相等，
     * 而 hashCode() 方法用于获取对象的哈希码，以便在集合等数据结构中使用。
     * 在这个例子中，根据 channel 字段来定义对象的相等性和哈希值是合理的，
     * 因为 channel 是唯一标识一个连接的信息。
     */

    // 总结就是，只要两个对象的Channel相同，则认为是同一个对象
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        ConnectionInfo info = (ConnectionInfo) o;
        return Objects.equals(channel, info.channel);
    }

    @Override
    public int hashCode(){
        return Objects.hashCode(channel);
    }

    public int getUseCount() {
        return useCount.get();
    }

    public int incrementUseCount() {
        return this.useCount.incrementAndGet();
    }
}
