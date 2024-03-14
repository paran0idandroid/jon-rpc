package io.jon.rpc.serialization.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import io.jon.rpc.common.exception.SerializerException;
import io.jon.rpc.serialization.api.Serialization;
import io.jon.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@SPIClass
public class KryoSerialization implements Serialization {

    private final Logger logger = LoggerFactory.getLogger(KryoSerialization.class);
    @Override
    public <T> byte[] serialize(T obj) {
        logger.info("execute kryo serialize...");
        if(obj == null){
            throw new SerializerException("serialize object is null");
        }

        // 创建了一个 Kryo 对象实例 Kryo 是一个高性能的 Java 序列化库
        Kryo kryo = new Kryo();

        /**
         * 序列化结果更加独立:
         * 当序列化时不使用引用，每个对象都会被完整地序列化，不会与其他对象共享状态。
         * 这意味着序列化结果不会受到其他对象的影响，使得它们更加独立和可移植。
         *
         * 减少序列化数据大小:
         * 使用引用序列化时，如果多个对象引用相同的对象，只有一个实例会被序列化 其他引用则会使用标识符来表示
         * 而不使用引用时，每个对象都会完整地序列化，这可能会增加序列化数据的大小
         * 但有时会减少序列化数据的复杂性，从而可能减少序列化后的数据大小。
         *
         * 减少序列化/反序列化过程中的依赖:
         * 在某些情况下，对象之间的引用关系可能会导致序列化和反序列化的依赖关系变得复杂
         * 不使用引用可以简化这种依赖关系，使得序列化和反序列化过程更加直观和可控
         *
         * 避免共享状态带来的潜在问题:
         * 当多个对象共享同一个对象的状态时，可能会导致一些意外的行为或者难以排查的 bug
         * 不使用引用可以避免这种共享状态带来的潜在问题，使得程序的行为更加可预测和稳定
         *
         * 然而，需要注意的是，不使用引用也可能会导致一些问题，
         * 比如增加序列化数据的大小、降低序列化/反序列化的效率等
         * 因此，在选择是否使用引用来序列化对象时，需要根据具体的需求和场景来进行权衡和选择
         */
        // 设置 Kryo 实例不使用引用来序列化对象
        // 这意味着，如果对象被多次引用，Kryo 会多次序列化对象而不是引用相同的对象
        kryo.setReferences(false);


        /**
         * 注册对象类可以在一定程度上加快序列化和反序列化的速度，原因如下：
         * 定制序列化器：
         * 注册对象类时可以指定一个定制的序列化器
         * 这个序列化器可能比 Kryo 默认的序列化方式更高效
         * 例如，可以针对对象的特定结构和需求编写一个专门的序列化器
         * 这样可以更好地优化序列化和反序列化的性能
         *
         * 减少查找时间：
         * 注册对象类可以减少 Kryo 在序列化和反序列化过程中查找适合类的序列化器的时间
         * 当 Kryo 知道要处理的类时，它就可以直接使用注册的序列化器
         * 而不需要进行类的检索和匹配 从而节省时间
         *
         * 避免默认机制：
         * 当不注册对象类时，Kryo 序列化库可能会使用一种通用的默认序列化方式来处理对象
         * 这种方式可能不够高效。而注册了对象类后，可以使用一个更适合特定类的定制序列化器
         * 从而提高序列化和反序列化的效率
         *
         * 综上所述，注册对象类可以提高序列化和反序列化的速度
         * 因为它可以使用定制的序列化器和减少查找时间，从而优化序列化和反序列化的性能。
         */
        // 注册要序列化的对象类型和使用的序列化器
        // 这里使用了 JavaSerializer，它是 Kryo 库的一种默认序列化器，用于序列化 Java 对象
        kryo.register(obj.getClass(), new JavaSerializer());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 创建了一个 Kryo 输出流 Output，它将对象序列化后的数据写入到 ByteArrayOutputStream 中
        Output output = new Output(baos);
        // 将给定的对象 obj 序列化为字节数据，并写入到 Output 中
        // writeClassAndObject 方法用于序列化对象及其类型信息
        kryo.writeClassAndObject(output, obj);
        output.flush();
        output.close();
        byte[] bytes = baos.toByteArray();
        try{
            baos.flush();
            baos.close();
        }catch (IOException e){
            throw new SerializerException(e.getMessage(), e);
        }
        return bytes;
    }

    @Override
    public <T> T deserialization(byte[] data, Class<T> cls) {

        logger.info("execute kryo deserialize...");
        if(data == null){
            throw new SerializerException("deserialize data is null");
        }

        Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.register(cls, new JavaSerializer());
        // ByteArrayInputStream 用于从字节数组中读取数据
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        // Input 对象用于读取序列化后的数据，并提供给 Kryo 进行反序列化操作
        Input input = new Input(bais);
        // 调用了 Kryo 对象的 readClassAndObject 方法，从输入流中读取数据并反序列化为对象
        // (T) 表示将结果转换为泛型类型 T
        // 这个方法会读取对象的类型信息，并根据类型信息使用注册的序列化器来反序列化对象
        return (T) kryo.readClassAndObject(input);
    }
}
