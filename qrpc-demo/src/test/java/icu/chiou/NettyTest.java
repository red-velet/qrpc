package icu.chiou;

import icu.chiou.netty.AppClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * Author: chiou
 * createTime: 2023/7/21
 * Description: netty的学习测试类
 */
public class NettyTest implements Serializable {
    private String readAsString(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.markReaderIndex(); // 标记读指针位置
        byteBuf.readBytes(bytes);
        byteBuf.resetReaderIndex(); // 重置读指针到标记的位置
        return new String(bytes);
    }

    @Test
    public void testCompositeBuf() {
        //todo 测量基于组件化的实现逻辑上的零拷贝-CompositeBuf
        //假装这是个请求头
        ByteBuf head = Unpooled.buffer();
        head.writeBytes("This is the request header".getBytes());

        //假装这是个请求体
        ByteBuf body = Unpooled.buffer();
        body.writeBytes("This is the request body".getBytes());

        //组装
        CompositeByteBuf cbb = Unpooled.compositeBuffer();
        cbb.addComponents(head, body);

        // 输出组装后的结果
        System.out.println("Composite ByteBuf Content: " + cbb.toString());
        for (int i = 0; i < cbb.numComponents(); i++) {
            ByteBuf component = cbb.component(i);
            byte[] bytes = new byte[component.readableBytes()];
            component.readBytes(bytes);
            System.out.println(new String(bytes));
        }
    }


    @Test
    public void testWrappedBuf() {
        //todo 测量基于包装方式实现的零拷贝-wrappedBuf
        //需要把byte数组转换为buf
        //默认-拷贝进去
        byte[] bytes = "1234".getBytes(StandardCharsets.UTF_8);
        byte[] bytes2 = "5678".getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = Unpooled.wrappedBuffer(bytes, bytes2);
        //通过包装,不去拷贝,提升性能
        System.out.println("wrappedBuffer Content: ");
        System.out.println(buf.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void testSlice() {
        //todo 将一个ByteBuf切片,切片出来的引用同一片地址,数据共享
        //创建一个 ByteBuf
        ByteBuf buffer = Unpooled.buffer(20);
        buffer.writeBytes("Hello, this is a test".getBytes());

        // 创建一个 Slice 对象，从 ByteBuf 中截取一部分内容
        int length = 10;
        int offset = 6;
        ByteBuf slice = buffer.slice(offset, length);

        // 输出原始 ByteBuf 和 Slice 对象的内容
        System.out.println("Original ByteBuf: ");
        printByteBufContent(buffer);

        System.out.println("\nSlice: ");
        printByteBufContent(slice);
    }

    private static void printByteBufContent(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(byteBuf.readerIndex(), bytes);
        System.out.println(new String(bytes));
    }

    @Test
    public void testMessage() throws IOException {
        ByteBuf message = Unpooled.buffer();
        message.writeBytes("qpc".getBytes(StandardCharsets.UTF_8));
        message.writeByte(1);
        message.writeShort(125);
        message.writeInt(256);
        message.writeByte(1);
        message.writeByte(0);
        message.writeByte(2);
        message.writeLong(251455L);
        // 用对象流转化为字节数据
        AppClient appClient = new AppClient();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        oos.writeObject(appClient);
        byte[] bytes = outputStream.toByteArray();
        message.writeBytes(bytes);

        printAsBinary(message);

    }

    public static void printAsBinary(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(byteBuf.readerIndex(), bytes);

        String binaryString = ByteBufUtil.hexDump(bytes);
        StringBuilder formattedBinary = new StringBuilder();

        for (int i = 0; i < binaryString.length(); i += 2) {
            formattedBinary.append(binaryString.substring(i, i + 2)).append(" ");
        }

        System.out.println("Binary representation: " + formattedBinary.toString());
    }
}
