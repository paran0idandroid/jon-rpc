package io.jon.rpc.codec;

import io.jon.rpc.serialization.api.Serialization;
import io.jon.rpc.spi.loader.ExtensionLoader;

public interface RpcCodec {

    /**
     * 根据serializationType通过SPI获取序列句柄
     * @param serializationType 序列化方式
     * @return Serialization对象
     */
    default Serialization getSerialization(String serializationType){

        return ExtensionLoader.getExtension(Serialization.class, serializationType);
    }
}
