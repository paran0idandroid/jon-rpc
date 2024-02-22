package io.jon.rpc.protocol.response;

import io.jon.rpc.protocol.base.RpcMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RpcResponse extends RpcMessage {

    private static final long serialVersionUID = 425335064405584525L;

    private String error;

    private Object result;

    public boolean isError() {
        return error != null;
    }
}
