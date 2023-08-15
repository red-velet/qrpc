package icu.chiou.filter.impl;

import icu.chiou.core.QRpcProperties;
import icu.chiou.filter.FilterData;
import icu.chiou.filter.ProviderBeforeFilter;

/**
 * Author: chiou
 * createTime: 2023/8/14
 * Description: token过滤器
 */
public class TokenProviderBeforeFilter implements ProviderBeforeFilter {
    @Override
    public void doFilter(FilterData data) {
        String tokenFromConsumer = data.getConsumerAttributes().getOrDefault("token", "").toString();
        String tokenInProvider = (String) QRpcProperties.getInstance().getProviderAttributes().getOrDefault("token", "");
        if (!tokenInProvider.equals(tokenFromConsumer)) {
            throw new RuntimeException("token is not match!!!");
        }
    }
}
