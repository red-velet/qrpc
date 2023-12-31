package icu.chiou.netty.encoder;

import icu.chiou.common.constants.MessageFormatConstant;
import icu.chiou.core.QRpcProperties;
import icu.chiou.protocol.compress.CompressionFactory;
import icu.chiou.protocol.compress.Compressor;
import icu.chiou.protocol.serialize.SerializationFactory;
import icu.chiou.protocol.serialize.Serializer;
import icu.chiou.protocol.transport.QRpcRequest;
import icu.chiou.protocol.transport.RequestPayload;
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
 * Description: 请求的编码器-出战时，第一个经过的处理器
 * 报文结构:
 * 4byte = magic -- 魔数值 -- qrpc
 * 1byte = version -- 版本号
 * 2byte = headLength -- 首部长度
 * 4byte = fullLength -- 报文长度
 * 1byte = requestType
 * 1byte = compressType
 * 1byte = serializeType
 * 8byte = requestId
 * xxx byte = body
 * * * <pre>
 *  *  *   0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22
 *  *  *   +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
 *  *  *   |    magic          |ver |head  len|    full length    | qt | ser|comp|              RequestId                |
 *  *  *   +-----+-----+-------+----+----+----+----+-----------+----- ---+--------+----+----+----+----+----+----+---+---+
 *  *  *   |                                                                                                             |
 *  *  *   |                                         body                                                                |
 *  *  *   |                                                                                                             |
 *  *  *   +--------------------------------------------------------------------------------------------------------+---+
 *  *  * </pre>
 */
@Slf4j
public class QRpcRequestEncoder extends MessageToByteEncoder<QRpcRequest> {

    @Override
    protected void encode(ChannelHandlerContext ctx, QRpcRequest msg, ByteBuf out) throws Exception {
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
        out.writeByte(msg.getRequestType());
        out.writeByte(msg.getSerializeType());
        out.writeByte(msg.getCompressType());
        //请求id
        out.writeLong(msg.getRequestId());

        //根据配置进行序列化
        Serializer serializer = SerializationFactory.get(QRpcProperties.getInstance().getSerializeType());
        //请求体
        //byte[] body = getBodyBytes(msg.getRequestPayload());
        byte[] body = serializer.serialize(msg.getRequestPayload());
        //根据配置进行压缩
        Compressor compressor = CompressionFactory.get(QRpcProperties.getInstance().getCompressType());
        body = compressor.compress(body);

        out.writeBytes(body);
        //重新处理报文长度 再写上4个 full length
        int currIndex = out.writerIndex();//保存当前写指针位置
        //移动到之前的位置
        out.writerIndex(MessageFormatConstant.FULL_LENGTH_OFFSET);
        out.writeInt(body.length + MessageFormatConstant.HEADER_LENGTH_VALUE);
        //归位写指针
        out.writerIndex(currIndex);
        //日志记录
        if (log.isDebugEnabled()) {
            log.debug("请求【{}】已在服务调用方，完成报文的编码的工作", msg.getRequestId());
        }
    }

    private byte[] getBodyBytes(RequestPayload payload) {
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
