package icu.chiou;

import icu.chiou.discovery.RegistryConfig;

/**
 * Author: chiou
 * createTime: 2023/7/23
 * Description: 服务提供方启动器
 */
public class ProviderApplication {
    public static void main(String[] args) {
        QRpcBootstrap.getInstance()
                .application("first-qrpc-provider")//配置应用名
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))//配置注册中心
                .serialize("jdk")//配置序列化协议
                .scan("icu.chiou")//扫包
                //.publish(serviceConfig)//发布服务
                .start();//启动
    }
}
