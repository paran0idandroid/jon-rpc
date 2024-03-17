package io.jon.rpc.constants;

public class RpcConstants {

    /**
     * 魔数
     */
    public static final short MAGIC = 0x10;

    /**
     * 消息头，固定32个字节
     */
    public static final int HEADER_TOTAL_LEN = 32;

    /**
     * REFLECT_TYPE_JDK
     */
    public static final String REFLECT_TYPE_JDK = "jdk";

    /**
     * REFLECT_TYPE_CGLIB
     */
    public static final String REFLECT_TYPE_CGLIB = "cglib";

    /**
     * jdk序列化
     */
    public static final String SERIALIZATION_JDK = "jdk";

    /**
     * jdk序列化
     */
    public static final String SERIALIZATION_JSON = "json";

    /**
     * hessian2序列化
     */
    public static final String SERIALIZATION_HESSIAN2 = "hessian2";

    /**
     * 基于随机算法的负载均衡
     */
    public static final String SERVICE_LOAD_BALANCER_RANDOM = "random";
}
