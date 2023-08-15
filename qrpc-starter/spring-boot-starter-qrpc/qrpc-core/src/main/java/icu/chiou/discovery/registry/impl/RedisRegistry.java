package icu.chiou.discovery.registry.impl;

import icu.chiou.core.QRpcProperties;
import icu.chiou.discovery.registry.AbstractRegistry;
import icu.chiou.protocol.ServiceConfig;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.*;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Author: chiou
 * createTime: 2023/8/12
 * Description: redis注册中心有缺陷
 */
@Slf4j
public class RedisRegistry extends AbstractRegistry {
    private JedisPool jedisPool;
    private static final int TTL = 10;  // TTL in seconds
    private static final int HEARTBEAT_INTERVAL = TTL / 2;

    private Set<String> registeredServices = new HashSet<>();
    private ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();


    @Override
    public void register(ServiceConfig<?> serviceConfig) {
        //使用redis作为注册中心注册服务
        //路径 qrpc-metadata:provider:xxx.xxxx.xxx:ip+port
        /*
         * 服务注册: 服务端注册服务,起一个线程,一直去
         * 服务发现: 去redis拉取服务列表保存到本地
         */
        String key = getKey(serviceConfig.getInterface().getName(), serviceConfig.getGroup());
        String value = getValue(serviceConfig);
        try (Jedis jedis = getJedis()) {
            jedis.zadd(key, System.currentTimeMillis(), value);
            registeredServices.add(key); // Add service to the set
        }
        //startHeartbeatMonitoring(); // Start monitoring after registering each service
    }

    private String getKey(String serviceName, String group) {
        return "qrpc-metadata:providers:" + serviceName + ":" + group;
    }

    private String getValue(ServiceConfig<?> serviceConfig) {
        String ip = serviceConfig.getIp();
        int port = serviceConfig.getPort();
        return ip + ":" + port;
    }

    @Override
    public List<InetSocketAddress> lookup(String serviceName, String group) {
        //使用redis作为注册中心发现服务
        String key = getKey(serviceName, group);
        try (Jedis jedis = getJedis()) {
            Set<Tuple> instances = jedis.zrangeWithScores(key, 0, -1);
            List<InetSocketAddress> instanceList = new ArrayList<>();
            for (Tuple tuple : instances) {
                String[] instance = tuple.getElement().split(":");
                instanceList.add(new InetSocketAddress(instance[0], Integer.parseInt(instance[1])));
            }
            return instanceList;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("拉取服务列表异常");
        }
        return null;
    }

    public void startHeartbeatMonitoring() {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try (Jedis jedis = getJedis()) {
                for (String serviceKey : registeredServices) {
                    long ttl = jedis.ttl(serviceKey);
                    if (ttl < HEARTBEAT_INTERVAL) {
                        // Handle expired service, e.g., remove it from registeredServices
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }

    private Jedis getJedis() {
        if (jedisPool == null) {
            String registryAddress = QRpcProperties.getInstance().getRegistryAddress();
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxTotal(10);
            jedisPoolConfig.setMaxIdle(5);
            String[] split = registryAddress.split(":");
            String ip = split[0];
            int port = Integer.parseInt(split[1]);
            jedisPool = new JedisPool(jedisPoolConfig, ip, port);
        }
        String registryPassword = QRpcProperties.getInstance().getRegistryPassword();
        Jedis jedis = jedisPool.getResource();
        if (registryPassword != null && !registryPassword.equals("")) {
            jedis.auth(registryPassword);
        }
        return jedis;
    }

    public static void main(String[] args) {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        jedis.zadd("qrpc-metedata:providers:icu.chiou.service.HelloService:default", System.currentTimeMillis(), "127.0.0.1:8088");
        jedis.zadd("qrpc-metedata:providers:icu.chiou.service.HelloService:default", System.currentTimeMillis(), "127.0.0.1:8089");
        String key = "qrpc-metedata:providers:icu.chiou.service.HelloService:default";
        try {
            Set<Tuple> instances = jedis.zrangeWithScores(key, 0, -1);
            List<InetSocketAddress> instanceList = new ArrayList<>();
            for (Tuple tuple : instances) {
                String[] instance = tuple.getElement().split(":");
                instanceList.add(new InetSocketAddress(instance[0], Integer.parseInt(instance[1])));
            }
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("拉取服务列表异常");
        }
        System.out.println();
        jedis.close();


        jedis = new Jedis("localhost", 6379);
        //可以解决提供方关闭会话后服务会自动消失,然后进行reload
        jedis.setex("qrpc-metedata:icu.chiou.HelloService:127.0.0.1&8001", 2, "127.0.0.1:8001");
        jedis.setex("qrpc-metedata:icu.chiou.HelloService:127.0.0.1&8002", 2, "127.0.0.1:8002");
        String prefix = "qrpc-metedata:icu.chiou.HelloService";

        Set<String> keysWithPrefix = getKeysWithPrefix(jedis, prefix);

        // Print the keys with the specified prefix
        for (String key1 : keysWithPrefix) {
            System.out.println("Key: " + key1);
        }

        jedis.close();
    }


    public static Set<String> getKeysWithPrefix(Jedis jedis, String prefix) {
        Set<String> keysWithPrefix = new HashSet<>();
        String cursor = "0";
        ScanParams scanParams = new ScanParams().match(prefix + "*").count(1000);

        do {
            ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
            keysWithPrefix.addAll(scanResult.getResult());
            cursor = scanResult.getStringCursor();
        } while (!cursor.equals("0"));

        return keysWithPrefix;
    }
}
