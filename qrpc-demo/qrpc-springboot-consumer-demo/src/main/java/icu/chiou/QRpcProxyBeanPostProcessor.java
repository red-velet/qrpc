package icu.chiou;

import icu.chiou.annotation.QRpcService;
import icu.chiou.proxy.QRpcProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * Author: chiou
 * createTime: 2023/8/2
 * Description: No Description
 */
@Component
public class QRpcProxyBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            QRpcService qRpcService = field.getAnnotation(QRpcService.class);
            if (qRpcService != null) {
                Class<?> type = field.getType();
                Object proxy = QRpcProxyFactory.getProxy(type);
                field.setAccessible(true);
                try {
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return bean;
    }
}
