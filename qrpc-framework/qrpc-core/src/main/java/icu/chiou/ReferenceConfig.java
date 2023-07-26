package icu.chiou;

import icu.chiou.discovery.Registry;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

/**
 * Author: chiou
 * createTime: 2023/7/23
 * Description: No Description
 */
@Slf4j
public class ReferenceConfig<T> {
    private Class<T> interfaceRef;
    private Registry registry;

    public void setInterface(Class<T> consumerInterface) {
        this.interfaceRef = consumerInterface;
    }


    public Class<T> getInterface() {
        return interfaceRef;
    }

    public Class<T> getInterfaceRef() {
        return interfaceRef;
    }

    public void setInterfaceRef(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    /**
     * 代理设计模式：生成api接口的代理对象
     *
     * @return 代理对象
     */
    public T get() {
        //todo 使用动态代理,完成一些工作,如通过注册中心获取具体实现类
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceRef};
        Object proxyInstance = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                log.info("methodName -> {}", method.getName());
                log.info("method args -> {}", args);
                //1.发现服务-从注册中心寻找可用服务
                //todo 每次调用该方法都需要去注册中心拉取服务列表吗?
                //     如何选择一个合适的服务,而不是只选择第一个?
                InetSocketAddress address = registry.lookup(interfaceRef.getName());
                if (log.isDebugEnabled()) {
                    log.debug("服务调用方发现了服务【{}】的可用主机【{}】", interfaceRef.getName(), address);
                }
                //2.使用netty连接服务提供者-服务器,发送【调用服务名+方法名+参数列表】,返回结果

                return null;
            }
        });
        return (T) proxyInstance;
    }
}
