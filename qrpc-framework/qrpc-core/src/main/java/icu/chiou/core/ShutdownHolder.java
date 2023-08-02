package icu.chiou.core;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

/**
 * Author: chiou
 * createTime: 2023/8/2
 * Description: No Description
 */
public class ShutdownHolder {
    //挡板
    public static AtomicBoolean IS_GATE_OPEN = new AtomicBoolean(false);
    //请求计数器
    public static LongAdder REQUEST_COUNTER = new LongAdder();
}
