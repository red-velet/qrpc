package icu.chiou;

import icu.chiou.discovery.RegistryConfig;
import icu.chiou.impl.HelloQRpcImpl;

/**
 * Author: chiou
 * createTime: 2023/7/23
 * Description: 服务提供方启动器
 */
public class ProviderApplication {
    public static void main(String[] args) {
        //todo 服务提供方需要做的事情：注册服务、启动服务
        //1.封装需要注册的服务
        //2.定义注册中心
        ServiceConfig<HelloQRpc> serviceConfig = new ServiceConfig();
        serviceConfig.setInterface(HelloQRpc.class);
        serviceConfig.setRef(new HelloQRpcImpl());

        //3.通过启动引导程序,启动服务
        //(1) 配置可选项-应用名称、注册中心、序列化协议
        //(2) 启动服务
        QRpcBootstrap.getInstance()
                .application("first-qrpc-provider")//配置应用名
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))//配置注册中心
                .protocol(new ProtocolConfig("jdk"))//配置序列化协议
                .publish(serviceConfig)//发布服务
                .start();//启动
    }
}
