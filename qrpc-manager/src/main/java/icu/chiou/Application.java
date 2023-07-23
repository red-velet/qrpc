package icu.chiou;

import icu.chiou.utils.zookeeper.ZookeeperNode;
import icu.chiou.utils.zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

/**
 * Author: chiou
 * createTime: 2023/7/23
 * Description: 注册中心的管理页面
 */
@Slf4j
public class Application {
    public static void main(String[] args) {
        //todo 创建节点目录
        //todo zookeeper客户端创建

        //创建zookeeper实例
        ZooKeeper zookeeper = ZookeeperUtil.createZookeeper();
        //定义节点和数据
        String basePath = "/qrpc-metadata";
        String providerPath = basePath + "/providers";
        String consumerPath = basePath + "/consumers";
        ZookeeperNode baseNode = new ZookeeperNode(basePath, null);
        ZookeeperNode providerNode = new ZookeeperNode(providerPath, null);
        ZookeeperNode consumerNode = new ZookeeperNode(consumerPath, null);
        List.of(baseNode, providerNode, consumerNode).forEach(node -> {
            ZookeeperUtil.createNode(zookeeper, node, null, CreateMode.PERSISTENT);
        });

        ZookeeperUtil.close(zookeeper);
    }
}
