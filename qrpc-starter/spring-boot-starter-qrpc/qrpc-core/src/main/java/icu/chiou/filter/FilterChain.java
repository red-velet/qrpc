package icu.chiou.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: chiou
 * createTime: 2023/8/14
 * Description: No Description
 */
public class FilterChain {
    List<Filter> filters = new ArrayList<>();

    public void addFilter(Filter filter) {
        filters.add(filter);
    }


    public void addFilter(List<Object> filters) {
        for (Object filter : filters) {
            addFilter((Filter) filter);
        }
    }

    public void doFilter(FilterData data) {
        for (Filter filter : filters) {
            filter.doFilter(data);
        }
    }
}
