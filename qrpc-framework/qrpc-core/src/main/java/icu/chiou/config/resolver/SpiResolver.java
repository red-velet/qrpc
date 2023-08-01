package icu.chiou.config.resolver;

import icu.chiou.compress.Compressor;
import icu.chiou.compress.CompressorFactory;
import icu.chiou.config.Configuration;
import icu.chiou.config.ObjectWrapper;
import icu.chiou.config.SpiHandler;
import icu.chiou.loadbalancer.LoadBalancer;
import icu.chiou.serialize.Serializer;
import icu.chiou.serialize.SerializerFactory;

import java.util.List;

/**
 * Author: chiou
 * createTime: 2023/7/31
 * Description: spi机制的解析器
 */
public class SpiResolver {
    public void loadFromSpi(Configuration configuration) {
        // 我的spi的文件中配置了很多实现（自由定义，只能配置一个实现，还是多个）
        List<ObjectWrapper<LoadBalancer>> loadBalancerWrappers = SpiHandler.getList(LoadBalancer.class);
        // 将其放入工厂
        if (loadBalancerWrappers != null && loadBalancerWrappers.size() > 0) {
            configuration.setLoadBalancer(loadBalancerWrappers.get(0).getImpl());
        }

        List<ObjectWrapper<Compressor>> compressorObjectWrappers = SpiHandler.getList(Compressor.class);
        if (compressorObjectWrappers != null) {
            compressorObjectWrappers.forEach(CompressorFactory::addCompressor);
        }

        List<ObjectWrapper<Serializer>> serializerObjectWrappers = SpiHandler.getList(Serializer.class);
        if (serializerObjectWrappers != null) {
            serializerObjectWrappers.forEach(SerializerFactory::addSerializer);
        }

    }
}
