package io.jon.rpc.flow.processor;

import io.jon.rpc.constants.RpcConstants;
import io.jon.rpc.protocol.header.RpcHeader;
import io.jon.rpc.spi.annotation.SPI.SPI;

@SPI(RpcConstants.FLOW_POST_PROCESSOR_PRINT)
public interface FlowPostProcessor {
    /**
     * 流控分析后置处理器方法
     */
    void postRpcHeaderProcessor(RpcHeader rpcHeader);
}

