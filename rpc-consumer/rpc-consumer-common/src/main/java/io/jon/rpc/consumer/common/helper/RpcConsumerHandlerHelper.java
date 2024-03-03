package io.jon.rpc.consumer.common.helper;

import io.jon.rpc.consumer.common.handler.RpcConsumerHandler;
import io.jon.rpc.protocol.meta.ServiceMeta;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcConsumerHandlerHelper {

    private static Map<String, RpcConsumerHandler> rpcConsumerHandlerMap;

    static{
        rpcConsumerHandlerMap = new ConcurrentHashMap<>();
    }

    private static String getKey(ServiceMeta key){
        return key.getServiceAddr()
                .concat("_")
                .concat(String.valueOf(key.getServicePort()));
    }

    public static void put(ServiceMeta key, RpcConsumerHandler value){
        rpcConsumerHandlerMap.put(getKey(key), value);
    }

    public static RpcConsumerHandler get(ServiceMeta key){
        return rpcConsumerHandlerMap.get(getKey(key));
    }

    public static void closeRpcClientHandler(){
        Collection<RpcConsumerHandler> rpcClientHandlers = rpcConsumerHandlerMap.values();

        if(rpcClientHandlers != null){
            rpcClientHandlers.forEach(RpcConsumerHandler::close);
        }
        rpcConsumerHandlerMap.clear();
    }
}
