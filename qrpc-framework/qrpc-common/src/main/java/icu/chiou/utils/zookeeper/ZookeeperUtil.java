package icu.chiou.utils.zookeeper;

import icu.chiou.constants.Constant;
import icu.chiou.exceptions.ZookeeperException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Author: chiou
 * createTime: 2023/7/23
 * Description: Zookeeper工具类
 */
@Slf4j
public class ZookeeperUtil {
    /**
     * 使用默认配置创建zookeeper实例
     *
     * @return zookeeper实例
     */
    public static ZooKeeper createZookeeper() {
        //todo 创建节点目录
        //默认连接参数
        String connectString = Constant.DEFAULT_ZK_CONNECT;
        int timeOut = Constant.DEFAULT_ZK_TIMEOUT;
        return createZookeeper(connectString, timeOut);
    }

    /**
     * 自定义配置创建zookeeper实例
     *
     * @param connectString 连接地址
     * @param timeOut       超时事件
     * @return zookeeper实例
     */
    public static ZooKeeper createZookeeper(String connectString, int timeOut) {
        //todo 创建节点目录
        //todo zookeeper客户端创建
        //定义连接参数
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            //创建zookeeper实例
            final ZooKeeper zooKeeper = new ZooKeeper(connectString, timeOut, event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    log.error("来自manager端:客户端连接成功");
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
            return zooKeeper;
        } catch (IOException | InterruptedException e) {
            log.error("创建基础目录时,产生异常如下:", e);
            throw new ZookeeperException();
        }
    }

    /**
     * 该方法用于创建节点
     *
     * @param zooKeeper  zk实例
     * @param node       节点
     * @param watcher    watcher
     * @param createMode 节点类型
     * @return true:创建成功 false:存在 异常:创建出错
     */
    public static Boolean createNode(ZooKeeper zooKeeper, ZookeeperNode node, Watcher watcher, CreateMode createMode) {
        //创建节点,不存在才创建
        try {
            if (zooKeeper.exists(node.getNodePath(), watcher) == null) {
                String replyBasePath = zooKeeper.create(node.getNodePath(), node.getData(), ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
                log.debug("根节点成功创建 -> 【{}】", replyBasePath);
                return true;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("节点创建失败,该节点已存在:【{}】", node.getNodePath());
                }
                return false;
            }
        } catch (KeeperException | InterruptedException e) {
            log.error("创建基础目录时,产生异常如下:", e);
            throw new ZookeeperException();
        }
    }

    /**
     * 关闭zookeeper客户端连接
     *
     * @param zookeeper zookeeper实例
     */
    public static void close(ZooKeeper zookeeper) {
        if (zookeeper != null) {
            try {
                zookeeper.close();
            } catch (InterruptedException e) {
                log.error("关闭zookeeper时发生异常:", e);
                throw new RuntimeException(e);
            }
        }
    }
}
