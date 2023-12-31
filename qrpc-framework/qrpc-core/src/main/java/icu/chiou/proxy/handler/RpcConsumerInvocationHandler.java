package icu.chiou.proxy.handler;

import icu.chiou.NettyBootstrapInitializer;
import icu.chiou.QRpcBootstrap;
import icu.chiou.annotation.Retry;
import icu.chiou.compress.CompressorFactory;
import icu.chiou.discovery.Registry;
import icu.chiou.enumeration.RequestType;
import icu.chiou.exceptions.DiscoveryException;
import icu.chiou.exceptions.NetworkException;
import icu.chiou.protection.CircuitBreaker;
import icu.chiou.serialize.SerializerFactory;
import icu.chiou.transport.message.QRpcRequest;
import icu.chiou.transport.message.RequestPayload;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 作者：chiou
 * 创建时间：2023/7/26
 * 描述：客户端的反射处理器
 * 本类封装了客户端通信的基础逻辑，每一个代理对象的远程调用过程都封装在了invoke方法中。
 * 1. 发现可用服务
 * 2. 建立连接
 * 3. 发送请求
 * 4. 得到响应
 */
@Slf4j
public class RpcConsumerInvocationHandler implements InvocationHandler {
    // 维护一个注册中心
    private Registry registry;
    private Class<?> interfaceRef;
    private String group;

    public RpcConsumerInvocationHandler(Registry registry, Class<?> interfaceRef, String group) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
        this.group = group;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        log.info("进入代理对象->methodName -> {}", method.getName());
        log.info("进入代理对象->method args -> {}", args);

        // 从接口判断是否需要重试
        Retry retry = method.getAnnotation(Retry.class);
        // 默认值0，不重试
        int tryTime = 0;
        int intervalTime = 0;
        if (retry != null) {
            tryTime = retry.tryTimes();
            intervalTime = retry.intervalTime();
        }

        while (true) {
            // 1. 封装报文
            RequestPayload requestPayload = RequestPayload.builder()
                    .interfaceName(interfaceRef.getName())
                    .methodName(method.getName())
                    .paramsType(method.getParameterTypes())
                    .paramsValue(args)
                    .returnType(method.getReturnType())
                    .build();

            // 2. 创建请求
            QRpcRequest qRpcRequest = QRpcRequest.builder()
                    .requestId(QRpcBootstrap.getInstance().getConfiguration().getIdGenerator().generateId())
                    .requestType(RequestType.REQUEST.getId())
                    .serializeType(SerializerFactory.getSerializer(QRpcBootstrap.getInstance().getConfiguration().getSerializeType()).getCode())
                    .compressType(CompressorFactory.getCompressor(QRpcBootstrap.getInstance().getConfiguration().getCompressType()).getCode())
                    .requestPayload(requestPayload)
                    .build();

            // 3. 将请求存入本地线程, 需要在合适的时候调用remove方法
            QRpcBootstrap.REQUEST_THREAD_LOCAL.set(qRpcRequest);

            // 4. 发现服务-从注册中心寻找可用服务，拉取服务列表，并通过客户端负载均衡器寻找一个可用的服务
            InetSocketAddress address = QRpcBootstrap
                    .getInstance()
                    .getConfiguration()
                    .getLoadBalancer()
                    .selectAvailableService(interfaceRef.getName(), group);
            try {
                // 熔断器
                // 5. 获取当前地址所对应的断路器
                Map<SocketAddress, CircuitBreaker> everyIpBreaker = QRpcBootstrap.getInstance().getConfiguration().everyIpBreaker;
                CircuitBreaker circuitBreaker = everyIpBreaker.get(address);
                if (circuitBreaker == null) {
                    circuitBreaker = new CircuitBreaker(5, 0.5f);
                    everyIpBreaker.put(address, circuitBreaker);
                }
                if (qRpcRequest.getRequestType() != RequestType.HEART_DANCE.getId() && circuitBreaker.isBreak()) {
                    // 5.1 断路器打开了，不发送请求，抛出异常
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            QRpcBootstrap
                                    .getInstance()
                                    .getConfiguration()
                                    .everyIpBreaker.get(address)
                                    .reset();
                        }
                    }, 5000);
                    throw new RuntimeException("当前熔断器已开启，无法发送请求!!!");
                }

                if (log.isDebugEnabled()) {
                    log.debug("服务调用方发现了服务【{}】的可用主机【{}】", interfaceRef.getName(), address);
                }

                // 6. 尝试获取可用通道
                Channel channel = getAvailableChannel(address);
                if (log.isDebugEnabled()) {
                    log.debug("获取了和【{}】建立的连接通道", address);
                }

                // 7. 写出报文
                CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                // 将completableFuture暴露出去
                QRpcBootstrap.PENDING_REQUEST.put(qRpcRequest.getRequestId(), completableFuture);
                // 这里直接写出了请求：请求的实例会直接进入pipeline执行出站的一系列操作
                channel.writeAndFlush(qRpcRequest).addListener((ChannelFutureListener) promise -> {
                    if (!promise.isSuccess()) {
                        completableFuture.completeExceptionally(promise.cause());
                    }
                });

                // 8. 清理ThreadLocal
                QRpcBootstrap.REQUEST_THREAD_LOCAL.remove();

                // 9. 返回响应的结果
                return completableFuture.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("对方法【{}】进行远程调用时，发生异常，正在重试第【{}】次...", method.getName(), 3 - tryTime, e);

                // 记录异常/错误次数
                CircuitBreaker circuitBreaker = QRpcBootstrap.getInstance().getConfiguration().everyIpBreaker.get(address);
                circuitBreaker.recordErrorRequestCount();

                // 重试次数减一，过一段时间重试
                tryTime--;
                try {
                    // 间隔重试
                    Thread.sleep(intervalTime);
                } catch (InterruptedException ex) {
                    log.error("在发生重试时发生异常..." + ex);
                }
                if (tryTime < 0) {
                    log.error("对方法【{}】进行远程调用时，发生异常，重试第【{}】次，依旧不可调用...", method.getName(), 3 - tryTime, e);
                    break;
                }
            }
        }
        throw new RuntimeException("执行远程方法【" + method.getName() + "】调用失败...");
    }

    private static Channel getAvailableChannel(InetSocketAddress address) throws RuntimeException {
        // 1. 尝试从缓存中获取通道
        Channel channel = QRpcBootstrap.CHANNEL_CACHE.get(address);
        if (channel == null) {
            // 2. 缓存中没有，建立新的连接和channel
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
            // 3. 阻塞获取channel
            try {
                channel = completableFuture.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("获取通道时发送了异常", e);
                throw new DiscoveryException(e);
            }

            // 4. 将channel添加到缓存
            QRpcBootstrap.CHANNEL_CACHE.put(address, channel);
        }
        // 5. 还是拿不到就抛异常
        if (channel == null) {
            log.error("获取或与【{}】建立通道时发送了异常", address);
            throw new NetworkException("获取通道时发送了异常");
        }
        return channel;
    }
}
