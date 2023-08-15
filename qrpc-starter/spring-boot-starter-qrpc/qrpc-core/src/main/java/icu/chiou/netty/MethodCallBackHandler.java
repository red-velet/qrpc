package icu.chiou.netty;

import icu.chiou.common.enumeration.ResponseCode;
import icu.chiou.common.exceptions.ResponseException;
import icu.chiou.core.QRpcApplicationContext;
import icu.chiou.filter.FilterChain;
import icu.chiou.filter.FilterData;
import icu.chiou.filter.FilterFactory;
import icu.chiou.protection.CircuitBreaker;
import icu.chiou.protocol.transport.QRpcResponse;
import icu.chiou.router.LoadBalancer;
import icu.chiou.router.LoadBalancerFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * Author: chiou
 * createTime: 2023/7/26
 * Description: 这是一个用于测试的类
 */
@Slf4j
public class MethodCallBackHandler extends SimpleChannelInboundHandler<QRpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, QRpcResponse msg) throws Exception {
        try {
            FilterData filterData = new FilterData(msg);
            FilterChain chain = FilterFactory.getConsumerAfterFilterChain();
            chain.doFilter(filterData);
        } catch (Exception e) {
            log.error("ConsumerAfterFilterChain filter error --->> MethodCallBackHandler channelRead0");
        }

        //获取响应码
        byte code = msg.getCode();

        //获取当前ip的熔断器
        SocketAddress socketAddress = ctx.channel().remoteAddress();
        CircuitBreaker circuitBreaker = QRpcApplicationContext.getInstance().everyIpBreaker.get(socketAddress);


        //从全局挂起的前期中获取与之匹配的、待处理的completeFuture
        CompletableFuture<Object> completableFuture = QRpcApplicationContext.PENDING_REQUEST.get(msg.getRequestId());


        if (code == ResponseCode.SUCCESS.getCode()) {
            completableFuture.complete(msg);
        } else if (code == ResponseCode.SUCCESS_HEART_DANCE.getCode()) {
            completableFuture.complete(null);
        } else if (code == ResponseCode.RATE_LIMIT.getCode()) {
            if (circuitBreaker != null) {
                circuitBreaker.recordErrorRequestCount();
            }
            completableFuture.complete(msg);
        } else if (code == ResponseCode.UNAUTHENTICATED.getCode()) {
            if (circuitBreaker != null) {
                circuitBreaker.recordErrorRequestCount();
            }
            completableFuture.complete(msg);
        } else if (code == ResponseCode.FAIL.getCode()) {
            if (circuitBreaker != null) {
                circuitBreaker.recordErrorRequestCount();
            }
            throw new ResponseException(code, ResponseCode.FAIL.getDesc());
        } else if (code == ResponseCode.RESOURCE_NOT_FOUND.getCode()) {
            if (circuitBreaker != null) {
                circuitBreaker.recordErrorRequestCount();
            }
            throw new ResponseException(code, ResponseCode.RESOURCE_NOT_FOUND.getDesc());

        } else if (code == ResponseCode.CLOSING.getCode()) {
            if (circuitBreaker != null) {
                circuitBreaker.recordErrorRequestCount();
            }
            //将当前服务从健康服务列表移除
            QRpcApplicationContext.CHANNEL_CACHE.remove((InetSocketAddress) socketAddress);

            //获取负载均衡器进行重新拉取服务选择
            //获得对应方法的负载均衡器，进行重新的loadBalance
            LoadBalancer loadBalancer = LoadBalancerFactory.CACHE.get(QRpcApplicationContext.REQUEST_THREAD_LOCAL.get().getRequestPayload().getInterfaceName());
            loadBalancer.reloadBalance(
                    QRpcApplicationContext.REQUEST_THREAD_LOCAL.get().getRequestPayload().getInterfaceName(),
                    QRpcApplicationContext.CHANNEL_CACHE.keySet().stream().toList()
            );
            throw new ResponseException(code, ResponseCode.CLOSING.getDesc());
        }
    }
}
