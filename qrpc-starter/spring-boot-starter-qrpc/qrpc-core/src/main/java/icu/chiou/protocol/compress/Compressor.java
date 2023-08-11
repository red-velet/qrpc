package icu.chiou.protocol.compress;

/**
 * Author: chiou
 * createTime: 2023/7/28
 * Description: 抽象的压缩器,规定压缩器的行为
 */
public interface Compressor {
    /**
     * 压缩的方法: 把字节数组压缩变小
     *
     * @param bytes 字节数组
     * @return 压缩后的字节数组
     */
    byte[] compress(byte[] bytes);

    /**
     * 解压缩的方法: 把字节数组解压缩
     *
     * @param bytes 字节数组
     * @return 解压后的字节数组
     */
    byte[] decompress(byte[] bytes);

}
