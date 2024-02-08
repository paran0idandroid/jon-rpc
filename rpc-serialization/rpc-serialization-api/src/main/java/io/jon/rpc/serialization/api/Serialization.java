package io.jon.rpc.serialization.api;

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
