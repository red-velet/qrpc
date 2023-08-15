package icu.chiou.common.exceptions;

/**
 * Author: chiou
 * createTime: 2023/8/15
 * Description: No Description
 */
public class NeedExecHeartException extends RuntimeException {
    public NeedExecHeartException() {
        super("heart request need exec now");
    }

    public NeedExecHeartException(String message) {
        super(message);
    }
}
