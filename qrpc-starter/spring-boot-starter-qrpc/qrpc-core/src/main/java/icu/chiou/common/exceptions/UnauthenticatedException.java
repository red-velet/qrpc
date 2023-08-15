package icu.chiou.common.exceptions;

/**
 * Author: chiou
 * createTime: 2023/8/15
 * Description: 身份认证未通过异常
 */
public class UnauthenticatedException extends RuntimeException {
    public UnauthenticatedException(String message) {
        super(message);
    }
}
