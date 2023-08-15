package icu.chiou.consumer;

import icu.chiou.common.enumeration.RequestType;
import icu.chiou.common.enumeration.ResponseCode;
import icu.chiou.common.exceptions.DiscoveryException;
import icu.chiou.common.exceptions.NetworkException;
import icu.chiou.core.QRpcApplicationContext;
import icu.chiou.core.QRpcProperties;
import icu.chiou.discovery.registry.Registry;
import icu.chiou.filter.FilterChain;
import icu.chiou.filter.FilterData;
import icu.chiou.filter.FilterFactory;
import icu.chiou.netty.NettyBootstrapInitializer;
import icu.chiou.protection.CircuitBreaker;
import icu.chiou.protocol.transport.QRpcRequest;
import icu.chiou.protocol.transport.QRpcResponse;
import icu.chiou.protocol.transport.RequestPayload;
import icu.chiou.router.LoadBalancer;
import icu.chiou.router.LoadBalancerFactory;
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

    private int retryCount;
    private String loadBalancerType;
    private long timeOut;
    private long intervalTime;

    private CircuitBreaker circuitBreaker;

    public RpcConsumerInvocationHandler(Registry registry, Class<?> interfaceRef, String group, int retryCount, long intervalTime, String loadBalancerType, long timeOut, CircuitBreaker circuitBreaker) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
        this.group = group;
        this.retryCount = retryCount;
        this.intervalTime = intervalTime;
        this.loadBalancerType = loadBalancerType;
        this.timeOut = timeOut;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        while (true) {
            // 1. 封装报文
            RequestPayload requestPayload = RequestPayload.builder()
                    .interfaceName(interfaceRef.getName())
                    .methodName(method.getName())
                    .paramsType(method.getParameterTypes())
                    .paramsValue(args)
                    .returnType(method.getReturnType())
                    .consumerAttributes(QRpcProperties.getInstance().getConsumerAttributes())
                    .providerAttributes(QRpcProperties.getInstance().getProviderAttributes())
                    .build();

            FilterData filterData = new FilterData(requestPayload);
            FilterChain filterChain = FilterFactory.getConsumerBeforeFilterChain();
            filterChain.doFilter(filterData);

            //todo 暂时这样写
            String serialization = QRpcProperties.getInstance().getSerializeType();
            byte serializeCode = 1;
            if (serialization.equals("jdk")) {
                serializeCode = 1;
            } else if (serialization.equals("hessian")) {
                serializeCode = 2;
            } else if (serialization.equals("json")) {
                serializeCode = 3;
            }

            String compressType = QRpcProperties.getInstance().getSerializeType();
            byte compressCode = 1;
            if (compressType.equals("gzip")) {
                compressCode = 1;
            }

            // 2. 创建请求
            QRpcRequest qRpcRequest = QRpcRequest.builder()
                    .requestId(QRpcProperties.getInstance().getIdGenerator().generateId())
                    .requestType(RequestType.REQUEST.getId())
                    .serializeType(serializeCode)
                    .compressType(compressCode)
                    .requestPayload(requestPayload)
                    .build();

            // 3. 将请求存入本地线程, 需要在合适的时候调用remove方法
            QRpcApplicationContext.REQUEST_THREAD_LOCAL.set(qRpcRequest);

            // 4. 发现服务-从注册中心寻找可用服务，拉取服务列表，并通过客户端负载均衡器寻找一个可用的服务
            LoadBalancer loadBalancer = LoadBalancerFactory.get(interfaceRef.getName(), loadBalancerType);
            InetSocketAddress address = loadBalancer.selectAvailableService(interfaceRef.getName(), group);
            try {
                //查看当前调用者是否开启熔断器服务
                if (this.circuitBreaker != null) {
                    // 熔断器
                    //不是心跳请求并且断路器已经打开了
                    // 5. 获取当前地址所对应的断路器
                    Map<SocketAddress, CircuitBreaker> everyIpBreaker = QRpcApplicationContext.getInstance().everyIpBreaker;
                    CircuitBreaker ipCircuitBreaker = everyIpBreaker.get(address);
                    if (everyIpBreaker.get(address) == null) {
                        int maxErrorRequestCount = this.circuitBreaker.getMaxErrorRequestCount();
                        float maxErrorRequestRate = this.circuitBreaker.getMaxErrorRequestRate();
                        ipCircuitBreaker = new CircuitBreaker(maxErrorRequestCount, maxErrorRequestRate);
                        everyIpBreaker.put(address, ipCircuitBreaker);
                    }
                    if (qRpcRequest.getRequestType() != RequestType.HEART_DANCE.getId() && ipCircuitBreaker.isBreak()) {
                        // 5.1 断路器打开了，不发送请求，抛出异常
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                QRpcApplicationContext
                                        .getInstance()
                                        .everyIpBreaker.get(address)
                                        .reset();
                            }
                        }, 5000);
                        throw new RuntimeException("当前熔断器已开启，无法发送请求!!!");
                    }
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
                QRpcApplicationContext.PENDING_REQUEST.put(qRpcRequest.getRequestId(), completableFuture);
                // 这里直接写出了请求：请求的实例会直接进入pipeline执行出站的一系列操作
                channel.writeAndFlush(qRpcRequest).addListener((ChannelFutureListener) promise -> {
                    if (!promise.isSuccess()) {
                        completableFuture.completeExceptionally(promise.cause());
                    }
                });

                // 8. 清理ThreadLocal
                QRpcApplicationContext.REQUEST_THREAD_LOCAL.remove();

                // 9. 返回响应的结果
                QRpcResponse response = (QRpcResponse) completableFuture.get(10, TimeUnit.SECONDS);
                if (response.getCode() == ResponseCode.UNAUTHENTICATED.getCode()) {
                    return null;
                } else if (response.getCode() == ResponseCode.RATE_LIMIT.getCode()) {
                    return null;
                }
                return response.getBody();
            } catch (Exception e) {
                log.error("对方法【{}】进行远程调用时，发生异常，正在重试第【{}】次...", method.getName(), 3 - retryCount, e);
                if (this.circuitBreaker != null) {
                    // 记录异常/错误次数
                    CircuitBreaker circuitBreaker = QRpcApplicationContext.getInstance().everyIpBreaker.get(address);
                    circuitBreaker.recordErrorRequestCount();
                }
                // 重试次数减一，过一段时间重试
                retryCount--;
                try {
                    // 间隔重试
                    Thread.sleep(intervalTime);
                } catch (InterruptedException ex) {
                    log.error("在发生重试时发生异常..." + ex);
                }
                if (retryCount < 0) {
                    log.error("对方法【{}】进行远程调用时，发生异常，重试第【{}】次，依旧不可调用...", method.getName(), 3 - retryCount, e);
                    break;
                }
            }
        }
        throw new RuntimeException("执行远程方法【" + method.getName() + "】调用失败...");
    }

    private static Channel getAvailableChannel(InetSocketAddress address) throws RuntimeException {
        // 1. 尝试从缓存中获取通道
        Channel channel = QRpcApplicationContext.CHANNEL_CACHE.get(address);
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
            QRpcApplicationContext.CHANNEL_CACHE.put(address, channel);
        }
        // 5. 还是拿不到就抛异常
        if (channel == null) {
            log.error("获取或与【{}】建立通道时发送了异常", address);
            throw new NetworkException("获取通道时发送了异常");
        }
        return channel;
    }
}
