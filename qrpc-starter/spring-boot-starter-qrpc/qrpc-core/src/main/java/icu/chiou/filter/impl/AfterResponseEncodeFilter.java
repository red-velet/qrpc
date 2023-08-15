package icu.chiou.filter.impl;

import icu.chiou.filter.FilterData;
import icu.chiou.filter.ProviderAfterFilter;
import icu.chiou.protection.ShutdownHolder;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: chiou
 * createTime: 2023/8/15
 * Description: No Description
 */
@Slf4j
public class AfterResponseEncodeFilter implements ProviderAfterFilter {
    @Override
    public void doFilter(FilterData data) {
        //请求计数器减一
        ShutdownHolder.REQUEST_COUNTER.decrement();
        if (log.isDebugEnabled()) {
            log.debug("请求计时器【➖1】,余量请求={}.....", ShutdownHolder.REQUEST_COUNTER.sum());
        }
    }
}
