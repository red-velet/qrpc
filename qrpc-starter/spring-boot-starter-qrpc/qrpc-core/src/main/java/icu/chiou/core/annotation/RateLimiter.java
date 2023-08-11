package icu.chiou.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: chiou
 * createTime: 2023/8/10
 * Description: No Description
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimiter {
    int allMaxCapacity() default 20;

    int tokensPerReplenish() default 20;
}
