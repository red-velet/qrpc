package icu.chiou.exceptions;

/**
 * Author: chiou
 * createTime: 2023/7/28
 * Description: 压缩时发生异常
 */
public class CompressException extends RuntimeException {
    public CompressException() {
    }

    public CompressException(String message) {
        super(message);
    }

    public CompressException(Throwable cause) {
        super(cause);
    }
}
