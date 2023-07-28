package icu.chiou.compress.wrapper;

import icu.chiou.compress.Compressor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Author: chiou
 * createTime: 2023/7/28
 * Description: 压缩器的包装类
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CompressorWrapper {
    private byte code;
    private String compressType;
    private Compressor compressor;
}
