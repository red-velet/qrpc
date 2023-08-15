package icu.chiou.filter;

import icu.chiou.config.spi.SpiLoader;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
            provdierBeforeFilterChain.addFilter(getOrderFilterList(ProviderBeforeFilter.class));
            provdierAfterFilterChain.addFilter(getOrderFilterList(ProviderAfterFilter.class));
        } catch (IOException | ClassNotFoundException e) {
            log.error("服务提供方过滤器加载异常 -> initProviderFilter()", e);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void initConsumerFilter() {
        try {
            SpiLoader.getInstance().loadExtension(ConsumerBeforeFilter.class);
            SpiLoader.getInstance().loadExtension(ConsumerAfterFilter.class);
            consumerBeforeFilterChain.addFilter(getOrderFilterList(ConsumerBeforeFilter.class));
            consumerAfterFilterChain.addFilter(getOrderFilterList(ConsumerAfterFilter.class));
        } catch (IOException | ClassNotFoundException e) {
            log.error("服务调用方过滤器加载异常 -> initConsumerFilter()", e);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
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

    public static List<Object> getOrderFilterList(Class clazz) throws InstantiationException, IllegalAccessException {
        String fileName = clazz.getName();
        Map<String, Class> map = SpiLoader.getInstance().getFileContent(fileName);
        Map<Integer, Class> treeMap = new TreeMap<>();
        ArrayList<Object> filters = new ArrayList<>();
        for (Map.Entry<String, Class> entry : map.entrySet()) {
            Class value = entry.getValue();
            String key = entry.getKey();
            treeMap.put(Integer.parseInt(key), value);
        }
        for (Map.Entry<Integer, Class> entry : treeMap.entrySet()) {
            Integer key = entry.getKey();
            Class value = entry.getValue();
            filters.add(value.newInstance());
        }
        return filters;
    }
}
