package io.jon.rpc.registry.api.config;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;


@AllArgsConstructor
@Data
public class RegistryConfig implements Serializable {

    private static final long serialVersionUID = -7248658103788758893L;

    //注册地址
    private String registryAddr;
    //注册类型
    private String registryType;
    //负载均衡类型
    private String registryLoadBalanceType;
}
