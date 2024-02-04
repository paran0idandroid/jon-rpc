package io.jon.rpc.protocol.request;

import io.jon.rpc.protocol.base.RpcMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RpcRequest extends RpcMessage {

    private static final long serialVersionUID = 5555776886650396129L;

    //类名
    private String className;

    //方法名
    private String methodName;

    //参数类型数组
    private Class<?>[] parameterTypes;

    //参数数组
    private Object[] parameters;

    //版本号
    private String version;

    //服务分组
    private String group;
}
