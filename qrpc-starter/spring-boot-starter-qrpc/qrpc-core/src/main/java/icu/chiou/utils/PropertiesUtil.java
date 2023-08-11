package icu.chiou.utils;

import icu.chiou.core.annotation.PropertiesField;
import icu.chiou.core.annotation.PropertiesPrefix;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Author: chiou
 * createTime: 2023/8/10
 * Description: No Description
 */
@Slf4j
public class PropertiesUtil {
    /**
     * 根据对象中的配置匹配配置文件
     *
     * @param o
     * @param environment
     */
    public static void init(Object o, Environment environment) {
        final Class<?> aClass = o.getClass();
        log.info("qrpc-init()--> o.class is {}", o);
        // 获取前缀
        final PropertiesPrefix prefixAnnotation = aClass.getAnnotation(PropertiesPrefix.class);
        if (prefixAnnotation == null) {
            throw new NullPointerException(aClass + " @PropertiesPrefix 不存在");
        }
        String prefix = prefixAnnotation.value();
        log.info("qrpc-init()--> prefixAnnotation.value() {}", prefix);
        // 前缀参数矫正
        if (!prefix.contains(".")) {
            prefix += ".";
        }
        // 遍历对象中的字段
        for (Field field : aClass.getDeclaredFields()) {
            PropertiesField fieldAnnotation = field.getAnnotation(PropertiesField.class);
            if (fieldAnnotation == null) continue;
            ;
            String fieldValue = fieldAnnotation.value();
            log.info("qrpc-init()--> fieldAnnotation.value() {}", fieldValue);

            if (fieldValue == null || fieldValue.equals("")) {
                fieldValue = convertToHyphenCase(field.getName());
            }
            try {
                field.setAccessible(true);
                final Class<?> type = field.getType();
                log.info("qrpc-init()--> field.getType() {}", type);

                final Object value = handle(environment, prefix + fieldValue, type);
                if (value == null) continue;
                field.set(o, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            field.setAccessible(false);
        }
    }


    public static String convertToHyphenCase(String input) {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isUpperCase(c)) {
                output.append('-');
                output.append(Character.toLowerCase(c));
            } else {
                output.append(c);
            }
        }

        return output.toString();
    }

    public static Object handle(final Environment environment, final String prefix, final Class<?> targetClass) {
        try {
            // 获取 Binder 类的 Class 对象
            Class<?> binderClass = Class.forName("org.springframework.boot.context.properties.bind.Binder");

            // 获取 Binder 类的 get 方法，该方法接受一个 Environment 参数
            Method getMethod = binderClass.getDeclaredMethod("get", Environment.class);

            // 获取 Binder 类的 bind 方法，该方法接受一个属性前缀和目标类的 Class 参数
            Method bindMethod = binderClass.getDeclaredMethod("bind", String.class, Class.class);

            // 使用反射调用 get 方法，传入 environment 参数，获取一个 Binder 对象
            Object binderObject = getMethod.invoke(null, environment);

            // 对属性前缀进行处理，如果最后一个字符是点号，移除它
            String prefixParam = prefix.endsWith(".") ? prefix.substring(0, prefix.length() - 1) : prefix;

            // 使用反射调用 bind 方法，传入前缀和目标类的 Class 参数，获取一个绑定结果对象
            Object bindResultObject = bindMethod.invoke(binderObject, prefixParam, targetClass);

            // 获取绑定结果对象的 get 方法
            Method resultGetMethod = bindResultObject.getClass().getDeclaredMethod("get");

            // 使用反射调用绑定结果对象的 get 方法，返回最终的绑定结果值
            Object invoke = resultGetMethod.invoke(bindResultObject);
            log.info("qrpc-init()--> field.getType() {}", invoke);

            return invoke;
        } catch (final ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
                       | IllegalArgumentException | InvocationTargetException ex) {
            // 如果发生异常，返回 null
            return null;
        }
    }
}
