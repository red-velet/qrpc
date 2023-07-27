package icu.chiou.enumeration;

/**
 * Author: chiou
 * createTime: 2023/7/27
 * Description: 请求类型
 */
public enum RequestType {
    REQUEST((byte) 1, "普通请求"),
    HEART_DANCE((byte) 2, "心跳请求");

    RequestType() {
    }

    RequestType(byte id, String type) {
        this.id = id;
        this.type = type;
    }

    private byte id;
    private String type;

    public byte getId() {
        return id;
    }

    public void setId(byte id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
