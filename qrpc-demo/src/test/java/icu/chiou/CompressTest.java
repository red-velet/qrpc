package icu.chiou;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Author: chiou
 * createTime: 2023/7/21
 * Description: 压缩类使用-测试
 */
public class CompressTest {
    @Test
    public void testGzipCompress() throws IOException {
        //todo 联系gzip压缩的api
        // 本质就是将byte数组输入,将结果输出到另一个字节数组
        byte[] bytes = new byte[]{1, 2, 5, 8, 1, 22, 33, 21, 55, 6, 123, 73, 12, 52, 123, 73, 12, 52, 33, 21};
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        gzipOutputStream.write(bytes);
        gzipOutputStream.finish();
        byte[] toByteArray = byteArrayOutputStream.toByteArray();
        System.out.println("未压缩的长度" + bytes.length);
        System.out.println("压缩后的长度" + toByteArray.length);
        System.out.println("压缩后的数据" + Arrays.toString(toByteArray));
    }

    @Test
    public void testGzipDecompress() throws IOException {
        //todo 联系gzip解压缩的api
        byte[] source = new byte[]{31, -117, 8, 0, 0, 0, 0, 0, 0, -1, 99, 100, 98, -27, 96, 20, 83, 20, 53, 103, -85, -10, -28, 49, 1, 97, 69, 81, 0, 59, 80, -80, 110, 20, 0, 0, 0};
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(source);
        GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
        byte[] bytes1 = gzipInputStream.readAllBytes();
        System.out.println("解压前的长度" + source.length);
        System.out.println("解压后的长度" + bytes1.length);
        System.out.println("解压后的数据" + Arrays.toString(bytes1));
    }
}
