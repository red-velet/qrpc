package icu.chiou.core;

import icu.chiou.core.annotation.PropertiesField;
import icu.chiou.core.annotation.PropertiesPrefix;
import icu.chiou.utils.IDGenerator;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: chiou
 * createTime: 2023/8/10
 * Description: ConfigurationProperties替换了@PropertiesPrefix
 */
//@Component
//@ConfigurationProperties(prefix = "qrpc")
@PropertiesPrefix("qrpc.")
public class QRpcProperties {
    /**
     * netty 端口
     */
    @PropertiesField
    private Integer port = 8095;

    /**
     * 注册中心地址
     */
    @PropertiesField
    private String registryAddress = "127.0.0.1:2181";


    @PropertiesField
    private String group = "default";

    /**
     * 注册中心类型
     */
    @PropertiesField
    private String registryType = "zookeeper";

    /**
     * 注册中心密码
     */
    @PropertiesField
    private String registryPassword;

    /**
     * 序列化
     */
    @PropertiesField
    private String serializeType = "jdk";


    /**
     * 压缩
     */
    @PropertiesField
    private String compressType = "gzip";

    /**
     * 轮询策略
     */
    private String loadBalanceType = "roundRobin";

    /**
     * 压缩
     */
    private IDGenerator idGenerator = new IDGenerator(1, 2);

    /**
     * 服务端额外配置数据
     */
    @PropertiesField("provider")
    private Map<String, Object> providerAttributes = new HashMap<>();

    /**
     * 客户端额外配置数据
     */
    @PropertiesField("consumer")
    private Map<String, Object> consumerAttributes = new HashMap<>();
    private static QRpcProperties rpcProperties = new QRpcProperties();

    public static QRpcProperties getInstance() {
        return rpcProperties;
    }

    public String getLoadBalanceType() {
        return loadBalanceType;
    }

    public void setLoadBalanceType(String loadBalanceType) {
        this.loadBalanceType = loadBalanceType;
    }

    private QRpcProperties() {
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }


    public String getRegistryType() {
        return registryType;
    }

    public void setRegistryType(String registryType) {
        if (registryType == null || registryType.equals("")) {
            registryType = "zookeeper";
        }
        this.registryType = registryType;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public IDGenerator getIdGenerator() {
        return idGenerator;
    }

    public String getRegistryPassword() {
        return registryPassword;
    }

    public void setRegistryPassword(String registryPassowrd) {
        this.registryPassword = registryPassowrd;
    }

    public String getSerializeType() {
        return serializeType;
    }

    public void setSerializeType(String serializeType) {
        if (serializeType == null || serializeType.equals("")) {
            serializeType = "jdk";
        }
        this.serializeType = serializeType;
    }

    public Map<String, Object> getProviderAttributes() {
        return providerAttributes;
    }

    public void setProviderAttributes(Map<String, Object> providerAttributes) {
        this.providerAttributes = providerAttributes;
    }

    public Map<String, Object> getConsumerAttributes() {
        return consumerAttributes;
    }

    public void setConsumerAttributes(Map<String, Object> consumerAttributes) {
        this.consumerAttributes = consumerAttributes;
    }

    public String getCompressType() {
        return compressType;
    }

    public void setCompressType(String compressType) {
        if (compressType == null || compressType.equals("")) {
            this.compressType = "gzip";
        }
        this.compressType = compressType;
    }


    /**
     * 做一个能够解析任意对象属性的工具类
     *
     * @param environment
     */
    public static void init(Environment environment) {

    }
}
