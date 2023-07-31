package icu.chiou.watcher;

import icu.chiou.NettyBootstrapInitializer;
import icu.chiou.QRpcBootstrap;
import icu.chiou.discovery.Registry;
import icu.chiou.loadbalancer.LoadBalancer;
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
        // 当前的阶段是否发生了变化
        if (event.getType() == Event.EventType.NodeChildrenChanged) {
            if (log.isDebugEnabled()) {
                log.debug("检测到服务【{}】下有节点上/下线，将重新拉取服务列表...", event.getPath());
            }
            // 拉取最新的服务
            String serviceName = getServiceName(event.getPath());
            Registry registry = QRpcBootstrap.getInstance().getRegistry();
            List<InetSocketAddress> addresses = registry.lookup(serviceName);
            for (InetSocketAddress address : addresses) {
                //上线新服务: 最新服务列表有addresses-address,本地缓存CHANNEL_CACHE没有
                if (!QRpcBootstrap.CHANNEL_CACHE.containsKey(address)) {
                    // 根据地址建立连接，并且缓存
                    Channel channel = null;
                    try {
                        channel = NettyBootstrapInitializer.getBootstrap()
                                .connect(address).sync().channel();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    QRpcBootstrap.CHANNEL_CACHE.put(address, channel);
                }

            }

            // 下线服务: 最新服务列表addresses-address没有,本地缓存有CHANNEL_CACHE
            // 处理下线的节点 可能会在CHANNEL_CACHE 不在address
            for (Map.Entry<InetSocketAddress, Channel> entry : QRpcBootstrap.CHANNEL_CACHE.entrySet()) {
                if (!addresses.contains(entry.getKey())) {
                    QRpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                }
            }

            // 获得负载均衡器，进行重新的loadBalance
            LoadBalancer loadBalancer = QRpcBootstrap.getInstance().getConfiguration().getLoadBalancer();
            loadBalancer.reloadBalance(serviceName, addresses);
        }
    }

    private String getServiceName(String path) {
        String[] split = path.split("/");
        return split[split.length - 1];
    }
}
