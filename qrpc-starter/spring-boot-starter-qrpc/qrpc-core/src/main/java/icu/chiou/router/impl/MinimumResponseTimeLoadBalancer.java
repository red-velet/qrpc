package icu.chiou.router.impl;

import icu.chiou.core.QRpcApplicationContext;
import icu.chiou.router.AbstractLoadBalancer;
import icu.chiou.router.Selector;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * Author: chiou
 * createTime: 2023/7/30
 * Description: No Description
 */
public class MinimumResponseTimeLoadBalancer extends AbstractLoadBalancer {
    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new MinimumResponseTimeSelector(serviceList);
    }

    private static class MinimumResponseTimeSelector implements Selector {
        public MinimumResponseTimeSelector(List<InetSocketAddress> serviceList) {
        }

        @Override
        public InetSocketAddress getNext() {
            Map.Entry<Long, Channel> entry = QRpcApplicationContext.ANSWER_TIME_CHANNEL_CACHE.firstEntry();
            if (entry != null) {
                return (InetSocketAddress) entry.getValue().remoteAddress();
            }

            // 直接从缓存中获取一个可用的就行了
            Channel channel = (Channel) QRpcApplicationContext.CHANNEL_CACHE.values().toArray()[0];
            return (InetSocketAddress) channel.remoteAddress();
        }

        @Override
        public void reBalance() {

        }
    }
}
