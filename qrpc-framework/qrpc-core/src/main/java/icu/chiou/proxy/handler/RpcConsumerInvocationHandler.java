package icu.chiou.proxy.handler;

import icu.chiou.NettyBootstrapInitializer;
import icu.chiou.QRpcBootstrap;
import icu.chiou.discovery.Registry;
import icu.chiou.exceptions.DiscoveryException;
import icu.chiou.exceptions.NetworkException;
import icu.chiou.transport.message.QRpcRequest;
import icu.chiou.transport.message.RequestPayload;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Author: chiou
 * createTime: 2023/7/26
 * Description: 客户端的反射处理器
 * 本类封装了客户端通信的基础逻辑，每一个代理对象的远程调用过程都封装在了invoke方法
 * 1.发现可用服务
 * 2.建立连接
 * 3.发送请求
 * 4.得到响应
 */
@Slf4j
public class RpcConsumerInvocationHandler implements InvocationHandler {
    //维护一个注册中心
    private Registry registry;
    private Class<?> interfaceRef;

    public RpcConsumerInvocationHandler(Registry registry, Class<?> interfaceRef) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("进入代理对象->methodName -> {}", method.getName());
        log.info("进入代理对象->method args -> {}", args);
        //1.发现服务-从注册中心寻找可用服务
        //todo 每次调用该方法都需要去注册中心拉取服务列表吗?
        //     如何选择一个合适的服务,而不是只选择第一个?
        InetSocketAddress address = registry.lookup(interfaceRef.getName());
        if (log.isDebugEnabled()) {
            log.debug("服务调用方发现了服务【{}】的可用主机【{}】", interfaceRef.getName(), address);
        }
        //2.尝试获取可用通道
        Channel channel = getAvaiableChannel(address);
        if (log.isDebugEnabled()) {
            log.debug("获取了和【{}】建立的连接通道", address);
        }

        //3.封装报文
        //todo 封装报文
        RequestPayload requestPayload = RequestPayload.builder()
                .interfaceName(interfaceRef.getName())
                .methodName(method.getName())
                .paramsType(method.getParameterTypes())
                .paramsValue(args)
                .returnType(method.getReturnType())
                .build();

        QRpcRequest qRpcRequest = QRpcRequest.builder()
                .requestId(1L)
                .requestType((byte) 1)
                .serializeType((byte) 1)
                .compressType((byte) 1)
                .requestPayload(requestPayload)
                .build();

        //调用方法,返回结果
        /*
         * -------------------------同步策略 -------------------------
         */
//                ChannelFuture channelFuture = channel.writeAndFlush(new Object()).await();
//                if (channelFuture.isDone()) {
//                    Object result = channelFuture.getNow();
//                } else if (!channelFuture.isSuccess()) {
//                    //捕获子线程-异步任务中的异常
//                    Throwable throwable = channelFuture.cause();
//                    throw new RuntimeException(throwable);
//                }
        /*
         * -------------------------同步策略 -------------------------
         */

        /*
         * -------------------------异步策略 -------------------------
         */
        //4.写出报文
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        //将completableFuture暴露出去
        QRpcBootstrap.PENDING_REQUEST.put(1L, completableFuture);
        //这里直接写出了请求：请求的实例会直接进入pipeline执行出站的一系列操作
        channel.writeAndFlush(qRpcRequest).addListener((ChannelFutureListener) promise -> {
            //当前promise将来返回的结果是什么？ -》 writeAndFlush写出的结果
            //但是我们需要获取的服务提供方的响应，那才是需要的结果，所以此处只需要处理异常即可
            //所以我们需要在此次将completableFuture挂起，等将来服务提供方响应后再获取
//                    if (promise.isDone()) {
//                        if (log.isDebugEnabled()) {
//                            log.debug("来自服务消费者->completableFuture->promise数据已经写出去了");
//                        }
//                        completableFuture.complete(promise.getNow());
//                    }
            if (!promise.isSuccess()) {
                completableFuture.completeExceptionally(promise.cause());
            }
        });
        //此处阻塞，会等待get执行
        //如果还没用执行完成，我们就在pipeline的handler执行调用返回结果
        //5.获得响应的结果
        return completableFuture.get(10, TimeUnit.SECONDS);
    }

    private static Channel getAvaiableChannel(InetSocketAddress address) throws RuntimeException {
        //1.尝试从缓存中获取通道
        Channel channel = QRpcBootstrap.CHANNEL_CACHE.get(address);
        if (channel == null) {
            //2.缓存中没有,建立新的连接和channel
            CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
            NettyBootstrapInitializer.getBootstrap()
                    .connect(address)
                    .addListener((ChannelFutureListener) promise -> {
                        if (promise.isDone()) {
                            if (log.isDebugEnabled()) {
                                log.debug("来自服务消费者->代理->completableFuture->promise已经和【{}】建立了连接", address);
                            }
                            completableFuture.complete(promise.channel());
                        } else if (!promise.isSuccess()) {
                            completableFuture.completeExceptionally(promise.cause());
                        }
                    });
            //3.阻塞获取channel
            try {
                channel = completableFuture.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("获取通道时发送了异常", e);
                throw new DiscoveryException(e);
            }

            //4.将channel添加到缓存
            QRpcBootstrap.CHANNEL_CACHE.put(address, channel);
        }
        //5.还是拿不到就抛异常
        if (channel == null) {
            log.error("获取或与【{}】建立通道时发送了异常", address);
            throw new NetworkException("获取通道时发送了异常");
        }
        return channel;
    }
}
