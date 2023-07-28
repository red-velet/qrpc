package icu.chiou.serialize.wrapper;

import icu.chiou.serialize.Serializer;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Author: chiou
 * createTime: 2023/7/28
 * Description: No Description
 */
@AllArgsConstructor
@Getter
public class SerializerWrapper {
    private byte code;
    private String serializeType;
    private Serializer serializer;
}
