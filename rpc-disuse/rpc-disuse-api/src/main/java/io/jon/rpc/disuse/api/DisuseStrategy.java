package io.jon.rpc.disuse.api;

import io.jon.rpc.constants.RpcConstants;
import io.jon.rpc.disuse.api.connection.ConnectionInfo;
import io.jon.rpc.spi.annotation.SPI.SPI;

import java.util.List;

@SPI(RpcConstants.RPC_CONNECTION_DISUSE_STRATEGY_DEFAULT)
public interface DisuseStrategy {

    /**
     * 从连接列表中根据规则获取一个连接对象
     */
    ConnectionInfo selectConnection(List<ConnectionInfo> connectionList);

}
