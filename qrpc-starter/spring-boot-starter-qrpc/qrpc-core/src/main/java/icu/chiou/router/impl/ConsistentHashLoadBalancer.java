package icu.chiou.router.impl;

import icu.chiou.core.QRpcApplicationContext;
import icu.chiou.protocol.transport.QRpcRequest;
import icu.chiou.router.AbstractLoadBalancer;
import icu.chiou.router.Selector;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Author: chiou
 * createTime: 2023/7/30
 * Description: 轮询方式-一致性hash
 */
@Slf4j
public class ConsistentHashLoadBalancer extends AbstractLoadBalancer {


    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new ConsistentHashSelector(serviceList, 128);
    }

    private static class ConsistentHashSelector implements Selector {
        //hash环用来存储服务器地址
        private SortedMap<Integer, InetSocketAddress> circle = new TreeMap<>();

        //虚拟节点的个数
        private int virtualNodes;

        public ConsistentHashSelector(List<InetSocketAddress> serviceList, int virtualNodes) {
            this.virtualNodes = virtualNodes;
            for (InetSocketAddress address : serviceList) {
                //需要把每个节点添加到hash环
                addNodeToHashCircle(address);
            }

        }

        /**
         * 将每个节点挂载到hash环上
         *
         * @param address 节点的地址
         */
        private void addNodeToHashCircle(InetSocketAddress address) {
            //为每个节点生成虚拟节点,进行挂载
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(address.toString() + "-" + i);
                //挂载到hash环上
                circle.put(hash, address);
            }
        }

        /**
         * 将每个节点取消挂载到hash环上
         *
         * @param address 节点的地址
         */
        private void removeNodeFromHashCircle(InetSocketAddress address) {
            //为每个节点生成虚拟节点,进行挂载
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(address.toString() + "-" + i);
                //取消挂载
                circle.remove(hash);
            }
        }


        @Override
        public InetSocketAddress getNext() {
            //获取请求
            QRpcRequest qRpcRequest = QRpcApplicationContext.REQUEST_THREAD_LOCAL.get();

            //通过请求的特征来进行选择服务
            String id = Long.toString(qRpcRequest.getRequestId());

            //通过请求特征进行获取hash
            int hash = hash(id);

            //判断hash是否可以直接落于服务器
            if (!circle.containsKey(hash)) {
                //获取值与当前hash最接近的,hash-服务的集合
                SortedMap<Integer, InetSocketAddress> biggerMap = circle.tailMap(hash);
                hash = biggerMap.isEmpty() ? circle.firstKey() : biggerMap.firstKey();
            }

            //返回
            return circle.get(hash);
        }


        @Override
        public void reBalance() {

        }

        /**
         * 具体的hash算法
         *
         * @param target 目标对象
         * @return 4个字节对象组成的int值 - hash值
         */
        public int hash(String target) {
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            byte[] digest = md.digest(target.getBytes());
            int res = 0;
            for (int i = 0; i < 4; i++) {
                res = res << 8;
                if (digest[i] < 0) {
                    res = res | (digest[i] & 255);
                } else {
                    res = res | digest[i];
                }
            }
            return res;
        }

    }
}
