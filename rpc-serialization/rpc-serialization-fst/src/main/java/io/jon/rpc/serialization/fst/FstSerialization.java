package io.jon.rpc.serialization.fst;

import io.jon.rpc.common.exception.SerializerException;
import io.jon.rpc.serialization.api.Serialization;
import io.jon.rpc.spi.annotation.SPIClass;
import org.nustaq.serialization.FSTConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SPIClass
public class FstSerialization implements Serialization {

    private final Logger logger = LoggerFactory.getLogger(FstSerialization.class);
    @Override
    public <T> byte[] serialize(T obj) {
        logger.info("execute fst serialize...");

        if(obj == null){
            throw new SerializerException("serialize object is null");
        }
        // 获取了默认的 FST 配置对象
        // FSTConfiguration 是 FST 库的核心类之一，它用于配置 FST 序列化/反序列化的行为
        FSTConfiguration conf = FSTConfiguration.getDefaultConfiguration();
        conf.registerClass(obj.getClass());
        // 将给定的 Java 对象序列化为字节数组
        return conf.asByteArray(obj);
    }

    @Override
    public <T> T deserialization(byte[] data, Class<T> cls) {

        logger.info("execute fst deserialize...");
        if(data == null){
            throw new SerializerException("deserialize data is null");
        }

        // 将字节数组反序列化为 Java 对象。data 是要反序列化的字节数组
        // 这里的 (T) 是一个类型转换操作，将反序列化后的对象转换为泛型类型 T
        FSTConfiguration conf = FSTConfiguration.getDefaultConfiguration();
        conf.registerClass(cls);
        return (T) conf.asObject(data);
    }
}
