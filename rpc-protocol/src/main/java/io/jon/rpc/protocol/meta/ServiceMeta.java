package io.jon.rpc.protocol.meta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ServiceMeta implements Serializable {

    private static final long serialVersionUID = 6289735590272020366L;

    //服务名称
    private String serviceName;
    //服务版本号
    private String serviceVersion;
    //服务地址
    private String serviceAddr;
    //服务端口
    private int servicePort;
    //服务分组
    private String serviceGroup;
    //服务权重
    private int weight;

}
