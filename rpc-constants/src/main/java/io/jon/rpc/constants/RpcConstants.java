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
     * json序列化
     */
    public static final String SERIALIZATION_JSON = "json";

    /**
     * hessian2序列化
     */
    public static final String SERIALIZATION_HESSIAN2 = "hessian2";

    /**
     * hessian2序列化
     */
    public static final String SERIALIZATION_PROTOSTUFF = "protostuff";

    /**
     * 基于随机算法的负载均衡
     */
    public static final String SERVICE_LOAD_BALANCER_RANDOM = "random";

    /**
     * 最小权重
     */
    public static final int SERVICE_WEIGHT_MIN = 1;

    /**
     * 最大权重
     */
    public static final int SERVICE_WEIGHT_MAX = 100;

    /**
     * 增强型负载均衡前缀
     */
    public static final String SERVICE_ENHANCED_LOAD_BALANCER_PREFIX = "enhanced_";

    /**
     * 心跳ping消息
     */
    public static final String HEARTBEAT_PING = "ping";

    /**
     * 心跳pong消息
     */
    public static final String HEARTBEAT_PONG = "pong";

    /**
     * decoder
     */
    public static final String CODEC_DECODER = "decoder";

    /**
     * encoder
     */
    public static final String CODEC_ENCODER = "encoder";

    /**
     * handler
     */
    public static final String CODEC_HANDLER = "handler";

    /**
     * server-idle-handler
     */
    public static final String CODEC_SERVER_IDLE_HANDLER = "server-idle-handler";

    /**
     * client-idle-handler
     */
    public static final String CODEC_CLIENT_IDLE_HANDLER = "client-idle-handler";

}
