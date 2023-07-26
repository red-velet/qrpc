package icu.chiou;

import icu.chiou.discovery.Registry;
import icu.chiou.proxy.handler.RpcConsumerInvocationHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

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
        InvocationHandler invocationHandler = new RpcConsumerInvocationHandler(registry, interfaceRef);

        //使用动态代理生成代理对象
        Object proxyInstance = Proxy.newProxyInstance(classLoader, classes, invocationHandler);
        return (T) proxyInstance;
    }
}
