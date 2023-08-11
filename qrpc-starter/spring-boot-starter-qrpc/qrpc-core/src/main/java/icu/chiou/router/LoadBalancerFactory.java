package icu.chiou.router;

import icu.chiou.config.spi.SpiLoader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: chiou
 * createTime: 2023/8/10
 * Description: No Description
 */
public class LoadBalancerFactory {
    public static final Map<String, LoadBalancer> CACHE = new ConcurrentHashMap<>();

    public static LoadBalancer get(String serviceLoadBalancer) {
        return SpiLoader.getInstance().get(serviceLoadBalancer);
    }

    public static LoadBalancer get(String ref, String serviceLoadBalancer) {
        LoadBalancer loadBalancer = CACHE.get(ref);
        if (loadBalancer == null) {
            loadBalancer = SpiLoader.getInstance().get(serviceLoadBalancer);
            CACHE.put(ref, loadBalancer);
        }
        return loadBalancer;
    }

    public static void init() throws Exception {
        SpiLoader.getInstance().loadExtension(LoadBalancer.class);
    }
}
