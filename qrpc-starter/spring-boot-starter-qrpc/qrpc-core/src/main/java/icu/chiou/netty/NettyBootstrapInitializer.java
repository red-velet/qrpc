package icu.chiou.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: chiou
 * createTime: 2023/7/26
 * Description: 单例Bootstrap todo 这里有什么问题？
 */
@Slf4j
public class NettyBootstrapInitializer {
    private static final Bootstrap BOOTSTRAP = new Bootstrap();

    private static final NioEventLoopGroup GROUP = new NioEventLoopGroup();

    static {
        BOOTSTRAP
                .group(GROUP)//添加处理任务的线程
                .channel(NioSocketChannel.class)//添加channel,选择一个什么样的channel
                .handler(new ConsumerChannelInitializer());
    }

    private NettyBootstrapInitializer() {

    }

    public static Bootstrap getBootstrap() {
        //添加通道化配置
        return BOOTSTRAP;
    }
}
