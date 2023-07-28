package icu.chiou.serialize.wrapper.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import icu.chiou.exceptions.SerializeException;
import icu.chiou.serialize.Serializer;
import icu.chiou.transport.message.RequestPayload;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: chiou
 * createTime: 2023/7/28
 * Description: json格式的序列化器
 */
@Slf4j
public class Fastjson2Serializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        //使用fastjson序列化对象
        try {
            byte[] bytes = JSON.toJSONBytes(object);
            if (log.isDebugEnabled()) {
                log.debug("请求报文内对象【{}】,使用fastjson2方式成功完成了【序列化】操作", object);
            }
            if (log.isDebugEnabled()) {
                log.debug("使用fastjson2方式成功完成【序列化】操作后的数组长度:{}", bytes.length);
            }
            return bytes;
        } catch (RuntimeException e) {
            log.error("序列化对象【{}】时出现异常:", object, e);
            throw new SerializeException("fastjson2【序列化】时出现异常");
        }

    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || clazz == null) {
            return null;
        }
        //使用fastjson反序列化对象
        try {
            Object object = JSON.parseObject(bytes, clazz, JSONReader.Feature.SupportClassForName);
            if (log.isDebugEnabled()) {
                log.debug("响应报文内对象【{}】,使用fastjson方式成功完成了【反序列化】操作", clazz);
            }
            return (T) object;
        } catch (RuntimeException e) {
            log.error("反序列化对象【{}】时出现异常:", clazz, e);
            throw new SerializeException("fastjson【反序列化】时出现异常");
        }
    }

    public static void main(String[] args) {
        Fastjson2Serializer jsonSerializer = new Fastjson2Serializer();
        RequestPayload payload = RequestPayload.builder()
                .interfaceName("xxxxx")
                .methodName("xxxxx")
                .returnType(String.class)
                .build();
        System.out.println("\n序列化:");
        byte[] bytes = jsonSerializer.serialize(payload);
        System.out.println("bytes:" + bytes.length);

        //FastJSON 在较早版本中不支持直接将 Class 对象作为属性进行反序列化
        //1.换高版本
        //2.开启JSONReader.Feature.SupportClassForName
        //3.用String替换classname，自己再自定义设置
        System.out.println("\n反序列化:");
        RequestPayload requestPayload = jsonSerializer.deserialize(bytes, RequestPayload.class);
        System.out.println("requestPayload = " + requestPayload);
    }
}
