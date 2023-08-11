package icu.chiou.protocol.serialize.wrapper;

import icu.chiou.protocol.serialize.Serializer;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Author: chiou
 * createTime: 2023/7/28
 * Description: 序列化器的包装类
 */
@Deprecated
@AllArgsConstructor
@Getter
public class SerializerWrapper {
    private byte code;
    private String serializeType;
    private Serializer serializer;
}
