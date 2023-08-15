package icu.chiou.filter.impl;

import icu.chiou.common.enumeration.RequestType;
import icu.chiou.common.exceptions.NeedExecHeartException;
import icu.chiou.filter.FilterData;
import icu.chiou.filter.ProviderBeforeFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: chiou
 * createTime: 2023/8/15
 * Description: 请求处理器
 */
@Slf4j
public class RequestTypeFilter implements ProviderBeforeFilter {
    @Override
    public void doFilter(FilterData data) {
        if (log.isDebugEnabled()) {
            log.debug("➡️enter RequestTypeFilter.....");
        }
        if (data.getRequestType() == RequestType.HEART_DANCE.getId()) {
            if (log.isDebugEnabled()) {
                log.debug("🪲request type is heart dance.....");
            }
            throw new NeedExecHeartException();
        }
    }
}
