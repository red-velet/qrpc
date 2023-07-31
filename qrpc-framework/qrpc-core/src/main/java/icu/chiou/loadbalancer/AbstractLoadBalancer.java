package icu.chiou.loadbalancer;

import icu.chiou.QRpcBootstrap;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: chiou
 * createTime: 2023/7/30
 * Description: 负载均衡器的模板方法
 */
public abstract class AbstractLoadBalancer implements LoadBalancer {

    private Map<String, Selector> cache = new ConcurrentHashMap<>(8);


    @Override
    public InetSocketAddress selectAvailableService(String name) {
        //轮询的负载均衡算法实现
        //通过服务名,拉取服务列表缓存
        Selector selector = cache.get(name);
        if (null == selector) {
            List<InetSocketAddress> serviceList = QRpcBootstrap.getInstance().getRegistry().lookup(name);
            selector = getSelector(serviceList);
            cache.put(name, selector);
        }

        //提供一些算法,获取一个合适的服务
        return selector.getNext();
    }

    /**
     * 子类进行拓展: 获取一个负载均衡器
     *
     * @param serviceList 服务列表
     * @return 负载均衡算法选择器
     */
    protected abstract Selector getSelector(List<InetSocketAddress> serviceList);

    @Override
    public synchronized void reloadBalance(String serviceName, List<InetSocketAddress> addresses) {
        // 我们可以根据新的服务列表生成新的selector
        cache.put(serviceName, getSelector(addresses));
    }
}
