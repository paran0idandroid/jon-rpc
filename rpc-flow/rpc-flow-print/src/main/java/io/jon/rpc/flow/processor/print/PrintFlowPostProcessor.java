package io.jon.rpc.flow.processor.print;

import io.jon.rpc.flow.processor.FlowPostProcessor;
import io.jon.rpc.protocol.header.RpcHeader;
import io.jon.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SPIClass
public class PrintFlowPostProcessor implements FlowPostProcessor {

    private final Logger logger = LoggerFactory.getLogger(PrintFlowPostProcessor.class);
    @Override
    public void postRpcHeaderProcessor(RpcHeader rpcHeader) {
        logger.info(getRpcHeaderString(rpcHeader));
    }

    private String getRpcHeaderString(RpcHeader rpcHeader) {

        return  "流控分析获取RpcHeader: " +
                "magic: " + rpcHeader.getMagic() +
                ", requestId: " + rpcHeader.getRequestId() +
                ", msgType: " + rpcHeader.getMsgType() +
                ", serializationType: " + rpcHeader.getSerializationType() +
                ", status: " + rpcHeader.getStatus() +
                ", msgLen: " + rpcHeader.getMsgLen();
    }
}
