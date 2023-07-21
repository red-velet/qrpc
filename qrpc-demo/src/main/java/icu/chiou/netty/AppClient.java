package icu.chiou.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Author: chiou
 * createTime: 2023/7/21
 * Description: netty 客户端
 */
public class AppClient implements Serializable {
    public static void main(String[] args) {
        //启动客户端
        new AppClient().run();
    }

    public void run() {
        NioEventLoopGroup group = null;
        try {
            //定义线程池
            group = new NioEventLoopGroup(10);
            //定义启动辅助类
            Bootstrap bootstrap = new Bootstrap();
            //添加通道化配置
            bootstrap
                    .group(group)//添加处理任务的线程
                    .remoteAddress(new InetSocketAddress(9091))//绑定服务端地址
                    .channel(NioSocketChannel.class)//添加channel
                    .handler(new ChannelInitializer<SocketChannel>() {//添加handler
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ClientChannelHandler());
                        }
                    });
            //尝试连接服务器
            ChannelFuture channelFuture = bootstrap.connect().sync();
            //获取channel
            Channel channel = channelFuture.channel();
            //从channel写出数据
            channel.writeAndFlush(Unpooled.copiedBuffer("hello,netty!!!".getBytes(StandardCharsets.UTF_8)));

            //客户端阻塞,等待服务端的响应
            channel.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
