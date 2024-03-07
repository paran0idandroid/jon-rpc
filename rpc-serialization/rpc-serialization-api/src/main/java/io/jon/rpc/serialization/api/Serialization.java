package io.jon.rpc.serialization.api;

import io.jon.rpc.constants.RpcConstants;
import io.jon.rpc.spi.annotation.SPI.SPI;

@SPI(RpcConstants.SERIALIZATION_JSON)
public interface Serialization {

    /**
     * 序列化
     */
    <T> byte[] serialize(T obj);

    /**
     * 反序列化
     */
    <T> T deserialization(byte[] data, Class<T> cls);
}
