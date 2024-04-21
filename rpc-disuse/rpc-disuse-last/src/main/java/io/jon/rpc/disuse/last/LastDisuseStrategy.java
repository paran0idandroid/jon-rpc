package io.jon.rpc.disuse.last;

import io.jon.rpc.disuse.api.DisuseStrategy;
import io.jon.rpc.disuse.api.connection.ConnectionInfo;
import io.jon.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@SPIClass
public class LastDisuseStrategy implements DisuseStrategy {
    private final Logger logger = LoggerFactory.getLogger(LastDisuseStrategy.class);
    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        logger.info("执行最晚连接淘汰策略......");
        if (connectionList.isEmpty()) return null;
        return connectionList.get(connectionList.size() - 1);
    }
}

