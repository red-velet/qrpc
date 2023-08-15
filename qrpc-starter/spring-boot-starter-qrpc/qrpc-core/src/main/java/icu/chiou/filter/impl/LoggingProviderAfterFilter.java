package icu.chiou.filter.impl;

import icu.chiou.filter.FilterData;
import icu.chiou.filter.ProviderAfterFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: chiou
 * createTime: 2023/8/15
 * Description: No Description
 */
@Slf4j
public class LoggingProviderAfterFilter implements ProviderAfterFilter {
    @Override
    public void doFilter(FilterData data) {
        log.debug("1=enter LoggingProviderAfterFilter doFilter --->>> {}", data);
        log.debug("2=enter LoggingProviderAfterFilter doFilter --->>> {}", data);
        log.debug("3=enter LoggingProviderAfterFilter doFilter --->>> {}", data);
    }
}
