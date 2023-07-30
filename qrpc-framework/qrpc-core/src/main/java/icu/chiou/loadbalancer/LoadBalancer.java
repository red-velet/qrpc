package icu.chiou.loadbalancer;

import java.net.InetSocketAddress;

/**
 * Author: chiou
 * createTime: 2023/7/28
 * Description: 抽象的负载均衡器接口，定义子类的行为
 */
public interface LoadBalancer {
    /**
     * 根据服务名获取一个可用的服务
     *
     * @param name 服务吗
     * @return 可用服务地址
     */
    InetSocketAddress selectAvailableService(String name);
}
