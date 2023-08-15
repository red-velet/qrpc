package icu.chiou.filter.impl;

import icu.chiou.common.enumeration.ResponseCode;
import icu.chiou.filter.ConsumerAfterFilter;
import icu.chiou.filter.FilterData;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: chiou
 * createTime: 2023/8/15
 * Description: No Description
 */
@Slf4j
public class LoggingByCodeAfterFilter implements ConsumerAfterFilter {
    @Override
    public void doFilter(FilterData data) {
        //日志记录
        byte code = data.getCode();
        if (ResponseCode.SUCCESS_HEART_DANCE.getCode() == code) {
            if (log.isDebugEnabled()) {
                log.debug("🪲【LoggingByCodeAfterFilter】--->>>code:{} desc:{}",
                        ResponseCode.SUCCESS_HEART_DANCE.getCode(),
                        ResponseCode.SUCCESS_HEART_DANCE.getDesc());
            }
        } else if (ResponseCode.SUCCESS.getCode() == code) {

            if (log.isDebugEnabled()) {
                log.debug("🪲【LoggingByCodeAfterFilter】--->>>code:{} desc:{}",
                        ResponseCode.SUCCESS.getCode(),
                        ResponseCode.SUCCESS.getDesc());
            }
        } else if (ResponseCode.RATE_LIMIT.getCode() == code) {
            if (log.isDebugEnabled()) {
                log.debug("🪲【LoggingByCodeAfterFilter】--->>>code:{} desc:{}",
                        ResponseCode.RATE_LIMIT.getCode(),
                        ResponseCode.RATE_LIMIT.getDesc());
            }
        } else if (ResponseCode.UNAUTHENTICATED.getCode() == code) {
            if (log.isDebugEnabled()) {
                log.debug("🪲【LoggingByCodeAfterFilter】--->>>code:{} desc:{}",
                        ResponseCode.UNAUTHENTICATED.getCode(),
                        ResponseCode.UNAUTHENTICATED.getDesc());
            }
        } else if (ResponseCode.RESOURCE_NOT_FOUND.getCode() == code) {
            if (log.isDebugEnabled()) {
                log.debug("🪲【LoggingByCodeAfterFilter】--->>>code:{} desc:{}",
                        ResponseCode.RESOURCE_NOT_FOUND.getCode(),
                        ResponseCode.RESOURCE_NOT_FOUND.getDesc());
            }
        } else if (ResponseCode.FAIL.getCode() == code) {
            if (log.isDebugEnabled()) {
                log.debug("🪲【LoggingByCodeAfterFilter】--->>>code:{} desc:{}",
                        ResponseCode.FAIL.getCode(),
                        ResponseCode.FAIL.getDesc());
            }
        } else if (ResponseCode.CLOSING.getCode() == code) {
            if (log.isDebugEnabled()) {
                log.debug("🪲【LoggingByCodeAfterFilter】--->>>code:{} desc:{}",
                        ResponseCode.CLOSING.getCode(),
                        ResponseCode.CLOSING.getDesc());
            }
        }
    }
}
