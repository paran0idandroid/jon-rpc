package io.jon.rpc.loadbalancer.consistenthash;

import io.jon.rpc.loadbalancer.api.ServiceLoadBalancer;
import io.jon.rpc.protocol.meta.ServiceMeta;
import io.jon.rpc.spi.annotation.SPIClass;
import org.apache.curator.x.discovery.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
// 高效性：
// 一致性哈希算法具有较好的性能，尤其在节点的增加或减少时，对于重新分配负载的成本较低。
// 负载均衡：
// 一致性哈希算法能够较好地在服务实例之间分配负载，使得负载能够较为均匀地分布。
// 容错性：
// 由于一致性哈希算法将服务实例映射到一个连续的区间上，因此在服务实例变化（例如节点故障）时
// 只会对其附近的区间产生影响，而不会对整个系统造成较大的影响。
// 灵活性：
// 可以根据具体的业务需求调整虚拟节点的数量，以达到更好的负载均衡效果。
//
// 一致性哈希带来的复杂性：
// 一致性哈希算法相对于其他负载均衡算法来说，实现较为复杂，特别是在处理节点的动态增加和减少时需要一定的算法实现。
// 节点分布不均匀问题：
// 尽管一致性哈希算法能够较好地解决节点增减的问题，但是在节点分布不均匀的情况下
// 仍可能导致某些节点负载较高，而另一些节点负载较低的情况。
// 单点故障：
// 如果使用的ZooKeeper服务出现故障或者网络问题，可能会影响负载均衡的正常运行。
// 数据倾斜：
// 虽然一致性哈希算法能够减少由于节点增减而带来的数据迁移，但是在某些情况下
// 仍可能出现数据倾斜的问题，即部分节点负载较高，而另一些节点负载较低。


@SPIClass
public class ZKConsistentHashLoadBalancer<T> implements ServiceLoadBalancer<T> {

    private final static int VIRTUAL_NODE_SIZE = 10;
    private final static String VIRTUAL_NODE_SPLIT = "#";

    private final Logger logger = LoggerFactory.getLogger(ZKConsistentHashLoadBalancer.class);

    @Override
    public T select(List<T> servers, int hashCode, String ip) {
        logger.info("基于Zookeeper的一致性Hash算法的负载均衡策略...");
        TreeMap<Integer, T> ring = makeConsistentHashRing(servers);
        return allocateNode(ring, hashCode);
    }

    // 该方法接收一个哈希环和哈希码作为参数，然后根据哈希码选择一个服务实例
    // 在方法中，首先查找哈希环中大于等于给定哈希码的最小键值对
    // 如果找不到，则选择哈希环中的第一个键值对
    // 如果哈希环为空，则抛出异常
    private T allocateNode(TreeMap<Integer, T> ring, int hashCode) {
        Map.Entry<Integer, T> entry = ring.ceilingEntry(hashCode);
        if (entry == null) {
            entry = ring.firstEntry();
        }
        if (entry == null){
            throw new RuntimeException("not discover useful service, please register service in registry center.");
        }
        return entry.getValue();
    }

    // 该方法接收服务实例列表作为参数，然后根据一致性哈希算法构建哈希环
    // 在方法中，对于每个服务实例，会生成一定数量的虚拟节点，并将这些虚拟节点及其对应的服务实例放入哈希环中
    private TreeMap<Integer, T> makeConsistentHashRing(List<T> servers) {
        TreeMap<Integer, T> ring = new TreeMap<>();
        for (T instance : servers) {
            for (int i = 0; i < VIRTUAL_NODE_SIZE; i++) {
                ring.put((buildServiceInstanceKey(instance) + VIRTUAL_NODE_SPLIT + i).hashCode(), instance);
            }
        }
        return ring;
    }

    private String buildServiceInstanceKey(T instance) {
        return Objects.toString(instance);
    }
}
