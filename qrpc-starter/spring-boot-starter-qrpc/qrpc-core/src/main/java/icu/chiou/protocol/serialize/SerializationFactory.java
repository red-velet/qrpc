package icu.chiou.protocol.serialize;

import icu.chiou.config.spi.SpiLoader;

/**
 * Author: chiou
 * createTime: 2023/8/10
 * Description: No Description
 */
public class SerializationFactory {
    public static Serializer get(String serialization) {
        return SpiLoader.getInstance().get(serialization);
    }

    public static void init() throws Exception {
        SpiLoader.getInstance().loadExtension(Serializer.class);
    }
}
