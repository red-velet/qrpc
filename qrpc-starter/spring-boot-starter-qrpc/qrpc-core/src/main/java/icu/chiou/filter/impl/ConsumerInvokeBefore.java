package icu.chiou.filter.impl;

import icu.chiou.filter.ConsumerBeforeFilter;
import icu.chiou.filter.FilterData;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: chiou
 * createTime: 2023/8/15
 * Description: No Description
 */
@Slf4j
public class ConsumerInvokeBefore implements ConsumerBeforeFilter {
    @Override
    public void doFilter(FilterData data) {
        log.debug("checkFilter enter -->> ConsumerInvokeBefore -->> 想要调用 {} {} {}", data.getInterfaceName(), data.getMethodName(), data.getParamsValue());
    }
}
