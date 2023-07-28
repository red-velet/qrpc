package icu.chiou.serialize;

/**
 * Author: chiou
 * createTime: 2023/7/28
 * Description: 抽象的序列化器,规定序列化器的行为
 */
public interface Serializer {
    
    /**
     * 序列化的方法
     *
     * @param object 待序列化的对象
     * @return 序列化化后的字节数组
     */
    byte[] serialize(Object object);


    /**
     * 反序列化的方法
     *
     * @param bytes 待反序列化的字节数组
     * @param clazz 目标类的Class对象
     * @param <T>   目标类泛型
     * @return 目标类
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
