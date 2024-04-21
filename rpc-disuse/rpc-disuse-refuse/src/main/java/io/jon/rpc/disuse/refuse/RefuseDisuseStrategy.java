package io.jon.rpc.disuse.refuse;

import io.jon.rpc.common.exception.RefuseException;
import io.jon.rpc.disuse.api.DisuseStrategy;
import io.jon.rpc.disuse.api.connection.ConnectionInfo;
import io.jon.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@SPIClass
public class RefuseDisuseStrategy implements DisuseStrategy {
    private final Logger logger = LoggerFactory.getLogger(RefuseDisuseStrategy.class);
    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        logger.info("执行拒绝连接淘汰策略......");
        throw new RefuseException("拒绝新的连接......");
    }
}
