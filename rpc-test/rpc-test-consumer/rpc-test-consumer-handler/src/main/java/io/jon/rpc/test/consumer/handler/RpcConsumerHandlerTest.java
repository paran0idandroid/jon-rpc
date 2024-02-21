package io.jon.rpc.test.consumer.handler;

import io.jon.rpc.constants.RpcConstants;
import io.jon.rpc.consumer.common.RpcConsumer;
import io.jon.rpc.consumer.common.context.RpcContext;
import io.jon.rpc.consumer.common.future.RPCFuture;
import io.jon.rpc.protocol.RpcProtocol;
import io.jon.rpc.protocol.enumeration.RpcType;
import io.jon.rpc.protocol.header.RpcHeaderFactory;
import io.jon.rpc.protocol.request.RpcRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcConsumerHandlerTest {

    public static void main(String[] args) throws Exception {

        RpcConsumer consumer = RpcConsumer.getInstance();
//        consumer.sendRequest(getRpcRequestProtocol());
//        RPCFuture future = RpcContext.getContext().getRPCFuture();
//        RPCFuture future = consumer.sendRequest(getRpcRequestProtocol());
//        log.info("从服务消费者获取到的数据===>>>" + future.get());
        consumer.sendRequest(getRpcRequestProtocol());
        log.info("no return data");
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
        request.setOneway(true);
        protocol.setBody(request);
        return protocol;
    }
}
