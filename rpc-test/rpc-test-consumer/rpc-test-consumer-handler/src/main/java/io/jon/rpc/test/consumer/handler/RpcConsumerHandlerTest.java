package io.jon.rpc.test.consumer.handler;

import io.jon.rpc.common.exception.RegistryException;
import io.jon.rpc.constants.RpcConstants;
import io.jon.rpc.consumer.common.RpcConsumer;
import io.jon.rpc.consumer.common.context.RpcContext;
import io.jon.rpc.protocol.RpcProtocol;
import io.jon.rpc.protocol.enumeration.RpcType;
import io.jon.rpc.protocol.header.RpcHeaderFactory;
import io.jon.rpc.protocol.request.RpcRequest;
import io.jon.rpc.proxy.api.callback.AsyncRPCCallback;
import io.jon.rpc.proxy.api.future.RPCFuture;
import io.jon.rpc.registry.api.RegistryService;
import io.jon.rpc.registry.api.config.RegistryConfig;
import io.jon.rpc.registry.zookeeper.ZookeeperRegistryService;
import io.jon.rpc.spi.loader.ExtensionLoader;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class RpcConsumerHandlerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcConsumerHandlerTest.class);


    public static void main(String[] args) throws Exception {
        RpcConsumer consumer = RpcConsumer.getInstance()
                .setHeartbeatInterval(300000)
                .setRetryInterval(10000)
                .setDirectServerUrl("127.0.0.1:27880")
                .setEnableDirectServer(true)
                .setRetryTimes(3)
                .setScanNotActiveChannelInterval(60000);
        RPCFuture rpcFuture = consumer.sendRequest(getRpcRequestProtocol(), getRegistryService("127.0.0.1:2181", "zookeeper", "random"));
        rpcFuture.addCallback(new AsyncRPCCallback() {
            @Override
            public void onSuccess(Object result) {
                LOGGER.info("从服务消费者获取到的数据===>>>" + result);
            }

            @Override
            public void onException(Exception e) {
                LOGGER.info("抛出了异常===>>>" + e);
            }
        });
        Thread.sleep(200);
        consumer.close();
    }

    public static void mainAsync(String[] args) throws Exception {
        RpcConsumer consumer = RpcConsumer.getInstance()
                .setHeartbeatInterval(300000)
                .setRetryInterval(10000)
                .setDirectServerUrl("127.0.0.1:27880")
                .setEnableDirectServer(true)
                .setRetryTimes(3)
                .setScanNotActiveChannelInterval(60000);
        consumer.sendRequest(getRpcRequestProtocol(), getRegistryService("127.0.0.1:2181", "zookeeper", "random"));
        RPCFuture future = RpcContext.getContext().getRPCFuture();
        LOGGER.info("从服务消费者获取到的数据===>>>" + future.get());
        consumer.close();
    }

    //TODO 修改
    private static RegistryService getRegistryService(String registryAddress, String registryType, String registryLoadBalanceType) {
        if (StringUtils.isEmpty(registryType)){
            throw new IllegalArgumentException("registry type is null");
        }
        RegistryService registryService = ExtensionLoader.getExtension(RegistryService.class, registryType);
        try {
            registryService.init(new RegistryConfig(registryAddress, registryType, registryLoadBalanceType));
        } catch (Exception e) {
            LOGGER.error("RpcClient init registry service throws exception:{}", e);
            throw new RegistryException(e.getMessage(), e);
        }
        return registryService;
    }

    public static void mainSync(String[] args) throws Exception {
        RpcConsumer consumer = RpcConsumer.getInstance()
                .setHeartbeatInterval(300000)
                .setRetryInterval(10000)
                .setDirectServerUrl("127.0.0.1:27880")
                .setEnableDirectServer(true)
                .setRetryTimes(3)
                .setScanNotActiveChannelInterval(60000);
        RPCFuture future = consumer.sendRequest(getRpcRequestProtocol(), getRegistryService("127.0.0.1:2181", "zookeeper", "random"));
        LOGGER.info("从服务消费者获取到的数据===>>>" + future.get());
        consumer.close();
    }

    private static RpcProtocol<RpcRequest> getRpcRequestProtocol(){
        //模拟发送数据
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<RpcRequest>();
        protocol.setHeader(RpcHeaderFactory.getRequestHeader("jdk", RpcType.REQUEST.getType()));
        RpcRequest request = new RpcRequest();
        request.setClassName("io.jon.rpc.test.api.DemoService");
        request.setGroup("jon");
        request.setMethodName("hello");
        request.setParameters(new Object[]{"jon"});
        request.setParameterTypes(new Class[]{String.class});
        request.setVersion("1.0.0");
        request.setAsync(false);
        request.setOneway(false);
        protocol.setBody(request);
        return protocol;
    }

    private static RpcProtocol<RpcRequest> getRpcRequestProtocolAsync(){
        //模拟发送数据
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<RpcRequest>();
        protocol.setHeader(RpcHeaderFactory.getRequestHeader("jdk", RpcType.REQUEST.getType()));
        RpcRequest request = new RpcRequest();
        request.setClassName("io.jon.rpc.test.api.DemoService");
        request.setGroup("jon");
        request.setMethodName("hello");
        request.setParameters(new Object[]{"jon"});
        request.setParameterTypes(new Class[]{String.class});
        request.setVersion("1.0.0");
        request.setAsync(true);
        request.setOneway(false);
        protocol.setBody(request);
        return protocol;
    }

    private static RpcProtocol<RpcRequest> getRpcRequestProtocolSync(){
        //模拟发送数据
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<RpcRequest>();
        protocol.setHeader(RpcHeaderFactory.getRequestHeader("jdk", RpcType.REQUEST.getType()));
        RpcRequest request = new RpcRequest();
        request.setClassName("io.jon.rpc.test.api.DemoService");
        request.setGroup("jon");
        request.setMethodName("hello");
        request.setParameters(new Object[]{"jon"});
        request.setParameterTypes(new Class[]{String.class});
        request.setVersion("1.0.0");
        request.setAsync(false);
        request.setOneway(false);
        protocol.setBody(request);
        return protocol;
    }
}

