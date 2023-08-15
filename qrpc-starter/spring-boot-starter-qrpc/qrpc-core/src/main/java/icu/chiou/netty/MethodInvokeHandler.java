package icu.chiou.netty;

import icu.chiou.common.enumeration.ResponseCode;
import icu.chiou.core.QRpcApplicationContext;
import icu.chiou.protocol.ServiceConfig;
import icu.chiou.protocol.transport.QRpcRequest;
import icu.chiou.protocol.transport.QRpcResponse;
import icu.chiou.protocol.transport.RequestPayload;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Author: chiou
 * createTime: 2023/7/27
 * Description: 方法调用处理器
 */
@Slf4j
public class MethodInvokeHandler extends SimpleChannelInboundHandler<QRpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, QRpcRequest msg) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("➡️enter MethodInvokeHandler");
        }
        //1.封装响应
        QRpcResponse qRpcResponse = QRpcResponse.builder()
                .requestId(msg.getRequestId())
                .serializeType(msg.getSerializeType())
                .compressType(msg.getCompressType())
                .build();

        //2.获取通道
        RequestPayload payload = msg.getRequestPayload();
        try {
            //3.方法调用
            Object result = invokeTargetMethod(payload);
            if (log.isDebugEnabled()) {
                log.debug("请求【{}】已经在服务端完成方法的调用", msg.getRequestId());
            }
            qRpcResponse.setCode(ResponseCode.SUCCESS.getCode());
            qRpcResponse.setBody(result);
        } catch (RuntimeException e) {
            log.error("调用服务【{}】的【{}】方法时发生异常，参数【{}】", payload.getInterfaceName(), payload.getMethodName(), payload.getParamsValue(), e);
            qRpcResponse.setCode(ResponseCode.FAIL.getCode());
        }

        //4.写出响应
        ctx.channel().writeAndFlush(qRpcResponse);
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
