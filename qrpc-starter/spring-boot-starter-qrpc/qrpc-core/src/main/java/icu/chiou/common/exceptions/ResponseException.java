package icu.chiou.common.exceptions;

import lombok.Getter;

/**
 * Author: chiou
 * createTime: 2023/8/2
 * Description: 响应异常
 */
@Getter
public class ResponseException extends RuntimeException {
    private byte code;
    private String message;

    public ResponseException(byte code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
