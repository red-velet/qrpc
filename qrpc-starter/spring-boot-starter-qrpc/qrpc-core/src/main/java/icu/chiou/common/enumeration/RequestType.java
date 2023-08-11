package icu.chiou.common.enumeration;

/**
 * 作者：chiou
 * 创建时间：2023/7/27
 * 描述：请求类型
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

    private byte id; // 请求类型的唯一标识符
    private String type; // 请求类型的描述

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
