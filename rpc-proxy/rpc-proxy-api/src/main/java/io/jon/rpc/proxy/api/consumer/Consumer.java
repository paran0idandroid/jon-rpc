package io.jon.rpc.proxy.api.consumer;

import io.jon.rpc.protocol.RpcProtocol;
import io.jon.rpc.protocol.request.RpcRequest;
import io.jon.rpc.proxy.api.future.RPCFuture;

public interface Consumer {

    /**
     * 消费者发送request请求
     */
    RPCFuture sendRequest(RpcProtocol<RpcRequest> protocol) throws Exception;
}
