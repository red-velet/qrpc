package icu.chiou.netty;

import icu.chiou.common.enumeration.RequestType;
import icu.chiou.common.enumeration.ResponseCode;
import icu.chiou.core.QRpcApplicationContext;
import icu.chiou.protection.RateLimiter;
import icu.chiou.protection.ShutdownHolder;
import icu.chiou.protection.TokenBucketRateLimiter;
import icu.chiou.protocol.ServiceConfig;
import icu.chiou.protocol.transport.QRpcRequest;
import icu.chiou.protocol.transport.QRpcResponse;
import icu.chiou.protocol.transport.RequestPayload;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Objects;

/**
 * Author: chiou
 * createTime: 2023/7/27
 * Description: No Description
 */
@Slf4j
public class MethodInvokeHandler extends SimpleChannelInboundHandler<QRpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, QRpcRequest msg) throws Exception {
        //1.封装响应
        QRpcResponse qRpcResponse = QRpcResponse.builder()
                .requestId(msg.getRequestId())
                .serializeType(msg.getSerializeType())
                .compressType(msg.getCompressType())
                .build();

        //2.获取通道
        Channel channel = ctx.channel();

        //3.查看挡板状态,挡板如已开启,直接返回一个响应
        if (ShutdownHolder.IS_GATE_OPEN.get()) {
            qRpcResponse.setCode(ResponseCode.CLOSING.getCode());
            channel.writeAndFlush(qRpcResponse);
        }

        //4.请求计数器加一
        ShutdownHolder.REQUEST_COUNTER.increment();

        //心跳请求和其它请求
        if (msg.getRequestType() == RequestType.HEART_DANCE.getId()) {
            //处理心跳请求
            //日志记录
            if (log.isDebugEnabled()) {
                log.debug("请求【{}】为心跳请求,已经在服务端接收到", msg.getRequestId());
            }
            qRpcResponse.setCode(ResponseCode.SUCCESS_HEART_DANCE.getCode());
            //6.写出响应
            ctx.channel().writeAndFlush(qRpcResponse);

            //7.请求计数器减一
            ShutdownHolder.REQUEST_COUNTER.decrement();
            return;
        } else {
            //正常方法调用
            //查看服务是否设置了限流器
            //1.服务先被设置限流才开启--发布服务的时候需要进行判断-有就设置缓存    2.具体有哪些ip被限流
            RequestPayload payload = msg.getRequestPayload();
            TokenBucketRateLimiter limiter = (TokenBucketRateLimiter) QRpcApplicationContext.LIMITER_SERVER_LIST.get(payload.getInterfaceName());
            if (limiter != null) {
                //5.添加限流操作
                //5.1添加缓存
                //为当前ip添加限流
                SocketAddress socketAddress = channel.remoteAddress();
                Map<SocketAddress, RateLimiter> everyIpRateLimiter = QRpcApplicationContext.getInstance().getEveryIpRateLimiter();
                //没有就为当前ip创建一个限流器
                RateLimiter ipLimiter = everyIpRateLimiter.get(socketAddress);
                if (Objects.isNull(ipLimiter)) {
                    int capacity = limiter.getCapacity();
                    int rate = limiter.getRate();
                    ipLimiter = new TokenBucketRateLimiter(capacity, rate);
                    everyIpRateLimiter.put(socketAddress, ipLimiter);
                }

                //5.2限流
                Boolean pass = ipLimiter.isAllowRequest();
                if (!pass) {
                    //被限流了-直接返回
                    qRpcResponse.setCode(ResponseCode.RATE_LIMIT.getCode());
                    //6.写出响应
                    ctx.channel().writeAndFlush(qRpcResponse);

                    //7.请求计数器减一
                    ShutdownHolder.REQUEST_COUNTER.decrement();
                } else {
                    //正常方法调用
                    //1.获取负载内容
                    try {
                        //2.根据负载内容进行方法调用
                        Object result = invokeTargetMethod(payload);
                        //日志记录
                        if (log.isDebugEnabled()) {
                            log.debug("请求【{}】已经在服务端完成方法的调用", msg.getRequestId());
                        }
                        qRpcResponse.setCode(ResponseCode.SUCCESS.getCode());
                        qRpcResponse.setBody(result);
                    } catch (RuntimeException e) {
                        log.error("调用服务【{}】的【{}】方法时发生异常，参数【{}】", payload.getInterfaceName(), payload.getMethodName(), payload.getParamsValue(), e);
                        qRpcResponse.setCode(ResponseCode.FAIL.getCode());
                    }
                }

            }
            //该服务如果没有限流器就直接,获取负载内容进行方法调用
            try {
                //2.根据负载内容进行方法调用
                Object result = invokeTargetMethod(payload);
                //日志记录
                if (log.isDebugEnabled()) {
                    log.debug("请求【{}】已经在服务端完成方法的调用", msg.getRequestId());
                }
                qRpcResponse.setCode(ResponseCode.SUCCESS.getCode());
                qRpcResponse.setBody(result);
            } catch (RuntimeException e) {
                log.error("调用服务【{}】的【{}】方法时发生异常，参数【{}】", payload.getInterfaceName(), payload.getMethodName(), payload.getParamsValue(), e);
                qRpcResponse.setCode(ResponseCode.FAIL.getCode());
            }
        }

        //6.写出响应
        ctx.channel().writeAndFlush(qRpcResponse);

        //7.请求计数器减一
        ShutdownHolder.REQUEST_COUNTER.decrement();
    }

    /**
     * 调用目标对象的方法
     *
     * @param payload 目标对象
     * @return 方法的返回值
     */
    private Object invokeTargetMethod(RequestPayload payload) {
        String methodName = payload.getMethodName();
        String interfaceName = payload.getInterfaceName();
        Class<?>[] paramsType = payload.getParamsType();
        Class<?> returnType = payload.getReturnType();
        Object[] paramsValue = payload.getParamsValue();

        //寻找到匹配的暴露出去的具体实例的方法，进行调用
        ServiceConfig<?> serviceConfig = QRpcApplicationContext.SERVICE_LIST.get(interfaceName);
        Object refImpl = serviceConfig.getRef();

        //通过反射进行调用
        Class<?> clazz = refImpl.getClass();
        Method method = null;
        Object returnValue = null;
        try {
            method = clazz.getMethod(methodName, paramsType);
            returnValue = method.invoke(refImpl, paramsValue);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return returnValue;
    }
}
