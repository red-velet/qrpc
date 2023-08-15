package icu.chiou.filter;

import icu.chiou.config.spi.SpiLoader;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Author: chiou
 * createTime: 2023/8/14
 * Description: 过滤器工厂
 */
@Slf4j
public class FilterFactory {
    private static final FilterChain provdierBeforeFilterChain = new FilterChain();
    private static final FilterChain provdierAfterFilterChain = new FilterChain();
    private static final FilterChain consumerBeforeFilterChain = new FilterChain();
    private static final FilterChain consumerAfterFilterChain = new FilterChain();

    public static void initProviderFilter() {
        try {
            SpiLoader.getInstance().loadExtension(ProviderBeforeFilter.class);
            SpiLoader.getInstance().loadExtension(ProviderAfterFilter.class);
            provdierBeforeFilterChain.addFilter(SpiLoader.getInstance().gets(ProviderBeforeFilter.class));
            provdierAfterFilterChain.addFilter(SpiLoader.getInstance().gets(ProviderAfterFilter.class));
        } catch (IOException | ClassNotFoundException e) {
            log.error("服务提供方过滤器加载异常 -> initProviderFilter()", e);
        }
    }

    public static void initConsumerFilter() {
        try {
            SpiLoader.getInstance().loadExtension(ConsumerBeforeFilter.class);
            SpiLoader.getInstance().loadExtension(ConsumerAfterFilter.class);
            consumerBeforeFilterChain.addFilter(SpiLoader.getInstance().gets(ConsumerBeforeFilter.class));
            consumerAfterFilterChain.addFilter(SpiLoader.getInstance().gets(ConsumerAfterFilter.class));
        } catch (IOException | ClassNotFoundException e) {
            log.error("服务调用方过滤器加载异常 -> initConsumerFilter()", e);
        }
    }

    public static FilterChain getProvdierBeforeFilterChain() {
        return provdierBeforeFilterChain;
    }

    public static FilterChain getProvdierAfterFilterChain() {
        return provdierAfterFilterChain;
    }

    public static FilterChain getConsumerBeforeFilterChain() {
        return consumerBeforeFilterChain;
    }

    public static FilterChain getConsumerAfterFilterChain() {
        return consumerAfterFilterChain;
    }

}
