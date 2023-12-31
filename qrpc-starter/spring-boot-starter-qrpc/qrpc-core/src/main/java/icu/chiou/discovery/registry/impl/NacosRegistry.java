package icu.chiou.discovery.registry.impl;

import icu.chiou.common.constants.Constant;
import icu.chiou.discovery.registry.AbstractRegistry;
import icu.chiou.protocol.ServiceConfig;
import icu.chiou.utils.NetUtil;
import icu.chiou.utils.zookeeper.ZookeeperNode;
import icu.chiou.utils.zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Author: chiou
 * createTime: 2023/7/25
 * Description: zookeeper注册中心
 */
@Slf4j
public class NacosRegistry extends AbstractRegistry {

    private ZooKeeper nacos;

    public NacosRegistry() {
        nacos = ZookeeperUtil.createZookeeper();
    }

    public NacosRegistry(String connectString, int timeOut) {
        this.nacos = ZookeeperUtil.createZookeeper(connectString, timeOut);
    }

    @Override
    public void register(ServiceConfig<?> serviceConfig) {
        //获取当前待发布服务的节点名
        String nodePath = Constant.BASE_PROVIDERS_PATH + "/" + serviceConfig.getInterface().getName();
        //该节点为持久节点
        if (!ZookeeperUtil.exist(nacos, nodePath, null)) {
            //1.创建服务持久节点
            ZookeeperNode node = new ZookeeperNode(nodePath, null);
            ZookeeperUtil.createNode(nacos, node, null, CreateMode.PERSISTENT);
        }
        //2.创建主机节点 ip:port
        //服务提供方的端口一般自己设定
        //但是ip应该设置为局域网ip
        //todo 后续处理端口问题
        String needRegisterNodePath = nodePath + "/" + NetUtil.getIp() + ":" + "8088";
        if (!ZookeeperUtil.exist(nacos, needRegisterNodePath, null)) {
            //创建持久节点
            ZookeeperNode node = new ZookeeperNode(needRegisterNodePath, null);
            ZookeeperUtil.createNode(nacos, node, null, CreateMode.EPHEMERAL);
        }

        if (log.isDebugEnabled()) {
            log.debug("服务【{}】已被注册", serviceConfig.getInterface().getName());
        }
    }

    @Override
    public List<InetSocketAddress> lookup(String serviceName, String group) {
        return null;
    }
}
