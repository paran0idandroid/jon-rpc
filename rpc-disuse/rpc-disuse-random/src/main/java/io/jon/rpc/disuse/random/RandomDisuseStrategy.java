package io.jon.rpc.disuse.random;

import io.jon.rpc.disuse.api.DisuseStrategy;
import io.jon.rpc.disuse.api.connection.ConnectionInfo;
import io.jon.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

@SPIClass
public class RandomDisuseStrategy implements DisuseStrategy {
    private final Logger logger = LoggerFactory.getLogger(RandomDisuseStrategy.class);
    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        logger.info("执行随机连接淘汰策略......");
        if (connectionList.isEmpty()) return null;
        return connectionList.get(new Random().nextInt(connectionList.size()));
    }
}
