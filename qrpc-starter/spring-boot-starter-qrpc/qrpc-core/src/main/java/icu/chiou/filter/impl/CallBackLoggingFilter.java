package icu.chiou.filter.impl;

import icu.chiou.common.enumeration.ResponseCode;
import icu.chiou.filter.ConsumerAfterFilter;
import icu.chiou.filter.FilterData;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: chiou
 * createTime: 2023/8/15
 * Description: 响应日志打印
 */
@Slf4j
public class CallBackLoggingFilter implements ConsumerAfterFilter {
    @Override
    public void doFilter(FilterData data) {
        byte code = data.getCode();
        if (code == ResponseCode.SUCCESS.getCode()) {
            if (log.isDebugEnabled()) {
                log.debug("🪲响应返回成功: code:{} desc{}",
                        ResponseCode.SUCCESS.getCode(),
                        ResponseCode.SUCCESS.getDesc());
            }
        } else if (code == ResponseCode.SUCCESS_HEART_DANCE.getCode()) {
            if (log.isDebugEnabled()) {
                log.debug("🪲响应返回成功: code:{} desc{}",
                        ResponseCode.SUCCESS_HEART_DANCE.getCode(),
                        ResponseCode.SUCCESS_HEART_DANCE.getDesc());
            }
        } else if (code == ResponseCode.RATE_LIMIT.getCode()) {
            if (log.isDebugEnabled()) {
                log.debug("🪲响应返回成功: code:{} desc{}",
                        ResponseCode.RATE_LIMIT.getCode(),
                        ResponseCode.RATE_LIMIT.getDesc());
            }
        } else if (code == ResponseCode.UNAUTHENTICATED.getCode()) {
            if (log.isDebugEnabled()) {
                log.debug("🪲响应返回成功: code:{} desc{}",
                        ResponseCode.UNAUTHENTICATED.getCode(),
                        ResponseCode.UNAUTHENTICATED.getDesc());
            }
        } else if (code == ResponseCode.FAIL.getCode()) {
            if (log.isDebugEnabled()) {
                log.debug("🪲响应返回成功: code:{} desc{}",
                        ResponseCode.FAIL.getCode(),
                        ResponseCode.FAIL.getDesc());
            }
        } else if (code == ResponseCode.RESOURCE_NOT_FOUND.getCode()) {
            if (log.isDebugEnabled()) {
                log.debug("🪲响应返回成功: code:{} desc{}",
                        ResponseCode.RESOURCE_NOT_FOUND.getCode(),
                        ResponseCode.RESOURCE_NOT_FOUND.getDesc());
            }
        } else if (code == ResponseCode.CLOSING.getCode()) {
            if (log.isDebugEnabled()) {
                log.debug("🪲响应返回成功: code:{} desc{}",
                        ResponseCode.CLOSING.getCode(),
                        ResponseCode.CLOSING.getDesc());
            }
        }
    }
}
