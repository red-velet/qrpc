package icu.chiou;

import icu.chiou.discovery.Registry;
import icu.chiou.discovery.RegistryConfig;
import icu.chiou.discovery.impl.ZookeeperRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: chiou
 * createTime: 2023/7/23
 * Description: No Description
 */
@Slf4j
public class QRpcBootstrap {
    //QRpcBootstrap是个单例,每个应用只有一个
    private final static QRpcBootstrap BOOTSTRAP = new QRpcBootstrap();
    //定义相关的配置
    private String applicationName;
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;

    //维护一个zookeeper示例
    //private ZooKeeper zooKeeper;

    //注册中心
    private Registry registry = new ZookeeperRegistry();

    //端口
    private static final String port = "8088";

    //维护已经且发布的服务列表
    private final static Map<String, ServiceConfig<?>> SERVICE_LIST = new ConcurrentHashMap<>(16);

    private QRpcBootstrap() {
        //构造启动程序时需要做一些初始化
    }

    public static QRpcBootstrap getInstance() {
        return BOOTSTRAP;
    }

    /**
     * 该方法用于设置应用名
     *
     * @param applicationName 应用名
     * @return this-返回当前实例对象
     */
    public QRpcBootstrap application(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    /**
     * 该方法用于设置注册中心
     *
     * @param registryConfig 注册中心
     * @return this-返回当前实例对象
     */
    public QRpcBootstrap registry(RegistryConfig registryConfig) {
        //当前临时配置zookeeper在这里
        //todo 尝试使用工厂方法获取注册中心
        //zooKeeper = ZookeeperUtil.createZookeeper();
        this.registry = registryConfig.getRegistry();
        this.registryConfig = registryConfig;
        return this;
    }

    /**
     * 该方法用于设置当前暴露服务使用的序列化协议
     *
     * @param protocolConfig 序列化协议的封装
     * @return this-返回当前实例对象
     */
    public QRpcBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        if (log.isDebugEnabled()) {
            log.debug("当前工程使用了【{}】协议进行序列化", protocolConfig.toString());
        }
        return this;
    }

    //---------------------------------服务提供方的api-------------------------------------------------

    /**
     * 该方法用于发布服务
     * 发布服务的核心:将接口的实现注册到服务中心
     *
     * @param service 独立封装的需要发布的服务
     * @return this-返回当前实例对象
     */
    public QRpcBootstrap publish(ServiceConfig<?> service) {
        //抽象了注册中心的概念
        //把服务发布到注册中心
        registry.register(service);
        //1.当服务调用方，通过接口、方法名、参数列表调用方法时，怎么知道是具体哪个实例的方法呢？
        //(1) new一个 (2) spring beadFactory.getBean(class) (3) 手动维护映射院系
        SERVICE_LIST.put(service.getInterface().getName(), service);
        return this;
    }

    /**
     * 该方法用于批量发布服务
     * 发布服务的核心:将接口的实现注册到服务中心
     *
     * @param services 封装的需要发布的服务集合
     * @return this-返回当前实例对象
     */
    public QRpcBootstrap publish(List<?> services) {
        return this;
    }

    /**
     * 该方法用于启动服务(netty服务)
     */
    public void start() {
        //启动netty服务
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    //---------------------------------服务提供方的api-------------------------------------------------


    //---------------------------------服务调用方的api-------------------------------------------------

    public QRpcBootstrap reference(ReferenceConfig<?> reference) {
        //在这个方法里我们是否可以拿到相关的配置项: 如注册中心
        //配置reference,便于后面调用get方法时,生成代理对象

        return this;
    }

    //---------------------------------服务调用方的api-------------------------------------------------

}
