package icu.chiou.discovery.watcher;

import icu.chiou.core.QRpcApplicationContext;
import icu.chiou.core.QRpcProperties;
import icu.chiou.discovery.registry.Registry;
import icu.chiou.discovery.registry.RegistryFactory;
import icu.chiou.netty.NettyBootstrapInitializer;
import icu.chiou.router.LoadBalancer;
import icu.chiou.router.LoadBalancerFactory;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * Author: chiou
 * createTime: 2023/7/31
 * Description: No Description
 */
@Slf4j
public class UpAndDownWatcher implements Watcher {
    @Override
    public void process(WatchedEvent event) {
        log.debug("⬆️⬇️enter process 检测到服务【{}】下有节点上/下线，将重新拉取服务列表...", event.getPath());

        // 当前的阶段是否发生了变化
        if (event.getType() == Event.EventType.NodeChildrenChanged) {
            if (log.isDebugEnabled()) {
                log.debug("⬆️⬇️检测到服务【{}】下有节点上/下线，将重新拉取服务列表...", event.getPath());
            }
            // 拉取最新的服务
            String serviceName = getServiceName(event.getPath());
            Registry registry = RegistryFactory.get(QRpcProperties.getInstance().getRegistryType());
            List<InetSocketAddress> addresses = registry.lookup(serviceName, QRpcProperties.getInstance().getGroup());
            for (InetSocketAddress address : addresses) {
                //上线新服务: 最新服务列表有addresses-address,本地缓存CHANNEL_CACHE没有
                if (!QRpcApplicationContext.CHANNEL_CACHE.containsKey(address)) {
                    // 根据地址建立连接，并且缓存
                    Channel channel = null;
                    try {
                        channel = NettyBootstrapInitializer.getBootstrap()
                                .connect(address).sync().channel();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    QRpcApplicationContext.CHANNEL_CACHE.put(address, channel);
                }

            }

            // 下线服务: 最新服务列表addresses-address没有,本地缓存有CHANNEL_CACHE
            // 处理下线的节点 可能会在CHANNEL_CACHE 不在address
            for (Map.Entry<InetSocketAddress, Channel> entry : QRpcApplicationContext.CHANNEL_CACHE.entrySet()) {
                if (!addresses.contains(entry.getKey())) {
                    QRpcApplicationContext.CHANNEL_CACHE.remove(entry.getKey());
                }
            }

            // 获得所有的负载均衡器，进行重新的loadBalance
            for (Map.Entry<String, LoadBalancer> entry : LoadBalancerFactory.CACHE.entrySet()) {
                LoadBalancer value = entry.getValue();
                value.reloadBalance(serviceName, addresses);
            }

        }
    }

    private String getServiceName(String path) {
        String[] split = path.split("/");
        return split[split.length - 2];
    }
}
