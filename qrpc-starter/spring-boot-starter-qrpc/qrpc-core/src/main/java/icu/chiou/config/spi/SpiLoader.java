package icu.chiou.config.spi;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: chiou
 * createTime: 2023/8/10
 * Description: spi加载
 */
@Slf4j
public class SpiLoader {

    // 系统SPI
    private static final String SYSTEM_LOADER = "META-INF/qrpc-services/";

    // 用户SPI
    private static final String DIY_LOADER = "META-INF/qrpc-services-diy/";

    private static String[] prefixs = {SYSTEM_LOADER, DIY_LOADER};

    // spi文件内容的key和value
    public static final Map<String, Class<?>> contentCache = new ConcurrentHashMap<>();
    // spi文件的key-文件名和value-文件内容
    public static final Map<String, Map<String, Class>> fileCache = new ConcurrentHashMap<>();

    // 实例化的bean
    public static final Map<String, Object> singletonsObject = new ConcurrentHashMap<>();


    private static final SpiLoader SPI_CONFIGURATION_LOADER = new SpiLoader();

    private SpiLoader() {

    }

    public static SpiLoader getInstance() {
        return SPI_CONFIGURATION_LOADER;
    }

    /**
     * 获取bean
     *
     * @param name
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public <T> T get(String name) {
        if (!singletonsObject.containsKey(name)) {
            try {
                singletonsObject.put(name, contentCache.get(name).newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return (T) singletonsObject.get(name);
    }

    /**
     * 获取接口下所有的类
     *
     * @param clazz
     * @return
     */
    public List<Object> gets(Class clazz) {

        final String name = clazz.getName();
        if (!fileCache.containsKey(name)) {
            try {
                throw new ClassNotFoundException(clazz + "未找到");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        final Map<String, Class> stringClassMap = fileCache.get(name);
        List<Object> objects = new ArrayList<>();
        if (stringClassMap.size() > 0) {
            stringClassMap.forEach((k, v) -> {
                try {
                    objects.add(singletonsObject.getOrDefault(k, v.newInstance()));
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        }

        return objects;
    }

    /**
     * 根据spi机制初加载bean的信息放入map
     *
     * @param clazz
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void loadExtension(Class clazz) throws IOException, ClassNotFoundException {
        if (clazz == null) {
            throw new IllegalArgumentException("class 没找到");
        }
        ClassLoader classLoader = this.getClass().getClassLoader();
        Map<String, Class> classMap = new HashMap<>();
        // 从系统SPI以及用户SPI中找bean
        for (String prefix : prefixs) {
            String spiFilePath = prefix + clazz.getName();

            Enumeration<URL> enumeration = classLoader.getResources(spiFilePath);

            while (enumeration.hasMoreElements()) {
                URL url = enumeration.nextElement();
                InputStreamReader inputStreamReader = null;
                inputStreamReader = new InputStreamReader(url.openStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    log.info("qqqrpc-spi-loadExtension-line is {}........", line);
                    String[] lineArr = line.split("=");
                    String key = lineArr[0];//name
                    String value = lineArr[1];//全路径名
                    final Class<?> aClass = Class.forName(value);
                    contentCache.put(key, aClass);
                    classMap.put(key, aClass);
                    log.info("加载bean key:{} , value:{}", key, value);
                }
            }
        }
        fileCache.put(clazz.getName(), classMap);
    }

    public Map<String, Class> getFileContent(String fileName) {
        Map<String, Class> stringClassMap = fileCache.get(fileName);
        return stringClassMap;
    }
}
