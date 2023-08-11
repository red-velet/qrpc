package icu.chiou.discovery.registry.impl;

import icu.chiou.common.constants.Constant;
import icu.chiou.common.exceptions.DiscoveryException;
import icu.chiou.common.exceptions.ZookeeperException;
import icu.chiou.core.QRpcProperties;
import icu.chiou.discovery.registry.AbstractRegistry;
import icu.chiou.discovery.watcher.UpAndDownWatcher;
import icu.chiou.protocol.ServiceConfig;
import icu.chiou.utils.NetUtil;
import icu.chiou.utils.zookeeper.ZookeeperNode;
import icu.chiou.utils.zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: chiou
 * createTime: 2023/7/25
 * Description: zookeeper注册中心
 */
@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {

    private ZooKeeper zooKeeper;

    public ZookeeperRegistry() {
        zooKeeper = ZookeeperUtil.createZookeeper();
    }

    public ZookeeperRegistry(String connectString, int timeOut) {
        this.zooKeeper = ZookeeperUtil.createZookeeper(connectString, timeOut);
    }

    @Override
    public void register(ServiceConfig<?> serviceConfig) {
        //获取当前待发布服务的节点名
        String nodePath = Constant.BASE_PROVIDERS_PATH + "/" + serviceConfig.getInterface().getName();
        //该节点为持久节点,不存在就创建服务节点
        if (!ZookeeperUtil.exist(zooKeeper, nodePath, null)) {
            //1.创建服务持久节点
            ZookeeperNode node = new ZookeeperNode(nodePath, null);
            ZookeeperUtil.createNode(zooKeeper, node, null, CreateMode.PERSISTENT);
        }
        //该节点为分组节点,不存在就创建服务节点
        nodePath = nodePath + "/" + serviceConfig.getGroup();
        if (!ZookeeperUtil.exist(zooKeeper, nodePath, null)) {
            //1.创建服务持久节点
            ZookeeperNode node = new ZookeeperNode(nodePath, null);
            ZookeeperUtil.createNode(zooKeeper, node, null, CreateMode.PERSISTENT);
        }
        //2.创建主机节点 ip:port
        //服务提供方的端口一般自己设定
        //但是ip应该设置为局域网ip
        //todo 后续处理端口问题
        String needRegisterNodePath = nodePath + "/" + NetUtil.getIp() + ":" + QRpcProperties.getInstance().getPort();
        if (!ZookeeperUtil.exist(zooKeeper, needRegisterNodePath, null)) {
            //创建持久节点
            ZookeeperNode node = new ZookeeperNode(needRegisterNodePath, null);
            ZookeeperUtil.createNode(zooKeeper, node, null, CreateMode.EPHEMERAL);
        }

        if (log.isDebugEnabled()) {
            log.debug("服务【{}】已被注册", serviceConfig.getInterface().getName());
        }
    }

    @Override
    public List<InetSocketAddress> lookup(String serviceName, String group) {
        //1.找到服务对应的节点
        String serviceNodePath = Constant.BASE_PROVIDERS_PATH + "/" + serviceName + "/" + group;
        //思考: 如果注册中心挂了怎么办?
        //a.先重试
        //b.
        int maxRetries = 3; // 最大重试次数
        int retryDelayMs = 1000; // 初始重试延迟时间，单位毫秒
        int maxRetryDelayMs = 5000; // 最大重试延迟时间，单位毫秒
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try {
                List<String> serviceList = ZookeeperUtil.getChildrenList(zooKeeper, serviceNodePath, new UpAndDownWatcher());
                //2.从zookeeper中获取其子节点列表
                //获取了所有可用服务列表
                List<InetSocketAddress> collect = serviceList.stream().map(ipString -> {
                    String[] ipAndPort = ipString.split(":");
                    String ip = ipAndPort[0];
                    int port = Integer.parseInt(ipAndPort[1]);
                    return new InetSocketAddress(ip, port);
                }).collect(Collectors.toList());
                if (collect.size() == 0) {
                    throw new DiscoveryException("没有发现可用服务列表");
                }
                return collect;
            } catch (ZookeeperException e) {
                // 处理Zookeeper异常，例如连接问题等
                log.error("Zookeeper异常：{}", e.getMessage());
            }
            // 进行重试，使用指数退避策略
            retryCount++;
            int currentDelay = retryDelayMs * (1 << (retryCount - 1));
            retryDelayMs = Math.min(currentDelay, maxRetryDelayMs);

            try {
                Thread.sleep(retryDelayMs);
            } catch (InterruptedException e) {
                log.error("重试线程中断异常：{}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        throw new ZookeeperException("重试多次仍未找到可用服务列表");
    }
}
