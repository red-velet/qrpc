package icu.chiou.serialize;

import icu.chiou.config.ObjectWrapper;
import icu.chiou.exceptions.SerializeException;
import icu.chiou.serialize.wrapper.impl.Fastjson2Serializer;
import icu.chiou.serialize.wrapper.impl.HessianSerializer;
import icu.chiou.serialize.wrapper.impl.JdkSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: chiou
 * createTime: 2023/7/28
 * Description: 序列化器工厂
 */
@Slf4j
public class SerializerFactory {
    private static final ConcurrentHashMap<String, ObjectWrapper<Serializer>> SERIALIZER_CACHE = new ConcurrentHashMap<>(8);
    private static final ConcurrentHashMap<Byte, ObjectWrapper<Serializer>> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>(8);

    static {
        ObjectWrapper<Serializer> jdkWrapper = new ObjectWrapper<Serializer>((byte) 1, "jdk", new JdkSerializer());
        ObjectWrapper<Serializer> fastjson2Wrapper = new ObjectWrapper<Serializer>((byte) 2, "json", new Fastjson2Serializer());
        ObjectWrapper<Serializer> hessianWrapper = new ObjectWrapper<Serializer>((byte) 3, "hessian", new HessianSerializer());
        SERIALIZER_CACHE.put("jdk", jdkWrapper);
        SERIALIZER_CACHE.put("json", fastjson2Wrapper);
        SERIALIZER_CACHE.put("hessian", hessianWrapper);

        SERIALIZER_CACHE_CODE.put((byte) 1, jdkWrapper);
        SERIALIZER_CACHE_CODE.put((byte) 2, fastjson2Wrapper);
        SERIALIZER_CACHE_CODE.put((byte) 3, hessianWrapper);
    }


    /**
     * 使用工厂方法获取一个SerializerWrapper
     *
     * @param serializeType 序列化类型
     * @return SerializerWrapper
     */
    public static ObjectWrapper<Serializer> getSerializer(String serializeType) {
        if (serializeType == null) {
            log.error("传入的序列化类型【{}】不合法", serializeType);
            throw new SerializeException("请传入合法的序列化类型参数");
        }
        ObjectWrapper<Serializer> objectWrapper = SERIALIZER_CACHE.get(serializeType);
        if (objectWrapper == null) {
            log.error("设置的序列化类型【{}】暂不支持,请选择支持的序列化类型", serializeType);
            throw new SerializeException("设置的序列化类型【{}】暂不支持,请选择支持的序列化类型");
        }
        return objectWrapper;
    }


    /**
     * 使用工厂方法获取一个SerializerWrapper
     *
     * @param code 序列化类型码
     * @return SerializerWrapper
     */
    public static ObjectWrapper<Serializer> getSerializer(byte code) {
        ObjectWrapper<Serializer> objectWrapper = SERIALIZER_CACHE_CODE.get(code);
        if (objectWrapper == null) {
            log.error("设置的序列化类型【{}】暂不支持,请选择支持的序列化类型", code);
            throw new SerializeException("设置的序列化类型【{}】暂不支持,请选择支持的序列化类型");
        }
        return objectWrapper;
    }

    /**
     * 添加一个新的序列化策略
     *
     * @param objectWrapper 包装类
     */
    public static void addSerializer(ObjectWrapper<Serializer> objectWrapper) {
        SERIALIZER_CACHE.put(objectWrapper.getName(), objectWrapper);
        SERIALIZER_CACHE_CODE.put(objectWrapper.getCode(), objectWrapper);
    }
}
