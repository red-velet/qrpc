package icu.chiou.constants;

import java.nio.charset.StandardCharsets;

/**
 * Author: chiou
 * createTime: 2023/7/26
 * Description: No Description
 */
public class MessageFormatConstant {
    public static final byte[] MAGIC = "qprc".getBytes(StandardCharsets.UTF_8);
    public static final byte VERSION = 1;
    public static final short HEADER_LENGTH = (short) (MAGIC.length + 1 + 2 + 4 + 1 + 1 + 1 + 8);
    
}
