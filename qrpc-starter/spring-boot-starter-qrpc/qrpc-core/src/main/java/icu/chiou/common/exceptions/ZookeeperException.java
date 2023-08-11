package icu.chiou.common.exceptions;

/**
 * Author: chiou
 * createTime: 2023/7/23
 * Description: No Description
 */
public class ZookeeperException extends RuntimeException {
    public ZookeeperException() {
    }

    public ZookeeperException(String message) {
        super(message);
    }

    public ZookeeperException(Throwable cause) {
        super(cause);
    }
}
