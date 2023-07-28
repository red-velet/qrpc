package icu.chiou.compress;

import icu.chiou.compress.wrapper.CompressorWrapper;
import icu.chiou.compress.wrapper.impl.GzipCompressor;
import icu.chiou.exceptions.CompressException;
import icu.chiou.exceptions.SerializeException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: chiou
 * createTime: 2023/7/28
 * Description: 压缩器工厂
 */
@Slf4j
public class CompressorFactory {
    private static final ConcurrentHashMap<String, CompressorWrapper> COMPRESSOR_CACHE = new ConcurrentHashMap<>(8);
    private static final ConcurrentHashMap<Byte, CompressorWrapper> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>(8);

    static {
        CompressorWrapper gzipWrapper = new CompressorWrapper((byte) 1, "jdk", new GzipCompressor());
        COMPRESSOR_CACHE.put("gzip", gzipWrapper);

        COMPRESSOR_CACHE_CODE.put((byte) 1, gzipWrapper);
    }


    /**
     * 使用工厂方法获取一个CompressorWrapper
     *
     * @param compressorType 压缩器类型
     * @return CompressorWrapper
     */
    public static CompressorWrapper getCompressor(String compressorType) {
        if (compressorType == null) {
            log.error("传入的压缩器类型【{}】不合法", compressorType);
            throw new SerializeException("请传入合法的压缩器类型参数");
        }
        CompressorWrapper compressorWrapper = COMPRESSOR_CACHE.get(compressorType);
        if (compressorWrapper == null) {
            log.error("设置的压缩器类型【{}】暂不支持,请选择支持的压缩器类型", compressorType);
            throw new SerializeException("设置的压缩器类型【{}】暂不支持,请选择支持的压缩器类型");
        }
        return compressorWrapper;
    }


    /**
     * 使用工厂方法获取一个CompressorWrapper
     *
     * @param code 压缩器类型码
     * @return CompressorWrapper
     */
    public static CompressorWrapper getCompressor(byte code) {
        CompressorWrapper CompressorWrapper = COMPRESSOR_CACHE_CODE.get(code);
        if (CompressorWrapper == null) {
            log.error("设置的压缩器类型【{}】暂不支持,请选择支持的压缩器类型", code);
            throw new CompressException("设置的压缩器类型【{}】暂不支持,请选择支持的压缩器类型");
        }
        return CompressorWrapper;
    }
}
