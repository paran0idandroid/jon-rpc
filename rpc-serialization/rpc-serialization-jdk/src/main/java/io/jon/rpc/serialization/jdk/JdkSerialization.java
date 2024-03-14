package io.jon.rpc.serialization.jdk;

import io.jon.rpc.common.exception.SerializerException;
import io.jon.rpc.serialization.api.Serialization;
import io.jon.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

@SPIClass
public class JdkSerialization implements Serialization {

    private final Logger logger = LoggerFactory.getLogger(JdkSerialization.class);

    @Override
    public <T> byte[] serialize(T obj) {

        logger.info("execute jdk serialize...");

        if(obj == null){
            throw new SerializerException("serialize object is null");
        }

        try{
            /**
             * 这段代码的目的是将一个Java对象序列化为字节数组。
             * 这在网络通信、对象持久化等场景中常被使用，使得对象可以在不同的环境中进行传输或保存。
             */
            // 创建一个ByteArrayOutputStream对象os，这是一个在内存中操作字节数组的输出流。
            // 它会将写入的数据存储在内部的字节数组中。
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            // 创建一个ObjectOutputStream对象out，它是一个用于将对象序列化为字节流的输出流。
            // 这里将其连接到前面创建的ByteArrayOutputStream，以便将序列化的数据写入到内存中的字节数组。
            ObjectOutputStream out = new ObjectOutputStream(os);
            // 使用ObjectOutputStream的writeObject方法将指定的对象obj序列化
            // 并将序列化后的字节写入连接的ByteArrayOutputStream中
            out.writeObject(obj);
            return os.toByteArray();
        }catch (IOException e){
            throw new SerializerException(e.getMessage(), e);
        }
    }

    @Override
    public <T> T deserialization(byte[] data, Class<T> cls) {

        logger.info("execute jdk deserialize...");

        if(data == null){
            throw new SerializerException("deserialize data is null");
        }
        try{
            // 如果 data 不为空，接下来创建一个 ByteArrayInputStream 对象 is，用于从提供的字节数组 data 中读取数据
            ByteArrayInputStream is = new ByteArrayInputStream(data);
            // 创建一个 ObjectInputStream 对象 in，用于从 ByteArrayInputStream 中读取对象，这个对象输入流将用于反序列化操作
            ObjectInputStream in = new ObjectInputStream(is);
            // 调用ObjectInputStream的 readObject()方法 来读取并反序列化对象
            return (T) in.readObject();
        }catch (Exception e){
            throw new SerializerException(e.getMessage(), e);
        }
    }
}
