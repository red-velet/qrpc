package icu.chiou;


import icu.chiou.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: chiou
 * createTime: 2023/7/23
 * Description: 服务调用方启动器
 */
@Slf4j
public class ConsumerApplication {
    public static void main(String[] args) throws InterruptedException {
        //todo 服务消费者需要做的事情：获取具体待消费对象实例(代理对象:封装连接、获取对象)
        //reference进行代理,其中封装连接、返回对象
        ReferenceConfig<HelloQRpc> reference = new ReferenceConfig<>();
        reference.setInterface(HelloQRpc.class);

        //代理具体要干的事情
        //1.连接注册中心
        //2.拉取服务列表
        //3.选择一个服务,与其建立连接
        //4.发送请求,携带参数(接口名、方法名、参数列表),获得响应
        QRpcBootstrap.getInstance()
                .application("first-qrpc-consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("json")
                .compress("gzip")
                .reference(reference);
        HelloQRpc helloQRpc = reference.get();

//        while (true) {
//            try {
//                Thread.sleep(5000);
//                log.info("++++++++>>>>>>>>>>>>>>>>>>>>>>===============>>>>>>>>>>>>>>>>>>>");
//                //获取代理对象
//                String love = helloQRpc.say("i love you");
//                log.info("远程方法调用返回 --> ❤️ {}", love);
//            } catch (RuntimeException e) {
//                e.printStackTrace();
//            }
//        }
        for (int i = 0; i < 500; i++) {
            try {
                log.info("++++++++>>>>>>>>>>>>>>>>>>>>>>===============>>>>>>>>>>>>>>>>>>>");
                //获取代理对象
                String love = helloQRpc.say("i love you");
                log.info("远程方法调用返回 --> ❤️ {}", love);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }
}
