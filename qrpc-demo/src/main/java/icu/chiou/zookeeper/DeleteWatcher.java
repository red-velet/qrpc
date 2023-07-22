package icu.chiou.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 * Author: chiou
 * createTime: 2023/7/22
 * Description: No Description
 */
public class DeleteWatcher implements Watcher {

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getType() == Event.EventType.NodeDeleted) {
            System.out.println("来自MyWatch---节点被删除");
        } else {
            System.out.println("经过了DeleteWatch---它来了,但是什么也没有留下");
        }
    }
}
