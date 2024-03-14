package io.jon.rpc.serialization.protostuff;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import io.jon.rpc.common.exception.SerializerException;
import io.jon.rpc.serialization.api.Serialization;
import io.jon.rpc.spi.annotation.SPIClass;
import org.objenesis.ObjenesisStd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SPIClass
public class ProtoStuffSerialization implements Serialization {

    private final Logger logger = LoggerFactory.getLogger(ProtoStuffSerialization.class);

    private Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

    // 创建了一个 ObjenesisStd 对象，并根据需要启用了缓存
    // 通过 objenesis 对象，可以在需要实例化对象时，直接实例化而无需调用对象的构造函数
    private ObjenesisStd objenesis = new ObjenesisStd(true);


    /**
     * @SuppressWarnings("unchecked") 是 Java 中的一个注解
     * 用于告诉编译器在特定地方禁止警告信息的输出
     * 在这种情况下，警告通常是由未经检查的转换引起的
     *
     * 在 Java 中，泛型是用于提供编译时类型安全性的机制
     * 但在某些情况下，由于类型擦除（type erasure）等原因
     * 编译器无法完全检查泛型类型的安全性
     * 例如，在使用泛型时，如果进行类型转换并且编译器无法检查该转换的类型安全性
     * 则会产生 unchecked 警告
     *
     * 通过使用 @SuppressWarnings("unchecked") 注解
     * 告诉编译器在被注解的代码块中，不要输出未经检查的转换的警告
     * 这并不是说你可以不必关心类型安全性，而是在你确定类型转换是安全的情况下
     * 你可以使用这个注解来隐藏警告
     *
     * 需要注意的是，使用 @SuppressWarnings("unchecked") 注解时
     * 应当尽量确保类型转换是安全的，以避免在运行时出现类型相关的错误
     */
    @SuppressWarnings("unchecked")
    // Schema 在 Protostuff 库中用于描述数据结构和序列化格式的信息
    private <T> Schema<T> getSchema(Class<T> cls){

        // 首先尝试从缓存的 Schema Map 中获取给定类 cls 的 Schema 对象
        // cachedSchema 是一个 Map，用于缓存类对象和对应的 Schema 对象的映射关系
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if(schema == null){
            // 使用 RuntimeSchema 的静态方法 createFrom(cls) 来创建给定类 cls 的 Schema 对象
            schema = RuntimeSchema.createFrom(cls);
            if(schema != null){
                cachedSchema.put(cls, schema);
            }
        }
        return schema;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> byte[] serialize(T obj) {
        logger.info("execute protostuff serialize...");

        if(obj == null){
            throw new SerializerException("serialize object is null");
        }

        Class<T> cls = (Class<T>) obj.getClass();
        // 用于临时存储序列化过程中产生的字节数据
        // LinkedBuffer 是 Protostuff 库中的一个缓冲区实现
        // 它能够动态地分配内存，并在序列化完成后释放
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try{
            Schema<T> schema = getSchema(cls);

            // 将对象 obj 使用给定的 Schema 对象 schema 进行序列化 并将序列化后的字节数组返回
            // buffer 参数用于存储序列化过程中产生的字节数据，以减少内存分配和复制操作，提高性能
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        }catch (Exception e){
            throw new SerializerException(e.getMessage(), e);
        }finally {
            buffer.clear();
        }
    }

    @Override
    public <T> T deserialization(byte[] data, Class<T> cls) {
        logger.info("execute protostuff deserialize...");
        if(data == null){
            throw new SerializerException("deserialize data is null");
        }

        try{
            T message = (T) objenesis.newInstance(cls);
            Schema<T> schema = getSchema(cls);
            // 将字节数组 data 中的数据合并到对象 message 中
            // 这个方法会根据提供的 Schema 对象 schema
            // 将字节数组中的数据解析并填充到对象 message 的对应属性中
            ProtostuffIOUtil.mergeFrom(data, message, schema);
            return message;
        }catch (Exception e){
            throw new SerializerException(e.getMessage(), e);
        }
    }
}
