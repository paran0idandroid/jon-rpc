package io.jon.rpc.protocol.base;

import lombok.Data;

import java.io.Serializable;

@Data
public class RpcMessage implements Serializable {

    //是否单向发送
    private boolean oneway;

    //是否异步调用
    private boolean async;


}
