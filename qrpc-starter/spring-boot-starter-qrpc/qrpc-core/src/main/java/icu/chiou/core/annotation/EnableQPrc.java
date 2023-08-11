package icu.chiou.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: chiou
 * createTime: 2023/8/10
 * Description: 都开启配置
 */
@EnableQRpcConsumer
@EnableQRpcProvider
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableQPrc {
}
