package io.jon.rpc.protocol.enumeration;

public enum RpcType {

    //请求消息
    REQUEST(1),
    //响应消息
    RESPONSE(2),
    //心跳消息
    HEARTBEAT(3);

    private final int type;

    RpcType(int type) {
        this.type = type;
    }
}
