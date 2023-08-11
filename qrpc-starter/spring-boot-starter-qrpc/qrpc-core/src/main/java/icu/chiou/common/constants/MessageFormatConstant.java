package icu.chiou.common.constants;

/**
 * Author: chiou
 * createTime: 2023/7/26
 * Description: No Description
 * <pre>
 *  *  *   0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22
 *  *  *   +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
 *  *  *   |    magic          |ver |head  len|    full length    | qt | ser|comp|              RequestId                |
 *  *  *   +-----+-----+-------+----+----+----+----+-----------+----- ---+--------+----+----+----+----+----+----+---+---+
 *  *  *   |                                                                                                             |
 *  *  *   |                                         body                                                                |
 *  *  *   |                                                                                                             |
 *  *  *   +--------------------------------------------------------------------------------------------------------+---+
 * </pre>
 */
public class MessageFormatConstant {
    public static final byte[] MAGIC_VALUE = "qprc".getBytes();
    public static final byte VERSION_VALUE = 1;
    public static final short HEADER_LENGTH_VALUE = (byte) (MAGIC_VALUE.length + 1 + 2 + 4 + 1 + 1 + 1 + 8);
    public static final int MAX_FRAME_LENGTH = 1024 * 1024;
    public static final int MAGIC_LENGTH = 4;
    public static final int VERSION_LENGTH = 1;
    public static final int HEADER_LENGTH_LENGTH = 2;
    public static final int FULL_LENGTH_OFFSET = MAGIC_LENGTH + VERSION_LENGTH + HEADER_LENGTH_LENGTH;
    public static final int FULL_LENGTH_LENGTH = 4;
    public static final int ADJUSTMENT_LENGTH = -(FULL_LENGTH_OFFSET + FULL_LENGTH_LENGTH);
    public static final int INITIAL_BYTES_TO_STRIP = 0;
}
