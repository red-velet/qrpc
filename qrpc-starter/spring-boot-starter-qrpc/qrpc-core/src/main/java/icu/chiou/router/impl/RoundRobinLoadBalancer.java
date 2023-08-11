package icu.chiou.router.impl;

import icu.chiou.common.exceptions.LoadBalancerException;
import icu.chiou.router.AbstractLoadBalancer;
import icu.chiou.router.Selector;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author: chiou
 * createTime: 2023/7/28
 * Description: 轮询方式-负载均衡器
 * 功能1：维护服务列表缓存
 * 功能2：选择一个服务
 */
@Slf4j
public class RoundRobinLoadBalancer extends AbstractLoadBalancer {


    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new RoundRobinSelector(serviceList);
    }


    private static class RoundRobinSelector implements Selector {
        private List<InetSocketAddress> serviceList;
        private AtomicInteger index;

        public RoundRobinSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
            this.index = new AtomicInteger(0);
        }

        @Override
        public InetSocketAddress getNext() {
            if (serviceList == null || serviceList.size() == 0) {
                log.error("进行负载均衡时,发现选取服务列时为空...");
                throw new LoadBalancerException();
            }
            InetSocketAddress address = serviceList.get(index.get());
            //判断游标是否溢出
            if (serviceList.size() - 1 == index.get()) {
                //重置
                index.set(0);
            } else {
                //指针后移
                index.incrementAndGet();
            }
            return address;
        }

        @Override
        public void reBalance() {

        }
    }
}
