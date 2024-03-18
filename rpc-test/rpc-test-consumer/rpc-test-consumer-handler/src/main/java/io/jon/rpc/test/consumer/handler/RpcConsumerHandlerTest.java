package io.jon.rpc.test.consumer.handler;

import io.jon.rpc.common.exception.RegistryException;
import io.jon.rpc.constants.RpcConstants;
import io.jon.rpc.consumer.common.RpcConsumer;
import io.jon.rpc.protocol.RpcProtocol;
import io.jon.rpc.protocol.enumeration.RpcType;
import io.jon.rpc.protocol.header.RpcHeaderFactory;
import io.jon.rpc.protocol.request.RpcRequest;
import io.jon.rpc.proxy.api.callback.AsyncRPCCallback;
import io.jon.rpc.proxy.api.future.RPCFuture;
import io.jon.rpc.registry.api.RegistryService;
import io.jon.rpc.registry.api.config.RegistryConfig;
import io.jon.rpc.registry.zookeeper.ZookeeperRegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public class RpcConsumerHandlerTest {

    public static void main(String[] args) throws Exception {

        RpcConsumer consumer = RpcConsumer.getInstance();
        RPCFuture future = consumer.sendRequest(
                getRpcRequestProtocol(),
                getRegistryService(
                        "127.0.0.1:2181",
                        "zookeeper",
                        "robin"));
//        consumer.sendRequest(getRpcRequestProtocol());
//        RPCFuture future = RpcContext.getContext().getRPCFuture();
//        log.info("从服务消费者获取到的数据===>>>" + future.get());
//        consumer.sendRequest(getRpcRequestProtocol());
//        log.info("no return data");

        future.addCallback(new AsyncRPCCallback() {
            @Override
            public void onSuccess(Object result) {
                log.info("从服务消费者获取到的数据===>>>" + result);
            }

            @Override
            public void onException(Exception e) {
                log.info("抛出了异常===>>>" + e);
            }
        });
        Thread.sleep(200);
        consumer.close();
    }

    private static RpcProtocol<RpcRequest> getRpcRequestProtocol(){

        //模拟发送的数据
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        protocol.setHeader(RpcHeaderFactory.getRequestHeader(RpcConstants.REFLECT_TYPE_JDK, RpcType.REQUEST.getType()));
        RpcRequest request = new RpcRequest();
        request.setClassName("io.jon.rpc.test.api.DemoService");
        request.setGroup("jon");
        request.setMethodName("hello");
        request.setParameterTypes(new Class[]{String.class});
        request.setParameters(new Object[]{"jon"});
        request.setVersion("1.0.0");
        request.setAsync(false);
        request.setOneway(false);
        protocol.setBody(request);
        return protocol;
    }

    private static RegistryService getRegistryService(String registryAddress, String registryType, String registryLoadBalanceType) {

        if(StringUtils.isEmpty(registryType)){
            throw new IllegalArgumentException("registry type is null");
        }

        //TODO SPI扩展
        ZookeeperRegistryService registryService = new ZookeeperRegistryService();
        try{
            registryService.init(new RegistryConfig(registryAddress, registryType, registryLoadBalanceType));
        }catch (Exception e){
            log.error("RpcClient init registry service throws exception:{}", e);
            throw new RegistryException(e.getMessage(), e);
        }
        return registryService;
    }
}
