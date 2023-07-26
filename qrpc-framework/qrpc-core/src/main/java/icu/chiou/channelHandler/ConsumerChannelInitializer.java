package icu.chiou.channelHandler;

import icu.chiou.channelHandler.handler.MySimpleChannelInboundHandler;
import icu.chiou.channelHandler.handler.QRpcMessageEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Author: chiou
 * createTime: 2023/7/26
 * Description: No Description
 */
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                //出战的编码器
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                .addLast(new QRpcMessageEncoder())


                //入站的编码器
                .addLast(new MySimpleChannelInboundHandler());
    }
}
