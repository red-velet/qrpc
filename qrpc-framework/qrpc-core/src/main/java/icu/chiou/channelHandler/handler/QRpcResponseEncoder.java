package icu.chiou.channelHandler.handler;

import icu.chiou.constants.MessageFormatConstant;
import icu.chiou.transport.message.QRpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Author: chiou
 * createTime: 2023/7/27
 * Description: 响应的编码器
 * * * * <pre>
 *  *  *  *   0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22
 *  *  *  *   +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
 *  *  *  *   |    magic          |ver |head  len|    full length    | cd | ser|comp|              RequestId                |
 *  *  *  *   +-----+-----+-------+----+----+----+----+-----------+----- ---+--------+----+----+----+----+----+----+---+---+
 *  *  *  *   |                                                                                                             |
 *  *  *  *   |                                         body                                                                |
 *  *  *  *   |                                                                                                             |
 *  *  *  *   +--------------------------------------------------------------------------------------------------------+---+
 *  *  *  * </pre>
 */
@Slf4j
public class QRpcResponseEncoder extends MessageToByteEncoder<QRpcResponse> {
    @Override
    protected void encode(ChannelHandlerContext ctx, QRpcResponse msg, ByteBuf out) throws Exception {
        //封装报文
        //魔术值 4个字节
        out.writeBytes(MessageFormatConstant.MAGIC_VALUE);
        //版本号
        out.writeByte(1);
        //首部长度
        out.writeShort(MessageFormatConstant.HEADER_LENGTH_VALUE);
        //报文长度
        //todo 还不确定先跳过4个位置
        out.writeInt(out.writerIndex() + MessageFormatConstant.FULL_LENGTH_LENGTH);
        //类型
        out.writeByte(msg.getCode());
        out.writeByte(msg.getCompressType());
        out.writeByte(msg.getSerializeType());
        //请求id
        out.writeLong(msg.getRequestId());

        //
        byte[] body = getBodyBytes(msg.getBody());
        if (body != null) {
            out.writeBytes(body);
        }
        int bodyLength = body == null ? 0 : body.length;
        //重新处理报文长度 再写上4个 full length
        int currIndex = out.writerIndex();//保存当前写指针位置
        //移动到之前的位置
        out.writerIndex(MessageFormatConstant.FULL_LENGTH_OFFSET);
        out.writeInt(bodyLength + MessageFormatConstant.HEADER_LENGTH_VALUE);
        //归位写指针
        out.writerIndex(currIndex);

        if (log.isDebugEnabled()) {
            log.debug("响应【{}】已在服务提供方，完成报文的编码工作", msg.getRequestId());
        }
    }

    private byte[] getBodyBytes(Object object) {
        //todo 此处序列化太固定，而且还没开始写压缩
        if (object == null) {
            return null;
        }
        //使用java序列化对象
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("序列化时出现异常:", e);
            throw new RuntimeException(e);
        }
    }
}
