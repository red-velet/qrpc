package icu.chiou.protocol.compress;

import icu.chiou.common.exceptions.CompressException;
import icu.chiou.common.exceptions.SerializeException;
import icu.chiou.config.ObjectWrapper;
import icu.chiou.protocol.compress.wrapper.impl.GzipCompressor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: chiou
 * createTime: 2023/7/28
 * Description: 压缩器工厂
 */
@Slf4j
public class CompressorFactory {
    private static final ConcurrentHashMap<String, ObjectWrapper<Compressor>> COMPRESSOR_CACHE = new ConcurrentHashMap<>(8);
    private static final ConcurrentHashMap<Byte, ObjectWrapper<Compressor>> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>(8);

    static {
        ObjectWrapper<Compressor> objectWrapper = new ObjectWrapper<>((byte) 1, "jdk", new GzipCompressor());
        COMPRESSOR_CACHE.put("gzip", objectWrapper);

        COMPRESSOR_CACHE_CODE.put((byte) 1, objectWrapper);
    }


    /**
     * 使用工厂方法获取一个CompressorWrapper
     *
     * @param compressorType 压缩器类型
     * @return CompressorWrapper
     */
    public static ObjectWrapper<Compressor> getCompressor(String compressorType) {
        if (compressorType == null) {
            log.error("传入的压缩器类型【{}】不合法", compressorType);
            throw new SerializeException("请传入合法的压缩器类型参数");
        }
        ObjectWrapper<Compressor> objectWrapper = COMPRESSOR_CACHE.get(compressorType);
        if (objectWrapper == null) {
            log.error("设置的压缩器类型【{}】暂不支持,请选择支持的压缩器类型", compressorType);
            throw new SerializeException("设置的压缩器类型【{}】暂不支持,请选择支持的压缩器类型");
        }
        return objectWrapper;
    }


    /**
     * 使用工厂方法获取一个CompressorWrapper
     *
     * @param code 压缩器类型码
     * @return CompressorWrapper
     */
    public static ObjectWrapper<Compressor> getCompressor(byte code) {
        ObjectWrapper<Compressor> objectWrapper = COMPRESSOR_CACHE_CODE.get(code);
        if (objectWrapper == null) {
            log.error("设置的压缩器类型【{}】暂不支持,请选择支持的压缩器类型", code);
            throw new CompressException("设置的压缩器类型【{}】暂不支持,请选择支持的压缩器类型");
        }
        return objectWrapper;
    }

    /**
     * 给工程新增一个新的压缩策略
     *
     * @param compressorObjectWrapper
     */
    public static void addCompressor(ObjectWrapper<Compressor> compressorObjectWrapper) {
        COMPRESSOR_CACHE.put(compressorObjectWrapper.getName(), compressorObjectWrapper);
        COMPRESSOR_CACHE_CODE.put(compressorObjectWrapper.getCode(), compressorObjectWrapper);
    }
}
