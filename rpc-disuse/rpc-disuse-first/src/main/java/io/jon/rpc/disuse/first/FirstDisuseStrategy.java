package io.jon.rpc.disuse.first;

import io.jon.rpc.disuse.api.DisuseStrategy;
import io.jon.rpc.disuse.api.connection.ConnectionInfo;
import io.jon.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@SPIClass
public class FirstDisuseStrategy implements DisuseStrategy {
    private final Logger logger = LoggerFactory.getLogger(FirstDisuseStrategy.class);
    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        logger.info("执行最早连接淘汰策略...");
        return connectionList.get(0);
    }
}

