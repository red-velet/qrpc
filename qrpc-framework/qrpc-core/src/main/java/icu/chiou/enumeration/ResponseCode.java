package icu.chiou.enumeration;

/**
 * Author: chiou
 * createTime: 2023/7/27
 * Description: 枚举类-响应码
 */
public enum ResponseCode {
    SUCCESS((byte) 1, "成功"), FAIL((byte) 2, "失败");
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
