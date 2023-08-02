package icu.chiou.enumeration;

/**
 * Author: chiou
 * createTime: 2023/7/27
 * Description: 枚举类-响应码
 * 成功码 20-方法成功调用  22-心跳成功返回
 * 负载码 31-服务器负载过高,被限流
 * 错误码 44-请求方法不存在 - 客户端
 * 错误码 50-请求方法不存在 - 服务端
 */
public enum ResponseCode {
    SUCCESS((byte) 20, "方法调用成功"),
    SUCCESS_HEART_DANCE((byte) 22, "心跳检测成功"),
    RATE_LIMIT((byte) 31, "服务端限流"),
    RESOURCE_NOT_FOUND((byte) 44, "请求资源不存在"),
    FAIL((byte) 50, "方法调用发生异常"),
    ;
    private byte code;
    private String desc;

    ResponseCode(byte code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
