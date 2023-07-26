package icu.chiou.channelHandler.handler;

import icu.chiou.constants.MessageFormatConstant;
import icu.chiou.transport.message.QRpcRequest;
import icu.chiou.transport.message.RequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Author: chiou
 * createTime: 2023/7/26
 * Description: 出战时，第一个经过的处理器
 * 报文结构:
 * 4byte = magic -- 魔数值 -- qrpc
 * 1byte = version
 * 2byte = headLength -- 首部长度
 * 4byte = fullLength -- 报文长度
 * 1byte = requestType
 * 1byte = compressType
 * 1byte = serializeType
 * 8byte = requestId
 * xxx byte = body
 */
@Slf4j
public class QRpcMessageEncoder extends MessageToByteEncoder<QRpcRequest> {

    @Override
    protected void encode(ChannelHandlerContext ctx, QRpcRequest msg, ByteBuf out) throws Exception {
        //todo 需要为不同的消息类型做不同的处理: 普通信息、心跳信息
        //封装报文
        //魔术值 4个字节
        out.writeBytes(MessageFormatConstant.MAGIC);
        //版本号
        out.writeByte(1);
        //首部长度
        out.writeShort(MessageFormatConstant.HEADER_LENGTH);
        //报文长度
        //todo 还不确定先跳过4个位置
        out.writeInt(out.writerIndex() + 4);
        //类型
        out.writeByte(msg.getRequestType());
        out.writeByte(msg.getCompressType());
        out.writeByte(msg.getSerializeType());
        //请求id
        out.writeLong(msg.getRequestId());
        byte[] body = getBodyBytes(msg.getRequestPayload());
        out.writeBytes(body);
        //重新处理报文长度 再写上4个 full length
        int currIndex = out.writerIndex();//保存当前写指针位置
        //移动到之前的位置
        out.writeInt(7);
        out.writeInt(body.length + MessageFormatConstant.HEADER_LENGTH);
        //归位写指针
        out.writerIndex(currIndex);
    }

    private byte[] getBodyBytes(RequestPayload payload) {
        //todo 此处序列化太固定，而且还没开始写压缩
        //使用java序列化对象
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(payload);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("序列化时出现异常:", e);
            throw new RuntimeException(e);
        }
    }
}
