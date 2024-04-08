package io.jon.rpc.cache.result;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class CacheResultKey implements Serializable {

    private static final long serialVersionUID = -6202259281732648573L;

    // 保存缓存时的时间戳
    private long cacheTimeStamp;
    // 类名
    private String className;
    // 方法名
    private String methodName;
    // 参数类型数组
    private Class<?>[] parameterTypes;
    // 参数数组
    private Object[] parameters;
    // 版本号
    private String version;
    // 服务分组
    private String group;

    public CacheResultKey(
            String className,
            String methodName,
            Class<?>[] parameterTypes,
            Object[] parameters,
            String version,
            String group
    ){
        this.className = className;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.parameters = parameters;
        this.version = version;
        this.group = group;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;

        CacheResultKey cacheResultKey = (CacheResultKey) obj;
        return Objects.equals(className, cacheResultKey.className)
                && Objects.equals(methodName, cacheResultKey.methodName)
                && Arrays.equals(parameterTypes, cacheResultKey.parameterTypes)
                && Arrays.equals(parameters, cacheResultKey.parameters)
                && Objects.equals(version, cacheResultKey.version)
                && Objects.equals(group, cacheResultKey.group);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(className, methodName, version, group);
        result = 31 * result + Arrays.hashCode(parameterTypes);
        result = 31 * result + Arrays.hashCode(parameters);
        return result;
    }

    public long getCacheTimeStamp() {
        return cacheTimeStamp;
    }

    public void setCacheTimeStamp(long cacheTimeStamp) {
        this.cacheTimeStamp = cacheTimeStamp;
    }



}
