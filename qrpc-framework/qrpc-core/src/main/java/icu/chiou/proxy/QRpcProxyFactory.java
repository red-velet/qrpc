package icu.chiou.proxy;

import icu.chiou.QRpcBootstrap;
import icu.chiou.ReferenceConfig;
import icu.chiou.discovery.RegistryConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: chiou
 * createTime: 2023/8/2
 * Description: No Description
 */
public class QRpcProxyFactory {
    public static Map<Class<?>, Object> cache = new ConcurrentHashMap<>(64);

    public static <T> T getProxy(Class<T> clazz) {
        Object bean = cache.get(clazz);
        if (bean != null) {
            return (T) bean;
        }

        ReferenceConfig<T> reference = new ReferenceConfig<>();
        reference.setInterface(clazz);

        QRpcBootstrap.getInstance()
                .application("first-qrpc-consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("json")
                .compress("gzip")
                .group("primary")
                .reference(reference);
        T t = reference.get();
        cache.put(clazz, t);
        return t;
    }
}
