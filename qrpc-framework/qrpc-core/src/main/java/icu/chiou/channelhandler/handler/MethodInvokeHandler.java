package icu.chiou.channelhandler.handler;

import icu.chiou.QRpcBootstrap;
import icu.chiou.ServiceConfig;
import icu.chiou.enumeration.RequestType;
import icu.chiou.enumeration.ResponseCode;
import icu.chiou.protection.RateLimiter;
import icu.chiou.protection.TokenBucketRateLimiter;
import icu.chiou.transport.message.QRpcRequest;
import icu.chiou.transport.message.QRpcResponse;
import icu.chiou.transport.message.RequestPayload;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.Map;

/**
 * Author: chiou
 * createTime: 2023/7/27
 * Description: No Description
 */
@Slf4j
public class MethodInvokeHandler extends SimpleChannelInboundHandler<QRpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, QRpcRequest msg) throws Exception {
        //封装响应
        QRpcResponse qRpcResponse = QRpcResponse.builder()
                .requestId(msg.getRequestId())
                .serializeType(msg.getSerializeType())
                .compressType(msg.getCompressType())
                .build();

        //添加限流操作
        //添加缓存
        Channel channel = ctx.channel();
        SocketAddress socketAddress = channel.remoteAddress();
        Map<SocketAddress, RateLimiter> everyIpRateLimiter = QRpcBootstrap.getInstance().getConfiguration().getEveryIpRateLimiter();
        RateLimiter rateLimiter = everyIpRateLimiter.get(socketAddress);
        if (rateLimiter == null) {
            rateLimiter = new TokenBucketRateLimiter(20, 20);
            everyIpRateLimiter.put(socketAddress, rateLimiter);
        }

        //限流
        Boolean pass = rateLimiter.isAllowRequest();
        if (!pass) {
            qRpcResponse.setCode(ResponseCode.RATE_LIMIT.getCode());
        } else if (msg.getRequestType() == RequestType.HEART_DANCE.getId()) {//心跳
            //日志记录
            if (log.isDebugEnabled()) {
                log.debug("请求【{}】为心跳请求,已经在服务端接收到", msg.getRequestId());
            }
            qRpcResponse.setCode(ResponseCode.SUCCESS_HEART_DANCE.getCode());
        } else {//方法调用
            //1.获取负载内容
            RequestPayload payload = msg.getRequestPayload();
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

        //4.写出响应
        ctx.channel().writeAndFlush(qRpcResponse);
    }

    public static Integer count = 1;

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
        ServiceConfig<?> serviceConfig = QRpcBootstrap.SERVICE_LIST.get(interfaceName);
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
//        if (count > 0) {
//            try {
//                Thread.sleep(12000);
//                count--;
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
        return returnValue;
    }
}
