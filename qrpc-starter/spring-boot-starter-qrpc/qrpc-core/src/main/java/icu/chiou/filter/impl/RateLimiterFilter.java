package icu.chiou.filter.impl;

import icu.chiou.common.exceptions.RequestRejectedException;
import icu.chiou.core.QRpcApplicationContext;
import icu.chiou.filter.FilterData;
import icu.chiou.filter.ProviderBeforeFilter;
import icu.chiou.protection.RateLimiter;
import icu.chiou.protection.TokenBucketRateLimiter;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Objects;

/**
 * Author: chiou
 * createTime: 2023/8/15
 * Description: 限流
 */
@Slf4j
public class RateLimiterFilter implements ProviderBeforeFilter {
    @Override
    public void doFilter(FilterData data) {
        if (log.isDebugEnabled()) {
            log.debug("➡️enter RateLimiterFilter.....");
        }
        //查看服务是否设置了限流器
        TokenBucketRateLimiter limiter = (TokenBucketRateLimiter) QRpcApplicationContext.LIMITER_SERVER_LIST.get(data.getInterfaceName());
        if (limiter != null) {
            Map<SocketAddress, RateLimiter> everyIpRateLimiter = QRpcApplicationContext.getInstance().getEveryIpRateLimiter();
            RateLimiter ipLimiter = everyIpRateLimiter.get(data.getSocketAddress());
            if (Objects.isNull(ipLimiter)) {
                int capacity = limiter.getCapacity();
                int rate = limiter.getRate();
                ipLimiter = new TokenBucketRateLimiter(capacity, rate);
                everyIpRateLimiter.put(data.getSocketAddress(), ipLimiter);
            }
            //限流
            Boolean pass = ipLimiter.isAllowRequest();
            if (!pass) {
                if (log.isDebugEnabled()) {
                    log.debug("🪲request is be limit.....");
                }
                throw new RequestRejectedException();
            }
        }
    }
}
