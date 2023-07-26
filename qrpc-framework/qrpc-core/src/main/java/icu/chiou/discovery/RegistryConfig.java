package icu.chiou.discovery;

import icu.chiou.constants.Constant;
import icu.chiou.discovery.impl.NacosRegistry;
import icu.chiou.discovery.impl.ZookeeperRegistry;
import icu.chiou.exceptions.DiscoveryException;

/**
 * Author: chiou
 * createTime: 2023/7/23
 * Description: 配置类-注册中心配置
 */
public class RegistryConfig {
    //约定优于配置,通过connectString获取注册中心类型
    private String connectString;

    public RegistryConfig(String connectString) {
        this.connectString = connectString;
    }

    /**
     * 简单工厂根据注入的参数不同返回不同的注册中心实例
     *
     * @return 注册中心实例
     */
    public Registry getRegistry() {
        //1.获取类型
        String registryType = getRegistryConfigParam(connectString, true).toLowerCase().trim();
        String connectString = getRegistryConfigParam(this.connectString, false);
        //2.根据类型返回对应实例
        if ("zookeeper".equals(registryType)) {
            //注册中心是zookeeper
            return new ZookeeperRegistry(connectString, Constant.DEFAULT_ZK_TIMEOUT);
        } else if ("nacos".equals(registryType)) {
            //注册中心是nacos
            return new NacosRegistry(connectString, Constant.DEFAULT_ZK_TIMEOUT);
        }
        //获取不到注册中心
        throw new DiscoveryException("未发现合适的注册中心");
    }

    /**
     * 获取注册中心配置参数
     *
     * @param connectString 连接参数
     * @param ofType        flag
     * @return 参数
     */
    public String getRegistryConfigParam(String connectString, boolean ofType) {
        String[] typeAndHost = connectString.split("://");
        if (typeAndHost.length != 2) {
            throw new RuntimeException("设置的注册中心url格式不合法");
        }
        if (ofType) {
            return typeAndHost[0];
        } else {
            return typeAndHost[1];
        }
    }
}
