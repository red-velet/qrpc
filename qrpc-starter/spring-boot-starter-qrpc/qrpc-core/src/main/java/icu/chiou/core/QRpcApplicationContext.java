package icu.chiou.core;

import icu.chiou.protection.CircuitBreaker;
import icu.chiou.protection.RateLimiter;
import icu.chiou.protocol.ServiceConfig;
import icu.chiou.protocol.transport.QRpcRequest;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: chiou
 * createTime: 2023/8/11
 * Description: rpc上下文 - 存储元数据信息和缓存
 */
public class QRpcApplicationContext {
    public static final QRpcApplicationContext CONTEXT = new QRpcApplicationContext();

    private QRpcApplicationContext() {

    }

    public static QRpcApplicationContext getInstance() {
        return CONTEXT;
    }

    //维护已经且发布的服务列表
    public final static Map<String, ServiceConfig<?>> SERVICE_LIST = new ConcurrentHashMap<>(16);

    //netty的连接缓存-channel
    public final static Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);
    //响应时间
    public final static TreeMap<Long, Channel> ANSWER_TIME_CHANNEL_CACHE = new TreeMap<>();

    //全局被挂起的completableFuture
    public final static Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);

    public static final ThreadLocal<QRpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();

    public final static Map<String, RateLimiter> LIMITER_SERVER_LIST = new ConcurrentHashMap<>(128);
    //限流器
    public Map<SocketAddress, RateLimiter> everyIpRateLimiter = new ConcurrentHashMap<>(64);
    //断路器
    public Map<SocketAddress, CircuitBreaker> everyIpBreaker = new ConcurrentHashMap<>(64);

    public Map<SocketAddress, RateLimiter> getEveryIpRateLimiter() {
        return everyIpRateLimiter;
    }

    public Map<SocketAddress, CircuitBreaker> getEveryIpBreaker() {
        return everyIpBreaker;
    }
}
