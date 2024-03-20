package io.jon.rpc.provider.common.scanner;

import io.jon.rpc.annotation.RpcService;
import io.jon.rpc.common.helper.RpcServiceHelper;
import io.jon.rpc.common.scanner.ClassScanner;
import io.jon.rpc.constants.RpcConstants;
import io.jon.rpc.protocol.meta.ServiceMeta;
import io.jon.rpc.registry.api.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @RpcService注解扫描器
 */
public class RpcServiceScanner extends ClassScanner {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(RpcServiceScanner.class);

    /**
     * 扫描指定包下的类，并筛选使用@RpcService注解标注的类
     */
    public static Map<String, Object> doScannerWithRpcServiceAnnotationFilterAndRegistryService
    (String host, int port, String scanPackage, RegistryService registryService) throws Exception{
       Map<String, Object> handlerMap = new HashMap<>();
        List<String> classNameList = getClassNameList(scanPackage, true);
        if(classNameList == null || classNameList.isEmpty()){
            return handlerMap;
        }

        classNameList.stream().forEach(className -> {
            try{
                Class<?> clazz = Class.forName(className);
                RpcService rpcService = clazz.getAnnotation(RpcService.class);
                if(rpcService != null){

                    //优先使用interfaceClass，如果为空则使用interfaceClassName
                    ServiceMeta serviceMeta = new ServiceMeta(
                            getServiceName(rpcService),
                            rpcService.version(),
                            host,
                            port,
                            rpcService.group(),
                            getWeight(rpcService.weight())
                    );

                    registryService.register(serviceMeta);
                    handlerMap.put(
                            RpcServiceHelper.buildServiceKey(
                                    serviceMeta.getServiceName(),
                                    serviceMeta.getServiceVersion(),
                                    serviceMeta.getServiceGroup()),
                            clazz.newInstance()
                    );
                }
            }catch (Exception e){
                LOGGER.error("scan classes throws exception: {}", e);
            }
        });

        return handlerMap;
    }

    /**
     * 获取serviceName
     */
    private static String getServiceName(RpcService rpcService){
        //优先使用interfaceClass
        Class clazz = rpcService.interfaceClass();
        if (clazz == null || clazz == void.class){
            return rpcService.interfaceClassName();
        }
        String serviceName = clazz.getName();
        if (serviceName == null || serviceName.trim().isEmpty()){
            serviceName = rpcService.interfaceClassName();
        }
        return serviceName;
    }

    /**
     * 将传入的服务权重范围限制在1~100
     */
    private static int getWeight(int weight){

        if (weight < RpcConstants.SERVICE_WEIGHT_MIN){
            weight = RpcConstants.SERVICE_WEIGHT_MIN;
        }
        if (weight > RpcConstants.SERVICE_WEIGHT_MAX){
            weight = RpcConstants.SERVICE_WEIGHT_MAX;
        }
        return weight;
    }
}
