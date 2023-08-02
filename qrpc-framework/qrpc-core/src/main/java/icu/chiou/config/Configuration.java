package icu.chiou.config;

import icu.chiou.IDGenerator;
import icu.chiou.config.resolver.SpiResolver;
import icu.chiou.config.resolver.XmlResolver;
import icu.chiou.discovery.RegistryConfig;
import icu.chiou.loadbalancer.LoadBalancer;
import icu.chiou.loadbalancer.impl.RoundRobinLoadBalancer;
import icu.chiou.protection.CircuitBreaker;
import icu.chiou.protection.RateLimiter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: chiou
 * createTime: 2023/7/31
 * Description: 配置项类-配置上下文
 */
@Setter
@Getter
@Slf4j
public class Configuration {
    //配置信息-->应用名
    private String applicationName;
    //配置信息-->端口
    private int port = 8096;

    //配置信息-->注册中心
    private RegistryConfig registryConfig = new RegistryConfig("zookeeper://127.0.0.1");


    //配置信息-->id生成器
    private IDGenerator idGenerator = new IDGenerator(1L, 1L);


    //配置信息-->序列化协议
    private String serializeType = "jdk";

    //配置信息-->压缩协议
    private String compressType = "gzip";

    //配置信息-->负载均衡策略
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();

    //限流器
    public Map<SocketAddress, RateLimiter> everyIpRateLimiter = new ConcurrentHashMap<>(64);
    //断路器
    public Map<SocketAddress, CircuitBreaker> everyIpBreaker = new ConcurrentHashMap<>(64);

    public Configuration() {
        //1.成员变量默认配置项


        //2.spi机制发现相关配置项
        SpiResolver spiResolver = new SpiResolver();
        spiResolver.loadFromSpi(this);

        //3.xml的配置信息
        XmlResolver xmlResolver = new XmlResolver();
        xmlResolver.loadFromXml(this);

        //4.编程式配置项
    }
}
