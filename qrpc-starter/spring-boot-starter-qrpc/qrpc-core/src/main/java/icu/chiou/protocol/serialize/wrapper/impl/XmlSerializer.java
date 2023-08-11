package icu.chiou.protocol.serialize.wrapper.impl;


import icu.chiou.protocol.serialize.Serializer;

/**
 * Author: chiou
 * createTime: 2023/7/28
 * Description: xml格式的序列化器
 */
public class XmlSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        return new byte[0];
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return null;
    }
}
