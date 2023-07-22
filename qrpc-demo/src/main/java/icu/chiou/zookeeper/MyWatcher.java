package icu.chiou.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 * Author: chiou
 * createTime: 2023/7/22
 * Description: 自定义实现自己的监听器watcher
 */
public class MyWatcher implements Watcher {
    @Override
    public void process(WatchedEvent watchedEvent) {
        //todo 判断不同的事件类,做不同的判断
        Event.EventType type = watchedEvent.getType();
        if (type == Event.EventType.None) {//连接类型
            Event.KeeperState state = watchedEvent.getState();
            if (state == Event.KeeperState.SyncConnected) {
                System.out.println("来自MyWatch---zookeeper连接成功");
            } else if (state == Event.KeeperState.AuthFailed) {
                System.out.println("来自MyWatch---zookeeper身份认证失败");
            } else if (state == Event.KeeperState.Disconnected) {
                System.out.println("来自MyWatch---zookeeper断开连接");
            }
        } else if (type == Event.EventType.NodeCreated) {//节点创建类型
            System.out.println("来自MyWatch---节点被创建");
        } else if (type == Event.EventType.NodeDeleted) {//节点删除类型
            System.out.println("来自MyWatch---节点被删除");
        } else if (type == Event.EventType.NodeDataChanged) {//节点修改类型
            System.out.println("来自MyWatch---节点数据发送变化");
        } else if (type == Event.EventType.NodeChildrenChanged) {//字节点创建类型
            System.out.println("来自MyWatch---子节点被创建");
        }
    }
}
