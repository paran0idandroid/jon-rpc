package io.jon.rpc.codec;

import io.jon.rpc.flow.processor.FlowPostProcessor;
import io.jon.rpc.protocol.header.RpcHeader;
import io.jon.rpc.serialization.api.Serialization;
import io.jon.rpc.spi.loader.ExtensionLoader;
import io.jon.rpc.threadpool.FlowPostProcessorThreadPool;

public interface RpcCodec {

    /**
     * 根据serializationType通过SPI获取序列句柄
     * @param serializationType 序列化方式
     * @return Serialization对象
     */
    default Serialization getSerialization(String serializationType){

        return ExtensionLoader.getExtension(Serialization.class, serializationType);
    }

    default void postFlowProcessor(FlowPostProcessor flowPostProcessor, RpcHeader header){
        //异步调用流控分析后置处理器
        FlowPostProcessorThreadPool.submit(()->{
            flowPostProcessor.postRpcHeaderProcessor(header);
        });
    }
}
