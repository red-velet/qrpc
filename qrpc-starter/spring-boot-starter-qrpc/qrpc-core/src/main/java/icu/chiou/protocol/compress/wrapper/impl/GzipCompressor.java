package icu.chiou.protocol.compress.wrapper.impl;

import icu.chiou.common.exceptions.CompressException;
import icu.chiou.protocol.compress.Compressor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Author: chiou
 * createTime: 2023/7/28
 * Description: gzip压缩器
 */
@Slf4j
public class GzipCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] bytes) {
        //todo 联系gzip压缩的api
        // 本质就是将byte数组输入,将结果输出到另一个字节数组

        try (
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        ) {
            gzipOutputStream.write(bytes);
            gzipOutputStream.finish();
            if (log.isDebugEnabled()) {
                log.debug("请求报文内对象【{}】,使用gzip方式成功完成了【压缩】操作", bytes.getClass());
            }
            byte[] afterCompressArray = byteArrayOutputStream.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("使用gzip方式成功完成【压缩】操作后的数组长度:{} -> {}", bytes.length, afterCompressArray.length);
            }
            return afterCompressArray;
        } catch (IOException e) {
            log.error("压缩对象【{}】时出现异常:", bytes.getClass());
            throw new CompressException("gzip【压缩】时出现异常");
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
        ) {
            byte[] afterDecompressArray = gzipInputStream.readAllBytes();
            if (log.isDebugEnabled()) {
                log.debug("响应报文内对象【{}】,使用gzip方式成功完成了【解压缩】操作", bytes.getClass());
            }
            if (log.isDebugEnabled()) {
                log.debug("使用gzip方式成功完成【解压缩】操作后的数组长度:{} --> {}", bytes.length, afterDecompressArray.length);
            }
            return afterDecompressArray;
        } catch (IOException e) {
            log.error("解压缩对象【{}】时出现异常:", bytes.getClass());
            throw new CompressException("gzip【解压缩】时出现异常");
        }
    }
}
