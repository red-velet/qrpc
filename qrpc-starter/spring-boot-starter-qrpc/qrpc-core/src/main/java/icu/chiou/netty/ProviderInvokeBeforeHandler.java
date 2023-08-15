package icu.chiou.netty;

import icu.chiou.common.enumeration.ResponseCode;
import icu.chiou.common.exceptions.NeedExecHeartException;
import icu.chiou.common.exceptions.RequestRejectedException;
import icu.chiou.common.exceptions.UnauthenticatedException;
import icu.chiou.filter.FilterChain;
import icu.chiou.filter.FilterData;
import icu.chiou.filter.FilterFactory;
import icu.chiou.protocol.transport.QRpcRequest;
import icu.chiou.protocol.transport.QRpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: chiou
 * createTime: 2023/8/14
 * Description: 方法调用前的前置处理器
 */
@Slf4j
public class ProviderInvokeBeforeHandler extends SimpleChannelInboundHandler<QRpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, QRpcRequest msg) {
        QRpcResponse qRpcResponse = QRpcResponse.builder()
                .requestId(msg.getRequestId())
                .serializeType(msg.getSerializeType())
                .compressType(msg.getCompressType())
                .build();

        FilterData filterData = new FilterData();
        filterData.setSocketAddress(ctx.channel().remoteAddress());
        filterData.setConsumerAttributes(msg.getRequestPayload().getConsumerAttributes());
        filterData.setRequestType(msg.getRequestType());
        filterData.setInterfaceName(msg.getRequestPayload().getInterfaceName());

        FilterChain provdierBeforeFilterChain = FilterFactory.getProvdierBeforeFilterChain();
        try {
            provdierBeforeFilterChain.doFilter(filterData);
            ctx.fireChannelRead(msg);
        } catch (UnauthenticatedException e) {
            qRpcResponse.setCode(ResponseCode.UNAUTHENTICATED.getCode());
            ctx.channel().writeAndFlush(qRpcResponse);
        } catch (RequestRejectedException e) {
            qRpcResponse.setCode(ResponseCode.RATE_LIMIT.getCode());
            ctx.channel().writeAndFlush(qRpcResponse);
        } catch (NeedExecHeartException e) {
            qRpcResponse.setCode(ResponseCode.SUCCESS_HEART_DANCE.getCode());
            ctx.channel().writeAndFlush(qRpcResponse);
        }
    }
}
