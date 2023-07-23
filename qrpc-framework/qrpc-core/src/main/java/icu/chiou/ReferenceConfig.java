package icu.chiou;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Author: chiou
 * createTime: 2023/7/23
 * Description: No Description
 */
public class ReferenceConfig<T> {
    private Class<T> interfaceRef;

    public void setInterface(Class<T> consumerInterface) {
        this.interfaceRef = consumerInterface;
    }


    public Class<T> getInterface() {
        return interfaceRef;
    }

    public T get() {
        //todo 使用动态代理,完成一些工作,如通过注册中心获取具体实现类
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceRef};
        Object proxyInstance = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println("hello proxy!!!");
                return null;
            }
        });
        return (T) proxyInstance;
    }
}
