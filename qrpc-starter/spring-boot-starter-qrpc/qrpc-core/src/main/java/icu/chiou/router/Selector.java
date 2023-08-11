package icu.chiou.router;

import java.net.InetSocketAddress;

/**
 * Author: chiou
 * createTime: 2023/7/28
 * Description: 具体的工作者 - 负责工作和监察
 */
public interface Selector {
    /**
     * 从服务列表中,根据设置的算法,返回一个合适的服务节点
     *
     * @return 服务节点
     */
    InetSocketAddress getNext();

    //todo 服务动态上下线
    void reBalance();
}
