package icu.chiou.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: chiou
 * createTime: 2023/8/10
 * Description: 熔断器
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CircuitBreaker {
    int allowMaxErrorRequest() default 5;

    float allErrorRate() default 0.5f;
}
