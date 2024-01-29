package io.jon.rpc.test.scanner;

import io.jon.rpc.common.scanner.ClassScanner;
import io.jon.rpc.common.scanner.server.RpcReferenceScanner;
import io.jon.rpc.common.scanner.server.RpcServiceScanner;
import org.junit.Test;

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
         RpcServiceScanner.doScannerWithRpcServiceAnnotationFilterAndRegistryService("io.jon.rpc.test.scanner");
    }

    /**
     * 扫描io.jon.rpc.test.scanner包下所有标注了@RpcReference注解的类
     */
    @Test
    public void testScannerClassNameListByRpcReference() throws Exception {
        RpcReferenceScanner.doScannerWithRpcReferenceAnnotationFilter("io.jon.rpc.test.scanner");
    }

}
