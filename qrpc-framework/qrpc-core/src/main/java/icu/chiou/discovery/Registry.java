package icu.chiou.discovery;

import icu.chiou.ServiceConfig;

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
}
