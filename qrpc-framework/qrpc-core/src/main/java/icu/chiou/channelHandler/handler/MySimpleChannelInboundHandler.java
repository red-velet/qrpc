package icu.chiou.channelHandler.handler;

import icu.chiou.QRpcBootstrap;
import icu.chiou.transport.message.QRpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

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
        //服务提供方返回的结果
        Object returnValue = msg.getBody();

        //从全局挂起的前期中获取与之匹配的、待处理的completeFuture
        CompletableFuture<Object> completableFuture = QRpcBootstrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(returnValue);
        //日志记录
        if (log.isDebugEnabled()) {
            log.debug("寻找到编号【{}】completableFuture处理响应结果", msg.getRequestId());
        }
    }
}
