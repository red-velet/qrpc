package icu.chiou.exceptions;

/**
 * Author: chiou
 * createTime: 2023/7/25
 * Description: No Description
 */
public class DiscoveryException extends RuntimeException {
    public DiscoveryException() {
    }

    public DiscoveryException(String message) {
        super(message);
    }

    public DiscoveryException(Throwable cause) {
        super(cause);
    }
}
