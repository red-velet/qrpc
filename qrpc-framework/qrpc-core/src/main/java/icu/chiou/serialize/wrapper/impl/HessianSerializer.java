package icu.chiou.serialize.wrapper.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import icu.chiou.exceptions.SerializeException;
import icu.chiou.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Author: chiou
 * createTime: 2023/7/28
 * Description: hessian的序列化器
 */
@Slf4j
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        //使用hessian序列化对象
        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ) {
            Hessian2Output hessian2Output = new Hessian2Output(baos);
            hessian2Output.writeObject(object);
            hessian2Output.flush();
            if (log.isDebugEnabled()) {
                log.debug("请求报文内对象【{}】,使用hessian方式成功完成了序列化操作", object);
            }
            byte[] bytes = baos.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("使用hessian方式成功完成序列化操作后的数组长度:{}", bytes.length);
            }
            return bytes;
        } catch (IOException e) {
            log.error("序列化对象【{}】时出现异常:", object, e);
            throw new SerializeException("hessian序列化时出现异常");
        }

    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || clazz == null) {
            return null;
        }
        //使用hessian反序列化对象
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ) {
            Hessian2Input hessianInput = new Hessian2Input(bais);
            T t = (T) hessianInput.readObject();
            if (log.isDebugEnabled()) {
                log.debug("响应报文内对象【{}】,使用hessian方式成功完成了反序列化操作", clazz);
            }
            return t;
        } catch (IOException e) {
            log.error("反序列化对象【{}】时出现异常:", clazz, e);
            throw new SerializeException("hessian序列化时出现异常");
        }
    }
}
