package icu.chiou.discovery.registry;

import icu.chiou.protocol.ServiceConfig;

import java.net.InetSocketAddress;
import java.util.List;

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
     * @param group       分组名
     * @return 服务的地址(ip + 端口)
     */
    List<InetSocketAddress> lookup(String serviceName, String group);
}
