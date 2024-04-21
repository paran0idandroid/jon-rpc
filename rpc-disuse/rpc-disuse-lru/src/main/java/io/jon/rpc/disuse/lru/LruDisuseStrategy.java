package io.jon.rpc.disuse.lru;

import io.jon.rpc.disuse.api.DisuseStrategy;
import io.jon.rpc.disuse.api.connection.ConnectionInfo;
import io.jon.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SPIClass
public class LruDisuseStrategy implements DisuseStrategy {
    private final Logger logger = LoggerFactory.getLogger(LruDisuseStrategy.class);
    // 淘汰最后使用时间最早的连接
    private final Comparator<ConnectionInfo> lastUseTimeComparator = (o1, o2) -> {
        return o1.getLastUseTime() - o2.getLastUseTime() > 0 ? 1 : -1;
    };
    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        logger.info("执行最近未被使用连接淘汰策略......");
        if (connectionList.isEmpty()) return null;
        Collections.sort(connectionList, lastUseTimeComparator);
        return connectionList.get(0);
    }
}
