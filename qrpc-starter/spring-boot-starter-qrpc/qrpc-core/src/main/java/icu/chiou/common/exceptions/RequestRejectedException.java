package icu.chiou.common.exceptions;

/**
 * Author: chiou
 * createTime: 2023/8/15
 * Description: No Description
 */
public class RequestRejectedException extends RuntimeException {
    public RequestRejectedException() {
        super("Request is be excluded,New requests are currently rejected");
    }

    public RequestRejectedException(String message) {
        super(message);
    }
}

