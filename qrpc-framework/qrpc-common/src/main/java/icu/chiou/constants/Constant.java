package icu.chiou.constants;

/**
 * Author: chiou
 * createTime: 2023/7/23
 * Description: 常量
 */
public class Constant {
    //zookeeper的默认连接地址
    public static final String DEFAULT_ZK_CONNECT = "127.0.0.1:2181";

    //zookeeper的超时时间
    public static final int DEFAULT_ZK_TIMEOUT = 30000;

    //服务提供方和调用方在注册中心的基础路径
    public static final String BASE_PROVIDERS_PATH = "/qrpc-metadata/providers";
    public static final String BASE_CONSUMERS_PATH = "/qrpc-metadata/consumers";
}
