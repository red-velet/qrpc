package icu.chiou;

import icu.chiou.discovery.RegistryConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Author: chiou
 * createTime: 2023/8/2
 * Description: No Description
 */
@Component
public class QRpcStarter implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        QRpcBootstrap.getInstance()
                .application("first-qrpc-provider")//配置应用名
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))//配置注册中心
                .serialize("json")//配置序列化协议
                .scan("icu.chiou.impl")//扫包
                //.publish(serviceConfig)//发布服务
                .start();//启动
    }
}
