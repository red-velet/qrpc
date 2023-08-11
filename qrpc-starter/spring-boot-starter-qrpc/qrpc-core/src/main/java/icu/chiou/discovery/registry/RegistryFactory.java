package icu.chiou.discovery.registry;

import icu.chiou.config.spi.SpiLoader;

import java.io.IOException;

/**
 * Author: chiou
 * createTime: 2023/8/10
 * Description: 注册中心工厂
 */
public class RegistryFactory {
    public static Registry get(String registryType) {
        return SpiLoader.getInstance().get(registryType);
    }

    public static void init() throws IOException, ClassNotFoundException {
        SpiLoader.getInstance().loadExtension(Registry.class);
    }
}
