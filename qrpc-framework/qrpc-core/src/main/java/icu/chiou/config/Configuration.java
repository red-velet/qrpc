package icu.chiou.config;

import icu.chiou.IDGenerator;
import icu.chiou.ProtocolConfig;
import icu.chiou.discovery.RegistryConfig;
import icu.chiou.loadbalancer.LoadBalancer;
import icu.chiou.loadbalancer.impl.RoundRobinLoadBalancer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: chiou
 * createTime: 2023/7/31
 * Description: 配置项类
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

    //配置信息-->序列化协议
    private ProtocolConfig protocolConfig = new ProtocolConfig("jdk");


    //配置信息-->id生成器
    private IDGenerator idGenerator = new IDGenerator(1L, 1L);


    //配置信息-->序列化协议
    private String serializeType = "jdk";


    //配置信息-->压缩协议
    private String compressType = "gzip";

    //配置信息-->负载均衡策略
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();

    public Configuration() {
        // 读取xml获得上边的信息

    }
}