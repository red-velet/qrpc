package icu.chiou.netty;

import icu.chiou.common.enumeration.RequestType;
import icu.chiou.common.enumeration.ResponseCode;
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
 * Description: No Description
 */
@Slf4j
public class ProviderInvokeBeforeHandler extends SimpleChannelInboundHandler<QRpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, QRpcRequest msg) {
        log.info("---->>>>>>> enter TokenProviderHandler");
        if (msg.getRequestType() == RequestType.HEART_DANCE.getId()) {
            QRpcResponse qRpcResponse = QRpcResponse.builder()
                    .requestId(msg.getRequestId())
                    .serializeType(msg.getSerializeType())
                    .compressType(msg.getCompressType())
                    .code(ResponseCode.SUCCESS_HEART_DANCE.getCode())
                    .build();
            ctx.channel().writeAndFlush(qRpcResponse);
            log.info("---->>>>>>> enter TokenProviderHandler 心跳返回");
            return;
        }
        log.info("---->>>>>>> enter TokenProviderHandler 比较token");
        FilterData filterData = new FilterData(msg.getRequestPayload());
        FilterChain provdierBeforeFilterChain = FilterFactory.getProvdierBeforeFilterChain();
        try {
            provdierBeforeFilterChain.doFilter(filterData);
            ctx.fireChannelRead(msg);
            if (log.isDebugEnabled()) {
                log.debug("身份校验通过,token is match");
            }
        } catch (Exception e) {
            log.error("TokenProviderHandler exception: --> ", e);
            QRpcResponse qRpcResponse = QRpcResponse.builder()
                    .requestId(msg.getRequestId())
                    .serializeType(msg.getSerializeType())
                    .compressType(msg.getCompressType())
                    .code(ResponseCode.UNAUTHENTICATED.getCode())
                    .build();
            ctx.channel().writeAndFlush(qRpcResponse);
        }
    }
}
