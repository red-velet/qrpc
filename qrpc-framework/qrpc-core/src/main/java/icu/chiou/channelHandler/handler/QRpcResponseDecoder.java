package icu.chiou.channelHandler.handler;

import icu.chiou.constants.MessageFormatConstant;
import icu.chiou.transport.message.QRpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Author: chiou
 * createTime: 2023/7/27
 * Description: 响应的解码器
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
public class QRpcResponseDecoder extends LengthFieldBasedFrameDecoder {
    public QRpcResponseDecoder() {
        //找到具体报文payload的具体位置,截取出来进行解析
        super(
                MessageFormatConstant.MAX_FRAME_LENGTH,//最大帧产固定
                MessageFormatConstant.FULL_LENGTH_OFFSET,//记录报文总长度的full length的的偏移量-开始的位置
                MessageFormatConstant.FULL_LENGTH_LENGTH,//记录报文总长度的full length的长度-四个字节
                MessageFormatConstant.ADJUSTMENT_LENGTH,//减去的魔数值、版本号、full、header等，以便获取报文开始位置
                MessageFormatConstant.INITIAL_BYTES_TO_STRIP);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx, in);
        if (decode instanceof ByteBuf) {
            return decodeFrame((ByteBuf) decode);
        }
        return null;
    }

    private Object decodeFrame(ByteBuf byteBuf) {
        //1.解析魔数值
        byte[] magic = new byte[MessageFormatConstant.MAGIC_LENGTH];
        byteBuf.readBytes(magic);
        //校验魔数值是否匹配
        for (int i = 0; i < magic.length; i++) {
            if (magic[i] != MessageFormatConstant.MAGIC_VALUE[i]) {
                throw new RuntimeException("获得的请求不合法!!!");
            }
        }

        //2.解析版本号
        byte version = byteBuf.readByte();
        //校验版本号是否匹配
        if (version != MessageFormatConstant.VERSION_VALUE) {
            throw new RuntimeException("获取请求的版本已不被支持!!!");
        }

        //3.解析头部的长度
        short header_length = byteBuf.readShort();


        //4.解析总长度
        int full_length = byteBuf.readInt();

        //5.解析响应码
        byte responseCode = byteBuf.readByte();

        //6.解析序列化类型
        byte serializeType = byteBuf.readByte();

        //7.解析压缩类型
        byte compressType = byteBuf.readByte();

        //8.解析请求id
        long requestId = byteBuf.readLong();

        //将解析后的报文封装成QRpcRequest对象 todo 心跳请求没有负载payload直接返回
        QRpcResponse qRpcResponse = QRpcResponse.builder()
                .requestId(requestId)
                .code(responseCode)
                .serializeType(serializeType)
                .compressType(compressType)
                .build();

        //todo 心跳
//        if (requestType == RequestType.HEART_DANCE.getId()) {
//            return qRpcRequest;
//        }

        //9.解析请求内容
        //9.1读取
        int bodyLength = full_length - header_length;
        byte[] bodyArr = new byte[bodyLength];
        byteBuf.readBytes(bodyArr);

        //9.2解压缩

        //9.3反序列化
        try (
                //自动关流
                ByteArrayInputStream bais = new ByteArrayInputStream(bodyArr);
                ObjectInputStream ois = new ObjectInputStream(bais);
        ) {
            Object body = ois.readObject();
            //封装
            qRpcResponse.setBody(body);
        } catch (IOException | ClassNotFoundException e) {
            log.error("响应【{}】反序列化时发送了异常", requestId, e);
            throw new RuntimeException(e);
        }

        if (log.isDebugEnabled()) {
            log.debug("响应【{}】已经在服务调用方，完成报文的解码工作", qRpcResponse.getRequestId());
        }

        return qRpcResponse;
    }
}
