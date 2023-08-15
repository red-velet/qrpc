package icu.chiou.core;

import icu.chiou.config.spi.SpiLoader;
import icu.chiou.core.annotation.QRpcApi;
import icu.chiou.core.annotation.RateLimiter;
import icu.chiou.discovery.registry.Registry;
import icu.chiou.discovery.registry.RegistryFactory;
import icu.chiou.filter.FilterFactory;
import icu.chiou.netty.MethodInvokeHandler;
import icu.chiou.netty.ProviderInvokeAfterHandler;
import icu.chiou.netty.ProviderInvokeBeforeHandler;
import icu.chiou.netty.decoder.QRpcRequestDecoder;
import icu.chiou.netty.encoder.QRpcResponseEncoder;
import icu.chiou.protection.QRpcShutdownHook;
import icu.chiou.protection.TokenBucketRateLimiter;
import icu.chiou.protocol.ServiceConfig;
import icu.chiou.protocol.compress.CompressionFactory;
import icu.chiou.protocol.serialize.SerializationFactory;
import icu.chiou.router.LoadBalancerFactory;
import icu.chiou.utils.NetUtil;
import icu.chiou.utils.PropertiesUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/**
 * Author: chiou
 * createTime: 2023/8/10
 * Description: No Description
 */
@Slf4j
public class QRpcProviderPostProcessor implements InitializingBean, BeanPostProcessor, EnvironmentAware {


    private QRpcProperties qRpcProperties;

    @Override
    public void setEnvironment(Environment environment) {
        QRpcProperties properties = QRpcProperties.getInstance();
        PropertiesUtil.init(properties, environment);
        qRpcProperties = properties;

        log.info("读取配置文件成功{}", qRpcProperties.getPort());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Thread t = new Thread(() -> {
            try {
                start();
            } catch (Exception e) {
                log.error("start rpc server error.", e);
            }
        });
        t.setDaemon(true);
        t.start();

        //加载spi
        RegistryFactory.init();
        SerializationFactory.init();
        CompressionFactory.init();
        LoadBalancerFactory.init();
        FilterFactory.initProviderFilter();
        log.info("spi加载成功........\n" +
                "fileCache {}\n" +
                "contentCache {}", SpiLoader.fileCache, SpiLoader.contentCache);
    }

    private void start() {
        //注册关闭应用程序的钩子函数
        Runtime.getRuntime().addShutdownHook(new QRpcShutdownHook());
        //启动netty服务
        EventLoopGroup boss = null;
        EventLoopGroup worker = null;
        try {
            //1.创建EventGroup
            boss = new NioEventLoopGroup(2);
            worker = new NioEventLoopGroup(10);

            //2.服务器引导程序
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new LoggingHandler())
                                    .addLast(new QRpcRequestDecoder())
                                    .addLast(new ProviderInvokeBeforeHandler())
                                    .addLast(new MethodInvokeHandler())
                                    .addLast(new ProviderInvokeAfterHandler())
                                    .addLast(new QRpcResponseEncoder());
                        }
                    });

            //3.绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(qRpcProperties.getPort()).sync();
            log.info("启动provider netty加载成功........");
            //4.获取数据
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        // 找到bean上带有 QRpcApi 注解的类
        QRpcApi rpcApi = beanClass.getAnnotation(QRpcApi.class);
        if (rpcApi != null) {
            try {
                // 服务注册
                Registry registry = RegistryFactory.get(qRpcProperties.getRegistryType());
                ServiceConfig<?> serviceConfig = new ServiceConfig<>();
                serviceConfig.setRef(beanClass.getConstructor().newInstance());
                serviceConfig.setGroup(beanClass.getAnnotation(QRpcApi.class).group());
                serviceConfig.setInterface(beanClass.getInterfaces()[0]);
                serviceConfig.setIp(NetUtil.getIp());
                serviceConfig.setPort(qRpcProperties.getPort());
                registry.register(serviceConfig);
                log.info("发布服务postProcessBeforeInitialization -- 发布服务成功serviceConfig {}........", serviceConfig);


                //将发布的服务缓存
                QRpcApplicationContext.SERVICE_LIST.put(serviceConfig.getInterface().getName(), serviceConfig);

            } catch (Exception e) {
                log.error("failed to register service ", e);
            }
        }
        //判断该方法是否被限流
        RateLimiter limiter = beanClass.getAnnotation(RateLimiter.class);
        if (limiter != null) {
            //创建限流器添加到缓存
            icu.chiou.protection.RateLimiter rateLimiter = new TokenBucketRateLimiter(limiter.allMaxCapacity(), limiter.tokensPerReplenish());
            QRpcApplicationContext.LIMITER_SERVER_LIST.put(beanClass.getInterfaces()[0].getName(), rateLimiter);
        }
        return bean;
    }
}
