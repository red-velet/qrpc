package icu.chiou.core.annotation;

import icu.chiou.core.QRpcProviderPostProcessor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: chiou
 * createTime: 2023/8/10
 * Description: 开启提供方自动配置
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(QRpcProviderPostProcessor.class)
public @interface EnableQRpcProvider {
}
