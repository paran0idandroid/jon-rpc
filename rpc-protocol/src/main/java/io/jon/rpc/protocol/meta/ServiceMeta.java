package io.jon.rpc.protocol.meta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ServiceMeta implements Serializable {

    private static final long serialVersionUID = 6289735590272020366L;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务版本号
     */
    private String serviceVersion;

    /**
     * 服务地址
     */
    private String serviceAddr;

    /**
     * 服务端口
     */
    private int servicePort;

    /**
     * 服务分组
     */
    private String serviceGroup;

    /**
     * 服务提供者实例的权重
     */
    private int weight;

    public ServiceMeta(String serviceName, String serviceVersion, String serviceGroup, String serviceAddr, int servicePort, int weight) {
        this.serviceName = serviceName;
        this.serviceVersion = serviceVersion;
        this.serviceAddr = serviceAddr;
        this.servicePort = servicePort;
        this.serviceGroup = serviceGroup;
        this.weight = weight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, serviceVersion, serviceAddr, servicePort, serviceGroup, weight);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceMeta serviceMeta = (ServiceMeta) o;
        return Objects.equals(serviceName, serviceMeta.serviceName)
                && Objects.equals(serviceVersion, serviceMeta.serviceVersion)
                && Objects.equals(serviceAddr, serviceMeta.serviceAddr)
                && servicePort == serviceMeta.servicePort
                && Objects.equals(serviceGroup, serviceMeta.serviceGroup)
                && weight == serviceMeta.weight;
    }
}
