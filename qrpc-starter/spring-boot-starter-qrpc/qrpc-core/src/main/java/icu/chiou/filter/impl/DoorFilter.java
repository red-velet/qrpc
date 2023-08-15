package icu.chiou.filter.impl;

import icu.chiou.common.exceptions.RequestRejectedException;
import icu.chiou.filter.FilterData;
import icu.chiou.filter.ProviderBeforeFilter;
import icu.chiou.protection.ShutdownHolder;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: chiou
 * createTime: 2023/8/15
 * Description: 请求计数
 */
@Slf4j
public class DoorFilter implements ProviderBeforeFilter {
    @Override
    public void doFilter(FilterData data) {
        if (log.isDebugEnabled()) {
            log.debug("➡️enter TokenFilter.....");
        }
        //查看挡板状态,挡板如已开启,直接返回一个响应
        if (ShutdownHolder.IS_GATE_OPEN.get()) {
            if (log.isDebugEnabled()) {
                log.debug("❌server is closing,door is open,client request is rejected.....");
            }
            throw new RequestRejectedException();
        }
        //请求计数器加一
        ShutdownHolder.REQUEST_COUNTER.increment();
        if (log.isDebugEnabled()) {
            log.debug("请求计时器【➕1】,余量请求={}.....", ShutdownHolder.REQUEST_COUNTER.sum());
        }
    }
}
