package icu.chiou.common.exceptions;

/**
 * Author: chiou
 * createTime: 2023/7/30
 * Description: No Description
 */
public class LoadBalancerException extends RuntimeException {

    public LoadBalancerException() {
    }

    public LoadBalancerException(String message) {
        super(message);
    }

    public LoadBalancerException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoadBalancerException(Throwable cause) {
        super(cause);
    }

    public LoadBalancerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
