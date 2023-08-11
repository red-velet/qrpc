package icu.chiou.protocol.compress;

import icu.chiou.config.spi.SpiLoader;

/**
 * Author: chiou
 * createTime: 2023/8/10
 * Description: No Description
 */
public class CompressionFactory {
    public static Compressor get(String compressType) {
        return SpiLoader.getInstance().get(compressType);
    }

    public static void init() throws Exception {
        SpiLoader.getInstance().loadExtension(Compressor.class);
    }
}
