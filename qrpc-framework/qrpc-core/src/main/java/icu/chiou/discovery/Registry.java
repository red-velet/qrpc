package icu.chiou.discovery;

import icu.chiou.ServiceConfig;

import java.net.InetSocketAddress;

/**
 * Author: chiou
 * createTime: 2023/7/25
 * Description: 注册中心
 */
public interface Registry {
    /**
     * 注册服务
     *
     * @param serviceConfig 服务配置
     */
    void register(ServiceConfig<?> serviceConfig);

    /**
     * 从注册中心拉取一个服务
     *
     * @param serviceName 服务的名称
     * @return 服务的地址(ip + 端口)
     */
    InetSocketAddress lookup(String serviceName);
}
