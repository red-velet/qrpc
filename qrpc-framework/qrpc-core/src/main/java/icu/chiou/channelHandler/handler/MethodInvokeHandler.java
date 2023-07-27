package icu.chiou.channelHandler.handler;

import icu.chiou.QRpcBootstrap;
import icu.chiou.ServiceConfig;
import icu.chiou.transport.message.QRpcRequest;
import icu.chiou.transport.message.RequestPayload;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Author: chiou
 * createTime: 2023/7/27
 * Description: No Description
 */
@Slf4j
public class MethodInvokeHandler extends SimpleChannelInboundHandler<QRpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, QRpcRequest msg) throws Exception {
        //1.获取负载内容
        RequestPayload payload = msg.getRequestPayload();

        //2.根据负载内容进行方法调用
        Object obj = invokeTargetMethod(payload);

        //3.封装响应

        //4.写出响应

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
            log.error("调用服务【{}】的【{}】方法时发生异常，参数【{}】", interfaceName, methodName, paramsValue, e);
            throw new RuntimeException(e);
        }

        return returnValue;
    }
}
