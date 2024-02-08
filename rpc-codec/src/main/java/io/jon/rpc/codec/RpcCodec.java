package io.jon.rpc.codec;

import io.jon.rpc.serialization.api.Serialization;
import io.jon.rpc.serialization.jdk.JdkSerialization;

public interface RpcCodec {

    default Serialization getJdkSerialization(){
        return new JdkSerialization();
    }
}
