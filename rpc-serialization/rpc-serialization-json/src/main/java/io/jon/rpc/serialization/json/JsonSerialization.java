package io.jon.rpc.serialization.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.jon.rpc.common.exception.SerializerException;
import io.jon.rpc.serialization.api.Serialization;
import io.jon.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;


@SPIClass
public class JsonSerialization implements Serialization {

    private final Logger logger = LoggerFactory.getLogger(JsonSerialization.class);


    /**
     * private static ObjectMapper objMapper = new ObjectMapper();
     * 这行代码创建了一个私有的静态成员变量 objMapper，它是 ObjectMapper 类的一个实例，ObjectMapper 是 Jackson 库中的一个类，用于在 Java 对象和 JSON 数据之间进行序列化和反序列化。
     *
     * static { ... }
     * 这是一个静态初始化块，它在类加载时执行，用于对 objMapper 进行初始化和配置。
     *
     * SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     * 创建了一个日期格式化对象 dateFormat，用于指定日期在 JSON 中的序列化格式。
     *
     * objMapper.setDateFormat(dateFormat);
     * 设置 ObjectMapper 的日期格式化器，以便在序列化时使用指定的日期格式。
     *
     * objMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
     * 设置在序列化对象时，只包括非空值的属性，即排除掉值为 null 的属性。
     *
     * objMapper.enable(SerializationFeature.INDENT_OUTPUT);
     * 启用输出缩进，使生成的 JSON 数据格式化，易于阅读。
     *
     * objMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
     * 禁用自动关闭目标，这意味着在序列化时不会关闭输出流。
     *
     * objMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, false);
     * 禁用自动关闭 JSON 内容，这表示在序列化时不会关闭 JSON 内容。
     *
     * objMapper.disable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
     * 禁用在写入值之后刷新流，这可以提高性能。
     *
     * objMapper.disable(SerializationFeature.CLOSE_CLOSEABLE);
     * 禁用关闭可关闭的流，这表示在序列化时不会关闭相关的可关闭流。
     *
     * objMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
     * 禁用在序列化空对象时抛出异常的功能。
     *
     * objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
     * 禁用在遇到未知属性时抛出异常的功能。
     *
     * objMapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
     * 设置解析器忽略未定义的属性，这表示在反序列化时遇到未定义的属性会被忽略而不会抛出异常。
     */
    private static ObjectMapper objMapper = new ObjectMapper();

    static {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        objMapper.setDateFormat(dateFormat);
        objMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        objMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, false);
        objMapper.disable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
        objMapper.disable(SerializationFeature.CLOSE_CLOSEABLE);
        objMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objMapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
    }
    @Override
    public <T> byte[] serialize(T obj) {

        logger.info("execute json serialize...");

        if(obj == null){
            throw new SerializerException("serialize object is null");
        }
        byte[] bytes = new byte[0];
        try{
            bytes = objMapper.writeValueAsBytes(obj);
        }catch (JsonProcessingException e){
            throw new SerializerException(e.getMessage(), e);
        }
        return bytes;
    }

    @Override
    public <T> T deserialization(byte[] data, Class<T> cls) {
        logger.info("execute json deserialize...");
        if(data == null){
            throw new SerializerException("deserialize data is null");
        }

        T obj = null;
        try{
            obj = objMapper.readValue(data, cls);
        }catch (IOException e){
            throw new SerializerException(e.getMessage(), e);
        }
        return obj;
    }
}
