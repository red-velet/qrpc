package icu.chiou.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

/**
 * Author: chiou
 * createTime: 2023/7/21
 * Description: 服务器处理器
 */
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("服务端收到消息:" + buf.toString(StandardCharsets.UTF_8));
        //获取channel给服务端回复消息
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer("hello,client".getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //出现异常时的操作
        //打印异常,关闭通道
        cause.printStackTrace();
        ctx.close();
    }
}
