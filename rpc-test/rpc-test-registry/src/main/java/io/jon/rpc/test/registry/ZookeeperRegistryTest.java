package io.jon.rpc.test.registry;

import io.jon.rpc.protocol.meta.ServiceMeta;
import io.jon.rpc.registry.api.RegistryService;
import io.jon.rpc.registry.api.config.RegistryConfig;
import io.jon.rpc.registry.zookeeper.ZookeeperRegistryService;
import org.junit.Before;
import org.junit.Test;

public class ZookeeperRegistryTest {

    private RegistryService registryService;
    private ServiceMeta serviceMeta;

    @Before
    public void init() throws Exception{

        RegistryConfig registryConfig = new RegistryConfig("127.0.0.1:2181", "zookeeper", "robin");
        this.registryService = new ZookeeperRegistryService();
        this.registryService.init(registryConfig);
        this.serviceMeta = new ServiceMeta(
                ZookeeperRegistryService.class.getName(),
                "1.0.0",
                "127.0.0.1",
                8080,
                "jon");
    }

    @Test
    public void testRegister() throws Exception{

        this.registryService.register(serviceMeta);
    }

    @Test
    public void testUnRegister() throws Exception{
        this.registryService.unRegister(serviceMeta);
    }

    @Test
    public void testDiscovery() throws Exception{
        this.registryService.discovery(
                RegistryService.class.getName(),
                "jon".hashCode()
        );
    }

    @Test
    public void testDestroy() throws Exception{
        this.registryService.destroy();
    }
}
