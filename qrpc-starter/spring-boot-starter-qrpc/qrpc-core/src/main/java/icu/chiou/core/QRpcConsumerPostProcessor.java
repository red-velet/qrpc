package icu.chiou.core;

import icu.chiou.config.spi.SpiLoader;
import icu.chiou.consumer.RpcConsumerInvocationHandler;
import icu.chiou.core.annotation.CircuitBreaker;
import icu.chiou.core.annotation.QRpcService;
import icu.chiou.discovery.HeartbeatDetector;
import icu.chiou.discovery.registry.RegistryFactory;
import icu.chiou.filter.FilterFactory;
import icu.chiou.protocol.compress.CompressionFactory;
import icu.chiou.protocol.serialize.SerializationFactory;
import icu.chiou.router.LoadBalancerFactory;
import icu.chiou.utils.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Author: chiou
 * createTime: 2023/8/10
 * Description: No Description
 */
@Slf4j
public class QRpcConsumerPostProcessor implements InitializingBean, BeanPostProcessor, EnvironmentAware {
    private QRpcProperties qRpcProperties;

    @Override
    public void setEnvironment(Environment environment) {
        QRpcProperties properties = QRpcProperties.getInstance();
        PropertiesUtil.init(properties, environment);
        qRpcProperties = properties;
        log.info("🚀读取配置文件成功........");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //加载spi
        RegistryFactory.init();
        SerializationFactory.init();
        CompressionFactory.init();
        LoadBalancerFactory.init();
        FilterFactory.initConsumerFilter();

        log.info("🚀spi加载成功........\n" +
                "ileCache {}\n" +
                "contentCache {}", SpiLoader.fileCache, SpiLoader.contentCache);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            QRpcService qRpcService = field.getAnnotation(QRpcService.class);
            if (qRpcService != null) {
                Class<?> fieldClass = field.getType();
                //使用动态代理,完成一些工作,如通过注册中心获取具体实现类
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                Class[] classes = new Class[]{fieldClass};

                //查看断路器是否开启
                CircuitBreaker circuitBreaker = field.getAnnotation(CircuitBreaker.class);
                boolean isCircuitBreaker = false;
                icu.chiou.protection.CircuitBreaker breaker = null;
                if (circuitBreaker != null) {
                    breaker = new icu.chiou.protection.CircuitBreaker(circuitBreaker.allowMaxErrorRequest(), circuitBreaker.allErrorRate());
                }

                InvocationHandler invocationHandler = new RpcConsumerInvocationHandler(
                        RegistryFactory.get(qRpcProperties.getRegistryType()),
                        fieldClass,
                        qRpcService.group(),
                        qRpcService.retryTimes(),
                        qRpcService.intervalTime(),
                        qRpcService.loadBalancer(),
                        qRpcService.timeout(),
                        breaker
                );
                Object proxyInstance = null;
                try {
                    //使用动态代理生成代理对象
                    proxyInstance = Proxy.newProxyInstance(classLoader, classes, invocationHandler);

                } catch (Exception e) {
                    throw new RuntimeException("创建动态代理对象发生异常:", e);
                }

                try {
                    field.setAccessible(true);
                    field.set(bean, proxyInstance);
                } catch (IllegalAccessException e) {
                    log.error("创建动态dialing对象失败" + e);
                }

                //开启心跳检测
                HeartbeatDetector.detectorHeaderDance(fieldClass.getName());
            }
        }
        return bean;
    }
}
