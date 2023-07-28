package icu.chiou.exceptions;

/**
 * Author: chiou
 * createTime: 2023/7/28
 * Description: 序列化时发生异常
 */
public class SerializeException extends RuntimeException {
    public SerializeException() {
    }

    public SerializeException(String message) {
        super(message);
    }

    public SerializeException(Throwable cause) {
        super(cause);
    }
}
