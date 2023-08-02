package icu.chiou.channelhandler.handler;

import icu.chiou.QRpcBootstrap;
import icu.chiou.enumeration.ResponseCode;
import icu.chiou.exceptions.ResponseException;
import icu.chiou.protection.CircuitBreaker;
import icu.chiou.transport.message.QRpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * Author: chiou
 * createTime: 2023/7/26
 * Description: 这是一个用于测试的类
 */
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<QRpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, QRpcResponse msg) throws Exception {
        //获取响应码
        byte code = msg.getCode();

        //获取当前ip的熔断器
        SocketAddress socketAddress = ctx.channel().remoteAddress();
        CircuitBreaker circuitBreaker = QRpcBootstrap.getInstance().getConfiguration().everyIpBreaker.get(socketAddress);

        //从全局挂起的前期中获取与之匹配的、待处理的completeFuture
        CompletableFuture<Object> completableFuture = QRpcBootstrap.PENDING_REQUEST.get(msg.getRequestId());

        if (code == ResponseCode.SUCCESS.getCode()) {
            //服务提供方返回的结果
            Object returnValue = msg.getBody();
            completableFuture.complete(returnValue);
            //日志记录
            if (log.isDebugEnabled()) {
                log.debug("寻找到编号【{}】completableFuture处理响应结果", msg.getRequestId());
            }
        } else if (code == ResponseCode.SUCCESS_HEART_DANCE.getCode()) {
            completableFuture.complete(null);
            if (log.isDebugEnabled()) {
                log.debug("寻找到编号为【{}】的completableFuture,处理心跳检测，处理响应结果。", msg.getRequestId());
            }
        } else if (code == ResponseCode.RATE_LIMIT.getCode()) {
            circuitBreaker.recordErrorRequestCount();
            log.error("当前id【{}】请求,被限流,响应码【{}】", msg.getRequestId(), code);
            throw new ResponseException(code, ResponseCode.RATE_LIMIT.getDesc());

        } else if (code == ResponseCode.FAIL.getCode()) {
            circuitBreaker.recordErrorRequestCount();
            log.error("当前id【{}】请求,返回错误的结果,响应码【{}】", msg.getRequestId(), code);
            throw new ResponseException(code, ResponseCode.FAIL.getDesc());

        } else if (code == ResponseCode.RESOURCE_NOT_FOUND.getCode()) {
            circuitBreaker.recordErrorRequestCount();
            log.error("当前id【{}】请求,未找到目标资源,响应码【{}】", msg.getRequestId(), code);
            throw new ResponseException(code, ResponseCode.RESOURCE_NOT_FOUND.getDesc());

        }
    }
}
