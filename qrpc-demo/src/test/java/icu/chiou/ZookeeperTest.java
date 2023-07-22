package icu.chiou;

import icu.chiou.zookeeper.DeleteWatcher;
import icu.chiou.zookeeper.MyWatcher;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Author: chiou
 * createTime: 2023/7/22
 * Description: 测试zookeeper的使用
 */
public class ZookeeperTest {
    private ZooKeeper zooKeeper;

    @Before
    public void create() {
        //todo zookeeper客户端创建
        //传入参数:
        // 连接ip:port(集群就使用,间隔)
        // 超时时间(ms)
        // 监察者watcher(有默认的)
        //String connectString = "127.0.0.1:2181";
        //将本地的换成仿集群
        String connectString = "192.168.200.131:2181,192.168.200.132:2181,192.168.200.133:2181";
        int timeOut = 10000;
        try {
            //zooKeeper = new ZooKeeper(connectString, timeOut, null);
            zooKeeper = new ZooKeeper(connectString, timeOut, new MyWatcher());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCreatePersistentNode() {
        //todo 测试初始化,创建持久节点
        //传入参数:
        // 路径
        // 数据
        // 权限
        // 节点类型
        try {
            String result = zooKeeper.create("/qrpc",
                    "hello zookeeper,i'm qrpc".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
            System.out.println("result = " + result);
            //result = /qrpc
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (zooKeeper != null) {
                    zooKeeper.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testDeletePersistentNode() {
        //todo 测试删除持久节点
        //传入参数:
        // 路径
        // 版本 version,如果是-1就无视版本号
        try {
            zooKeeper.delete("/qrpc", -1);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (zooKeeper != null) {
                    zooKeeper.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testStat() {
        //todo 测试查看节点状态数据
        try {
            Stat stat = zooKeeper.exists("/qrpc", null);
            //修改数据
            zooKeeper.setData("/qrpc", "hi zk".getBytes(), -1);

            //当前节点的版本
            System.out.println("当前节点的版本 stat.getVersion() = " + stat.getVersion());
            //当前节点的acl的版本
            System.out.println("当前节点的acl的版本 stat.getAversion() = " + stat.getAversion());
            //当前子节点的版本
            System.out.println("当前子节点的版本 stat.getCversion() = " + stat.getCversion());

            System.out.println("stat.getCtime() = " + stat.getCtime());
            Date date = new Date(stat.getCtime());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String format = dateFormat.format(date);
            System.out.println("date = " + format);
            System.out.println("stat.toString() = " + stat.toString());
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (zooKeeper != null) {
                    zooKeeper.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testWatcher() {
        //todo 测试查看watcher
        try {
            //会传入开始设置的默认watcher
            Stat stat = zooKeeper.exists("/qrpc", true);
            //修改数据
            zooKeeper.setData("/qrpc", "hi zk".getBytes(), -1);

            while (true) {
                Thread.sleep(1000);
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (zooKeeper != null) {
                    zooKeeper.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testDeleteWatcher() {
        //todo 测试查看watcher
        try {
            //会传入开始设置的默认watcher
            //没有效果,因为exists对删除节点事件不可监控,学习时没注意到
            Stat stat = zooKeeper.exists("/qrpc", new DeleteWatcher());
            //修改数据
            zooKeeper.setData("/qrpc", "hi zk".getBytes(), -1);

            while (true) {
                Thread.sleep(1000);
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (zooKeeper != null) {
                    zooKeeper.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
