package icu.chiou.common.exceptions;

/**
 * Author: chiou
 * createTime: 2023/7/25
 * Description: 网络异常
 */
public class NetworkException extends RuntimeException {
    public NetworkException() {
    }

    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(Throwable cause) {
        super(cause);
    }
}
