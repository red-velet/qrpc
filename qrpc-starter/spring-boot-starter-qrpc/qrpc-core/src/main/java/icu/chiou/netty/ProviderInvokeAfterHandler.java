package icu.chiou.netty;

import icu.chiou.filter.FilterChain;
import icu.chiou.filter.FilterData;
import icu.chiou.filter.FilterFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: chiou
 * createTime: 2023/8/15
 * Description: No Description
 */
@Slf4j
public class ProviderInvokeAfterHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("enter--->>>ProviderInvokeAfterHandler--->>>write--->>>{}", msg);
        }
        try {
            FilterChain chain = FilterFactory.getProvdierAfterFilterChain();
            FilterData filterData = new FilterData();
            filterData.setBody(msg);
            chain.doFilter(filterData);
        } catch (Exception e) {
            log.error("获取/执行-->>>provider invoke 后置过滤器失败");
        }
        super.write(ctx, msg, promise);
    }
}
