package icu.chiou.core;

import icu.chiou.NettyBootstrapInitializer;
import icu.chiou.QRpcBootstrap;
import icu.chiou.compress.CompressorFactory;
import icu.chiou.discovery.Registry;
import icu.chiou.enumeration.RequestType;
import icu.chiou.serialize.SerializerFactory;
import icu.chiou.transport.message.QRpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Author: chiou
 * createTime: 2023/7/30
 * Description: 心跳检测器
 */
@Slf4j
public class HeartbeatDetector {
    public static void detectorHeaderDance(String serviceName) {
        //todo 心跳检测
        //1.不让客户端直接面向注册中心拉取服务，而是通过心跳检测拉取服务列表
        Registry registry = QRpcBootstrap.getInstance().getRegistry();
        List<InetSocketAddress> addressList = registry.lookup(serviceName);

        //建立连接
        for (InetSocketAddress address : addressList) {
            try {
                //2.将服务缓存
                if (!QRpcBootstrap.CHANNEL_CACHE.containsKey(address)) {
                    Channel channel = NettyBootstrapInitializer.getBootstrap()
                            .connect(address).sync().channel();
                    QRpcBootstrap.CHANNEL_CACHE.put(address, channel);
                }

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        //3.定期查看服务是否存活
        Thread thread = new Thread(() -> {
            new Timer().scheduleAtFixedRate(new MyTimeTask(), 2000, 2000);
        }, "QRpc-HeartDanceDetector-Thread");
        thread.setDaemon(true);
        thread.start();
    }

    private static class MyTimeTask extends TimerTask {
        @Override
        public void run() {
            //将响应时长的map清空
            QRpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.clear();
            Map<InetSocketAddress, Channel> channelCache = QRpcBootstrap.CHANNEL_CACHE;
            Set<Map.Entry<InetSocketAddress, Channel>> entries = channelCache.entrySet();
            //遍历所用channel
            for (Map.Entry<InetSocketAddress, Channel> entry : entries) {
                Channel channel = entry.getValue();
                //发生心跳请求
                QRpcRequest qRpcRequest = QRpcRequest.builder()
                        .requestId(QRpcBootstrap.ID_GENERATOR.generateId())
                        .requestType(RequestType.HEART_DANCE.getId())
                        .serializeType(SerializerFactory.getSerializer(QRpcBootstrap.SERIALIZE_TYPE).getCode())
                        .compressType(CompressorFactory.getCompressor(QRpcBootstrap.COMPRESS_TYPE).getCode())
                        .build();
                channel.writeAndFlush(qRpcRequest);
                long startTime = System.currentTimeMillis();

                //写出报文
                CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                //将completableFuture暴露出去
                QRpcBootstrap.PENDING_REQUEST.put(qRpcRequest.getRequestId(), completableFuture);
                //这里直接写出了请求：请求的实例会直接进入pipeline执行出站的一系列操作
                channel.writeAndFlush(qRpcRequest).addListener((ChannelFutureListener) promise -> {
                    if (!promise.isSuccess()) {
                        completableFuture.completeExceptionally(promise.cause());
                    }
                });
                long endTime = 0L;
                try {
                    completableFuture.get();
                    endTime = System.currentTimeMillis();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
                long time = endTime - startTime;
                //使用treeMap进行缓存
                QRpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.put(time, channel);
                log.debug("和[{}]服务器的响应时间是[{}].", entry.getKey(), time);
            }
            log.info("----------------------------------------响应时间的treeMap-----------------------------------------");
            for (Map.Entry<Long, Channel> entry : QRpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.entrySet()) {
                if (log.isDebugEnabled()) {
                    log.debug("【{}】 --> 【{}】 ", entry.getKey(), entry.getValue());
                }
            }
        }
    }
}
