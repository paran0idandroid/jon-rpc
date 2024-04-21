package io.jon.rpc.disuse.fifo;

import io.jon.rpc.disuse.api.DisuseStrategy;
import io.jon.rpc.disuse.api.connection.ConnectionInfo;
import io.jon.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SPIClass
public class FifoDisuseStrategy implements DisuseStrategy {
    private final Logger logger = LoggerFactory.getLogger(FifoDisuseStrategy.class);

    /**
     * 假设有两个 ConnectionInfo 对象，o1 的连接时间是1000毫秒，o2 的连接时间是500毫秒
     * 根据比较器的实现，o1.getConnectionTime() - o2.getConnectionTime() 的结果是500
     * 因此比较器会返回1，表示 o1 应该排在 o2 之后，即连接时间晚的对象在后面
     */
    private final Comparator<ConnectionInfo> connectionTimeComparator = (o1, o2) -> {
        return o1.getConnectionTime() - o2.getConnectionTime() > 0 ? 1 : -1;
    };
    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        logger.info("执行先进先出连接淘汰策略......");
        if (connectionList.isEmpty()) return null;
        Collections.sort(connectionList, connectionTimeComparator);
        return connectionList.get(0);
    }
}
