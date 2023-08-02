package icu.chiou.protection;

/**
 * Author: chiou
 * createTime: 2023/8/1
 * Description: 限流器的抽象 - 规定了限流器的行为
 */
public interface RateLimiter {

    /**
     * 判断是否允许请求
     *
     * @return 是否允许请求，true-允许，false-限流
     */
    Boolean isAllowRequest();
}
