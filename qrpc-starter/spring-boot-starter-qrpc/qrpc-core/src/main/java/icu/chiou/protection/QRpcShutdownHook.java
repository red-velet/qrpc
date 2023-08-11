package icu.chiou.protection;

import lombok.extern.slf4j.Slf4j;

/**
 * Author: chiou
 * createTime: 2023/8/2
 * Description: QPRC提供方程序关闭时执行的钩子函数
 */
@Slf4j
public class QRpcShutdownHook extends Thread {
    @Override
    public void run() {
        if (log.isDebugEnabled()) {
            log.debug("🚀🚀🚀应用程序正在关闭...");
        }
        //1.开启挡板,不接受新的请求
        ShutdownHolder.IS_GATE_OPEN.set(true);
        if (log.isDebugEnabled()) {
            log.debug("🚀🚀🚀挡板已开启...");
        }
        //2.处理剩余的请求,至请求计数器归零
        long start = System.currentTimeMillis();
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (log.isDebugEnabled()) {
                log.debug("🚀🚀🚀正在处理剩余请求...");
            }
            //请求处理完 / 超时10s 关闭程序
            if (ShutdownHolder.REQUEST_COUNTER.sum() == 0L || (System.currentTimeMillis() - start) >= 10000) {
                break;
            }
        }

        //3.执行其它关闭前操作,放行

    }
}
