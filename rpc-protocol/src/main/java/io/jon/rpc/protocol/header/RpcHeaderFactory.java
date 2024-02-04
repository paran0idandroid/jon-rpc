package io.jon.rpc.protocol.header;

import io.jon.rpc.common.id.IdFactory;
import io.jon.rpc.constants.RpcConstants;

public class RpcHeaderFactory {

    public static RpcHeader getRequestHeader(String serializationType, int messageType){

        RpcHeader header = new RpcHeader();
        Long requestId = IdFactory.getId();
        header.setMagic(RpcConstants.MAGIC);
        header.setRequestId(requestId);
        header.setMsgType((byte) messageType);
        header.setStatus((byte) 0x1);
        header.setSerializationType(serializationType);
        return header;

    }
}
