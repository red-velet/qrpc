package icu.chiou.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Author: chiou
 * createTime: 2023/7/28
 * Description: 抽象的负载均衡器接口，定义子类的行为
 */
public interface LoadBalancer {
    /**
     * 根据服务名获取一个可用的服务
     *
     * @param name  服务名
     * @param group 分组名
     * @return 可用服务地址
     */
    InetSocketAddress selectAvailableService(String name, String group);

    /**
     * 当感知节点发生了动态上下线，我们需要重新进行负载均衡
     *
     * @param serviceName 服务的名称
     */
    void reloadBalance(String serviceName, List<InetSocketAddress> addresses);

}
