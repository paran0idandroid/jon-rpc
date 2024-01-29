package io.jon.rpc.common.scanner.server;

import io.jon.rpc.annotation.RpcService;
import io.jon.rpc.common.scanner.ClassScanner;
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
    (String scanPackage) throws Exception{
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
                    //TODO 后续逻辑向注册中心注册服务元数据，同时向handlerMap中记录标注了RpcService注解的类实例
                    LOGGER.info("当前标注了@RpcService注解的类实例名称===>>> " + clazz.getName());
                    LOGGER.info("@RpcService注解上标注的属性信息如下：");
                    LOGGER.info("interfaceClass===>>> " + rpcService.interfaceClass().getName());
                    LOGGER.info("interfaceClassName===>>> " + rpcService.interfaceClassName());
                    LOGGER.info("version===>>> " + rpcService.version());
                    LOGGER.info("group===>>> " + rpcService.group());
                }
            }catch (Exception e){
                LOGGER.error("scan classes throws exception: {}", e);
            }
        });

        return handlerMap;
    }
}
