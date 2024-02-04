package io.jon.rpc.protocol;

import io.jon.rpc.protocol.header.RpcHeader;
import lombok.Data;

import java.io.Serializable;

@Data
public class RpcProtocol<T> implements Serializable {


    private static final long serialVersionUID = 292789485166173277L;

    private RpcHeader header;

    private T body;
}
