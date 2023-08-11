package icu.chiou.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: chiou
 * createTime: 2023/8/2
 * Description: No Description
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QRpcService {
    String group() default "default";

    long timeout() default 5000;

    String loadBalancer() default "roundRobin";

    int retryTimes() default 3;

    long intervalTime() default 2000;
}
