package io.jon.rpc.test.scanner;

import io.jon.rpc.common.scanner.ClassScanner;
import io.jon.rpc.common.scanner.server.RpcReferenceScanner;
import io.jon.rpc.protocol.meta.ServiceMeta;
import io.jon.rpc.provider.common.scanner.RpcServiceScanner;
import io.jon.rpc.registry.api.RegistryService;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class ScannerTest {


    /**
     * 扫描io.jon.rpc.test.scanner包下所有的类
     */
    @Test
    public void testScannerClassNameList() throws Exception {
        List<String> classNameList = ClassScanner.getClassNameList("io.jon.rpc.test.scanner", true);
        classNameList.forEach(System.out::println);
    }

    /**
     * 扫描io.jon.rpc.test.scanner包下所有标注了@RpcService注解的类
     */
    @Test
    public void testScannerClassNameListByRpcService() throws Exception {
         RpcServiceScanner.doScannerWithRpcServiceAnnotationFilterAndRegistryService(
                 "",
                 123,
                 "",
                 new RegistryService() {
                     @Override
                     public void register(ServiceMeta serviceMeta) throws Exception {

                     }

                     @Override
                     public void unRegister(ServiceMeta serviceMeta) throws Exception {

                     }

                     @Override
                     public ServiceMeta discovery(String serviceName, int invokerHashCode, String sourceIp) throws Exception {
                         return null;
                     }

                     @Override
                     public void destroy() throws IOException {

                     }

                     @Override
                     public ServiceMeta select(List<ServiceMeta> serviceMetaList, int invokerHashCode, String sourceIp) {
                         return null;
                     }

                     @Override
                     public List<ServiceMeta> discoveryAll() throws Exception {
                         return null;
                     }
                 });
    }

    /**
     * 扫描io.jon.rpc.test.scanner包下所有标注了@RpcReference注解的类
     */
    @Test
    public void testScannerClassNameListByRpcReference() throws Exception {
        RpcReferenceScanner.doScannerWithRpcReferenceAnnotationFilter("io.jon.rpc.test.scanner");
    }

}
