package icu.chiou.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Author: chiou
 * createTime: 2023/7/21
 * Description: netty 服务端
 */
public class AppServer {
    public static void main(String[] args) {
        new AppServer(9091).start();
    }

    private int port;

    public AppServer(int port) {
        this.port = port;
    }

    public void start() {
        EventLoopGroup boss = null;
        EventLoopGroup worker = null;
        try {
            //1.创建EventGroup
            boss = new NioEventLoopGroup(2);
            worker = new NioEventLoopGroup(10);

            //2.服务器引导程序
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ServerChannelHandler());
                        }
                    });

            //3.绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();

            //4.获取数据
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
