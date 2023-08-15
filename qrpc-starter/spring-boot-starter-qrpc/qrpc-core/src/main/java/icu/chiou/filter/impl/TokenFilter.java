package icu.chiou.filter.impl;

import icu.chiou.core.QRpcProperties;
import icu.chiou.filter.FilterData;
import icu.chiou.filter.ProviderBeforeFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: chiou
 * createTime: 2023/8/14
 * Description: token过滤器
 */
@Slf4j
public class TokenFilter implements ProviderBeforeFilter {
    @Override
    public void doFilter(FilterData data) {
        if (log.isDebugEnabled()) {
            log.debug("➡️enter TokenFilter.....");
        }
        String tokenFromConsumer = data.getConsumerAttributes().getOrDefault("token", "").toString();
        String tokenInProvider = (String) QRpcProperties.getInstance().getProviderAttributes().getOrDefault("token", "");
        if (!tokenInProvider.equals(tokenFromConsumer)) {
            throw new RuntimeException("❌token is not match!!!");
        }
        if (log.isDebugEnabled()) {
            log.debug("🪲token is match.....");
        }
    }
}
