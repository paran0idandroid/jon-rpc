package io.jon.rpc.disuse.defaultstrategy;

import io.jon.rpc.disuse.api.DisuseStrategy;
import io.jon.rpc.disuse.api.connection.ConnectionInfo;
import io.jon.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


@SPIClass
public class DefaultDisuseStrategy implements DisuseStrategy {
    private final Logger logger = LoggerFactory.getLogger(DefaultDisuseStrategy.class);
    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        logger.info("execute default disuse strategy... " +
                "which is eliminate the first connection in the connectionList");
        return connectionList.get(0);
    }

}
