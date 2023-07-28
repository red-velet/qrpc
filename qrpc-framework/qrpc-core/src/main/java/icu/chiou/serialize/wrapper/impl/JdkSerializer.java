package icu.chiou.serialize.wrapper.impl;

import icu.chiou.exceptions.SerializeException;
import icu.chiou.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * Author: chiou
 * createTime: 2023/7/28
 * Description: jdk的序列化器
 */
@Slf4j
public class JdkSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        //使用java序列化对象
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos);
        ) {
            oos.writeObject(object);
            if (log.isDebugEnabled()) {
                log.debug("请求报文内对象【{}】,使用jdk方式成功完成了【序列化】操作", object);
            }
            byte[] bytes = baos.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("使用jdk方式成功完成【序列化】操作后的数组长度:{}", bytes.length);
            }
            return bytes;
        } catch (IOException e) {
            log.error("序列化对象【{}】时出现异常:", object, e);
            throw new SerializeException("jdk【序列化】时出现异常");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || clazz == null) {
            return null;
        }
        //使用java反序列化对象
        try (ByteArrayInputStream baos = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(baos);
        ) {
            log.debug("响应报文内对象【{}】,使用jdk方式成功完成了【反序列化】操作", clazz);
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error("反序列化对象【{}】时出现异常:", clazz, e);
            throw new SerializeException("jdk【反序列化】时出现异常");
        }
    }
}
