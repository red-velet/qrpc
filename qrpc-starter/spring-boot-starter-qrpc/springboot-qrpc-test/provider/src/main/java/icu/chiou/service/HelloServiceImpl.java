package icu.chiou.service;

import icu.chiou.core.annotation.QRpcApi;
import icu.chiou.core.annotation.RateLimiter;
import org.springframework.stereotype.Component;

/**
 * Author: chiou
 * createTime: 2023/8/11
 * Description: No Description
 */
@QRpcApi
@RateLimiter(allMaxCapacity = 10, tokensPerReplenish = 10)
@Component
public class HelloServiceImpl implements HelloService {
    @Override
    public String say(String msg) {
        return "provider has receive your msg is -->" + msg;
    }
}
